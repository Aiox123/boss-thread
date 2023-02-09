package cn.nean.boss.service.impl;

import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ThreadServiceImpl implements ThreadService {

    @Override
    @Async("taskExecutor")
    public void updateBlogComments(long blogId) {
        try {
            TimeUnit.SECONDS.sleep(3);
            log.info("文章ID: {} 点赞数量更新完毕!",blogId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
