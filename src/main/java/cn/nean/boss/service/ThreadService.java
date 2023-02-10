package cn.nean.boss.service;

public interface ThreadService {
    void updateBlogComments(long blogId);

    void sendEmail(String email);
}
