package com.nasnav.controller;

import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.enumerations.PostStatus;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/post", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/{id}")
    public PostResponseDTO getPostById(@RequestHeader(TOKEN_HEADER) String userToken, @PathVariable Long id) throws BusinessException {
        return postService.getPostById(id);
    }

    @GetMapping("/homeTimeline")
    public PageImpl<PostResponseDTO> getUserTimeline(@RequestHeader(name = "User-Token", required = false) String token,
                                                     @RequestParam(required = false, defaultValue = "0") Integer start,
                                                     @RequestParam(required = false, defaultValue = "10") Integer count) {
        return postService.getHomeTimeLine(start, count);
    }

    @GetMapping("/userTimeline")
    public PageImpl<PostResponseDTO> getUserTimeline(@RequestHeader(name = "User-Token", required = false) String token,
                                                 @RequestParam(required = false, defaultValue = "0") Integer start,
                                                 @RequestParam(required = false, defaultValue = "10") Integer count,
                                                 @RequestParam long userId) {
        return postService.getUserTimeLine(userId, start, count);
    }

    @GetMapping("/pending")
    public PageImpl<PostResponseDTO> getUserPendingPosts(@RequestHeader(name = "User-Token", required = false) String token,
                                                         @RequestParam(required = false, defaultValue = "0") Integer start,
                                                         @RequestParam(required = false, defaultValue = "10") Integer count) {
        return postService.getUserPendingPosts(start, count);
    }

    @GetMapping("/orgReviews")
    public PageImpl<PostResponseDTO> getOrgReviews(@RequestHeader(name = "User-Token", required = false) String token,
                                                   @RequestParam(required = false, defaultValue = "0") Integer start,
                                                   @RequestParam(required = false, defaultValue = "10") Integer count,
                                                   @RequestParam(required = false) PostStatus postStatus) {
        return postService.getOrgReviews(postStatus, start, count);
    }

    @GetMapping("/orgSharedProducts")
    public PageImpl<PostResponseDTO> getOrgPosts(@RequestHeader(name = "User-Token", required = false) String token,
                                                 @RequestParam(required = false, defaultValue = "0") Integer start,
                                                 @RequestParam(required = false, defaultValue = "10") Integer count) {
        return postService.getOrgSharedProducts(start, count);
    }

    @PostMapping
    public PostResponseDTO createPost(@RequestHeader(TOKEN_HEADER) String userToken, @RequestBody PostCreationDTO post) throws BusinessException {
        return postService.createPost(post);
    }

    @PostMapping("/like")
    public Long likeOrDisLikePost(@RequestHeader(TOKEN_HEADER) String userToken, @RequestParam long postId, @RequestParam boolean likeAction){
        return postService.likeOrDisLikePost(postId, likeAction);
    }

    @PostMapping("/click")
    public void clickOnPost(@RequestHeader(TOKEN_HEADER) String userToken, @RequestParam long postId){
        postService.clickOnPost(postId);
    }

    @PutMapping("/approve")
    public void approveOrRejectReview(@RequestHeader(TOKEN_HEADER) String userToken,@RequestParam long postId, @RequestParam PostStatus postStatus){
        postService.approveOrRejectReview(postId, postStatus);
    }

    @PostMapping("/save")
    public void saveForLater(@RequestHeader(TOKEN_HEADER) String userToken, @RequestParam long postId){
         postService.saveForLater(postId);
    }

    @PostMapping("/unsave")
    public void unSavePost(@RequestHeader(TOKEN_HEADER) String userToken, @RequestParam long postId){
        postService.unSavePost(postId);
    }

    @GetMapping("/saved")
    public PageImpl<PostResponseDTO> getUserSavedPosts(@RequestHeader(name = "User-Token", required = false) String token,
                                                     @RequestParam(required = false, defaultValue = "0") Integer start,
                                                     @RequestParam(required = false, defaultValue = "10") Integer count) {
        return postService.getUserSavedPosts(start, count);
    }

}
