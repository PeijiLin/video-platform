package com.lpjpro.constant;

public class RedisKeys {
    public static final String VIDEO_HASH = "video:hash:%s"; // fileHash
    public static final String UPLOAD_HASH_INDEX = "upload:hash:%s:%s"; // fileHash:userId
    public static final String UPLOAD_SESSION = "upload:session:%s"; // uploadId
    public static final String UPLOAD_PARTS = "upload:parts:%s"; // uploadId
    public static final String LOCK_UPLOAD = "lock:upload:%s:%s"; // fileHash:userId
    
    public static final long SESSION_EXPIRE_SECONDS = 24 * 60 * 60; // 24 小时
}