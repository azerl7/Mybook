package com.geo.mybook.relation.biz.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geo.framework.common.response.PageResponse;
import com.geo.framework.common.util.DateUtils;
import com.geo.mybook.relation.biz.domain.po.Fans;
import com.geo.mybook.relation.biz.domain.vo.FindFansListReqVo;
import com.geo.mybook.relation.biz.domain.vo.FindFansUserResVo;
import com.geo.mybook.relation.biz.mapper.FansMapper;
import com.geo.mybook.relation.biz.rpc.UserRpcService;
import com.geo.mybook.relation.biz.service.FansService;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.geo.framework.common.util.Constants.RELATION_EXPIRE_SECONDS;
import static com.geo.framework.common.util.Constants.USER_FANS_KEY_PREFIX;

/*
creator：AZERL7
createTime：14:34
*/
@Service
public class FansServiceImpl extends ServiceImpl<FansMapper, Fans> implements FansService {

    @Resource
    private FansMapper fansMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserRpcService userRpcService;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public PageResponse<FindFansUserResVo> findFansList(FindFansListReqVo findFansListReqVo) {
        //1、获取数据
        Long userId=findFansListReqVo.getUserId();
        Integer pageNo=findFansListReqVo.getPageNo();
        //2、先从redis中获取数据
        String fansListRedisKey = USER_FANS_KEY_PREFIX+userId;
        Long total=stringRedisTemplate.opsForZSet().zCard(fansListRedisKey);
        if(ObjectUtil.isNull(total)){//依旧是初始化total避免null带来的问题
            total=0L;
        }
        List<FindFansUserResVo> findFansUserResVos=null;

        long limit=10L;
        if(total>0){//redis中有缓存数据，进行判断redis数据情况来进行处理
            long totalPage=PageResponse.getTotalPage(total,limit);
            //情况1、请求页码超出范围
            if(pageNo>totalPage){
                return PageResponse.success(null,pageNo,total);
            }
            long offset=PageResponse.getOffset(pageNo,limit);
            Set<String> followingUserIdsSet=stringRedisTemplate.opsForZSet()
                    .reverseRangeByScore(fansListRedisKey,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);

            if(CollectionUtil.isNotEmpty(followingUserIdsSet)){
                //不为空，去重之后提取用户id到集合
                List<Long> userIds=followingUserIdsSet.stream().map(Long::valueOf).toList();
                //RPC调用批量查询用户信息
                findFansUserResVos=rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserResVos);
            }
        }else{//从数据库查询
            total=fansMapper.selectCountByUserId(userId);
            long totalPage=PageResponse.getTotalPage(total,limit);
            //页码超出范围
            if(pageNo>totalPage){
                return PageResponse.success(null,pageNo,total);
            }

            long offset=PageResponse.getOffset(pageNo,limit);
            List<Fans> fans=fansMapper.selectPageListByUserId(userId,offset,limit);
            //记录不为空
            if(CollectionUtil.isNotEmpty(fans)){
                List<Long> userIds=fans.stream().map(Fans::getFansUserId).toList();
                findFansUserResVos=rpcUserServiceAndCountServiceAndDTO2VO(userIds,findFansUserResVos);
                threadPoolTaskExecutor.submit(()->syncFansList2Redis(userId));
            }

        }
        return PageResponse.success(findFansUserResVos,pageNo,total);
    }

    private List<FindFansUserResVo> rpcUserServiceAndCountServiceAndDTO2VO(List<Long> userIds,List<FindFansUserResVo> findFansUserResVo){
        //RPC:批量查询用户信息
        List<FindUserByIdResDTO> findUserByIdResDTOList=userRpcService.findByIds(userIds);

        //todo rpc: 批量查询用户的计数数据
        //若不为空，dto转vo
        if(CollectionUtil.isNotEmpty(findUserByIdResDTOList)){
            findFansUserResVo=findUserByIdResDTOList.stream()
                    .map(dto -> FindFansUserResVo.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickName())
                            .noteTotal(0L) // TODO: 这块的数据暂无，后续补充
                            .fansTotal(0L) // TODO: 这块的数据暂无，后续补充
                            .build()
                    ).toList();
        }

        return findFansUserResVo;
    }


    /**
     * 同步粉丝表到redis
     * @param userId 用户id
     */
    private void syncFansList2Redis(Long userId){
        List<Fans> fansList=fansMapper.select5000FansByUserId(userId);
        if(CollectionUtil.isNotEmpty(fansList)){
            String fansListRedisKey=USER_FANS_KEY_PREFIX+userId;
            Long expire=RELATION_EXPIRE_SECONDS+ RandomUtil.randomLong(RELATION_EXPIRE_SECONDS);
            String[] luaArgs=buildFansZSetLuaArgs(fansList,expire);

            DefaultRedisScript<Long> script=new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            stringRedisTemplate.execute(script,Collections.singletonList(fansListRedisKey),luaArgs);
        }
    }

    /**
     * 构建lua脚本参数
     * @param fansList 粉丝列表
     * @param expire ttl
     * @return string[]
     */

    private String[] buildFansZSetLuaArgs(List<Fans> fansList,Long expire){
        int argsLength=fansList.size()*2+1;
        String[] luaArgs=new String[argsLength];
        int i=0;
        for(Fans fans : fansList){
            luaArgs[i]= Long.toString(DateUtils.localDateTime2TimeStamp(fans.getCreateTime()));
            luaArgs[i+1]=fans.getFansUserId().toString();//时间作为分数
            i+=1;
        }
        luaArgs[argsLength-1]=expire.toString();
        return luaArgs;
    }
}
