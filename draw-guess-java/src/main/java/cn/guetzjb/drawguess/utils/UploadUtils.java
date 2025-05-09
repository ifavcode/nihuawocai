package cn.guetzjb.drawguess.utils;

import cn.guetzjb.drawguess.config.AppConfig;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadUtils {

    private final OSS ossClient;

    private String createDate() {
        LocalDate now = LocalDate.now();
        return now.getYear() + "-" + now.getMonthValue() + "-" + now.getDayOfMonth();
    }

    public String upload(MultipartFile file) throws IOException {
        String date = createDate();
        String fileName = date + "/" + file.getOriginalFilename();
        ossClient.putObject(AppConfig.BUCKET, fileName, file.getInputStream());
        return AppConfig.DOMAIN + fileName;
    }

    public String upload(String base64) throws IOException {
        String[] parts = base64.split(",");
        String imageData = parts.length > 1 ? parts[1] : parts[0];
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        String date = createDate();
        String fileName = date + "/" + UUID.randomUUID() + ".png";
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setObjectAcl(CannedAccessControlList.PublicRead);
        ossClient.putObject(AppConfig.BUCKET, fileName, new ByteArrayInputStream(imageBytes), metadata);
        return AppConfig.DOMAIN + fileName;
    }
}
