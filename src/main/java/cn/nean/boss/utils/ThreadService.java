package cn.nean.boss.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ThreadService {

    @Async("taskExecutor")
    public void updateBlogLikes(Long id) {
        try {
            TimeUnit.SECONDS.sleep(3);
            log.info("文章ID: {} 点赞数量更新完毕!",id);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
