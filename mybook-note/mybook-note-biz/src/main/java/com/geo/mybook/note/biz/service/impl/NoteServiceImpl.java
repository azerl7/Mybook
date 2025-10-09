package com.geo.mybook.note.biz.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.geo.framework.common.exception.BizException;
import com.geo.framework.common.response.Response;
import com.geo.framework.common.util.DateUtils;
import com.geo.framework.common.util.JsonUtils;
import com.geo.framewrok.biz.context.holder.LoginUserContextHolder;
import com.geo.mybook.note.biz.domain.dto.CollectUnCollectNoteMQDTO;
import com.geo.mybook.note.biz.domain.dto.LikeUnLikeNoteMQDTO;
import com.geo.mybook.note.biz.domain.dto.NoteOperatorMQDTO;
import com.geo.mybook.note.biz.domain.po.Channel;
import com.geo.mybook.note.biz.domain.po.Note;
import com.geo.mybook.note.biz.domain.po.NoteCollection;
import com.geo.mybook.note.biz.domain.po.NoteLike;
import com.geo.mybook.note.biz.domain.vo.*;
import com.geo.mybook.note.biz.enums.*;
import com.geo.mybook.note.biz.mapper.*;
import com.geo.mybook.note.biz.rpc.DistributedIdGeneratorRpcService;
import com.geo.mybook.note.biz.rpc.KeyValueRpcService;
import com.geo.mybook.note.biz.rpc.UserRpcService;
import com.geo.mybook.note.biz.service.NoteService;
import com.geo.mybook.user.dto.res.FindUserByIdResDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.geo.framework.common.util.Constants.*;
import static com.geo.mybook.note.biz.constant.MQConstants.*;
import static com.geo.mybook.note.biz.enums.ResponseCodeEnum.*;

