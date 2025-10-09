package com.geo.mybook.relation.biz.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.geo.framework.common.response.PageResponse;
import com.geo.framework.common.util.JsonUtils;
import com.geo.mybook.relation.biz.constant.MQConstants;
import com.geo.mybook.relation.biz.domain.dto.FollowUserMQDTO;
import com.geo.mybook.relation.biz.domain.dto.UnfollowUserMQDTO;
import com.geo.mybook.relation.biz.domain.vo.FindFollowingListReqVo;
import com.geo.mybook.relation.biz.domain.vo.FindFollowingUserResVo;
import com.geo.mybook.relation.biz.domain.vo.UnfollowUserReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import com.geo.framework.common.util.DateUtils;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import com.geo.mybook.relation.biz.domain.po.Following;
import com.geo.mybook.relation.biz.domain.vo.FollowUserReqVo;
import com.geo.mybook.relation.biz.enums.LuaResultEnum;
import com.geo.mybook.relation.biz.enums.ResponseEnum;
import com.geo.mybook.relation.biz.mapper.FollowingMapper;
import com.geo.mybook.relation.biz.rpc.UserRpcService;
import com.geo.mybook.relation.biz.service.FollowingService;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.geo.framework.common.util.Constants.*;
import static com.geo.mybook.relation.biz.constant.MQConstants.TAG_UNFOLLOW;
import static com.geo.mybook.relation.biz.constant.MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW;

