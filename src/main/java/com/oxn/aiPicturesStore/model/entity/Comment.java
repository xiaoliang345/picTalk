package com.oxn.aiPicturesStore.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField("post_id")
    private Long postId;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("parent_id")
    private Long parentId = 0L; // 0 表示根评论
    
    @TableField("reply_to_user_id")
    private Long replyToUserId;
    
    private String content;
    
    @TableField("create_time")
    private LocalDateTime createTime;
    
    @TableField("like_count")
    private Integer likeCount = 0;

    @TableField(exist = false)
    private String username; // 评论人用户名（用于展示）

    @TableField(exist = false)
    private String replyToUsername; // 被回复人用户名

    @TableField(exist = false)
    private List<Comment> children = new ArrayList<>(); // 子评论（递归）
}