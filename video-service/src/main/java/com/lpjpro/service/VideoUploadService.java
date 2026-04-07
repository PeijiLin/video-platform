package com.lpjpro.service;

import com.lpjpro.model.*;
import com.lpjpro.model.video.entity.Video;

public interface VideoUploadService {
    InitMultipartResponse initMultipartUpload(InitMultipartRequest request) throws InterruptedException;
    void saveUploadSession(String uploadId, String objectKey,
                      InitMultipartRequest request, Long userId);
    PartSignatureResponse getPartSignature(PartSignatureRequest request);
    CompleteMultipartResponse completeMultipartUpload(CompleteMultipartRequest request, Long userId);
    Video createVideoRecord(String objectKey, UploadSession session, Long userId);
    SubmitVideoInfoResponse submitVideoInfo(SubmitVideoInfoRequest request, Long userId);
}
