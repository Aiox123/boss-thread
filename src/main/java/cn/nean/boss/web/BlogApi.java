package cn.nean.boss.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.nean.boss.mapper.BlogMapper;
import cn.nean.boss.model.pojo.Blog;
import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
