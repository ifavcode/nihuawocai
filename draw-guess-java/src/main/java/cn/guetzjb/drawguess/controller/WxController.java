package cn.guetzjb.drawguess.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.guetzjb.drawguess.constant.RedisConstant;
import cn.guetzjb.drawguess.entity.R;
import cn.guetzjb.drawguess.entity.User;
import cn.guetzjb.drawguess.exception.ServiceException;
import cn.guetzjb.drawguess.repository.UserRepository;
import cn.guetzjb.drawguess.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.common.bean.oauth2.WxOAuth2AccessToken;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RequestMapping("/wx")
@RequiredArgsConstructor
@RestController
@Slf4j
public class WxController {

    private static final String URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";

    private final WxMpService wxService;
    private final RedisService redisService;
    private final UserRepository userRepository;

    @Value("${wx.mp.callback}")
    private String callback;

    @Value("${sa-token.timeout}")
    private int timeout;

    @GetMapping(value = "/check", produces = "text/plain;charset=utf-8")
    public String authGet(@RequestParam(name = "signature", required = false) String signature,
                          @RequestParam(name = "timestamp", required = false) String timestamp,
                          @RequestParam(name = "nonce", required = false) String nonce,
                          @RequestParam(name = "echostr", required = false) String echostr) {

        log.info("\n接收到来自微信服务器的认证消息：[{}, {}, {}, {}]", signature,
                timestamp, nonce, echostr);
        if (StringUtils.isAnyBlank(signature, timestamp, nonce, echostr)) {
            throw new IllegalArgumentException("请求参数非法，请核实!");
        }

        if (wxService.checkSignature(timestamp, nonce, signature)) {
            return echostr;
        }

        return "非法请求";
    }

    @GetMapping("/authorize")
    public String authorize() {
        return String.format(URL, wxService.getWxMpConfigStorage().getAppId(), URLEncoder.encode(callback, Charset.defaultCharset()));
    }


    /**
     *
     * @param code
     * @return token
     * 根据code换取微信用户信息，自动注册&登录
     */
    @GetMapping("/callback")
    public R callBack(@RequestParam String code) {
        try {
            WxOAuth2AccessToken accessToken = wxService.getOAuth2Service().getAccessToken(code);
            WxOAuth2UserInfo userInfo = wxService.getOAuth2Service().getUserInfo(accessToken, "zh-CN");
            User user = userRepository.findByOpenid(userInfo.getOpenid());
            if (user == null) {
                user = new User();
                user.setUsername(userInfo.getNickname());
                user.setNickname(userInfo.getNickname());
                user.setOpenid(userInfo.getOpenid());
                user.setPassword("123456");
                user.setAvatar(userInfo.getHeadImgUrl());
                user.setCreateTime(new Date());
                userRepository.save(user);
            }
            StpUtil.login(user.getId());
            StpUtil.getSession().set("user", user);
            redisService.setCacheObject(RedisConstant.USER + user.getId(), user, (long) timeout, TimeUnit.SECONDS);
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            return R.ok(tokenInfo);
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
