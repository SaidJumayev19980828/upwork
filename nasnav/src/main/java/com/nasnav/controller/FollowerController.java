package com.nasnav.controller;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.FollowerEntity;
import com.nasnav.service.FollowerServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/follow", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class FollowerController {
    @Autowired
    private FollowerServcie followerServcie;

    @GetMapping("/follower")
    public List<UserRepresentationObject> getAllFollowersByUserId(@RequestHeader(TOKEN_HEADER) String userToken,
                                                                  @RequestParam Long userId) {
        return followerServcie.getAllFollowersByUserId(userId);
    }

    @GetMapping("/following")
    public List<UserRepresentationObject> getAllFollowingsByUserId(@RequestHeader(TOKEN_HEADER) String userToken,
                                                                  @RequestParam Long followerId) {
        return followerServcie.getAllFollowingsByUserId(followerId);
    }

    @PostMapping
    public void followOrUnfollow(@RequestHeader(TOKEN_HEADER) String userToken,
                                 @RequestParam Boolean followAction,
                                 @RequestParam Long followerId) throws BusinessException {
        followerServcie.followOrUnfollow(followerId,followAction);
    }
}
