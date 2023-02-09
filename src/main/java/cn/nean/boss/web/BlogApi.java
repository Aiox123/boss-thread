package cn.nean.boss.web;

import cn.nean.boss.utils.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blog")
public class BlogApi {

    @Autowired
    ThreadService threadService;

    @GetMapping("/query/{id}")
    public String queryDetailsBlog(@PathVariable Long id){
        threadService.updateBlogLikes(id);
        return "文章ID" + id;
    }
}
