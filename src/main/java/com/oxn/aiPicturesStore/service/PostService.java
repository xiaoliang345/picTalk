package com.oxn.aiPicturesStore.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oxn.aiPicturesStore.common.PageRequest;
import com.oxn.aiPicturesStore.model.dto.post.PostQueryRequest;
import com.oxn.aiPicturesStore.model.entity.Picture;
import com.oxn.aiPicturesStore.model.entity.Post;
import com.oxn.aiPicturesStore.model.vo.PostVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 帖子服务接口
 */
public interface PostService extends IService<Post> {

    /**
     * 创建帖子
     *
     * @param userId     用户ID
     * @param title      标题
     * @param content    内容
     * @param imageUrls  图片URL列表
     * @return 创建的帖子
     */
    PostVO createPost(Long userId, String title, String content, List<Map<String, String>> imageUrls);

    /**
     * 更新帖子
     *
     * @param userId      用户ID
     * @param postId      帖子ID
     * @param title       标题
     * @param content     内容
     * @param imageUrls   图片URL列表
     * @return 更新后的帖子
     */
    PostVO updatePost(Long userId, Long postId, String title, String content, List<Map<String, String>> imageUrls);

    /**
     * 获取帖子详情及评论
     *
     * @param postId        帖子ID
     * @param currentUserId 当前用户ID
     * @return 帖子详情VO
     */
    PostVO getPostWithComments(Long postId, Long currentUserId);

    /**
     * 点赞帖子
     *
     * @param userId  用户ID
     * @param postId  帖子ID
     */
    Boolean likePost(Long userId, Long postId);

    /**
     * 分页获取帖子列表
     *
     * @param postQueryRequest 分页参数
     * @return 帖子分页列表
     */
    IPage<PostVO> listPostsByPage(PostQueryRequest postQueryRequest, HttpServletRequest request);

    /**
     * 删除帖子及其关联的评论和图片
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     */
    void deletePost(Long postId, Long userId);
}