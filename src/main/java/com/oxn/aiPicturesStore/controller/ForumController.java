package com.oxn.aiPicturesStore.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oxn.aiPicturesStore.common.BaseResponse;
import com.oxn.aiPicturesStore.common.DeleteRequest;
import com.oxn.aiPicturesStore.common.PageRequest;
import com.oxn.aiPicturesStore.common.ResultUtils;
import com.oxn.aiPicturesStore.enums.StatusCode;
import com.oxn.aiPicturesStore.exception.BusinessException;
import com.oxn.aiPicturesStore.exception.ThrowUtils;
import com.oxn.aiPicturesStore.model.dto.post.PostQueryRequest;
import com.oxn.aiPicturesStore.model.entity.Comment;
import com.oxn.aiPicturesStore.model.entity.Post;
import com.oxn.aiPicturesStore.model.entity.User;
import com.oxn.aiPicturesStore.model.vo.PostVO;
import com.oxn.aiPicturesStore.service.CommentService;
import com.oxn.aiPicturesStore.service.PostImageService;
import com.oxn.aiPicturesStore.service.PostService;
import com.oxn.aiPicturesStore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import com.oxn.aiPicturesStore.model.dto.comment.AddCommentDTO;
import com.oxn.aiPicturesStore.model.dto.post.CreatePostDTO;
import com.oxn.aiPicturesStore.model.dto.post.UpdatePostDTO;

@RestController
@RequestMapping("/forum")
@RequiredArgsConstructor
public class ForumController {

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostImageService postImageService;

    @PostMapping("/post")
    public BaseResponse<PostVO> createPost(@RequestBody CreatePostDTO createPostDTO, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId(); // 从 token/session 获取
        PostVO post = postService.createPost(userId, createPostDTO);
        return ResultUtils.success(post);
    }

    @GetMapping("/post/{id}")
    public BaseResponse<PostVO> getPost(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId(); // 从 token/session 获取
        PostVO vo = postService.getPostWithComments(id, userId);
        return ResultUtils.success(vo);
    }

    @PostMapping("/post/{id}/like")
    public BaseResponse<Boolean> likePost(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Boolean b = postService.likePost(userId, id);
        return ResultUtils.success(b);
    }

    @PostMapping("/comment")
    public BaseResponse<Comment> addComment(@RequestBody AddCommentDTO dto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId(); // 从 token/session 获取
        Comment comment = commentService.addComment(
                userId, dto.getPostId(), dto.getParentId(), dto.getReplyToUserId(), dto.getContent()
        );
        return ResultUtils.success(comment);
    }

    /**
     * 删除评论
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/comment/delete")
    public BaseResponse<Boolean> deleteComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        commentService.deleteComment(deleteRequest.getId(), userId);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取帖子列表
     *
     * @param postQueryRequest 分页参数
     * @param request     HTTP请求
     * @return 帖子分页列表
     */
    @GetMapping("/posts")
    public BaseResponse<IPage<PostVO>> listPostsByPage(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        // 获取帖子分页列表
        IPage<PostVO> postPage = postService.listPostsByPage(postQueryRequest,request);
        return ResultUtils.success(postPage);
    }

    /**
     * 上传图片
     *
     * @param multipartFile 图片文件列表
     * @return 图片上传结果列表
     */
    @PostMapping("/upload")
    public BaseResponse<Map<String, String>> uploadPicture(@RequestParam("file") MultipartFile multipartFile, Long postId,HttpServletRequest request) {
        userService.getLoginUser(request);
        ThrowUtils.throwIf(multipartFile.isEmpty(), StatusCode.PARAMS_ERROR, "请选择图片");
        return ResultUtils.success(postImageService.uploadPicture(multipartFile, postId));
    }

    /**
     * 更新帖子
     *
     * @param updatePostDTO 更新请求
     * @param request       HTTP请求
     * @return 是否更新成功
     */
    @PostMapping("/post/update")
    public BaseResponse<PostVO> updatePost(@RequestBody UpdatePostDTO updatePostDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(updatePostDTO == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long postId = updatePostDTO.getId();
        Post post = postService.getById(postId);
        ThrowUtils.throwIf(post == null, StatusCode.NOT_FOUND_ERROR, "帖子不存在");
        if (!userId.equals(post.getUserId())&& !userService.isAdmin(loginUser)) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR, "无权限编辑");
        }
        PostVO postVO = postService.updatePost(userId, postId, updatePostDTO.getTitle(), updatePostDTO.getContent(), updatePostDTO.getImageUrls());
        return ResultUtils.success(postVO);
    }

    /**
     * 删除帖子
     *
     * @param deleteRequest 删除请求
     * @param request       HTTP请求
     * @return 是否删除成功
     */
    @PostMapping("/post/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, StatusCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        Long postId = deleteRequest.getId();
        Post post = postService.getById(postId);
        ThrowUtils.throwIf(post == null, StatusCode.NOT_FOUND_ERROR, "帖子不存在");
        if (!userId.equals(post.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR, "无权限删除");
        }
        postService.deletePost(postId, userId);
        return ResultUtils.success(true);
    }
    
    /**
     * 设置帖子置顶状态
     *
     * @param postId 帖子ID
     * @param isTop  是否置顶 0-不置顶 1-置顶
     * @param request HTTP请求
     * @return 是否设置成功
     */
    @PostMapping("/post/top/{postId}")
    public BaseResponse<Boolean> setTop(@PathVariable Long postId, @RequestParam Integer isTop, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 检查是否是管理员
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(StatusCode.NO_AUTH_ERROR, "无权限操作");
        }
        
        Boolean result = postService.setTop(postId, isTop);
        return ResultUtils.success(result);
    }
}