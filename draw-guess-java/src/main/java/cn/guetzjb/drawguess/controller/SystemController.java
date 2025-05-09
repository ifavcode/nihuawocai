package cn.guetzjb.drawguess.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.guetzjb.drawguess.constant.RedisConstant;
import cn.guetzjb.drawguess.entity.R;
import cn.guetzjb.drawguess.entity.User;
import cn.guetzjb.drawguess.entity.dto.UserLoginDTO;
import cn.guetzjb.drawguess.exception.ServiceException;
import cn.guetzjb.drawguess.repository.UserRepository;
import cn.guetzjb.drawguess.service.RedisService;
import cn.guetzjb.drawguess.utils.BaseUtils;
import com.alibaba.druid.util.StringUtils;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
public class SystemController {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final Faker faker;

    @Value("${sa-token.timeout}")
    private int timeout;

    @PostMapping("/login")
    public R login(@RequestBody UserLoginDTO userLoginDTO) {
        User user = userRepository.findByUsername(userLoginDTO.getUsername());
        if (user == null) {
            throw new ServiceException("账户不存在");
        }
        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            throw new ServiceException("账户或者密码错误");
        }
        StpUtil.login(user.getId());
        StpUtil.getSession().set("user", user);
        redisService.setCacheObject(RedisConstant.USER + user.getId(), user, (long) timeout, TimeUnit.SECONDS);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return R.ok(tokenInfo);
    }

    @PostMapping("/register")
    public R register(@RequestBody User user) {
        Long count = userRepository.countByUsername(user.getUsername());
        if (count > 0) {
            throw new ServiceException("用户名已经被注册");
        }
        user.setCreateTime(new Date());
        if (StringUtils.isEmpty(user.getNickname())) {
            user.setNickname(faker.funnyName().name());
        }
        if (StringUtils.isEmpty(user.getAvatar())) {
            user.setAvatar(BaseUtils.generateRandomAvatar(user.getUsername()));
        }
        userRepository.save(user);
        return R.ok("注册成功");
    }
}
