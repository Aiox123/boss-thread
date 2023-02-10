package cn.nean.boss.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.nean.boss.mapper.BlogMapper;
import cn.nean.boss.model.common.RestResponse;
import cn.nean.boss.model.pojo.Blog;
import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.sleuth.instrument.async.LazyTraceThreadPoolTaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogApi {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ThreadService threadService;

    @Autowired
    BlogMapper blogMapper;

    private final LazyTraceThreadPoolTaskExecutor cacheMapExecutor;

    @Autowired
    public BlogApi(@Qualifier("cacheMapExecutor") LazyTraceThreadPoolTaskExecutor cacheMapExecutor){
        this.cacheMapExecutor = cacheMapExecutor;
    }

    // 本地缓存
    private static final Map<Long,Blog> BLOG_MAP = new ConcurrentHashMap<>();

    private static long lastSync = 0l;

    // 失效时间 30分钟
    private static final int EXPIRE = 30 * 60 * 1000;


    @GetMapping("/select/{blogId}")
    public RestResponse<Blog> selectBlog(@PathVariable Long blogId){
        if(blogId == null){
            return RestResponse.validFail("请求参数 blogId 不存在");
        }
        // 30分钟内，直接从缓存中获取否则查询数据库
        if(System.currentTimeMillis() - lastSync <= EXPIRE){
            return RestResponse.success(BLOG_MAP.get(blogId));
        }
        // 异步查询数据库
        cacheMapExecutor.execute(()->{
            synchronized (this){
                // 双重检查
                if(System.currentTimeMillis() - lastSync > EXPIRE){
                    long start = System.currentTimeMillis();
                    log.info("get blog begin: " + BLOG_MAP.size());
                    try{
                        lastSync = System.currentTimeMillis();
                        // 查询数据库
                        List<Blog> blogs = blogMapper.queryBlogs();
                        for (Blog blog : blogs) {
                            // 更新缓存
                            BLOG_MAP.put(blog.getId(),blog);
                        }
                    }catch (Exception e){
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                    log.info("get blog end cost: " + (System.currentTimeMillis() - start) + " size: " + BLOG_MAP.size());
                }
            }
        });
        return RestResponse.success(BLOG_MAP.get(blogId));
    }

    @GetMapping("/query")
    public Blog queryDetailsBlog(){
        long blogId = 1;
        threadService.updateBlogComments(blogId);
        String key = "blog:" + blogId;
        // 线程 Redis 缓存
        String blogJson = stringRedisTemplate.opsForValue().get(key);
        if(StrUtil.isNotBlank(blogJson)){
            Blog blog = JSONUtil.toBean(blogJson, Blog.class);
            log.info("缓存博客详情存在: {}",blog);
            return blog;
        }
        Blog blog = blogMapper.queryBlogById(blogId);
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(blog));
        log.info("博客详情: {}",blog);
        return blog;
    }



}
