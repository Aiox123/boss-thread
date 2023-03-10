package cn.nean.boss.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.nean.boss.mapper.BlogMapper;
import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


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
        //redisInc(blogId);
        int i = blogMapper.updateCommentsById(blogId);
        if(i > 0){
            log.info("文章ID: {} 浏览数量+1!", blogId);
        }
    }

    @Override
    @Async("emailExecutor")
    public void sendEmail(String email) {
        // 生成验证码
        int code = RandomUtil.randomInt(100000,999999);
        try {
            TimeUnit.SECONDS.sleep(2);
            // 发送验证码到邮箱
            log.info("验证码: {}",code);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // 将验证码存入Redis
    }

    /*
    *  存在线程安全风险
    *  目前解决方案 修改线程池核心线程数以及最大线程数为1
    * */
    private void redisInc(long blogId) {
        String key = "blog:comments:" + blogId;
        String bcJson = stringRedisTemplate.opsForValue().get(key);
        // 判断是否已经缓存
        if(StrUtil.isNotBlank(bcJson)){
            // 存在 判断 value是否达到 99
            if("9".equals(bcJson)){
                // 达到 10 更新数据库
                int i = blogMapper.updateCommentsById2(blogId);
                if(i > 0){
                    // 更新 redis 缓存的 value值
                    String comments = "1";
                    stringRedisTemplate.opsForValue().set(key,comments);
                    log.info("文章ID: {} 浏览数量增加10同步到mysql!", blogId);
                }
            }else {
                // 未达到 99 自增
                stringRedisTemplate.opsForValue().increment(key);
                log.info("文章ID: {} 浏览数量+1!", blogId);
            }
        }else {
            // 未对 blog 的 comments缓存 创建缓存
            String comments = "1";
            stringRedisTemplate.opsForValue().set(key,comments);
            log.info("文章ID: {} 浏览数量缓存初始化完毕!", blogId);
        }
    }
}
