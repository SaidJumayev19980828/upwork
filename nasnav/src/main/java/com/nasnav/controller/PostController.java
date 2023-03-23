package com.nasnav.controller;

import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/post", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/{id}")
    public PostResponseDTO getPostById(@PathVariable Long id) throws BusinessException {
        return postService.getPostById(id);
    }

    @PostMapping
    public PostResponseDTO createPost(@RequestBody PostCreationDTO post) throws BusinessException {
        return postService.createPost(post);
    }

    @PostMapping("/like")
    public Long likeOrDisLikePost(@RequestParam long postId, @RequestParam boolean likeAction){
        return postService.likeOrDisLikePost(postId, likeAction);
    }
}
