package com.lpjpro.utils;

public interface UploadProgressListener {
    void onProgress(long uploadedBytes, long totalBytes, double percentage);
    void onPartCompleted(int partNumber, long partSize);
    void onCompleted(String etag);
    void onError(String errorMessage);
}