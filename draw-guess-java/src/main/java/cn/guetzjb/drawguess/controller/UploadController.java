package cn.guetzjb.drawguess.controller;

import cn.guetzjb.drawguess.entity.R;
import cn.guetzjb.drawguess.exception.ServiceException;
import cn.guetzjb.drawguess.utils.UploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final UploadUtils uploadUtils;

    public record Base64Img(String url) {
    }

    @PostMapping("/base64")
    public R uploadImage(@RequestBody Base64Img base64) {
        try {
            String upload = uploadUtils.upload(base64.url());
            return R.ok(upload);
        } catch (IOException e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
