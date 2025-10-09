package com.geo.mybook.user.biz.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geo.framework.common.enums.DeletedEnum;
import com.geo.framework.common.enums.StatusEnum;
import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import com.geo.framework.common.util.JsonUtils;
import com.geo.framework.common.util.ParamUtils;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import com.geo.mybook.user.biz.domain.po.Role;
import com.geo.mybook.user.biz.domain.po.User;
import com.geo.mybook.user.biz.domain.vo.UpdateUserInfoReqVo;
import com.geo.mybook.user.biz.enums.ResponseCodeEnum;
import com.geo.mybook.user.biz.enums.SexEnum;
import com.geo.mybook.user.biz.mapper.RoleMapper;
import com.geo.mybook.user.biz.mapper.UserMapper;
import com.geo.mybook.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.geo.mybook.user.biz.rpc.OssRpcService;
import com.geo.mybook.user.biz.service.UserService;
import com.geo.mybook.user.dto.req.*;
import com.geo.mybook.user.dto.res.FindUserByAccountResDTO;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.geo.framework.common.util.Constants.*;
import static com.geo.framework.common.util.Constants.USER_ROLES_KEY;

/*
creator：AZERL7
createTime：11:53
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private OssRpcService ossRpcService;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource(name="taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private static final Cache<Long,FindUserByIdResDTO> LOCAL_CACHE= Caffeine.newBuilder()
            .initialCapacity(CAFFEINE_INIT_CAPACITY)//缓存初始容量
            .maximumSize(CAFFEINE_MAX_CAPACITY)//缓存最大容量，超过缓存则进行淘汰，默认使用 W-TinyLFU
            .expireAfterWrite(CAFFEINE_EXPIRE,TimeUnit.MINUTES)//设置在写入时间多久后过期
            .build();

    @Override
    public Response<String> updateUserInfo(UpdateUserInfoReqVo updateUserInfoReqVo) {
        //1、构建一个新的user
        User user=new User();
        user.setId(LoginUserContextHolder.getUserId());
        boolean needUpdate=false;
        MultipartFile avatarFile=updateUserInfoReqVo.getAvatar();
        if(ObjectUtil.isNotNull(avatarFile)){//因为后面的操作需要前面的数据，所以这里不能使用多线程。。你看可以用什么来优化吧
            String avatar=ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用oss服务，上传头像，url {}",avatar);
//            System.out.println(avatar+"头像");
            if(StringUtils.isBlank(avatar)){
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            user.setAvatar(avatar);
            needUpdate=true;
        }
        String nickname=updateUserInfoReqVo.getNickname();
        //昵称是否需要修改
        if(StringUtils.isNotBlank(nickname)){
            Preconditions.checkArgument(ParamUtils.checkNickName(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL00.getErrorMessage());
            user.setNickname(nickname);
            needUpdate=true;
        }
        //性别是否需要修改
        Integer sex=updateUserInfoReqVo.getSex();
        if(ObjectUtil.isNotNull(sex)){
            Preconditions.checkArgument(SexEnum.isValid(sex),ResponseCodeEnum.SEX_VALID_FAIL);
            user.setSex(sex.byteValue());
            needUpdate=true;
        }

        //生日是否需要修改
        LocalDate birthday=updateUserInfoReqVo.getBirthday();
        if(ObjectUtil.isNotNull(birthday)){
            user.setBirthday(birthday);
            needUpdate=true;
        }
        //个人简介是否需要修改
        String introduction=updateUserInfoReqVo.getIntroduction();
        if(StringUtils.isNotBlank(introduction)){
            Preconditions.checkArgument(ParamUtils.checkLength(introduction,100),ResponseCodeEnum.INTRODUCTION_VALID_FAIL);
            user.setIntroduction(introduction);
            needUpdate=true;
        }

        //背景图
        MultipartFile backgroundImgFile=updateUserInfoReqVo.getBackgroundImg();
        if(ObjectUtil.isNotNull(backgroundImgFile)){
            String background=ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 调用oss服务成功，上传背景图，url {}",background);
            if(StringUtils.isBlank(background)){
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_FAIL);
            }
//            System.out.println(background+"背景图");
            user.setBackgroundImg(background);
            needUpdate=true;
        }

        if(needUpdate){
            user.setUpdateTime(LocalDateTime.now());
            userMapper.updateByIdSelective(user);
        }
        return Response.success();
    }


    @Override
    public Response<Long> register(RegisterUserDTO registerUserDTO) {
        String account=registerUserDTO.getAccount();
        return transactionTemplate.execute(status->{
            try{
                //1、让全局id自增
//                Long mybookId = stringRedisTemplate.opsForValue().increment(MYBOOK_ID_GENERATOR_KEY);
                //1、调用分布式id获取服务
                Long id=Long.valueOf(distributedIdGeneratorRpcService.getUserId());
                Long mybookId=Long.valueOf(distributedIdGeneratorRpcService.getMybookId());
//                System.out.println("id "+id);
//                System.out.println("mybookId "+id);
                //2、创建用户
                User user = User.builder()
                        .id(id)
                        .mybookId(String.valueOf(mybookId)) // 自动生成小红书号 ID
                        .nickname(RandomUtil.randomString(15)) // 自动生成昵称
                        .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                        .build();
                if(account.matches(REGEX_EMAIL)){
                    user.setEmail(account);
                }else{
                    user.setPhone(account);
                }
                save(user);//使用mp方便获取id啥的
                Long userId=user.getId();

                //3、存入用户角色到redis
                Role role=roleMapper.selectByPrimaryKey(COMMON_USER_ROLE_ID);
                List<String> roles=new ArrayList<>(1);
                roles.add(role.getRoleKey());//默认为普通用户
//                System.out.println(roles);
                stringRedisTemplate.opsForValue().set(USER_ROLES_KEY+userId, JsonUtils.toJsonString(roles));
//                stringRedisTemplate.opsForList().leftPushAll(USER_ROLES_KEY+userId, String.valueOf(roles));
                return Response.success(userId);
            }catch(Exception e){
                status.setRollbackOnly();//标记事务为回滚
                log.error("==> 系统注册用户异常：",e);
                return null;
            }
        });
    }

    @Override
    public Response<FindUserByAccountResDTO> findByAccount(FindUserByAccountReqDTO findUserByPhoneReqDTO) {
        String account= findUserByPhoneReqDTO.getAccount();
        User user=getUserByAccount(account);
        if(ObjectUtil.isNull(user)){
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        FindUserByAccountResDTO build = FindUserByAccountResDTO.builder()
                .id(user.getId()).password(user.getPassword()).build();

        return Response.success(build);
    }

    /**
     * 根据账号判断使用那种方式
     * @param account 账号
     * @return 用户
     */
    private User getUserByAccount(String account){
        String column="phone";///默认为手机号
        if(account.matches(REGEX_EMAIL)){
            column="email";
        }else if(account.matches(REGEX_CHINA_PHONE)){
            column="phone";
        }
//        return query().eq(cloumn,account).one();//mp还是太吃性能了
        return userMapper.selectByAccount(column,account);
    }

    @Override
    public Response<String> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        Long userId=LoginUserContextHolder.getUserId();
        if (ObjectUtil.isNull(userId)) {
            throw new BizException(ResponseCodeEnum.NOT_LOGIN); // 或新增"用户ID为空"异常
        }
        User user=User.builder()
                .id(userId)
                .password(updateUserPasswordReqDTO.getEncodePassword())
                .updateTime(LocalDateTime.now()).build();
