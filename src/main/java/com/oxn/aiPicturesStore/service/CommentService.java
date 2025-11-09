package com.oxn.aiPicturesStore.service;

import com.oxn.aiPicturesStore.model.entity.Comment;
import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 添加评论
     *
     * @param userId         用户ID
     * @param postId         帖子ID
     * @param parentId       父评论ID，0表示根评论
     * @param replyToUserId  被回复用户ID
     * @param content        评论内容
     * @return 创建的评论
     */
    Comment addComment(Long userId, Long postId, Long parentId, Long replyToUserId, String content);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 获取帖子的所有评论
     *
     * @param postId 帖子ID
     * @return 评论列表
     */
    List<Comment> getCommentsByPostId(Long postId);

    /**
     * 点赞评论
     *
     * @param userId    用户ID
     * @param commentId 评论ID
     */
    void likeComment(Long userId, Long commentId);
}