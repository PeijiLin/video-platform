package com.lpjpro.controller;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lpjpro.constant.BaseResponse;
import com.lpjpro.model.category.VO.CategoryVO;
import com.lpjpro.model.category.entity.Category;
import com.lpjpro.service.CategoryService;
import com.lpjpro.utils.ResultUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.lpjpro.constant.RedisConstant.VIDEO_CATEGORY;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询所有分类信息
     * @return 分类表
     */
    @GetMapping("/all")
    public BaseResponse<List<CategoryVO>> allCategory() {
        ListOperations<String, Object> ops = redisTemplate.opsForList();
        String key = VIDEO_CATEGORY + "All";
        ObjectMapper objectMapper = new ObjectMapper();

        // 1. 尝试从缓存读取
        List<Object> cached = ops.range(key, 0, -1);
        if (cached != null && !cached.isEmpty()) {
            try {
                List<CategoryVO> result = cached.stream()
                        .map(obj -> {
                            try {
                                // 兼容处理：如果是字符串则反序列化，否则直接转换
                                if (obj instanceof String jsonStr) {
                                    return objectMapper.readValue(jsonStr, CategoryVO.class);
                                }
                                return objectMapper.convertValue(obj, CategoryVO.class);
                            } catch (JsonProcessingException e) {
                                log.warn("反序列化缓存失败: {}", obj, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .toList();
                if (!result.isEmpty()) {
                    return ResultUtils.success(result);
                }
            } catch (Exception e) {
                log.error("读取缓存异常", e);
                // 缓存失败不影响业务，继续查数据库
            }
        }

        // 2. 查询数据库
        List<Category> dbList = categoryService.list();
        List<CategoryVO> categoryVOS = dbList.stream()
                .map(category -> BeanUtil.copyProperties(category, CategoryVO.class))
                .toList();

        // 3. 写入缓存 - 【关键修复】逐个序列化后存入
        if (!categoryVOS.isEmpty()) {
            ops.rightPushAll(key, categoryVOS.stream()
                    .map(vo -> {
                        try {
                            return objectMapper.writeValueAsString(vo);
                        } catch (JsonProcessingException e) {
                            log.error("序列化缓存失败", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toArray());

            // 设置过期时间，避免缓存永不过期
            redisTemplate.expire(key, 24, TimeUnit.HOURS);
        }

        return ResultUtils.success(categoryVOS);
    }
}
