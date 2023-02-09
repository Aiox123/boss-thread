package cn.nean.boss.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.nean.boss.mapper.BlogMapper;
import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ThreadServiceImpl implements ThreadService {

    @Autowired
    BlogMapper blogMapper;

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    /*
     *  利用 Redis String类型存储 blog的comments
     *   利用自增指令实现 comments 增加
     *   每增加 100 同步一次数据库
     * */
    @Override
    @Async("taskExecutor")
    public void updateBlogComments(long blogId) {
        String key = "blog:comments:" + blogId;
        String bcJson = stringRedisTemplate.opsForValue().get(key);
        // 判断是否已经缓存
        if(StrUtil.isNotBlank(bcJson)){
            // 存在 判断 value是否达到 99
            if("99".equals(bcJson)){
                // 达到 100 更新数据库
                int i = blogMapper.updateCommentsById2(blogId);
                if(i > 0){
                    // 更新 redis 缓存的 value值
                    String comments = "1";
                    stringRedisTemplate.opsForValue().set(key,comments);
                    log.info("文章ID: {} 浏览数量增加100同步到mysql!",blogId);
                }
            }else {
                // 未达到 99 自增
                stringRedisTemplate.opsForValue().increment(key);
                log.info("文章ID: {} 浏览数量+1!",blogId);
            }
        }else {
            // 未对 blog 的 comments缓存 创建缓存
            String comments = "1";
            stringRedisTemplate.opsForValue().set(key,comments);
            log.info("文章ID: {} 浏览数量缓存初始化完毕!",blogId);
        }
    }
}
