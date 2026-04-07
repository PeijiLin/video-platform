package com.lpjpro.constant;

public interface RedisConstant {

    String USER_LIKED = "user::liked:";

    String VIDEO_CATEGORY = "video:category:";

    String USER_LOGIN = "user:login:";

    // Key
    String EMAIL = "EMAIL_";
    String EMAIL_REQUEST_VERIFY = "EMAIL_REQUEST_VERIFY_";

    // Category
    String CATEGORY_ALL = "全部";

    // 点赞锁
    String VIDEO_LIKES_LOCK = "lock:video:likes:";

    // 缓存时间
    int EXPIRE_TEN_SECOND = 10;
    int EXPIRE_ONE_MINUTE = 60;
    int EXPIRE_FIVE_MINUTE = 5 * 60;
    int EXPIRE_HALF_HOUR = 30 * 60;
    int EXPIRE_ONE_DAY = 24 * 60 * 60;
}