//        System.out.println(user+" user");
        updateById(user);//更新使用mp
//        System.out.println(b+" boolean");
        return Response.success();
    }

    @Override
    public Response<User> findById(FindUserByIdReqDTO findUserByIdReqDTO) {
        Long id=findUserByIdReqDTO.getUserId();
        User user=userMapper.selectByIdMybatis(id);
        //如果没有指定id列直接使用selectById会报空
        return Response.success(user);
    }

    @Override
    public Response<FindUserByIdResDTO> findById2nickname2avatar(FindUserByIdReqDTO findUserByIdReqDTO) {
        //1、获取用户id
        Long userId=findUserByIdReqDTO.getUserId();

        //1.5、先从本地缓存查找
        FindUserByIdResDTO findUserByIdResDTO=LOCAL_CACHE.getIfPresent(userId);
        if(ObjectUtil.isNotNull(findUserByIdResDTO)){
            log.info("==> 命中了本地缓存{}",findUserByIdReqDTO);
            return Response.success(findUserByIdResDTO);
        }

        //2、从redis查找缓存
        String key=USER_INFO_KEY_PREFIX+userId;
        String jsonStr=stringRedisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(jsonStr)){//2.1、存在查找的对象，转换之后返回，并且刷新缓存时间
            findUserByIdResDTO=JsonUtils.parseObject(jsonStr,FindUserByIdResDTO.class);

            //2.2、将redis缓存写入本地缓存
            FindUserByIdResDTO finalFindUserByIdResDTO = findUserByIdResDTO;
            threadPoolTaskExecutor.submit(()->{
                LOCAL_CACHE.put(userId, finalFindUserByIdResDTO);//因为是异步，如果隔得远的化可能被gc掉了
            });

            long expireSeconds=RandomUtil.randomLong(USER_INFO_EXPIRE);//随机再添加缓存活时间最多一天
            stringRedisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);

            return Response.success(findUserByIdResDTO);
        }
        //3、没在redis找到则从数据库找
        User user=userMapper.selectById2nickname2avatar2introduction(userId);
        if(ObjectUtil.isNull(user)){
            //3.1、添加空值redis缓存防止缓存穿透
            long expireSeconds=NULL_EXPIRE+RandomUtil.randomLong(NULL_EXPIRE);//至少一分钟，最多两分钟
            stringRedisTemplate.opsForValue().set(key,"",expireSeconds,TimeUnit.SECONDS);
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        FindUserByIdResDTO response=FindUserByIdResDTO.builder()
                .id(userId)
                .avatar(user.getAvatar())
                .nickName(user.getNickname())
                .introduction(user.getIntroduction())
                .build();

        //4、将信息存入redis进行缓存
        threadPoolTaskExecutor.submit(()->{
            //4.1、生成随机时间，避免缓存雪崩，保底一天
            long expireSeconds=USER_INFO_EXPIRE+RandomUtil.randomLong(USER_INFO_EXPIRE);
            stringRedisTemplate.opsForValue().set(key,JsonUtils.toJsonString(response),expireSeconds,TimeUnit.SECONDS);
        });
        return Response.success(response);
    }

    @Override
    public Response<List<FindUserByIdResDTO>> findByIds(FindUsersByIdsReqDTO findUsersByIdsReqDTO) {
        List<Long> userIds=findUsersByIdsReqDTO.getIds();
        List<String> redisKeys=userIds.stream()
                .map(id->{
                    return USER_INFO_KEY_PREFIX+id;
                })
                .toList();

        //1、先从redis获取到数据
        List<String> redisValues=stringRedisTemplate.opsForValue().multiGet(redisKeys);

        if(CollectionUtil.isNotEmpty(redisValues)){
            redisValues=redisValues.stream().filter(ObjectUtil::isNotNull).toList();//过滤掉空信息
        }

        //返回的参数
        List<FindUserByIdResDTO> findUserByIdResDTOList= Lists.newArrayList();
        //2、将过滤后的缓存集合转换为DTO实体类
        if(CollectionUtil.isNotEmpty(redisValues)){
            findUserByIdResDTOList=redisValues.stream()
                    .map(value->JsonUtils.parseObject(value,FindUserByIdResDTO.class))
                    .toList();
        }
        //2.1、如果被查询的用户都在redis则直接返回(简单匹配一下就是了)
        if(CollectionUtil.size(userIds)== CollectionUtil.size(findUserByIdResDTOList)){
            return Response.success(findUserByIdResDTOList);
        }

        //2.2、另外的情况：1、用户数据不存在，2、缓存数据不全，那么就筛选出来之后去数据库查询
        List<Long> userIdsNeedQuery=null;
        if(CollectionUtil.isNotEmpty(findUserByIdResDTOList)){
            //转map
            Map<Long,FindUserByIdResDTO> map=findUserByIdResDTOList.stream()
                    .collect(Collectors.toMap(FindUserByIdResDTO::getId,p->p));
            //筛选出需要查询db的用户id
            userIdsNeedQuery=userIds.stream().filter(id->ObjectUtil.isNull(map.get(id))).toList();
        }else{//如果缓存中一条用户信息都没有查到则直接全部都查找
            userIdsNeedQuery=userIds;
        }
        //3、数据库查询
        List<User> users=userMapper.selectByIds(userIdsNeedQuery);
        List<FindUserByIdResDTO> findUserByIdResDTOList1=null;
        if(CollectionUtil.isNotEmpty(users)){//查询记录不为空
            //DO转DTO
            findUserByIdResDTOList1=users.stream()
                    .map(user->FindUserByIdResDTO.builder()
                            .id(user.getId())
                            .nickName(user.getNickname())
                            .avatar(user.getAvatar())
                            .introduction(user.getIntroduction())
                            .build())
                    .toList();
            //4、异步用户信息同步到redis（使用pipeline管道）
            if(CollectionUtil.isNotEmpty(users)){
                //do转dto
                findUserByIdResDTOList1=users.stream()
                        .map(user->
                            FindUserByIdResDTO.builder()
                                    .id(user.getId())
                                    .nickName(user.getNickname())
                                    .avatar(user.getAvatar())
                                    .introduction(user.getIntroduction())
                                    .build()
                        )
                        .toList();
                List<FindUserByIdResDTO> finalFindUserByIdResDTOS=findUserByIdResDTOList1;
                threadPoolTaskExecutor.submit(()->{
                    //转换为map
                    Map<Long,FindUserByIdResDTO> map= finalFindUserByIdResDTOS.stream()
                            .collect(Collectors.toMap(FindUserByIdResDTO::getId,p->p));

                    //执行pipeline操作，一次性写入
                    stringRedisTemplate.executePipelined(new SessionCallback<>() {
                        @Override
                        public Object execute(RedisOperations operations) {
                            for (User user : users) {
                                Long userId = user.getId();

                                // 用户信息缓存 Redis Key
                                String userInfoRedisKey = USER_INFO_KEY_PREFIX+userId;

                                // DTO 转 JSON 字符串
                                FindUserByIdResDTO findUserInfoByIdRspDTO = map.get(userId);
                                String value = JsonUtils.toJsonString(findUserInfoByIdRspDTO);

                                // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
                                long expireSeconds = USER_INFO_EXPIRE + RandomUtil.randomInt(60*60*24);
                                operations.opsForValue().set(userInfoRedisKey, value, expireSeconds, TimeUnit.SECONDS);
                            }
                            return null;
                        }
                    });
                });
            }

        }

        //4、合并数据
        if(CollectionUtil.isNotEmpty(findUserByIdResDTOList1)){
            findUserByIdResDTOList.addAll(findUserByIdResDTOList1);
        }
        return Response.success(findUserByIdResDTOList);
    }
}
