package com.lpjpro.api.video.fallback;

import com.lpjpro.constant.BaseResponse;
import com.lpjpro.api.video.CategoryFeignClient;
import com.lpjpro.model.category.VO.CategoryVO;
import com.lpjpro.utils.ResultUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CategoryFeignClientFallback implements CategoryFeignClient {
    @Override
    public BaseResponse<List<CategoryVO>> allCategory() {
        return ResultUtils.success(new ArrayList<>());
    }
}
