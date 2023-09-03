package com.nasnav.service.jobs;

import com.nasnav.constatnts.CronExpression;
import com.nasnav.dao.PostLikesRepository;
import com.nasnav.persistence.AdvertisementProductEntity;
import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.PostService;
import com.nasnav.service.PostTransactionsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class AdvertisementJob {
    private final PostService postService;
    private final AdvertisementService advertisementService;
    private final PostLikesRepository postLikesRepository;
    private final PostTransactionsService postTransactionsService;
    private final int batchSize = 10;


    @Scheduled(cron = CronExpression.every5Minutes)
    public void calculateLikes() {
        PageImpl<PostEntity> page = postService.getAllPostsWithinAdvertisement(0, batchSize);
        for (PostEntity post : page.getContent()) {
            Long postLikes = postLikesRepository.countAllByPost_Id(post.getId());

            if (post.getProducts() != null) {
                if (postLikes > 0) {
                    Set<Long> productsInPost = post.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
                    if (!productsInPost.isEmpty()) {

                        List<AdvertisementProductEntity> advertisementProducts = advertisementService.findAdvertisementProducts(post.getAdvertisement().getId(), productsInPost);

                        int advertisementTotalLikes = advertisementProducts.stream().mapToInt(AdvertisementProductEntity::getLikes).sum();

                        if (advertisementTotalLikes > 0) {
                            if (postLikes > advertisementTotalLikes) {

                                int advertisementTotalCoins = advertisementProducts.stream().mapToInt(AdvertisementProductEntity::getCoins).sum();
                                long totalCoinsShouldBePaid = (postLikes / advertisementTotalLikes) * advertisementTotalCoins;
                                Long currentPaidCoins = postTransactionsService.getPaidCoins(post.getId());

                                if (totalCoinsShouldBePaid > currentPaidCoins) {
                                    postTransactionsService.saveTransactionsCoins(post.getId(), totalCoinsShouldBePaid - currentPaidCoins);
                                    log.info("pay {} to user {} for post {}", totalCoinsShouldBePaid - currentPaidCoins, post.getUser().getId(), post.getId());
                                }

                            }
                        }

                    }
                }
            }
        }
    }

}
