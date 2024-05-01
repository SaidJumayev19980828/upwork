package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.UserListFollowProjection;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.response.FollowerDTO;
import com.nasnav.dto.response.FollowerInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.FollowerServcie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;

@RestController
@RequestMapping(FollowerController.API_PATH)
@CrossOrigin("*")
public class FollowerController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/follow";
    @Autowired
    private FollowerServcie followerServcie;

    @GetMapping("/follower")
    public PageImpl<FollowerDTO> getAllFollowersByUserId(@RequestHeader(name = "User-Token", required = false) String token,
                                                         @RequestParam(required = false, defaultValue = "0") Integer start,
                                                         @RequestParam(required = false, defaultValue = "10") Integer count,
                                                         @RequestParam Long userId) {
        return followerServcie.getAllFollowersByUserId(userId, start, count);
    }

    @GetMapping("/following")
    public PageImpl<UserRepresentationObject> getAllFollowingsByUserId(@RequestHeader(name = "User-Token", required = false) String token,
                                                                       @RequestParam(required = false, defaultValue = "0") Integer start,
                                                                       @RequestParam(required = false, defaultValue = "10") Integer count,
                                                                       @RequestParam Long followerId) {
        return followerServcie.getAllFollowingsByUserId(followerId, start, count);
    }

    @GetMapping("/info")
    public FollowerInfoDTO getFollowerInfo(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                           @RequestParam Long userId) {
        return followerServcie.getFollowerInfoByUserId(userId);
    }

    @GetMapping("users/list")
    public PageImpl<UserListFollowProjection> getUsersList(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken ,
                                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                                           @RequestParam(required = false, defaultValue = "10") Integer count
                                                           ) {
        return followerServcie.getUsersWithFollowerStatus(start,count);
    }

    @PostMapping
    public void followOrUnfollow(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                 @RequestParam Boolean followAction,
                                 @RequestParam Long followerId) throws BusinessException {
        followerServcie.followOrUnfollow(followerId,followAction);
    }
}
