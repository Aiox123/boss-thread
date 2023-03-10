package cn.nean.boss.mapper;

import cn.nean.boss.model.pojo.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogMapper extends BaseMapper<Blog> {

    Blog queryBlogById(@Param("id") Long id);

    int updateCommentsById(@Param("id") Long id);

    int updateCommentsById2(@Param("id") Long id);

    List<Blog> queryBlogs();
}
