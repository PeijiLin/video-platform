package com.lpjpro.api.interaction;

import com.lpjpro.api.interaction.fallback.InteractionApiFallback;
import com.lpjpro.constant.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "interaction-service", name = "interaction-service", fallback = InteractionApiFallback.class)
public interface InteractionApi {

    @GetMapping("/video/favorites/getFavoritesList")
    BaseResponse<List<Boolean>> batchFavorites(@RequestParam("videoIds") List<Long> videoIds,
                                                @RequestParam("userId") Long userId);

    @GetMapping("/video/likes/getLikesList")
    BaseResponse<List<Boolean>> batchLikes(@RequestParam("videoIds") List<Long> videoIds,
                                            @RequestParam("userId") Long userId);
}