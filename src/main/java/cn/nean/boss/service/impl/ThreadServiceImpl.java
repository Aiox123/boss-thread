package cn.nean.boss.service.impl;

import cn.nean.boss.mapper.BlogMapper;
import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ThreadServiceImpl implements ThreadService {

    @Autowired
    BlogMapper blogMapper;

    @Override
    @Async("taskExecutor")
    public void updateBlogComments(long blogId) {
        int i = blogMapper.updateCommentsById(blogId);
        if(i > 0){
            log.info("文章ID: {} 点赞数量更新完毕!",blogId);
        }
    }
}
