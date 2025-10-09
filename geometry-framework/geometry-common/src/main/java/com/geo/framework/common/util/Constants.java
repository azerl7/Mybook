package com.geo.framework.common.util;


import cn.hutool.core.lang.RegexPool;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/*
creator：AZERL7
createTime：11:34
*/
public class Constants{
    //sm3
    public static final String SM3_KEY="this is a sm3 secret key";
    public static final Integer SM3_SALT_LENGTH=8;

    //bcrypt
    public static final Integer PASSWORD_SALT_LENGTH=12;

    //jwt
    public static final String JWT_KEY="this is a jwt secret key";
    public static final Long JWT_EXPIRE=86400000L;

    //sa-token
    public static final String SA_TOKEN_TOKEN_KEY_PREFIX = "Authorization:login:token:";

    //redis
    public static final String VERIFICATION_DATA_KEY="verification:data:";//验证的key,防止用户频繁请求,方便请求就手机号和邮箱用同一个了
    public static final String VERIFICATION_CODE_KEY="verification:code:";//验证码的key,用于获取用户的token是否正确
    public static final Long VERIFICATION_DATA_TTL=60000L;//验证手机邮箱的有效时间，方便重新获取1分钟
    public static final Long VERIFICATION_CODE_TTL=180000L;//验证码的有效时间，方便重新获取3分钟
    public static final String MYBOOK_ID_GENERATOR_KEY="mybook.id.generator";//生成的用户id
    public static final Long  COMMON_USER_ROLE_ID=1L;//普通用户角色id
    public static final String USER_ROLES_KEY = "user:roles:";//用户角色KEY前缀
    public static final String ROLE_PERMISSIONS_KEY = "role:permissions:";
    public static final String PUSH_PERMISSION_FLAG = "push.permission.flag";    // 权限同步标记 Key，分布式锁
    public static final String USER_INFO_KEY_PREFIX = "user:info:";//用户信息前缀
    public static final Long USER_INFO_EXPIRE=86400L;//用户信息缓存时间
    public static final Long NULL_EXPIRE=60L;//空值缓存时间
    public static final String NOTE_DETAIL_KEY = "note:detail:";
    public static final String USER_FOLLOWING_KEY_PREFIX="following:";
    public static final Long RELATION_EXPIRE_SECONDS=86400L;//ZADD缓存时间
    public static final String USER_FANS_KEY_PREFIX="fans:";
    public static final String COUNT_USER_KEY_PREFIX = "count:user:";
    public static final String FIELD_FANS_TOTAL = "fansTotal";
    public static final String FIELD_FOLLOWING_TOTAL = "followingTotal";
    public static final String BLOOM_USER_NOTE_LIKE_LIST_KEY = "bloom:note:likes:";
    public static final Long BLOOM_EXPIRE_SECONDS=86400L;//布隆过滤器缓存时间
    public static final String USER_NOTE_LIKE_ZSET_KEY = "user:note:likes:";//用于验证用户点赞的zset
    public static final Long USER_NOTE_LIKE_ZSET_EXPIRE=86400L;
    public static final String COUNT_NOTE_KEY_PREFIX = "count:note:";//分别有三个下属的家伙
    public static final String FIELD_LIKE_TOTAL = "likeTotal";
    public static final String BLOOM_USER_NOTE_COLLECT_LIST_KEY = "bloom:note:collects:";
    public static final Long BLOOM_USER_NOTE_COLLECT_LIST_EXPIRE=86400L;
    public static final String USER_NOTE_COLLECT_ZSET_KEY = "user:note:collects:";
    public static final Long USER_NOTE_COLLECT_ZSET_EXPIRE = 86400L;
    public static final String FIELD_COLLECT_TOTAL = "collectTotal";
    public static final String FIELD_NOTE_TOTAL = "noteTotal";
    public static final String BLOOM_TODAY_USER_NOTE_OPERATOR_LIST_KEY = "bloom:dataAlign:user:note:operators:";//布隆过滤器：日增变更数据，笔记发布数目数前缀
    public static final String BLOOM_TODAY_USER_FOLLOW_LIST_KEY = "bloom:dataAlign:user:follows:";//布隆过滤器：日增变更数据，用户关注数前缀
    public static final String BLOOM_TODAY_USER_FANS_LIST_KEY = "bloom:dataAlign:user:fans:";//布隆过滤器：日增变更数据，用户粉丝数前缀
    public static final String BLOOM_TODAY_NOTE_LIKE_NOTE_ID_LIST_KEY = "bloom:dataAlign:note:like:noteIds:";//布隆过滤器：日增变量变更数据，用户笔记点赞，取消点赞（笔记id）前缀
    public static final String BLOOM_TODAY_NOTE_LIKE_USER_ID_LIST_KEY = "bloom:dataAlign:note:like:userIds:";//布隆过滤器:日增用户变更量，用户笔记点赞，取消点赞（笔记发布者id）前缀
    public static final String BLOOM_TODAY_NOTE_COLLECT_NOTE_ID_LIST_KEY="bloom:dataAlign:note:collect:noteIds:";//布隆过滤器：日增变更数据，笔记收藏数，笔记id，前缀
    public static final String BLOOM_TODAY_NOTE_COLLECT_USER_ID_LIST_KEY="bloom:dataAlign:note:collect:userIds:";//布隆过滤器：日增变更数据，笔记收藏数前，用户id，缀
    public static final String CHANNEL_LIST_KEY = "channels:";//频道列表
    public static final String CHANNEL_NOTE_LIST_KEY_PREFIX = "channels:";

    //caffeine
    public static final Integer CAFFEINE_INIT_CAPACITY=8192;//caffeine默认条目
    public static final Integer CAFFEINE_MAX_CAPACITY=8192;
    public static final Integer CAFFEINE_EXPIRE=60;


    //email
    public static final String EMAIL_SMTP_HOST="smtp.163.com";
    public static final String EMAIL_FROM_EMAIL="13883154307@163.com";
    public static final String EMAIL_AUTH_CODE="SDV9Y969JkNz6MWd";

    //date
    public static final DateTimeFormatter DATE_LOCAL_DATE_TIME= java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DATE_LOCAL_DATE=DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_LOCAL_TIME=DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATE_YEAR_MONTH=DateTimeFormatter.ofPattern("yyyy-MM");

    //regex
    public static final String REGEX_EMAIL= RegexPool.EMAIL;
    public static final String REGEX_CHINA_PHONE="/^1[3456789]\\d{9}$/";

    //request
    public static final String USER_ID="userId";

    //oss
    public static final String MINIO_BUCKET_NAME="mybook";
    public static final String ALIYUN_BUCKET_NAME="azerl7-mybook";

    //service服务相关
    public static final String BIZ_TAG_MYBOOK_ID = "leaf-segment-mybook-id";
    public static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";

    //web
    public static final String TOKEN_HEADER_KEY = "Authorization";//header中的token存放键
    public static final String TOKEN_HEADER_VALUE_PREFIX = "Bearer ";//token前缀

    //date

    /**
     * DateTimeFormatter：年-月-日-时-分-秒
     */
    public static final DateTimeFormatter DATE_FORMAT_Y_M_D_H_M_S=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * DateTimeFormatter：年-月-日
     */
    public static final DateTimeFormatter DATE_FORMAT_Y_M_D=DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * DateTimeFormatter：月-日
     */
    public static final DateTimeFormatter DATE_FORMAT_M_D = DateTimeFormatter.ofPattern("MM-dd");

    /**
     * DateTimeFormatter：时：分
     */
    public static final DateTimeFormatter DATE_FORMAT_H_M = DateTimeFormatter.ofPattern("HH:mm");
}