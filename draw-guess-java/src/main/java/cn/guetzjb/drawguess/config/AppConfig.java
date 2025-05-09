package cn.guetzjb.drawguess.config;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class AppConfig {

    @Value("${draw.max-person}")
    private int maxPerson; // 房间一次最大人数
    @Value("${sa-token.token-name}")
    private String tokenName;
    @Value("${draw.seconds}")
    private Integer drawSeconds;
    @Value("${oss.bucket}")
    private String bucket;
    @Value("${oss.domain}")
    private String domain;
    @Value("${oss.region}")
    private String region;
    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${draw.ns}")
    private String namespace;

    public static int MAX_PERSON;
    public static String TOKEN_NAME;
    public static Integer DRAW_SECONDS;
    public static String BUCKET;
    public static String DOMAIN;
    public static String REGION;
    public static String ENDPOINT;
    public static String NAMESPACE;

    @PostConstruct
    public void init() {
        MAX_PERSON = this.maxPerson;
        TOKEN_NAME = this.tokenName;
        DRAW_SECONDS = this.drawSeconds;
        BUCKET = this.bucket;
        DOMAIN = this.domain;
        REGION = this.region;
        ENDPOINT = this.endpoint;
        NAMESPACE = this.namespace;
    }

    @Bean
    public OSS ossClient() throws ClientException {
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        return OSSClientBuilder.create()
                .endpoint(ENDPOINT)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(REGION)
                .build();
    }

}
