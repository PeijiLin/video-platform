package com.lpjpro.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lpjpro.model.UploadSession;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
* @author HL
* @description 针对表【upload_session(上传会话表)】的数据库操作Mapper
* @createDate 2026-03-16 15:01:16
* @Entity generator.domain.UploadSession
*/
public interface UploadSessionMapper extends BaseMapper<UploadSession> {
    @Select("SELECT * FROM upload_session WHERE file_hash = #{fileHash} AND user_id = #{userId} AND status = 'ACTIVE' AND expire_time > #{now}")
    UploadSession selectByHashAndUser(String fileHash, Long userId, LocalDateTime now);

    @Select("SELECT * FROM upload_session WHERE upload_id = #{uploadId} AND user_id = #{userId}")
    UploadSession selectByUploadIdAndUser(String uploadId, Long userId);
}




