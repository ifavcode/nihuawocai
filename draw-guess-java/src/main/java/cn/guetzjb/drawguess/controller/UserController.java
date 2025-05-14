package cn.guetzjb.drawguess.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.drawguess.constant.RedisConstant;
import cn.guetzjb.drawguess.entity.R;
import cn.guetzjb.drawguess.entity.User;
import cn.guetzjb.drawguess.entity.dto.UserDTO;
import cn.guetzjb.drawguess.entity.dto.UserUnDTO;
import cn.guetzjb.drawguess.exception.NotLoginException;
import cn.guetzjb.drawguess.exception.ServiceException;
import cn.guetzjb.drawguess.repository.UserRepository;
import cn.guetzjb.drawguess.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RedisService redisService;

    @Value("${sa-token.timeout}")
    private int timeout;

    @GetMapping("/info")
    public R info(@RequestParam(required = false, defaultValue = "false") Boolean refresh) {
        if (!StpUtil.isLogin()) {
            throw new NotLoginException("未登录");
        }
        long userId = StpUtil.getLoginIdAsLong();
        if (refresh != null && refresh) {
            Optional<User> byId = userRepository.findById(userId);
            if (byId.isEmpty()) {
                return R.ok("查无此人");
            }
            User user = byId.get();
            user.setPassword(null);
            redisService.setCacheObject(RedisConstant.USER + user.getId(), user, (long) timeout, TimeUnit.SECONDS);
            StpUtil.getSession().set("user", user);
            return R.ok(user);
        }
        User user = redisService.getCacheObject(RedisConstant.USER + userId);
        user.setPassword(null);
        return R.ok(user);
    }

    @GetMapping("/username")
    public R username(@RequestParam String username) {
        // 根据用户名查询用户草率的信息
        User user;
        if (StringUtils.isEmpty(username)) {
            user = userRepository.findById(1L).get(); // 保证存在
        } else {
            user = userRepository.findByUsername(username);
        }
        UserUnDTO build = UserUnDTO.builder().id(user.getId()).avatar(user.getAvatar()).nickname(user.getNickname()).username(username).build();
        return R.ok(build);
    }

    @GetMapping("/usernameBatch")
    public R usernameBatch(@RequestParam List<String> usernames) {
        Set<UserUnDTO> set = new HashSet<>();
        for (String username : usernames) {
            User user;
            if (StringUtils.isEmpty(username)) {
                user = userRepository.findById(1L).get();
            } else {
                user = userRepository.findByUsername(username);
            }
            UserUnDTO build = UserUnDTO.builder().id(user.getId()).avatar(user.getAvatar()).nickname(user.getNickname()).username(username).build();
            set.add(build);
        }
        return R.ok(set);
    }
}
