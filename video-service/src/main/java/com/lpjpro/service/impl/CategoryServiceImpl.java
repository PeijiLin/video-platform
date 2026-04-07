package com.lpjpro.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.mapper.CategoryMapper;
import com.lpjpro.model.category.entity.Category;
import com.lpjpro.service.CategoryService;
import org.springframework.stereotype.Service;


/**
* @author HL
* @description 针对表【category(视频分类表)】的数据库操作Service实现
* @createDate 2025-03-13 17:15:58
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService {
}




