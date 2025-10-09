package com.geo.mybook.count.biz.constant;


/*
creator：AZERL7
createTime：10:47
*/
public class MQConstants {
    public static final String TOPIC_COUNT_FOLLOWING="CountFollowingTopic";
    public static final String TOPIC_COUNT_FANS="CountFansTopic";
    public static final String TOPIC_COUNT_FANS_2_DB="CountFans2DBTopic";
    public static final String TOPIC_COUNT_FOLLOWING_2_DB = "CountFollowing2DBTopic";
    public static final String TOPIC_COUNT_NOTE_LIKE = "CountNoteLikeTopic";
    public static final String TOPIC_COUNT_NOTE_LIKE_2_DB = "CountNoteLike2DBTTopic";
    public static final String TOPIC_COUNT_NOTE_COLLECT = "CountNoteCollectTopic";
    public static final String TOPIC_COUNT_NOTE_COLLECT_2_DB = "CountNoteCollect2DBTTopic";
    //笔记相关服务
    public static final String TOPIC_NOTE_OPERATE = "NoteOperateTopic";//操作笔记
    public static final String TAG_NOTE_PUBLISH = "publishNote";//发布笔记
    public static final String TAG_NOTE_DELETE = "deleteNote";//删除笔记
}