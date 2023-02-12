package cn.nean.boss.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.config.listener.Listener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
@RefreshScope
@Slf4j
public class DynamicThreadPool implements InitializingBean {

    @Value("${core.size}")
    private String coreSize;

    @Value("${max.size}")
    private String maxSize;

    private static ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @Autowired
    private NacosConfigProperties nacosConfigProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 按照 nacos 配置初始化线程池
        threadPoolExecutor = new ThreadPoolExecutor(
                Integer.parseInt(coreSize),
                Integer.parseInt(maxSize),
                10L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10),
                new ThreadFactoryBuilder().setNameFormat("c_t_%d").build(),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        log.error("rejected");
                    }
                });
        //nacos配置变更监听
        nacosConfigManager.getConfigService().addListener(
                "boss-service.yml",
                nacosConfigProperties.getGroup(),
                new Listener() {
                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        //配置变更，修改线程池配置
                        log.info(configInfo);
                        changeThreadPoolConfig(Integer.parseInt(coreSize), Integer.parseInt(maxSize));
                    }
                });
    }

    /*
    *  业务模拟 ---- 续费 Vip
    * */
    public void renewVip(Long userId){
        threadPoolExecutor.execute(()->{
            try {
                TimeUnit.SECONDS.sleep(3);
                log.info(userId + "   续费成功!");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 打印当前线程池的状态
     */
    public String printThreadPoolStatus() {
        return String.format("core_size:%s,thread_current_size:%s;" +
                        "thread_max_size:%s;queue_current_size:%s,total_task_count:%s", threadPoolExecutor.getCorePoolSize(),
                threadPoolExecutor.getActiveCount(), threadPoolExecutor.getMaximumPoolSize(), threadPoolExecutor.getQueue().size(),
                threadPoolExecutor.getTaskCount());
    }

    /**
     * 修改线程池核心参数
     *
     * @param coreSize
     * @param maxSize
     */
    private void changeThreadPoolConfig(int coreSize, int maxSize) {
        threadPoolExecutor.setCorePoolSize(coreSize);
        threadPoolExecutor.setMaximumPoolSize(maxSize);
    }
}
