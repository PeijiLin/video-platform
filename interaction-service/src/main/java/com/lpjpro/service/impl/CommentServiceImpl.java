package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.mapper.CommentMapper;
import com.lpjpro.service.CommentService;
import org.springframework.stereotype.Service;
import com.lpjpro.model.comment.entity.Comment;

/**
* @author HL
* @description 针对表【comment(视频评论表)】的数据库操作Service实现
* @createDate 2025-04-26 17:01:46
*/
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
    implements CommentService {
}




