package com.geo.mybook.note.biz.constant;


/*
creator：AZERL7
createTime：17:09
*/
public class MQConstants {

    public static final String TOPIC_DELETE_NOTE_LOCAL_CACHE = "DeleteNoteLocalCacheTopic";//删除笔记缓存

    public static final String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE = "DelayDeleteNoteRedisCacheTopic";//延迟删除redis笔记

    //点赞和取消点赞通过type取消点赞
    public static final String TOPIC_LIKE_OR_UNLIKE="LikeUnlikeTopic";

    public static final String TAG_LIKE="Like";

    public static final String TAG_UNLIKE="Unlike";

    //收藏和取消收藏使用同一个
    public static final String TOPIC_COLLECT_OR_UN_COLLECT="CollectUnCollectTopic";

    public static final String TAG_COLLECT="Collect";

    public static final String TAG_UN_COLLECT="UnCollect";

    //计数服务消费消息
    public static final String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";
    public static final String TOPIC_COUNT_NOTE_COLLECT = "CountNoteCollectTopic";


    //笔记相关服务
    public static final String TOPIC_NOTE_OPERATE = "NoteOperateTopic";//操作笔记
    public static final String TAG_NOTE_PUBLISH = "publishNote";//发布笔记
    public static final String TAG_NOTE_DELETE = "deleteNote";//删除笔记
}
