package com.nasnav.test;

import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.service.jobs.AdvertisementJob;
import com.nasnav.persistence.PostEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.PostService;
import com.nasnav.service.PostTransactionsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdvertisementJobTest {
    private AdvertisementJob advertisementJob;
    @Mock
    private PostService postService;
    @Mock
    private AdvertisementService advertisementService;
    @Mock
    private PostLikesRepository postLikesRepository;
    @Mock
    private PostTransactionsService postTransactionsService;


    private static List<PostEntity> getPostEntities() {
        UserEntity user = new UserEntity();
        user.setId(150L);
        PostEntity pe1 = new PostEntity();
        pe1.setId(1L);
        pe1.setUser(user);
        PostEntity pe2 = new PostEntity();
        pe2.setId(2L);
        pe2.setUser(user);
        PostEntity pe3 = new PostEntity();
        pe3.setId(3L);
        pe3.setUser(user);
        PostEntity pe4 = new PostEntity();
        pe4.setId(4L);
        pe4.setUser(user);
        return List.of(pe1, pe2, pe3, pe4);
    }

    private static Map<Long, AdvertisementDTO> getAdvertisementByPost() {
        AdvertisementDTO v1 = new AdvertisementDTO();
        v1.setCoins(1000);
        v1.setLikes(20 * 1000);

        AdvertisementDTO v2 = new AdvertisementDTO();
        v2.setCoins(500);
        v2.setLikes(3 * 1000);

        AdvertisementDTO v3 = new AdvertisementDTO();
        v3.setCoins(200);
        v3.setLikes(650);

        AdvertisementDTO v4 = new AdvertisementDTO();
        v4.setCoins(600);
        v4.setLikes(12 * 1000);

        return Map.of(1L, v1, 2L, v2, 3L, v3, 4L, v4);
    }

    private static Map<Long, Long> getPaidCoinsByPost() {
        return Map.of(1L, 100L, 2L, 200L, 3L, 300L, 4L, 400L);
    }

    private static Map<Long, Long> getLikesByPost() {
        return Map.of(1L, 10 * 1000L, 2L, 6000L, 3L, 1950L, 4L, 0L);
    }

    @BeforeEach
    void init() {
        this.advertisementJob = new AdvertisementJob(postService, advertisementService, postLikesRepository, postTransactionsService);
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentLikesLessThanAdvertisementLikes1() {
        List<PostEntity> posts = getPostEntities().stream().skip(0).limit(1).collect(Collectors.toList());
        Mockito.when(postService.getAllPostsWithinAdvertisement(any(), any())).thenReturn(new PageImpl<>(posts));

        Map<Long, Long> likesByPost = getLikesByPost();
        likesByPost.forEach((key, value) -> Mockito.when(postLikesRepository.countAllByPost_Id(key)).thenReturn(value));

        Map<Long, AdvertisementDTO> advertisementByPost = getAdvertisementByPost();
        advertisementByPost.forEach((key, value) -> Mockito.when(advertisementService.findOneByPostId(key)).thenReturn(value));

        Map<Long, Long> paidCoinsByPost = getPaidCoinsByPost();
        paidCoinsByPost.forEach((key, value) -> Mockito.when(postTransactionsService.getPaidCoins(key)).thenReturn(value));

        advertisementJob.calculateLikes();

        posts.forEach(it -> {
            Mockito.verify(postLikesRepository).countAllByPost_Id(it.getId());
            Mockito.verify(advertisementService).findOneByPostId(it.getId());
            Mockito.verify(postTransactionsService).getPaidCoins(it.getId());
        });
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldCallSaveTransactionsWhenCurrentLikesGreeterThanAdvertisementLikes2() {
        List<PostEntity> posts = getPostEntities().stream().skip(1).limit(1).collect(Collectors.toList());
        Mockito.when(postService.getAllPostsWithinAdvertisement(any(), any())).thenReturn(new PageImpl<>(posts));

        Map<Long, Long> likesByPost = getLikesByPost();
        likesByPost.forEach((key, value) -> Mockito.when(postLikesRepository.countAllByPost_Id(key)).thenReturn(value));

        Map<Long, AdvertisementDTO> advertisementByPost = getAdvertisementByPost();
        advertisementByPost.forEach((key, value) -> Mockito.when(advertisementService.findOneByPostId(key)).thenReturn(value));

        Map<Long, Long> paidCoinsByPost = getPaidCoinsByPost();
        paidCoinsByPost.forEach((key, value) -> Mockito.when(postTransactionsService.getPaidCoins(key)).thenReturn(value));

        advertisementJob.calculateLikes();

        posts.forEach(it -> {
            Mockito.verify(postLikesRepository).countAllByPost_Id(it.getId());
            Mockito.verify(advertisementService).findOneByPostId(it.getId());
            Mockito.verify(postTransactionsService).getPaidCoins(it.getId());
        });
        Mockito.verify(postTransactionsService, Mockito.times(1)).saveTransactionsCoins(2L, 800L);

    }

    @Test
    void shouldCallSaveTransactionsWhenCurrentLikesGreeterThanAdvertisementLikes3() {
        List<PostEntity> posts = getPostEntities().stream().skip(2).limit(1).collect(Collectors.toList());
        Mockito.when(postService.getAllPostsWithinAdvertisement(any(), any())).thenReturn(new PageImpl<>(posts));

        Map<Long, Long> likesByPost = getLikesByPost();
        likesByPost.forEach((key, value) -> Mockito.when(postLikesRepository.countAllByPost_Id(key)).thenReturn(value));

        Map<Long, AdvertisementDTO> advertisementByPost = getAdvertisementByPost();
        advertisementByPost.forEach((key, value) -> Mockito.when(advertisementService.findOneByPostId(key)).thenReturn(value));

        Map<Long, Long> paidCoinsByPost = getPaidCoinsByPost();
        paidCoinsByPost.forEach((key, value) -> Mockito.when(postTransactionsService.getPaidCoins(key)).thenReturn(value));

        advertisementJob.calculateLikes();

        posts.forEach(it -> {
            Mockito.verify(postLikesRepository).countAllByPost_Id(it.getId());
            Mockito.verify(advertisementService).findOneByPostId(it.getId());
            Mockito.verify(postTransactionsService).getPaidCoins(it.getId());
        });
        Mockito.verify(postTransactionsService, Mockito.times(1)).saveTransactionsCoins(3L, 300L);
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentLikesLessThanAdvertisementLikes4() {
        List<PostEntity> posts = getPostEntities().stream().skip(3).limit(1).collect(Collectors.toList());
        Mockito.when(postService.getAllPostsWithinAdvertisement(any(), any())).thenReturn(new PageImpl<>(posts));

        Map<Long, Long> likesByPost = getLikesByPost();
        likesByPost.forEach((key, value) -> Mockito.when(postLikesRepository.countAllByPost_Id(key)).thenReturn(value));

        Map<Long, AdvertisementDTO> advertisementByPost = getAdvertisementByPost();
        advertisementByPost.forEach((key, value) -> Mockito.when(advertisementService.findOneByPostId(key)).thenReturn(value));

        Map<Long, Long> paidCoinsByPost = getPaidCoinsByPost();
        paidCoinsByPost.forEach((key, value) -> Mockito.when(postTransactionsService.getPaidCoins(key)).thenReturn(value));

        advertisementJob.calculateLikes();

        posts.forEach(it -> {
            Mockito.verify(postLikesRepository).countAllByPost_Id(it.getId());
            Mockito.verify(advertisementService).findOneByPostId(it.getId());
            Mockito.verify(postTransactionsService).getPaidCoins(it.getId());
        });

        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(any(), any());

    }
}
