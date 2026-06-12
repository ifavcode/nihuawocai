package cn.guetzjb.drawguess.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置。
 * <p>
 * broadcastExecutor 专门用于 WebSocket 消息广播（如画笔数据），
 * 将 JSON 序列化等 CPU 密集操作从 Netty 事件循环线程剥离，
 * 避免阻塞 I/O 线程导致消息堆积和卡顿。
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "broadcastExecutor")
    public Executor broadcastExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 序列化是 CPU 密集型，核心线程绑 CPU 核数
        executor.setCorePoolSize(cpuCores);
        executor.setMaxPoolSize(cpuCores * 2);
        // 队列缓冲瞬时流量尖峰
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("broadcast-");
        // 队列满时回退到调用线程执行，不丢消息
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
