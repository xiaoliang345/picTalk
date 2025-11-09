package com.oxn.aiPicturesStore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oxn.aiPicturesStore.common.PageRequest;
import com.oxn.aiPicturesStore.mapper.CommentMapper;
import com.oxn.aiPicturesStore.mapper.PostImageMapper;
import com.oxn.aiPicturesStore.mapper.PostMapper;
import com.oxn.aiPicturesStore.mapper.UserLikeMapper;
import com.oxn.aiPicturesStore.mapper.UserMapper;
import com.oxn.aiPicturesStore.model.entity.Comment;
import com.oxn.aiPicturesStore.model.entity.Post;
import com.oxn.aiPicturesStore.model.entity.PostImage;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.entity.UserLike;
import com.oxn.aiPicturesStore.model.vo.CommentVO;
import com.oxn.aiPicturesStore.model.vo.PostVO;
import com.oxn.aiPicturesStore.model.vo.UserVO;
import com.oxn.aiPicturesStore.service.PostService;
import com.oxn.aiPicturesStore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post>  implements PostService  {

    @Autowired
    private UserService userService;

    private final PostMapper postMapper;
    private final PostImageMapper postImageMapper;
    private final CommentMapper commentMapper;
    private final UserLikeMapper userLikeMapper;
    private final UserMapper userMapper;

    @Transactional
    public PostVO createPost(Long userId, String title, String content, List<Map<String, String>> imageUrls) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        postMapper.insert(post);

        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<PostImage> images = imageUrls.stream()
                    .map(map -> {
                        PostImage img = new PostImage();
                        img.setPostId(post.getId());
                        img.setImageUrl(map.get("url"));
                        img.setThumbnailUrl(map.get("thumbnailUrl"));
                        return img;
                    })
                    .collect(Collectors.toList());
            images.forEach(img -> postImageMapper.insert(img));
        }
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setImageUrls(imageUrls);
        return postVO;
    }

    @Transactional
    public PostVO updatePost(Long userId, Long postId, String title, String content, List<Map<String, String>> imageUrls) {
        // 检查帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查是否是帖子作者（只有作者可以编辑自己的帖子）
        User loginUser= userService.getById(userId);
        if (!post.getUserId().equals(userId)&&!userService.isAdmin(loginUser)) {
            throw new RuntimeException("没有权限编辑该帖子");
        }

        // 更新帖子内容
        post.setTitle(title);
        post.setContent(content);
        post.setUpdateTime(LocalDateTime.now());
        postMapper.updateById(post);

        // 如果提供了图片，则更新图片
        if (imageUrls != null) {
            // 删除原有的图片记录
            postImageMapper.delete(new QueryWrapper<PostImage>().eq("post_id", postId));

            // 插入新的图片记录
            if (!imageUrls.isEmpty()) {
                List<PostImage> images = imageUrls.stream()
                        .map(map -> {
                            PostImage img = new PostImage();
                            img.setPostId(postId);
                            img.setImageUrl(map.get("url"));
                            img.setThumbnailUrl(map.get("thumbnailUrl"));
                            return img;
                        })
                        .collect(Collectors.toList());
                images.forEach(img -> postImageMapper.insert(img));
            }
        }

        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        postVO.setImageUrls(imageUrls);
        return postVO;
    }

    public PostVO getPostWithComments(Long postId, Long currentUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) throw new RuntimeException("帖子不存在");


        // 查询发帖用户信息
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        setUserPostInfo(List.of(postVO));

        // 查询图片
        List<Map<String, String>> imageUrls = postImageMapper.selectList(
                new QueryWrapper<PostImage>().eq("post_id", postId).orderByAsc("sort")
        ).stream().map(postImage -> {
            Map<String, String> imgMap = new HashMap<>();
            imgMap.put("url", postImage.getImageUrl());
            imgMap.put("thumbnailUrl", postImage.getThumbnailUrl());
            return imgMap;
        }).collect(Collectors.toList());

        postVO.setImageUrls(imageUrls);

        // 查询一级评论（parentId = 0）
        List<Comment> rootComments = commentMapper.selectList(
                new QueryWrapper<Comment>().eq("post_id", postId).eq("parent_id", 0)
                        .orderBy(true, false, "create_time")
        );

        // 递归构建树（简单版，大数据量建议用 CTE 或前端处理）
        List<CommentVO> fullTree = buildCommentTree(toCommentVOList(rootComments), currentUserId);

        // 查询用户信息
        setUserNames(fullTree);
        postVO.setComments(fullTree);
        return postVO;
    }

    private List<CommentVO> buildCommentTree(List<CommentVO> comments, Long currentUserId) {
        for (CommentVO c : comments) {
            // 查询子评论
            List<Comment> children = commentMapper.selectList(
                    new QueryWrapper<Comment>().eq("parent_id", c.getId())
                            .orderBy(true, false, "create_time")
            );

            // 转换为CommentVO并递归构建树
            List<CommentVO> childVOs = toCommentVOList(children);
            c.setChildren(buildCommentTree(childVOs, currentUserId));

            // 设置是否已点赞
            boolean liked = userLikeMapper.selectCount(
                    new QueryWrapper<UserLike>()
                            .eq("user_id", currentUserId)
                            .eq("target_id", c.getId())
                            .eq("target_type", 2)
                            .orderBy(true, false, "create_time")
            ) > 0;
            // 可扩展：c.setLiked(liked);
        }
        return comments;
    }

    /**
     * 将Comment列表转换为CommentVO列表
     *
     * @param comments Comment列表
     * @return CommentVO列表
     */
    private List<CommentVO> toCommentVOList(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }

        return comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(comment, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private void setUserNames(List<CommentVO> comments) {
        Set<Long> userIds = new HashSet<>();
        collectUserIds(comments, userIds);

        // 如果没有用户ID，直接返回
        if (userIds.isEmpty()) {
            return;
        }

        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        setNames(comments, userMap);
    }

    private void collectUserIds(List<CommentVO> comments, Set<Long> userIds) {
        for (CommentVO c : comments) {
            userIds.add(c.getUserId());
            if (c.getReplyToUserId() != null) userIds.add(c.getReplyToUserId());
            collectUserIds(c.getChildren(), userIds);
        }
    }

    private void setNames(List<CommentVO> comments, Map<Long, User> userMap) {
        for (CommentVO c : comments) {
            User user = userMap.get(c.getUserId());
            if (user != null) {
                c.setUsername(user.getUserName());
                c.setUserAvatar(user.getUserAvatar());
            }
            User replyToUser = userMap.get(c.getReplyToUserId());
            if (replyToUser != null) {
                c.setReplyToUsername(replyToUser.getUserName());
                c.setUserAvatar(replyToUser.getUserAvatar());
            }
            setNames(c.getChildren(), userMap);
        }
    }

    public void likePost(Long userId, Long postId) {
        // 检查是否已点赞
        long count = userLikeMapper.selectCount(
                new QueryWrapper<UserLike>()
                        .eq("user_id", userId)
                        .eq("target_id", postId)
                        .eq("target_type", 1)
        );
        if (count > 0) return; // 已点赞，不重复操作

        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetId(postId);
        like.setTargetType(1);
        userLikeMapper.insert(like);

        postMapper.update(null,
                new UpdateWrapper<Post>().setSql("like_count = like_count + 1").eq("id", postId)
        );
    }

    @Override
    public IPage<PostVO> listPostsByPage(PageRequest pageRequest) {
        // 创建分页对象
        Page<Post> page = new Page<>(pageRequest.getCurrent(), pageRequest.getPageSize());

        // 创建查询条件
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();

        // 添加排序条件
        if (pageRequest.getSortField() != null && !pageRequest.getSortField().isEmpty()) {
            if ("ascend".equals(pageRequest.getSortOrder())) {
                queryWrapper.orderByAsc(pageRequest.getSortField());
            } else {
                queryWrapper.orderByDesc(pageRequest.getSortField());
            }
        } else {
            // 默认按创建时间倒序排列
            queryWrapper.orderByDesc("create_time");
        }

        // 执行分页查询
        IPage<Post> postPage = postMapper.selectPage(page, queryWrapper);


        // 查询并设置每个帖子的图片
        List<PostVO> posts = postPage.getRecords().stream().map(post -> {
            PostVO p = new PostVO();
            BeanUtils.copyProperties(post, p);
            return p;
        }).collect(Collectors.toList());

        if (!posts.isEmpty()) {
            // 获取所有帖子的ID
            List<Long> postIds = posts.stream().map(PostVO::getId).collect(Collectors.toList());

            // 查询所有相关的图片
            QueryWrapper<PostImage> imageQueryWrapper = new QueryWrapper<>();
            imageQueryWrapper.in("post_id", postIds);
            imageQueryWrapper.orderByAsc("sort");
            List<PostImage> postImages = postImageMapper.selectList(imageQueryWrapper);

            // 按post_id分组图片
            Map<Long, List<Map<String,String>>> imageMap = postImages.stream()
                    .collect(Collectors.groupingBy(
                            PostImage::getPostId,
                            Collectors.mapping(image->{
                                Map<String, String> imageUrl = new HashMap<>();
                                imageUrl.put("imageUrl", image.getImageUrl());
                                imageUrl.put("thumbnailUrl", image.getThumbnailUrl());
                                return imageUrl;
                            }, Collectors.toList())
                    ));

            // 将图片设置到对应的帖子中
            posts.forEach(post -> post.setImageUrls(imageMap.getOrDefault(post.getId(), new ArrayList<>())));

            // 查询并设置用户信息
            setUserPostInfo(posts);

            // 查询并设置评论信息
            setPostComments(posts);
        }

        // 转换为PostVO分页对象
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
       /* List<PostVO> postVOList = posts.stream()
                .map(post -> {
                    PostVO postVO = new PostVO();
                    BeanUtils.copyProperties(post, postVO);
                    return postVO;
                })
                .collect(Collectors.toList());*/
        postVOPage.setRecords(posts);

        return postVOPage;
    }

    /**
     * 设置帖子的用户信息
     *
     * @param posts 帖子列表
     */
    private void setUserPostInfo(List<PostVO> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // 收集所有用户ID
        Set<Long> userIds = posts.stream()
                .map(PostVO::getUserId)
                .filter(userId -> userId != null)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIds);
        List<User> users = userMapper.selectList(userQueryWrapper);

        // 构建用户信息映射
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 设置每个帖子的用户信息
        posts.forEach(post -> {
            User user = userMap.get(post.getUserId());
            if (user != null) {
                post.setUserAvatar(user.getUserAvatar());
                post.setUserName(user.getUserName());
            }
        });
    }

    /**
     * 设置帖子的评论信息
     *
     * @param posts 帖子列表
     */
    private void setPostComments(List<PostVO> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }

        // 收集所有帖子ID
        List<Long> postIds = posts.stream()
                .map(PostVO::getId)
                .filter(id -> id != null)
                .collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return;
        }

        // 查询所有相关评论
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.in("post_id", postIds);
        commentQueryWrapper.eq("parent_id", 0); // 只查询根评论
        List<Comment> comments = commentMapper.selectList(commentQueryWrapper);

        // 按post_id分组评论
        Map<Long, List<Comment>> commentMap = comments.stream()
                .collect(Collectors.groupingBy(Comment::getPostId));

        // 构建评论树结构
        Map<Long, List<CommentVO>> commentTreeMap = new HashMap<>();
        commentMap.forEach((postId, postComments) -> {
            List<CommentVO> commentVOs = toCommentVOList(postComments);
            commentTreeMap.put(postId, buildCommentTree(commentVOs, 0L));
        });

        // 设置每个帖子的评论信息
        posts.forEach(post -> {
            // 查询用户信息
            setUserNames(commentTreeMap.getOrDefault(post.getId(), new ArrayList<>()));
            post.setComments(commentTreeMap.getOrDefault(post.getId(), new ArrayList<>()));
        });
    }

    @Override
    @Transactional
    public void deletePost(Long postId, Long userId) {
        // 检查帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 检查是否是帖子作者（只有作者可以删除自己的帖子）
        User loginUser = userService.getById(userId);
        if (!post.getUserId().equals(userId)&&!userService.isAdmin(loginUser)) {
            throw new RuntimeException("没有权限删除该帖子");
        }

        // 删除帖子相关的评论
        commentMapper.delete(new QueryWrapper<Comment>().eq("post_id", postId));

        // 删除帖子相关的图片记录
        postImageMapper.delete(new QueryWrapper<PostImage>().eq("post_id", postId));

        // 删除帖子相关的点赞记录
        userLikeMapper.delete(new QueryWrapper<UserLike>().eq("target_id", postId).eq("target_type", 1));

        // 删除帖子
        postMapper.deleteById(postId);
    }
}