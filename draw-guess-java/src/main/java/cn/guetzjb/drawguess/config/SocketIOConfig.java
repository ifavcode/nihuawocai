package cn.guetzjb.drawguess.config;

import cn.hutool.core.io.resource.ClassPathResource;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DefaultExceptionListener;
import com.corundumstudio.socketio.protocol.JacksonJsonSupport;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.io.InputStream;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {

    @Value("${sa-token.token-name}")
    private String tokenName;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setPort(9000);
        config.setHostname("0.0.0.0");
//        config.setAllowHeaders(tokenName);
        config.setJsonSupport(new JacksonJsonSupport());
        config.setExceptionListener(new DefaultExceptionListener());
        // 开启压缩，减少画画数据传输量
        config.setHttpCompression(true);
        config.setWebsocketCompression(true);
        // 调整 Netty 线程池，提升并发处理能力
        int cpuCores = Runtime.getRuntime().availableProcessors();
        config.setBossThreads(1);
        config.setWorkerThreads(cpuCores * 2);
//        config.setKeyStore(getCertJks());
//        config.setKeyStorePassword("zfs2k32p");
        return new SocketIOServer(config);
    }

    private InputStream getCertJks() {
        ClassPathResource classPathResource = new ClassPathResource("guetzjb.cn.jks");
        return classPathResource.getStream();
    }

    @Bean
    public Faker faker() {
        return new Faker();
    }
}
