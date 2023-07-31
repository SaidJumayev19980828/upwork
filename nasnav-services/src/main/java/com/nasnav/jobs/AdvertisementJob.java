package com.nasnav.jobs;

import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.persistence.PostEntity;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.PostService;
import com.nasnav.service.PostTransactionsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AdvertisementJob {
    private final PostService postService;
    private final AdvertisementService advertisementService;
    private final PostLikesRepository postLikesRepository;
    private final PostTransactionsService postTransactionsService;
    private final int batchSize = 10;


    public void calculateLikes() {
        PageImpl<PostEntity> page = postService.getAllPostsWithinAdvertisement(0, batchSize);
        for (PostEntity post : page.getContent()) {
            Long allLikes = postLikesRepository.countAllByPost_Id(post.getId());
            AdvertisementDTO advertisement = advertisementService.findOneByPostId(post.getId());
            Long currentPaidCoins = postTransactionsService.getPaidCoins(post.getId());
            Long achievedCoins = allLikes / advertisement.getLikes() * advertisement.getCoins();
            if (achievedCoins > currentPaidCoins) {
                postTransactionsService.saveTransactionsCoins(post.getId(), achievedCoins - currentPaidCoins);
                log.info("pay {} to {}", advertisement.getCoins(), post.getUser().getId());
            }
        }
    }

}
