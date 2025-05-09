package cn.guetzjb.drawguess.service;

import cn.guetzjb.drawguess.constant.RedisConstant;
import cn.guetzjb.drawguess.entity.User;
import cn.guetzjb.drawguess.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final RedisService redisService;
    private final UserRepository userRepository;

    public User getProfile(long userId, boolean refresh) {
        User user = null;
        if (refresh) {
            user = userRepository.findById(userId).orElse(null);
        } else {
            user = redisService.getCacheObject(RedisConstant.USER + userId);
        }
        return user;
    }
}