/*
creator：AZERL7
createTime：14:38
*/
@Slf4j
@Service
public class FollowingServiceImpl extends ServiceImpl<FollowingMapper, Following> implements FollowingService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserRpcService userRpcService;
    @Resource
    private FollowingMapper followingMapper;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource(name="taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;


    @Override
    public Response<?> follow(FollowUserReqVo followUserReqVo) {
        //1、获取所需要的id
        Long followUserId=followUserReqVo.getFollowUserId();
        Long userId= LoginUserContextHolder.getUserId();

        //2、关注校验
        //2.1、不能关注自己
        if(ObjectUtil.equal(followUserId,userId)){
            throw new BizException(ResponseEnum.CANT_FOLLOW_YOUR_SELF);
        }
        //2.2、校验关注的用户是否存在
        FindUserByIdResDTO findUserByIdResDTO= userRpcService.findById(followUserId);
        if(ObjectUtil.isNull(findUserByIdResDTO)){
            throw new BizException(ResponseEnum.FOLLOW_USER_NOT_EXISTED);
        }

        //3、执行lua脚本
        String followingRedisKey=USER_FOLLOWING_KEY_PREFIX+userId;
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        //3.1、执行后返回
        script.setResultType(Long.class);
        LocalDateTime now =LocalDateTime.now();
        long timestamp= DateUtils.localDateTime2TimeStamp(now);
        //collection.singletonList用于创建不可修改的列表
        long result=stringRedisTemplate.execute(script, Collections.singletonList(followingRedisKey),followUserId.toString(),Long.toString(timestamp));

        LuaResultEnum luaResultEnum=LuaResultEnum.valueOf(result);
        if(ObjectUtil.isNull(luaResultEnum)){
            throw new RuntimeException("follow_chack_and_add 该lua脚本返回值错误");
        }

        //3.2、判断result
        switch(luaResultEnum){
            //超过上限了
            case FOLLOW_LIMIT -> throw new BizException(ResponseEnum.FOLLOW_COUNT_LIMIT);
            //已经关注该用户了
            case ALREADY_FOLLOWED -> throw  new BizException(ResponseEnum.ALREADY_FOLLOWED);
            //ZSET不存在
            case ZSET_NOT_EXISTS -> {
                //写入redis zset
                List<Following> followings=followingMapper.selectByUserId(userId);
                if(CollectionUtil.isEmpty(followings)){//如果为空则直接ZADD并设置过期时间
                    DefaultRedisScript<Long>   script2=new DefaultRedisScript<>();
                    script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_add_and_expire.lua")));
                    script2.setResultType(Long.class);
                    //todo 可以根据用户类型，设置不同的过期时间，比如大V用户，可以不用设置过期时间，或者设置很长
                    //如何判断一个用户是否是大V，可以使用计数服务来获取粉丝数量
                    stringRedisTemplate.execute(script2, Collections.singletonList(followingRedisKey),followUserId.toString(),Long.toString(timestamp),RELATION_EXPIRE_SECONDS.toString());
                }else{//若记录不为空，则将关注关系数据全部同步到redis并设置过期时间
                    String[] luaArgs=buildLuaArgs(followings,RELATION_EXPIRE_SECONDS);
                    DefaultRedisScript<Long> script3=new DefaultRedisScript<>();
                    script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                    script3.setResultType(Long.class);
                    result=stringRedisTemplate.execute(script3,Collections.singletonList(followingRedisKey),luaArgs);
                    checkLuaScriptResult(result);//todo 添加分布式事务的时候需要做判断
                }
            }
        }
        //4、发送mq消息写入数据库

        FollowUserMQDTO followUserMQDTO=FollowUserMQDTO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .createTime(LocalDateTime.now())
                .build();
        Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMQDTO)).build();

        //通过冒号连接，可以让 MQ 在发送给 Topic 的时候，携带上Tag
        String destination= TOPIC_FOLLOW_OR_UNFOLLOW+":"+MQConstants.TAG_FOLLOW;
        log.info("==> 开始发送关注操作 MQ ，消息体{}",followUserMQDTO);

        //异步发送mq消息
        rocketMQTemplate.asyncSend(destination,message,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ 消息发送成功，SendResult{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
//                throwable.printStackTrace();//不知道是啥啊输出看一下就是
                log.error("==> MQ消息发送异常",throwable);
            }
        });
        return Response.success();
    }


    private static void checkLuaScriptResult(Long result){
        LuaResultEnum luaResultEnum=LuaResultEnum.valueOf(result);
        if(ObjectUtil.isNull(luaResultEnum)){
            throw new RuntimeException("lua 返回结果错误");
        }
        switch (luaResultEnum){
            //关注数量达到上限
            case FOLLOW_LIMIT -> throw new BizException(ResponseEnum.FOLLOW_COUNT_LIMIT);
            //已经关注了
            case ALREADY_FOLLOWED -> throw new BizException(ResponseEnum.ALREADY_FOLLOWED);
        }
    }

    /**
     * 构建 Lua 脚本参数
     *
     * @param followings follow参数
     * @param expireSeconds 过期时间
     * @return object[]
     */
    private static String[] buildLuaArgs(List<Following> followings, long expireSeconds) {
        int argsLength = followings.size() * 2 + 1; // 每个关注关系有 2 个参数（score 和 value），再加一个过期时间
        String[] luaArgs = new String[argsLength];

        int i = 0;
        for (Following following : followings) {
            luaArgs[i] =Long.toString(DateUtils.localDateTime2TimeStamp(following.getCreateTime())); // 关注时间作为 score
            luaArgs[i + 1] = following.getFollowingUserId().toString();          // 关注的用户 ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] =Long.toString(expireSeconds); // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    public Response<?> unfollow(UnfollowUserReqVo unfollowUserReqVo){
        //1、获取取关和用户id
        Long unfollowUserId=unfollowUserReqVo.getUnfollowUserId();
        Long userId=LoginUserContextHolder.getUserId();

        //2、无法取关自己
        if(ObjectUtil.equal(userId,unfollowUserId)){
            throw new BizException(ResponseEnum.CANT_UNFOLLOW_YOUR_SELF);
        }

        //2.1、校验取关的用户是否存在（这部分按照正常来说可以省略，但是还是要写，避免出现极端情况）
        FindUserByIdResDTO findUserByIdResDTO=userRpcService.findById(unfollowUserId);
        if(ObjectUtil.isNull(findUserByIdResDTO)){
            throw new BizException(ResponseEnum.FOLLOW_USER_NOT_EXISTED);
        }

        //3、Redis lua校验删除验证
        String followRedisKey=USER_FOLLOWING_KEY_PREFIX+userId;
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/unfollow_check_and_delete.lua")));
        script.setResultType(Long.class);
        Long result=stringRedisTemplate.execute(script,Collections.singletonList(followRedisKey),unfollowUserId.toString());
        if(ObjectUtil.equal(result,LuaResultEnum.NOT_FOLLOWED.getCode())){//必须是关注过的用户才能取关
            throw new BizException(ResponseEnum.NOT_FOLLOWED);
        }

        if(ObjectUtil.equal(result,LuaResultEnum.ZSET_NOT_EXISTS.getCode())){//关注列表不存在
            //3.1、从数据库查询关注列表（因为redia的关注列表不存在可能是因为ttl过期了）（这里全部查出来可能有点慢，但是先这样了）
            List<Following> followingS=followingMapper.selectByUserId(userId);
            //添加随机ttl避免缓存雪崩
            //保底一天加随机的时间
            long expireSeconds=RELATION_EXPIRE_SECONDS+ RandomUtil.randomInt(60*60*24);
            if(CollectionUtil.isEmpty(followingS)){//3.2、判断是否真的有关注列表
                throw new BizException(ResponseEnum.NOT_FOLLOWED);//没有关注列表提示去关注
            }else{//3.2、写入redis，并添加随机ttl
                String[] luaArgs=buildLuaArgs(followingS,expireSeconds);
                DefaultRedisScript<Long> script1=new DefaultRedisScript<>();
                script1.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script1.setResultType(Long.class);
                //刷新一次关注列表
                stringRedisTemplate.execute(script1,Collections.singletonList(followRedisKey),luaArgs);
                //删除，并再次校验
                result=stringRedisTemplate.execute(script,Collections.singletonList(followRedisKey),luaArgs);
                if(ObjectUtil.equal(result,LuaResultEnum.NOT_FOLLOWED)){
                    throw new BizException(ResponseEnum.NOT_FOLLOWED);
                }
            }

        }

        //4、发送mq消息
        UnfollowUserMQDTO unfollowUserMQDTO=UnfollowUserMQDTO.builder()
                .userId(userId)
                .unfollowUserId(unfollowUserId)
                .createTime(LocalDateTime.now())
                .build();

        Message<String> message= MessageBuilder
                .withPayload(JsonUtils.toJsonString(unfollowUserMQDTO)).build();

        //通过冒号连接，可以让mq发送主题消息并且携带tag
        String destination=TOPIC_FOLLOW_OR_UNFOLLOW+":"+TAG_UNFOLLOW;
        log.info("==> 开始发送mq消息,消息体 {}",unfollowUserMQDTO);
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> MQ消息发送成功，sendResult{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> MQ消息发送失败，error",throwable);
            }
        });
        return Response.success();
    }

    @Override
    public PageResponse<FindFollowingUserResVo> findFollowingList(FindFollowingListReqVo findFollowingListReqVo) {
        //1、获取基本信息
        Long userId=findFollowingListReqVo.getUserId();
        Integer pageNo=findFollowingListReqVo.getPageNo();

        //2、先从redis获取数据
        String followingListRedisKey=USER_FOLLOWING_KEY_PREFIX+userId;
        Long total=stringRedisTemplate.opsForZSet().zCard(followingListRedisKey);
        if(ObjectUtil.isNull(total)){
            total=0L;//默认初始化为0，避免出现null出错等问题
        }
        List<FindFollowingUserResVo> findFollowingUserResVos=null;
        long limit=10;
        if(total>0){//缓存中有数据
            long totalPage=PageResponse.getTotalPage(total,limit);
            if(pageNo>totalPage){
                return PageResponse.success(null,pageNo,total);
            }

            long offset=(pageNo-1)*limit;
            //2.1、按照分数排序，然后通过set去重
            Set<String> followingUserIdsSet=stringRedisTemplate.opsForZSet()
                    .reverseRangeByScore(followingListRedisKey,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,offset,limit);

            //2.2、提取用户ID
            if(CollectionUtil.isNotEmpty(followingUserIdsSet)){
                List<Long> userIds=followingUserIdsSet.stream().map(Long::valueOf).toList();

                //RPC封装调用获取用户
                List<FindUserByIdResDTO> findFollowingUserResVoList=userRpcService.findByIds(userIds);
                if(CollectionUtil.isNotEmpty(findFollowingUserResVoList)){
                    findFollowingUserResVos=findFollowingUserResVoList.stream()
                            .map(dto->FindFollowingUserResVo
                                    .builder()
                                    .userId(dto.getId())
                                    .avatar(dto.getAvatar())
                                    .nickname(dto.getNickName())
                                    .introduction(dto.getIntroduction())
                                    .build())
                            .toList();
                }
            }
        }else{
            //redis中没有数据从数据库查询
            //1、计算数据量并进行分页
            long count=followingMapper.selectCountByUserId(userId);
            long totalPage=PageResponse.getTotalPage(count,limit);
            //请求的页码数量超过了总页数，
            if(pageNo>totalPage){
                return PageResponse.success(null,pageNo,total);
            }

            long offset=PageResponse.getOffset(pageNo,limit);

            List<Following> followingList=followingMapper.selectPageListByUserId(userId,offset,limit);
            //记录真实的记录总数
            total=count;

            //记录不为空
            if(CollectionUtil.isNotEmpty(followingList)){
                List<Long> userIdList=followingList.stream().map(Following::getFollowingUserId).toList();

                //2、调用 RPC 服务获取用户数据
                findFollowingUserResVos=rpcUserServiceAndDTO2VO(userIdList,findFollowingUserResVos);

                //3、异步同步关注列表全量同步到redis
                threadPoolTaskExecutor.submit(()->{
                   syncFollowingList2Redis(userId);
                });
            }
        }
        return PageResponse.success(findFollowingUserResVos,pageNo,total);
    }

    private void syncFollowingList2Redis(Long userId){
        List<Following> followingList=followingMapper.selectLimitByUserId(userId);
        log.info("## 跨线程监视 followingList {}",followingList);
        if(CollectionUtil.isNotEmpty(followingList)){
            String followingListRedisKey=USER_FOLLOWING_KEY_PREFIX+userId;
            //随机过期时间
            long expireTime=USER_INFO_EXPIRE+RandomUtil.randomLong(USER_INFO_EXPIRE);
            String[] luaArgs=buildLuaArgs(followingList,expireTime);
            DefaultRedisScript<Long> script=new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);

            try{
                Long execute = stringRedisTemplate.execute(script, Collections.singletonList(followingListRedisKey), luaArgs);
                log.info("==> lua 脚本执行完成 返回执行结果 {}",execute);
            }catch(Exception e){
                log.error("## Redis lua脚本执行错误，请检查参数等问题",e);
            }
        }
    }

    /**
     * 调用用户服务，并将dto转换为vo
     * @param userIds userIds
     * @param findFollowingUserResVos 查询关注用户信息
     * @return list
     */
    private List<FindFollowingUserResVo> rpcUserServiceAndDTO2VO(List<Long> userIds,List<FindFollowingUserResVo> findFollowingUserResVos){
        List<FindUserByIdResDTO> findUserByIdResDTOList=userRpcService.findByIds(userIds);
        //转换对象
        if(CollectionUtil.isNotEmpty(findUserByIdResDTOList)){
            findFollowingUserResVos=findUserByIdResDTOList.stream()
                    .map(dto -> FindFollowingUserResVo.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickName())
                            .introduction(dto.getIntroduction())
                            .build())
                    .toList();
        }
        return findFollowingUserResVos;
    }
}