/*
creator：AZERL7
createTime：11:09
*/
@Slf4j
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper,Note> implements NoteService {

    @Resource
    private NoteMapper noteMapper;

    @Resource
    private TopicMapper topicMapper;

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;

    @Resource
    private KeyValueRpcService keyValueRpcService;
    @Resource
    private UserRpcService userRpcService;

    @Resource(name="taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private NoteLikeMapper noteLikeMapper;

    //缓存id，缓存中的数据
    private static final Cache<Long,String> LOCAL_CACHE=Caffeine.newBuilder()
            .initialCapacity(CAFFEINE_INIT_CAPACITY) // 设置初始容量为 10000 个条目
            .maximumSize(CAFFEINE_MAX_CAPACITY) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(CAFFEINE_EXPIRE, TimeUnit.MINUTES) // 设置缓存条目在写入后 1 小时过期
            .build();
    @Resource
    private NoteCollectionMapper noteCollectionMapper;


    @Override
    public Response<String> publishNote(PublishNoteReqVo publishNoteReqVo) {
        //1、获取笔记类型
        Integer type=publishNoteReqVo.getType();
        NoteTypeEnum noteTypeEnum=NoteTypeEnum.valueOf(type);

        if(ObjectUtil.isNull(noteTypeEnum)){
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }

        //2、获取图片和视频地址
        String imgUrls=null;
        boolean isContentEmpty=true;//笔记内容是否为空，默认为空
        String videoUrl=null;//只允许用户上传一个视频
        switch(noteTypeEnum){
            case IMAGE_TEXT -> {
                imgUrls=imgNote(publishNoteReqVo.getImgUrls());
                break;
            }
            case VIDEO -> {
                videoUrl=videoNote(publishNoteReqVo.getVideoUrl());
                break;
            }
            default -> {
                break;
            }
        }

        //3、调用分布式id生成服务，生成笔记id
        String snowflakeId= distributedIdGeneratorRpcService.getSnowflakeId();

        //4、判断用户文章内容
        String contentUuid=null;
        String content=publishNoteReqVo.getContent();

        //4.1、如果填写了内容
        if(StringUtils.isNotBlank(content)){
            isContentEmpty=false;
            contentUuid= UUID.randomUUID().toString();
//            System.out.println(contentUuid);
            if (!keyValueRpcService.saveNoteContent(contentUuid,content)){
                throw new BizException(ResponseCodeEnum.NOTE_PUBLISH_FAIL);
            }
        }

        //5、话题
        Long topicId=publishNoteReqVo.getTopicId();
        String topicName=null;
        if(ObjectUtil.isNotNull(topicId)){
            topicName=topicMapper.selectNameByPrimaryKey(topicId);
        }

        //6、发布者id
        Long creatorId= LoginUserContextHolder.getUserId();

        //7、构建笔记
        Note note=Note.builder()
                .id(Long.valueOf(snowflakeId))
                .isContentEmpty(isContentEmpty)
                .creatorId(creatorId)
                .imgUrls(imgUrls)
                .title(publishNoteReqVo.getTitle())
                .topicId(topicId)
                .topicName(topicName)
                .type(type)
                .visible(NoteVisibleEnum.PUBLIC.getCode())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .status(NoteStatusEnum.NORMAL.getCode())
                .isTop(Boolean.FALSE)
                .videoUrl(videoUrl)
                .contentUuid(contentUuid)
                .build();

        try{
            save(note);//简单的存储业务使用mp
        }catch(Exception e){
            log.error("==>笔记存储失败",e);
            //如果笔记保存失败，则删除笔记内容
            if (StringUtils.isNotBlank(contentUuid)) {
                keyValueRpcService.deleteNoteContent(contentUuid);
            }
        }

        //8、发送mq消息通知计数服务
        NoteOperatorMQDTO noteOperatorMQDTO=NoteOperatorMQDTO.builder()
                .creatorId(creatorId)
                .noteId(Long.valueOf(snowflakeId))
                .type(NoteOperateEnum.PUBLISH.getCode())
                .build();

        Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(noteOperatorMQDTO)).build();
        String destination= TOPIC_NOTE_OPERATE+":"+TAG_NOTE_PUBLISH;
        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记发布】MQ 发送成功，SendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记发布】MQ 发送异常Throwable",throwable);
            }
        });

        return Response.success();
    }

    private String imgNote(List<String> imgUrlList){
        Preconditions.checkArgument(CollectionUtil.isNotEmpty(imgUrlList),"笔记内容不能为空");
        Preconditions.checkArgument(imgUrlList.size() <= 8, "笔记图片不能多于 8 张");
        return StringUtils.join(imgUrlList,",");
    }

    private String videoNote(String videoUrl){
        Preconditions.checkArgument(StringUtils.isNotBlank(videoUrl),"笔记视频不能为空");
        return videoUrl;
    }

    @Override
    public Response<FindNoteDetailResVo> findNoteDetail(FindNoteDetailReqVo findNoteDetailReqVo) {
        //1、获取笔记id
        Long noteId=findNoteDetailReqVo.getId();

        //2、获取当前登录用户
        Long userId=LoginUserContextHolder.getUserId();

        //2.2.5、从本地缓存中获取
        // 先从本地缓存中查询
        String findNoteDetailRspVOStrLocalCache = LOCAL_CACHE.getIfPresent(noteId);
        if (StringUtils.isNotBlank(findNoteDetailRspVOStrLocalCache)) {
            FindNoteDetailResVo findNoteDetailResVo = JsonUtils.parseObject(findNoteDetailRspVOStrLocalCache, FindNoteDetailResVo.class);
            log.info("==> 命中了本地缓存；{}", findNoteDetailRspVOStrLocalCache);
            // 可见性校验
            checkNoteVisibleFromVo(userId, findNoteDetailResVo);
            return Response.success(findNoteDetailResVo);
        }

        //2.5、从redis缓存中获取
        String noteDetailRedisKey=NOTE_DETAIL_KEY+noteId;
        String noteDetailJson=stringRedisTemplate.opsForValue().get(noteDetailRedisKey);

        // 若缓存中有该笔记的数据，则直接返回
        if (StringUtils.isNotBlank(noteDetailJson)) {
            FindNoteDetailResVo findNoteDetailResVo = JsonUtils.parseObject(noteDetailJson, FindNoteDetailResVo.class);
            //写入本地缓存
            threadPoolTaskExecutor.submit(()->{
                LOCAL_CACHE.put(noteId,Objects.isNull(findNoteDetailResVo) ? "null" : JsonUtils.toJsonString(findNoteDetailResVo));
            });
            // 可见性校验
            if (Objects.nonNull(findNoteDetailResVo)) {
                Integer visible = findNoteDetailResVo.getVisible();
                checkNoteVisible(visible, userId, findNoteDetailResVo.getCreatorId());
            }
            return Response.success(findNoteDetailResVo);
        }

        //3、查询笔记
        Note note=noteMapper.selectByPrimaryKey(noteId);

        //4、笔记不存在则抛出异常
        if(ObjectUtil.isNull(note)){
            threadPoolTaskExecutor.submit(() -> {
                //4.1、添加空缓存
                long expireSeconds = NULL_EXPIRE + RandomUtil.randomLong(NULL_EXPIRE);
                stringRedisTemplate.opsForValue().set(noteDetailRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(NOTE_NOT_FOUND);
        }

        //5、可见性校验
        Integer visible=note.getVisible();
        checkNoteVisible(visible,userId,note.getCreatorId());

        //6、RPC层调用：获取用户信息（已并发查询优化）
        Long createId=note.getCreatorId();
        CompletableFuture<FindUserByIdResDTO> userResultFuture =
                CompletableFuture.supplyAsync(()-> userRpcService.findById2Nickname2Avatar(createId),threadPoolTaskExecutor);

        //6.05、RPC层调用：获取noteContent（已并发查询优化）
        CompletableFuture<String> contentResultFuture = CompletableFuture.completedFuture(null);
        if (Objects.equals(note.getIsContentEmpty(), Boolean.FALSE)) {
            contentResultFuture = CompletableFuture
                    .supplyAsync(() -> keyValueRpcService.findNoteContent(note.getContentUuid()), threadPoolTaskExecutor);
        }

        CompletableFuture<String> finalContentResultFuture = contentResultFuture;
        CompletableFuture<FindNoteDetailResVo> resultFuture=CompletableFuture
                .allOf(userResultFuture,contentResultFuture )
                .thenApply(s->{
                    //.1、获取 Future 的返回结果
                    FindUserByIdResDTO findUserByIdResDTO=userResultFuture.join();
                    String content= finalContentResultFuture.join();

                    //6.1、获取笔记类型，转换连接
                    Integer noteType=note.getType();
                    String imgUrlsStr=note.getImgUrls();
                    List<String> imgUrls = null;
                    // 6.1.1、如果查询的是图文笔记，需要将图片链接的逗号分隔开，转换成集合
                    if (ObjectUtil.equals(noteType.intValue(), NoteTypeEnum.IMAGE_TEXT.getCode())//特殊处理一下Byte和Integer
                            && StringUtils.isNotBlank(imgUrlsStr)) {
                        imgUrls = List.of(imgUrlsStr.split(","));
                    }

                    //7、 构建返参 VO 实体类
                    return FindNoteDetailResVo.builder()
                            .id(noteId)
                            .type(note.getType())
                            .title(note.getTitle())
                            .content(content)
                            .imgUris(imgUrls)
                            .topicId(note.getTopicId())
                            .topicName(note.getTopicName())
                            .creatorId(note.getCreatorId())
                            .creatorName(findUserByIdResDTO.getNickName())
                            .avatar(findUserByIdResDTO.getAvatar())
                            .videoUri(note.getVideoUrl())
                            .updateTime(note.getUpdateTime())
                            .visible(visible)
                            .build();

                });
        //获取构建好的Vo实体类

        FindNoteDetailResVo findNoteDetailResVo= null;
        try {
            findNoteDetailResVo = resultFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        //8、添加redis缓存
        FindNoteDetailResVo finalFindNoteDetailResVo = findNoteDetailResVo;
        threadPoolTaskExecutor.submit(()-> {
            String noteDetailJson1 = JsonUtils.toJsonString(finalFindNoteDetailResVo);
            long expireSeconds = USER_INFO_EXPIRE + RandomUtil.randomLong(USER_INFO_EXPIRE);
            stringRedisTemplate.opsForValue().set(noteDetailRedisKey, noteDetailJson1, expireSeconds, TimeUnit.SECONDS);
        });
        return Response.success(findNoteDetailResVo);
    }

    /**
     * 校验笔记的可见性
     * @param visible 是否可见
     * @param currUserId 当前用户 ID
     * @param creatorId 笔记创建者
     */
    private void checkNoteVisible(Integer visible, Long currUserId, Long creatorId) {
        if (ObjectUtil.equals(visible, NoteVisibleEnum.PRIVATE.getCode())
                && !ObjectUtil.equals(currUserId, creatorId)) { // 仅自己可见, 并且访问用户为笔记创建者才能访问，非本人则抛出异常
            throw new BizException(ResponseCodeEnum.NOTE_PRIVATE);
        }
    }

    /**
     * 校验笔记的可见性（针对 VO 实体类）
     * @param userId  用户id
     * @param findNoteDetailResVo vo实体类
     */
    private void checkNoteVisibleFromVo(Long userId, FindNoteDetailResVo findNoteDetailResVo) {
        if (Objects.nonNull(findNoteDetailResVo)) {
            Integer visible = findNoteDetailResVo.getVisible();
            checkNoteVisible(Integer.valueOf(visible), userId, findNoteDetailResVo.getCreatorId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public  Response<String> updateNote(UpdateNoteReqVo updateNoteReqVo){
        //1、处理元数据
        Long noteId=updateNoteReqVo.getId();
        Integer type=updateNoteReqVo.getType();
        NoteTypeEnum noteTypeEnum=NoteTypeEnum.valueOf(type);

        if(ObjectUtil.isNull(noteTypeEnum)){
            throw new BizException(ResponseCodeEnum.NOTE_TYPE_ERROR);
        }
        String imgUrls=null;
        String videoUrl=null;
        switch(noteTypeEnum){
            case VIDEO -> {
                imgUrls=imgNote(updateNoteReqVo.getImgUrls());
            }
            case IMAGE_TEXT -> {
                videoUrl=videoNote(updateNoteReqVo.getVideoUrl());
            }
        }
        Long userId=LoginUserContextHolder.getUserId();
        Note exists=noteMapper.existsById(noteId);

        if(ObjectUtil.isNull(exists)){//判断是否有该笔记
            throw new BizException(NOTE_NOT_FOUND);
        }

        if(!ObjectUtil.equal(exists.getCreatorId(),userId)){//判断创建者
            throw new BizException(ResponseCodeEnum.NOTE_CANT_OPERATE);
        }

        Long topicId=updateNoteReqVo.getTopicId();
        String topicName=null;
        if(ObjectUtil.isNotNull(topicId)){
            topicName=topicMapper.selectNameByPrimaryKey(topicId);
            if(StringUtils.isBlank(topicName)){
                throw new BizException(ResponseCodeEnum.TOPIC_NOT_FOUND);
            }
        }

        // 2、构建note对象
        String content = updateNoteReqVo.getContent();
        String uuid=UUID.randomUUID().toString();
        Note note = Note.builder()
                .id(noteId)
                .isContentEmpty(StringUtils.isBlank(content))
                .imgUrls(imgUrls)
                .title(updateNoteReqVo.getTitle())
                .topicId(updateNoteReqVo.getTopicId())
                .topicName(topicName)
                .type(type)
                .updateTime(LocalDateTime.now())
                .videoUrl(videoUrl)
                .contentUuid(uuid)
                .build();
        updateById(note);//简单的update使用mybatisplus
        //3、刷新redis缓存（redis唯一，删除即可）
        stringRedisTemplate.delete(NOTE_DETAIL_KEY+note);
        //4、刷新本地缓存（通过同步发送消息到消息队列，广播删除缓存）
        rocketMQTemplate.syncSend(TOPIC_DELETE_NOTE_LOCAL_CACHE,noteId);
        log.info("==> MQ：删除笔记本地缓存发送成功");

//        //笔记内容更新
//        Note note1 = noteMapper.selectByPrimaryKey(noteId);
//        String uuid=note1.getContentUuid();

        // 一致性保证：延迟双删策略
        // 异步发送延时消息
        Message<String> message = MessageBuilder.withPayload(String.valueOf(noteId))
                .build();

        rocketMQTemplate.asyncSend(TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE, message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 笔记缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 笔记缓存消息发送失败...", e);
                    }
                },
                3000, // 超时时间(毫秒)
                1 // 延迟级别，1 表示延时 1s
        );

        boolean isUpdateSuccess=false;
        if(StringUtils.isBlank(content)){
            //如果更新有内容的为无内容的则直接删掉文章
            isUpdateSuccess=keyValueRpcService.deleteNoteContent(uuid);
        }else{
            // 若将无内容的笔记，更新为了有内容的笔记，需要重新生成 uuid
            uuid = StringUtils.isBlank(uuid) ? UUID.randomUUID().toString() : uuid;
            // RPC层调用获取key-value服务
            isUpdateSuccess = keyValueRpcService.saveNoteContent(uuid, content);
        }

        if(!isUpdateSuccess){//事务回退
            throw new BizException(NOTE_UPDATE_FAIL);
        }

        return Response.success();
    }

    @Override
    public void deleteNoteLocalCache(Long noteId) {
        LOCAL_CACHE.invalidate(noteId);
    }

    @Override
    public Response<?> deleteNote(DeleteNoteReqVo deleteNoteReqVo) {
        //1. 从数据库中删除
        Long noteId=deleteNoteReqVo.getId();

        Long userId=LoginUserContextHolder.getUserId();
        Note exists=noteMapper.existsById(noteId);

        if(ObjectUtil.isNull(exists)){
            throw new BizException(NOTE_NOT_FOUND);
        }

        if(!ObjectUtil.equal(exists.getCreatorId(),userId)){
            throw new BizException(NOTE_CANT_OPERATE);
        }

        Note note=Note.builder()
                .id(noteId)
                .status(NoteStatusEnum.DELETED.getCode())
                .updateTime(LocalDateTime.now())
                .build();
        boolean count=updateById(note);
        if(!count){
            throw new BizException(NOTE_NOT_FOUND);
        }
        //2. 从redis中删除
        String noteDetailRedisKey=NOTE_DETAIL_KEY+noteId;
        stringRedisTemplate.delete(noteDetailRedisKey);

        //3. 发送mq消息删除每台实例上的缓存
        rocketMQTemplate.syncSend(TOPIC_DELETE_NOTE_LOCAL_CACHE,noteId);
        log.info("==> mq:send delete local cache note message is success");

        //4、发送mq消息统计计数服务计算
        NoteOperatorMQDTO noteOperatorMQDTO=NoteOperatorMQDTO.builder()
                .noteId(noteId)
                .creatorId(userId)//如果不相同则不会进入此处，直接使用userId即可
                .type(NoteOperateEnum.DELETE.getCode())
                .build();

        Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(noteOperatorMQDTO)).build();
        String destination=TOPIC_NOTE_OPERATE+":"+TAG_NOTE_DELETE;

        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记删除】MQ 消息发送成功，SendResult:{}", sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记删除】MQ 消息发送失败,throwable:", throwable);
            }
        });

        return Response.success();
    }

    @Override
    public Response<?> visibleOnly(UpdateNoteVisibleOnlyMeVo updateNoteVisibleOnlyMeVo) {
        //1、获取笔记id
        Long noteId=updateNoteVisibleOnlyMeVo.getId();
        //获取用户id
        Long userId=LoginUserContextHolder.getUserId();
        Note exists=noteMapper.existsById(noteId);
        if(ObjectUtil.isNull(exists)){
            throw new BizException(NOTE_NOT_FOUND);
        }
        if(!ObjectUtil.equal(exists.getCreatorId(),userId)){
            throw new BizException(NOTE_CANT_OPERATE);
        }


        //2、构建数据,并更新
        Note note=Note.builder()
                .id(noteId)
                .visible(NoteVisibleEnum.PRIVATE.getCode())
                .updateTime(LocalDateTime.now())
                .build();

        boolean update = update().eq("id", noteId)
                .eq("status", 1)
                .set("visible", NoteVisibleEnum.PRIVATE.getCode())
                .set("update_time", LocalDateTime.now()).update();

        if(!update){
            throw new BizException(ResponseCodeEnum.NOTE_CANT_VISIBLE_ONLY_ME);
        }

        //3、删除redis
        stringRedisTemplate.delete(NOTE_DETAIL_KEY+noteId);
        //4、mq消息延迟双删
        rocketMQTemplate.syncSend(TOPIC_DELETE_NOTE_LOCAL_CACHE, noteId);
        log.info("==> MQ 发送延时消息删除本地笔记缓存");

        return Response.success();
    }

    @Override
    public Response<?> topNote(TopNoteReqVo topNoteReqVo) {
        //1、处理元数据
        Long noteId=topNoteReqVo.getId();
        Boolean isTop=topNoteReqVo.getIsTop();

        //2、更新数据

        boolean update = update()
                .eq("id",noteId)
                .eq("creator_id",LoginUserContextHolder.getUserId())//创建者才能修改笔记状态（审核修改审核状态除外）
                .set("is_top", isTop)
                .set("update_time", LocalDateTime.now()).update();


        if(!update){
            throw new BizException(NOTE_CANT_OPERATE);
        }
        //3、更新redis缓存
        stringRedisTemplate.delete(NOTE_DETAIL_KEY+noteId);
        //4、发送mq消息实现延迟双删
        rocketMQTemplate.syncSend(TOPIC_DELETE_NOTE_LOCAL_CACHE,noteId);
        log.info("==> MQ发送延时消息删除本地笔记缓存");
        return Response.success();
    }

    @Override
    public Response<?> likeNote(LikeNoteReqVo likeNoteReqVo) {
        //1、校验点赞的笔记是否存在
        Long noteId=likeNoteReqVo.getId();
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);
        //2、判断目标笔记是否已经被点赞过
        //布隆过滤器判断笔记是否点赞，需要验证存在
        Long userId=LoginUserContextHolder.getUserId();
        String bloomUserNoteLikeListKey=BLOOM_USER_NOTE_LIKE_LIST_KEY+userId;
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_like_check.lua")));
        script.setResultType(Long.class);
        Long result=stringRedisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey),noteId.toString());
        NoteLikeLuaResultEnum resultEnum=NoteLikeLuaResultEnum.valueOf(result);
        String userNoteLikeZSetKey = USER_NOTE_LIKE_ZSET_KEY+userId;

        switch(Objects.requireNonNull(resultEnum)){
            case NOTE_LIKED -> {
                //布隆过滤器村子假阳性
                //通过zset进行进一步验证，如果zset大小限制100如果zset也误判，则进行数据库查询验证
                Double score=stringRedisTemplate.opsForZSet().score(userNoteLikeZSetKey,noteId.toString());
                if( ObjectUtil.isNotNull(score)){
                    throw new BizException(NOTE_ALREADY_LIKED);//zset里面有，表示最近已经点赞
                }
                int count=noteLikeMapper.selectNoteIsLiked(userId,noteId);
                if(count>0){
                    //数据库中有记录但是zset不存在，则异步初始化zset
                    asyncInitUserNoteLikeZSet(userId,userNoteLikeZSetKey);
                    throw new BizException(NOTE_ALREADY_LIKED);
                }
            }

            //布隆过滤器不存在
            case NOT_EXIST -> {
                //从数据库中校验笔记是否被点赞，并异步初始化布隆过滤器，设置过期时间
                int count=noteLikeMapper.selectCountByUserIdAndNoteId(userId,noteId);
                long expire=BLOOM_EXPIRE_SECONDS+RandomUtil.randomLong(BLOOM_EXPIRE_SECONDS);
                if(count>0){
                    //查询到有笔记点赞记录但是没有布隆过滤器，那就是过期了。重新初始化一下
                    batchAddNoteLike2BloomAndExpire(userId, expire, bloomUserNoteLikeListKey);
                    throw new BizException(NOTE_ALREADY_LIKED);
                }
                //若目标未点赞则查询当前用户是否有点赞其他笔记，有则同步初始化布隆过滤器，保证布隆过滤器被初始化
                batchAddNoteLike2BloomAndExpire(userId, expire, bloomUserNoteLikeListKey);
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_note_like_and_expire.lua")));
                script.setResultType(Long.class);
                stringRedisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), noteId.toString(), Long.toString(expire));
            }
        }
        //3、更新用户ZSET点赞列表，用于验证布隆过滤器
        LocalDateTime now =LocalDateTime.now();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_like_check_and_update_zset.lua")));
        script.setResultType(Long.class);
        result=stringRedisTemplate.execute(script,Collections.singletonList(userNoteLikeZSetKey),noteId.toString(),Long.toString(DateUtils.localDateTime2TimeStamp(now)));
        //3.1、如果zset列表不存在
        if(Objects.equals(result,NoteLikeLuaResultEnum.NOT_EXIST.getCode())){
            //重新初始化zset列表
            //查询最新的前一百条
            List<NoteLike> noteLikeList=noteLikeMapper.selectLikedByUserIdAndLimit(userId,100);
            Long expire=USER_NOTE_LIKE_ZSET_EXPIRE+RandomUtil.randomLong(USER_NOTE_LIKE_ZSET_EXPIRE);
            DefaultRedisScript<Long> script1=new DefaultRedisScript<>();
            script1.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_like_zset_and_expire.lua")));
            script1.setResultType(Long.class);
            //数据库当中存在，则初始化zset，然后向里面添加一条
            if(CollectionUtil.isNotEmpty(noteLikeList)){
                String[] luaArgs=buildNoteLikeLuaArgs(noteLikeList,expire);
                stringRedisTemplate.execute(script1,Collections.singletonList(userNoteLikeZSetKey),luaArgs);
                //既然都是添加，为什么不包含到上面的脚本里面去，（你这就是典型的算法思维）
                //因为业务逻辑要分开，减少耦合性
                //再次调用note_like_check_and_update_zset.lua脚本，将新点赞的笔记添加到zset
                stringRedisTemplate.execute(script,Collections.singletonList(userNoteLikeZSetKey),noteId,DateUtils.localDateTime2TimeStamp(now));
            }else{//如果数据库中不存在，则初始化zset直接向里面添加一条
                List<String> luaArgs=Lists.newArrayList();
                luaArgs.add(Long.toString(DateUtils.localDateTime2TimeStamp(now)));
                luaArgs.add(noteId.toString());
                luaArgs.add(expire.toString());
                stringRedisTemplate.execute(script1,Collections.singletonList(userNoteLikeZSetKey), luaArgs.toArray());
            }
        }
        //4、mq发送消息入库
        LikeUnLikeNoteMQDTO likeUnLikeNoteMQDTO=LikeUnLikeNoteMQDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(LikeUnlikeNoteTypeEnum.LIKE.getCode())
                .createTime(now)
                .creatorId(creatorId)
                .build();

        Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnLikeNoteMQDTO))
                .build();
        String destination=TOPIC_LIKE_OR_UNLIKE+":"+TAG_LIKE;
        String hashKey=String.valueOf(userId);

        rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记点赞】MQ 发送成功，SendResult：{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记点赞】MQ 发送异常，throwable",throwable);
            }
        });
        return Response.success();
    }

    /**
     * 异步初始化zset
     */
    private void asyncInitUserNoteLikeZSet(Long userId,String userNoteLikeZSetKey){
        threadPoolTaskExecutor.submit(()->{
            boolean hasKey=stringRedisTemplate.hasKey(userNoteLikeZSetKey);
            //zset中不存在，则初始化zset
            if(!hasKey){
                //1、查询当前用户的100条数据
                List<NoteLike> noteLikeList= noteLikeMapper.selectLikedByUserIdAndLimit(userId,100);
                //2、构建luaArgs参数
                Long expire=USER_NOTE_LIKE_ZSET_EXPIRE+RandomUtil.randomLong(USER_NOTE_LIKE_ZSET_EXPIRE);
                String[] luaArgs=buildNoteLikeLuaArgs(noteLikeList,expire);
                //3、执行批量添加脚本
                DefaultRedisScript<Long> script=new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/lua/batch_add_note_like_zset_and_expire.lua")));
                script.setResultType(Long.class);
                stringRedisTemplate.execute(script,Collections.singletonList(userNoteLikeZSetKey),luaArgs);
            }
        });
    }


    /**
     * 构建noteLikeLuaArgs参数
     * @param noteLikeList noteLike列表
     * @param expire ttl
     * @return strings
     */
    private String[] buildNoteLikeLuaArgs(List<NoteLike> noteLikeList,Long expire){
        int argsLengths= noteLikeList.size()*2+1;//每个笔记点赞有两个参数，一个分数，一个值，参数结束之后有一个为ttl
        int i=0;
        String[] luaArgs=new String[argsLengths];
        for(NoteLike noteLike:noteLikeList){
            luaArgs[i]=Long.toString(DateUtils.localDateTime2TimeStamp(noteLike.getCreateTime()));
            luaArgs[i+1]=noteLike.getNoteId().toString();
            i+=2;
        }
        luaArgs[argsLengths-1]=expire.toString();
        return luaArgs;
    }

    /**
     * 异步向布隆过滤器添加内容，并且添加ttl
     * @param userId 用户id
     * @param expire ttl
     * @param bloomUserNoteLikeListKey 布隆过滤器key
     */
    private void batchAddNoteLike2BloomAndExpire(Long userId, long expire, String bloomUserNoteLikeListKey) {
        threadPoolTaskExecutor.submit(() -> {
            try {
                // 异步全量同步一下，并设置过期时间
                List<NoteLike> noteLikeList = noteLikeMapper.selectByUserId(userId);
                if (CollUtil.isNotEmpty(noteLikeList)) {
                    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                    //脚本不执行的情况下记得注意是不是路径写错了
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_note_like_and_expire.lua")));
                    script.setResultType(Long.class);
                    List<String> luaArgs = Lists.newArrayList();
                    noteLikeList.forEach(noteLike -> luaArgs.add(noteLike.getNoteId().toString())); // 将每个点赞的笔记 ID 传入
                    luaArgs.add(Long.toString(expire));  // 最后一个参数是过期时间（秒）
                    //lua仅仅支持数组数据结构不支持其他复杂类型结构
                    stringRedisTemplate.execute(script, Collections.singletonList(bloomUserNoteLikeListKey), luaArgs.toArray());//todo:测试验证
                }
            } catch (Exception e) {
                log.error("## 异步初始化布隆过滤器异常: ", e);
            }
        });
    }


    /**
     * 查询笔记是否存在并返回作者id
     * @param noteId 笔记id
     * @return creatorId 作者id
     */
    private Long checkNoteIsExistAndGetCreatorId(Long noteId){
        //1、从本地缓存校验
        String findNoteDetailResVoStrLocalCache=LOCAL_CACHE.getIfPresent(noteId);
        FindNoteDetailResVo findNoteDetailResVo=JsonUtils.parseObject(findNoteDetailResVoStrLocalCache,FindNoteDetailResVo.class);

        // 本地缓存没有去reids查找
        if(ObjectUtil.isNull(findNoteDetailResVoStrLocalCache)){
            String noteDetailRedisKey=NOTE_DETAIL_KEY+noteId;
            String noteDetailJson=stringRedisTemplate.opsForValue().get(noteDetailRedisKey);
            findNoteDetailResVo=JsonUtils.parseObject(noteDetailJson, FindNoteDetailResVo.class);
            //都不存在，去数据库校验
            if(ObjectUtil.isNull(findNoteDetailResVo)){
                Long creatorId = noteMapper.selectCreatorIdByNoteId(noteId);
                if(ObjectUtil.isNull(creatorId)){
                    throw new BizException(NOTE_NOT_FOUND);
                }
                //如果在数据库查询到，异步到缓存里
                threadPoolTaskExecutor.submit(()->{
                    FindNoteDetailReqVo findNoteDetailReqVo=FindNoteDetailReqVo.builder()
                            .id(noteId)
                            .build();
                    findNoteDetail(findNoteDetailReqVo);//为什么这里需要同步笔记详情到缓存？
                    //笔记本身可以不用进入详情页进行点赞，如果一个笔记比较火爆，很多人都在外面点赞，没有把笔记缓存的话，
                    // 会每次查询都查找数据库造成缓存击穿
                });
                return creatorId;
            }
        }
        return findNoteDetailResVo.getCreatorId();
    }

    @Override
    public Response<?> unlikeNote(UnlikeNoteReqVo unlikeNoteReqVo) {
        //1、获取笔记id
        Long noteId=unlikeNoteReqVo.getId();
        //2、校验笔记是否存在
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);
        //3、校验笔记是否已经点赞过
        Long  userId=LoginUserContextHolder.getUserId();
        String bloomUserNoteLikeListKey=BLOOM_USER_NOTE_LIKE_LIST_KEY+userId;
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_unlike_check.lua")));
        script.setResultType(Long.class);
        Long result=stringRedisTemplate.execute(script,Collections.singletonList(bloomUserNoteLikeListKey),noteId.toString());
        NoteUnlikeLuaResultEnum en=NoteUnlikeLuaResultEnum.valueOf(result);
        switch(en){
            //布隆过滤器不存在
            case NOT_EXIST -> {
                //异步初始化过滤器
                threadPoolTaskExecutor.submit(()->{
                    long expire=BLOOM_EXPIRE_SECONDS+RandomUtil.randomLong(BLOOM_EXPIRE_SECONDS);
                    batchAddNoteLike2BloomAndExpire(userId,expire,bloomUserNoteLikeListKey);
                });
                int count=noteLikeMapper.selectCountByUserIdAndNoteId(userId,noteId);
                //未点赞，抛出异常
                if(count==0){
                    throw new BizException(NOTE_NOT_LIKED);
                }
            }
            case NOTE_NOTE_LIKED -> throw new BizException(NOTE_NOT_LIKED);
        }
        //4、删除zset
        String userNoteLikeZSetKey=USER_NOTE_LIKE_ZSET_KEY+userId;
        stringRedisTemplate.opsForZSet().remove(userNoteLikeZSetKey,noteId.toString());

        //5、发送mq，数据库更新
        LikeUnLikeNoteMQDTO likeUnLikeNoteMQDTO=LikeUnLikeNoteMQDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(LikeUnlikeNoteTypeEnum.UNLIKE.getCode())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();

        Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(likeUnLikeNoteMQDTO)).build();
        String destination=TOPIC_LIKE_OR_UNLIKE+":"+TAG_UNLIKE;
        String hashKey=String.valueOf(userId);
        rocketMQTemplate.asyncSendOrderly(destination,message,hashKey,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记取消点赞】MQ 消息发送成功，SendResult：{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记取消点赞】mq 消息发送失败 throwable:     ",throwable);
            }
        });
        return Response.success();
    }

    public Response<?> collectNote(CollectNoteReqVo collectNoteReqVo){
        //笔记id
        Long noteId=collectNoteReqVo.getId();
        //1、校验被收藏的笔记是否存在
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);
        //2、判断目标笔记，是否已经被收藏过
        Long userId=LoginUserContextHolder.getUserId();
        String bloomUserNoteCollectListKey =BLOOM_USER_NOTE_COLLECT_LIST_KEY+userId;
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_collect_check.lua")));
        script.setResultType(Long.class);
        Long result=stringRedisTemplate.execute(script,Collections.singletonList(bloomUserNoteCollectListKey),noteId.toString());
        NoteCollectLuaResultEnum en=NoteCollectLuaResultEnum.valueOf(result);

        String userNoteCollectZSetKey = USER_NOTE_COLLECT_ZSET_KEY+userId;

        switch(en){
            //布隆过滤器不存在
            case NOT_EXIST ->{
                //初始化布隆过滤器并向其中添加已经被收藏
                int count=noteCollectionMapper.selectCountByUserIdAndNoteId(userId,noteId);
                //过期时间
                Long expire=BLOOM_USER_NOTE_COLLECT_LIST_EXPIRE+RandomUtil.randomLong(BLOOM_USER_NOTE_COLLECT_LIST_EXPIRE);
                //目标笔记数已被收藏
                if(count>0){
                    throw new BizException(NOTE_ALREADY_COLLECTED);
                }
                //如果笔记未被收藏，查询当前用户是否有收藏其他笔记，有则同步初始化布隆过滤器
                batchAddNoteCollect2BloomAndExpire(userId,expire,bloomUserNoteCollectListKey);
                //添加当前收藏笔记id到布隆过滤器中
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_add_note_collect_and_expire.lua")));
//                log.info("## 测试改变脚本文件之后会不会影响返回值");
                //好像是不会改变的,但是没有时间来测试，先直接写了后面回来测试吧
                script.setResultType(Long.class);
                stringRedisTemplate.execute(script,Collections.singletonList(bloomUserNoteCollectListKey),noteId.toString(),expire.toString());
            }
            //目标笔记已经被收藏，需要二次确认
            case NOTE_COLLECTED->{
                //去zset里面确认（但是收藏比起点赞来说要求没有这么高，可以先使用数据库验证）
                Double score=stringRedisTemplate.opsForZSet().score(userNoteCollectZSetKey,noteId.toString());
                if(ObjectUtil.isNotNull(score)){
                    throw new BizException(NOTE_ALREADY_COLLECTED);
                }
                int count=noteCollectionMapper.selectNoteIsCollected(userId, noteId);
                if(count>0){
                    //数据库中有记录，zset没有，则是zset过期了，异步重新初始化zset
                    //zset中插入300条,收藏列表可以被所有用户查看，而且用户主动访问的概率也要大一些，所以缓存多一些数据
                    //把当前笔记插入zset
                    asyncInitUserNoteCollectsZSet(userId,userNoteCollectZSetKey);
                    throw new BizException(NOTE_ALREADY_COLLECTED);
                }
            }
        }
        //3、更新用户zset表
        LocalDateTime now=LocalDateTime.now();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/note_collect_check_and_update_zset.lua")));
        script.setResultType(Long.class);
        result=stringRedisTemplate.execute(script,Collections.singletonList(userNoteCollectZSetKey),noteId.toString(),Long.toString(DateUtils.localDateTime2TimeStamp(now)));

        //如果zset列表不存在则需要重新初始化
        if(ObjectUtil.equal(result, NoteCollectLuaResultEnum.NOT_EXIST.getCode())){
            //3.1、重新初始化zset
            //3.1.1、从数据库查询最新的300条数据
            List<NoteCollection> noteCollectionList=noteCollectionMapper.selectCollectedByUserIdAndLimit(userId,300);
            Long expire=USER_NOTE_COLLECT_ZSET_EXPIRE+RandomUtil.randomLong(USER_NOTE_COLLECT_ZSET_EXPIRE);
            DefaultRedisScript<Long> script2=new DefaultRedisScript<>();
            script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_collect_zset_and_expire.lua")));
            script2.setResultType(Long.class);
            //3.1.2、如果数据库有数据则需要批量同步zset
            if(CollectionUtil.isNotEmpty(noteCollectionList)){
                String[] luaArgs=buildNoteCollectZSetLuaArgs(noteCollectionList,expire);
                stringRedisTemplate.execute(script2,Collections.singletonList(userNoteCollectZSetKey),luaArgs);
                //添加当前收藏
                stringRedisTemplate.execute(script,Collections.singletonList(userNoteCollectZSetKey),noteId.toString(),DateUtils.localDateTime2TimeStamp(now));
            }else{//如果没有收藏的笔记，则直接将当前收藏插入即可
                List<String> luaArgs=Lists.newArrayList();
                luaArgs.add(Long.toString(DateUtils.localDateTime2TimeStamp(now)));
                luaArgs.add(noteId.toString());
                luaArgs.add(expire.toString());
                stringRedisTemplate.execute(script2,Collections.singletonList(userNoteCollectZSetKey),luaArgs.toArray());
            }

        }
        //4、发送mq、将收藏数据入库
        CollectUnCollectNoteMQDTO collect=CollectUnCollectNoteMQDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(CollectUnCollectEnum.COLLECT.getCode())
                .createTime(now)
                .creatorId(creatorId)
                .build();

        Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(collect)).build();
        String destination=TOPIC_COLLECT_OR_UN_COLLECT+":"+TAG_COLLECT;
        String hashKey=String.valueOf(userId);
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记收藏】MQ 发送成功,sendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记收藏】 MQ 发送失败，throwable",throwable);
            }
        });
        return Response.success();
    }


    public Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO){
        Long userId=LoginUserContextHolder.getUserId();
        Long noteId=unCollectNoteReqVO.getNoteId();
        //1、校验笔记是否存在
        Long creatorId = checkNoteIsExistAndGetCreatorId(noteId);
        //2、校验笔记是否被收藏
        String bloomUserNoteCollectListKey=BLOOM_USER_NOTE_COLLECT_LIST_KEY+userId;
        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_note_uncollect_check.lua")));
        script.setResultType(Long.class);
        Long result=stringRedisTemplate.execute(script,Collections.singletonList(bloomUserNoteCollectListKey),noteId.toString());
        NoteUnCollectLuaResultEnum en=NoteUnCollectLuaResultEnum.valueOf(result);
        switch(en){
            //布隆过滤器不存在，异步初始化布隆过滤器
            case NOT_EXIST -> {
                threadPoolTaskExecutor.submit(()->{
                    Long expire=BLOOM_USER_NOTE_COLLECT_LIST_EXPIRE+RandomUtil.randomLong(BLOOM_USER_NOTE_COLLECT_LIST_EXPIRE);
                    batchAddNoteCollect2BloomAndExpire(userId,expire,bloomUserNoteCollectListKey);
                });

                int count=noteCollectionMapper.selectNoteIsCollected(userId,noteId);
                if(count==0){
                    throw new BizException(NOTE_NOT_COLLECTED);
                }
            }
            //未收藏，直接结束
            case NOTE_NOT_COLLECTED ->throw new BizException(NOTE_NOT_COLLECTED) ;
        }
        //3、删除zset中已收藏的笔记id
        String userNoteCollectZSetKey=USER_NOTE_COLLECT_ZSET_KEY+userId;
        stringRedisTemplate.opsForZSet().remove(userNoteCollectZSetKey, noteId.toString());

        //4、发送mq更新数据库
        CollectUnCollectNoteMQDTO  collection=CollectUnCollectNoteMQDTO.builder()
                .userId(userId)
                .noteId(noteId)
                .type(CollectUnCollectEnum.UN_COLLECT.getCode())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();
        Message<String> message=MessageBuilder.withPayload(JsonUtils.toJsonString(collection)).build();
        String destination=TOPIC_COLLECT_OR_UN_COLLECT+":"+TAG_UN_COLLECT;
        String hashKey=String.valueOf(userId);
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【笔记取消收藏】MQ 发送成功，SendResult:{}",sendResult);
            }

            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【笔记取消收藏】 MQ 发送失败throwable",throwable);
            }
        });
        return Response.success();
    }

    /**
     * 异步初始化布隆过滤器
     * @param userId 用户id
     * @param expire 过期时间
     */
    private void batchAddNoteCollect2BloomAndExpire(Long userId,Long expire,String bloomUserNoteCollectListKey){
        threadPoolTaskExecutor.submit(()->{
            try{
                List<NoteCollection> noteCollectionList=noteCollectionMapper.selectByUserId(userId);
                if(CollectionUtil.isNotEmpty(noteCollectionList)){
                    DefaultRedisScript<Long> script=new DefaultRedisScript<>();
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/bloom_batch_add_note_collect_and_expire.lua")));
                    script.setResultType(Long.class);
                    List<String> luaArgs=Lists.newArrayList();
                    noteCollectionList.forEach(noteCollection -> luaArgs.add(noteCollection.getNoteId().toString()));
                    luaArgs.add(expire.toString());
                    stringRedisTemplate.execute(script,Collections.singletonList(bloomUserNoteCollectListKey),luaArgs.toArray());
                }
            }catch(Exception e){
                log.error("## 异步初始化 【笔记收藏】 布隆过滤器异常",e);
            }
        });
    }


    /**
     *
     * 异步初始化用户收藏 ZSet
     * @param userId 用户id
     * @param userNoteCollectZSetKey zset key
     */
    private void asyncInitUserNoteCollectsZSet(Long userId,String userNoteCollectZSetKey){
        threadPoolTaskExecutor.submit(()->{
            //1、判断用户收藏笔记zset是否存在
            boolean hasKey=stringRedisTemplate.hasKey(userNoteCollectZSetKey);
            //2、不存在则初始化
            if(!hasKey){
                //查询当前用户最新收藏的 300 篇笔记 (因为是所有用户可以查看的数据所以需要多缓存一些)
                List<NoteCollection> noteCollectionList=noteCollectionMapper.selectCollectedByUserIdAndLimit(userId,300);
                //不为空则初始化
                if(CollectionUtil.isNotEmpty(noteCollectionList)){
                    Long expire=USER_NOTE_COLLECT_ZSET_EXPIRE+RandomUtil.randomLong(USER_NOTE_COLLECT_ZSET_EXPIRE);
                    String[] luaArgs=buildNoteCollectZSetLuaArgs(noteCollectionList,expire);
                    DefaultRedisScript<Long> script=new DefaultRedisScript<>();
                    script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/batch_add_note_collect_zset_and_expire.lua")));
                    script.setResultType(Long.class);
                    stringRedisTemplate.execute(script,Collections.singletonList(userNoteCollectZSetKey),luaArgs);
                }
            }
        });
    }
    /**
     * 构建笔记收藏 ZSET Lua 脚本参数
     *
     * @param noteCollectionList noteCollectionList
     * @param expire ttl
     * @return String[]
     */
    private static String[] buildNoteCollectZSetLuaArgs(List<NoteCollection> noteCollectionList, Long expire) {
        int argsLength = noteCollectionList.size() * 2 + 1; // 每个笔记收藏关系有 2 个参数（score 和 value），最后再跟一个过期时间
        String[] luaArgs = new String[argsLength];

        int i = 0;
        for (NoteCollection noteCollection : noteCollectionList) {
            luaArgs[i] = Long.toString(DateUtils.localDateTime2TimeStamp(noteCollection.getCreateTime())); // 收藏时间作为 score
            luaArgs[i + 1] = noteCollection.getNoteId().toString();          // 笔记ID 作为 ZSet value
            i += 2;
        }

        luaArgs[argsLength - 1] = expire.toString(); // 最后一个参数是 ZSet 的过期时间
        return luaArgs;
    }

    @Resource
    private ChannelMapper channelMapper;

    @Override
    public Response<?> getChanelList() {
        //todo:1、频道经常会被访问到，先从本地缓存读取 //缓存是单条数据，如果要查询多条数据缓存就不合适了，先使用redis，后面找机会优化
//        String channelList = LOCAL_CACHE.getIfPresent();
        //2、本地缓存没有去redis读取
        String channelListStr = stringRedisTemplate.opsForValue().get(CHANNEL_LIST_KEY);
        List<Channel> channelList=null;
        try {
            channelList = JsonUtils.parseList(channelListStr, Channel.class);
        }catch(Exception e){
            log.error("## 【json字符串】json字符串转换为Channel对象失败 str:{} ,exception :",channelListStr,e);
        }
        //3、redis没有去数据库读取
        if(Objects.isNull(channelList)){
            channelList=channelMapper.selectAllChannel();
            //4、存入redis
            if(CollectionUtil.isNotEmpty(channelList)){
                channelListStr=JsonUtils.toJsonString(channelList);
                stringRedisTemplate.opsForValue().set(CHANNEL_LIST_KEY,channelListStr);
            }
            //todo:5、存入本地缓存
        }
        return Response.success(channelList);
    }


    @Override
    public Response<?> getChannelNoteList(GetChannelNoteListReqVO getChannelNoteListReqVO) {
        Long channelId=getChannelNoteListReqVO.getChannelId();
        Long pageNo=getChannelNoteListReqVO.getPageNo();
        //todo：1、从缓存获取笔记
        //2、缓存没有从redis获取笔记信息
        String channelNoteListStr = stringRedisTemplate.opsForValue().get(CHANNEL_NOTE_LIST_KEY_PREFIX+channelId+":" + pageNo);
        List<Note> noteList=null;
        try{
            noteList=JsonUtils.parseList(channelNoteListStr, Note.class);
        }catch(Exception e){
            log.error("## 【json字符串】json字符串转换为Note对象失败 str:{} ,exception :",channelNoteListStr,e);
        }
        //3、redis没有去数据库获取信息
        if(CollectionUtil.isEmpty(noteList)){
            List<Long> topicIds=topicMapper.selectByChannelId(channelId);
            //todo: 获取size
            long size=10;
            noteList=noteMapper.selectByTopicIdsLimit(topicIds,(pageNo-1)*size);
            if(CollectionUtil.isNotEmpty(noteList)){//4、存入redis
                channelNoteListStr=JsonUtils.toJsonString(noteList);
                stringRedisTemplate.opsForValue().set(CHANNEL_NOTE_LIST_KEY_PREFIX+channelId+":"+pageNo,channelNoteListStr);
                //todo：5、存入缓存
            }
        }
        if(CollectionUtil.isEmpty(noteList)){
            throw new BizException(SYSTEM_ERROR);
        }
        //6、构建响应参数
        List<GetChannelNoteListResVO> getChannelNoteListResVOList = noteList.stream()
                .map(note -> GetChannelNoteListResVO.builder()
                        .id(note.getId())
                        .title(note.getTitle())
                        .creatorId(note.getCreatorId())
                        .cover(note.getImgUrls())
                        .videoUri(note.getVideoUrl())
                        .type(note.getType())
                        .avatar(
                                userRpcService.findById2Nickname2Avatar(note.getCreatorId()).getAvatar()
                        ).nickname(
                                userRpcService.findById2Nickname2Avatar(note.getCreatorId()).getNickName()
                        ).likeTotal(
                                0L
                        )
                        .build())
                .collect(Collectors.toList());

        Long userId=LoginUserContextHolder.getUserId();
        return Response.success(getChannelNoteListResVOList);
    }
}