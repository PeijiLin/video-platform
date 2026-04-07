package com.lpjpro.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lpjpro.exception.BusinessException;
import com.lpjpro.exception.ErrorCode;
import com.lpjpro.exception.ThrowsUtils;
import com.lpjpro.mapper.UserMapper;
import com.lpjpro.model.user.DTO.UserLoginRequest;
import com.lpjpro.model.user.DTO.UserRegisterRequest;
import com.lpjpro.model.user.VO.UserVO;
import com.lpjpro.model.user.entity.User;
import com.lpjpro.service.UserService;
import com.lpjpro.utils.CommonHandle;
import com.lpjpro.utils.JwtUtils;
import com.lpjpro.utils.StringUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.lpjpro.constant.RedisConstant.EMAIL;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Value("${jwt.sign-key:hzy123}")
    private String signKey;

    @Value("${jwt.access-token-expire:900000")
    private Long accessTokenExpire;

    @Value("${jwt.refresh-token-expire:604800000}")
    private Long refreshTokenExpire;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserMapper userMapper;

    @Transactional
    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        if (StringUtils.isAnyBlank(userRegisterRequest.getAccount(),userRegisterRequest.getPassword(),userRegisterRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码不能为空");
        }

        if (!Objects.equals(userRegisterRequest.getCheckPassword(), userRegisterRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码与验证密码不一致");
        }

        String email = userRegisterRequest.getEmail();
        ThrowsUtils.throwIf(!StringUtil.isValidEmail(email), ErrorCode.PARAMS_ERROR, "邮箱格式有误");

        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String code = (String) valueOperations.get(EMAIL + email);
        ThrowsUtils.throwIf(!StringUtils.equals(code, userRegisterRequest.getCode()), ErrorCode.ABNORMAL_VERIFICATION_CODE);

        String account = userRegisterRequest.getAccount();
        String password = userRegisterRequest.getPassword();
        verify(account,password);

        String safePassword = passwordEncoder.encode(password);

        User user = new User();
        user.setAccount(account);
        user.setPassword(safePassword);

        this.save(user);
        return user.getId();
    }

    @Override
    public Map<String, Object> userLogin(UserLoginRequest userLoginRequest) {
        if (StringUtils.isAnyBlank(userLoginRequest.getAccount(),userLoginRequest.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号或密码不能为空");
        }

        String account = userLoginRequest.getAccount();
        String password = userLoginRequest.getPassword();
        verify(account,password);

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getAccount, account);
        User user = this.getOne(userLambdaQueryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }

        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.set("last_login_time", LocalDateTime.now());
        this.update(userUpdateWrapper);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String accessToken = JwtUtils.generateAccessToken(claims, signKey, accessTokenExpire);

        String refreshToken = UUID.randomUUID().toString();
        String refreshTokenKey = "refresh_token:" + refreshToken;
        String userRefreshTokensKey = "user:refresh_tokens:" + user.getId();

        redisTemplate.opsForValue().set(refreshTokenKey, user.getId(), 7, TimeUnit.DAYS);
        redisTemplate.opsForSet().add(userRefreshTokensKey, refreshToken);
        redisTemplate.expire(userRefreshTokensKey, 7, TimeUnit.DAYS);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("access_token",accessToken);
        userInfo.put("userId", user.getId());
        userInfo.put("refresh_token", refreshTokenKey);
        return userInfo;
    }

    @Override
    public List<UserVO> batchSelect(List<Long> ids) {
        return userMapper.batchSelectIds(ids);
    }

    @Override
    public Map<String, String> refreshToken(Map<String, String> payload) {
        String oldRefreshToken = payload.get("refresh_token");

        String refreshTokenKey = "refresh_token:" + oldRefreshToken;
        Object userId = redisTemplate.opsForValue().get(refreshTokenKey);
        ThrowsUtils.throwIf(CommonHandle.isNull(userId), ErrorCode.NOT_LOGIN_ERROR);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        String newAccessToken = JwtUtils.generateAccessToken(claims, signKey, accessTokenExpire);

        String newRefreshToken = UUID.randomUUID().toString();
        String newRefreshTokenKey = "refresh_token:" + newRefreshToken;
        String userRefreshTokensKey = "user:refresh_tokens:" + userId;

        redisTemplate.opsForValue().set(newRefreshTokenKey, userId, 7, TimeUnit.DAYS);
        redisTemplate.opsForSet().add(userRefreshTokensKey, newRefreshToken);
        redisTemplate.expire(userRefreshTokensKey, 7, TimeUnit.DAYS);

        redisTemplate.delete(refreshTokenKey);
        HashMap<String, String> map = new HashMap<>();
        map.put("access_token", newAccessToken);
        map.put("refresh_token", newRefreshTokenKey);
        return map;
    }

    @Override
    public void logout(String authHeader) {
        String accessToken = authHeader.substring(7);
        String userId = JwtUtils.getUserIdFromToken(accessToken, signKey);

        String userRefreshTokensKey = "user:refresh_tokens:" + userId;
        Set<Object> tokens = redisTemplate.opsForSet().members(userRefreshTokensKey);

        if (tokens != null && !tokens.isEmpty()) {
            for (Object token : tokens) {
                redisTemplate.delete("refresh_token:" + token);
            }
        }

        redisTemplate.delete(userRefreshTokensKey);

        String blacklistKey = "blacklist:access_token:" + accessToken;
        redisTemplate.opsForValue().set(blacklistKey, "revoked", 30, TimeUnit.MINUTES);
    }

    private static UserVO getSafeUser(User user) {
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user,userVO);
        return userVO;
    }

    private static void verify(String account,String password) {
        if (StringUtils.length(account) < 4 || StringUtils.length(account) > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号长度需在4-20个字符之间");
        }

        if (StringUtils.length(password) < 8 || StringUtils.length(password) > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度需在8-20个字符之间");
        }
    }
}