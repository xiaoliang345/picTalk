package com.oxn.aiPicturesStore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.mapper.CommentMapper;
import com.oxn.aiPicturesStore.mapper.PostMapper;
import com.oxn.aiPicturesStore.mapper.UserLikeMapper;
import com.oxn.aiPicturesStore.model.entity.Comment;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.entity.UserLike;
import com.oxn.aiPicturesStore.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    @Autowired
    private UserServiceImpl userService;

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final UserLikeMapper userLikeMapper;



    @Transactional
    public Comment addComment(Long userId, Long postId, Long parentId, Long replyToUserId, String content) {
        // 验证帖子存在
        if (postMapper.selectById(postId) == null) throw new RuntimeException("帖子不存在");

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setReplyToUserId(replyToUserId);
        comment.setContent(content);
        commentMapper.insert(comment);
        return comment;
    }

    public void likeComment(Long userId, Long commentId) {
        // 类似 likePost，targetType=2
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        // 检查评论是否存在
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(StatusCode.NOT_FOUND_ERROR, "评论不存在");
        }

        // 检查是否有权限删除（评论创建者或管理员）
        User loginUser = userService.getById(userId);
        if (!comment.getUserId().equals(userId)&& !userService.isAdmin(loginUser)) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR, "无权限删除该评论");
        }

        // 删除评论及其所有子评论
        deleteCommentAndChildren(commentId);
    }

    /**
     * 递归删除评论及其子评论
     * @param commentId 评论ID
     */
    private void deleteCommentAndChildren(Long commentId) {
        // 先删除当前评论的所有子评论
        List<Comment> childComments = commentMapper.selectList(new QueryWrapper<Comment>().eq("parent_id", commentId));
        for (Comment child : childComments) {
            deleteCommentAndChildren(child.getId()); // 递归删除子评论的子评论
        }

        // 删除当前评论的点赞记录
        userLikeMapper.delete(new QueryWrapper<UserLike>().eq("target_id", commentId).eq("target_type", 2));

        // 最后删除当前评论
        commentMapper.deleteById(commentId);
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {

        return null;
    }
}