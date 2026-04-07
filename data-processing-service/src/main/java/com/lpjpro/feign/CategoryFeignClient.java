package com.lpjpro.feign;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.feign.fallback.CategoryFeignClientFallback;
import com.lpjpro.model.category.VO.CategoryVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(value = "video-service", fallback = CategoryFeignClientFallback.class)
public interface CategoryFeignClient {
    @GetMapping("/all")
    BaseResponse<List<CategoryVO>> allCategory();
}
