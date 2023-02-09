package cn.nean.boss.web;

import cn.nean.boss.mapper.BlogMapper;
import cn.nean.boss.model.pojo.Blog;
import cn.nean.boss.service.ThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/blog")
public class BlogApi {

    @Autowired
    ThreadService threadService;

    @Autowired
    BlogMapper blogMapper;

    @GetMapping("/query")
    public Blog queryDetailsBlog(){
        long blogId = 1;
        threadService.updateBlogComments(blogId);
        Blog blog = blogMapper.queryBlogById(blogId);
        log.info("博客详情: {}",blog);
        return blog;
    }
}
