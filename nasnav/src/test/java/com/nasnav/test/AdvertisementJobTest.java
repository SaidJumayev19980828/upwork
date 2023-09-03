package com.nasnav.test;

import com.nasnav.dao.PostLikesRepository;
import com.nasnav.persistence.*;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.PostService;
import com.nasnav.service.PostTransactionsService;
import com.nasnav.service.jobs.AdvertisementJob;
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
import java.util.Set;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdvertisementJobTest {
    private AdvertisementJob advertisementJob;
    @Mock
    private PostService postService;
    @Mock
    private AdvertisementService advertisementService;
    @Mock
    private PostLikesRepository postLikesRepository;
    @Mock
    private PostTransactionsService postTransactionsService;

    @BeforeEach
    void init() {
        this.advertisementJob = new AdvertisementJob(postService, advertisementService, postLikesRepository, postTransactionsService);
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentLikesIsZero() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(it -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(it.getId())).thenReturn(0L);
        });
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentPostProductIsZero() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        post.setProducts(List.of());
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(it -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(it.getId())).thenReturn(10L);
        });
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenAdvertisementProductsIsEmpty() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        ProductEntity p1 = new ProductEntity();
        p1.setId(1L);
        ProductEntity p2 = new ProductEntity();
        p2.setId(2L);
        post.setProducts(List.of(p1, p2));
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(p -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(p.getId())).thenReturn(10L);
            Set<Long> products = p.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
            Mockito.when(advertisementService.findAdvertisementProducts(p.getAdvertisement().getId(), products)).thenReturn(List.of());
        });
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenTotalAdvertisementProductsLikesIsZero() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        ProductEntity p1 = new ProductEntity();
        p1.setId(1L);
        ProductEntity p2 = new ProductEntity();
        p2.setId(2L);
        post.setProducts(List.of(p1, p2));
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(p -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(p.getId())).thenReturn(10L);
            Set<Long> products = p.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
            AdvertisementProductEntity e1 = new AdvertisementProductEntity();
            e1.setLikes(0);
            e1.setCoins(150);
            Mockito.when(advertisementService.findAdvertisementProducts(p.getAdvertisement().getId(), products)).thenReturn(List.of(e1));
        });
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenTotalAdvertisementProductsLikesIsGreeterThanPostLikes() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        ProductEntity p1 = new ProductEntity();
        p1.setId(1L);
        ProductEntity p2 = new ProductEntity();
        p2.setId(2L);
        post.setProducts(List.of(p1, p2));
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(p -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(p.getId())).thenReturn(10L);
            Set<Long> products = p.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
            AdvertisementProductEntity e1 = new AdvertisementProductEntity();
            e1.setLikes(5000);
            e1.setCoins(150);
            Mockito.when(advertisementService.findAdvertisementProducts(p.getAdvertisement().getId(), products)).thenReturn(List.of(e1));
        });
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentPaidCoinsIsGreeterThanPostLikesCoins() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        ProductEntity p1 = new ProductEntity();
        p1.setId(1L);
        ProductEntity p2 = new ProductEntity();
        p2.setId(2L);
        post.setProducts(List.of(p1, p2));
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(p -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(p.getId())).thenReturn(16000L);
            Set<Long> products = p.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
            AdvertisementProductEntity e1 = new AdvertisementProductEntity();
            e1.setLikes(6000);
            e1.setCoins(300);
            AdvertisementProductEntity e2 = new AdvertisementProductEntity();
            e2.setLikes(2000);
            e2.setCoins(150);
            Mockito.when(advertisementService.findAdvertisementProducts(p.getAdvertisement().getId(), products)).thenReturn(List.of(e1, e2));
        });
        Mockito.when(postTransactionsService.getPaidCoins(1L)).thenReturn(900L);
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentPaidCoinsIsGreeterThanPostLikesCoinsAbs() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        ProductEntity p1 = new ProductEntity();
        p1.setId(1L);
        ProductEntity p2 = new ProductEntity();
        p2.setId(2L);
        post.setProducts(List.of(p1, p2));
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(p -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(p.getId())).thenReturn(15000L);
            Set<Long> products = p.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
            AdvertisementProductEntity e1 = new AdvertisementProductEntity();
            e1.setLikes(6000);
            e1.setCoins(300);
            AdvertisementProductEntity e2 = new AdvertisementProductEntity();
            e2.setLikes(2000);
            e2.setCoins(150);
            Mockito.when(advertisementService.findAdvertisementProducts(p.getAdvertisement().getId(), products)).thenReturn(List.of(e1, e2));
        });
        Mockito.when(postTransactionsService.getPaidCoins(1L)).thenReturn(900L);
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(0)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    void shouldNotCallSaveTransactionsWhenCurrentPaidCoinsIsGreeterThanPostLikesCoinsAbso() {
        PostEntity post = new PostEntity();
        post.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        post.setUser(user);
        AdvertisementEntity advertisement = new AdvertisementEntity();
        advertisement.setId(1L);
        post.setAdvertisement(advertisement);
        ProductEntity p1 = new ProductEntity();
        p1.setId(1L);
        ProductEntity p2 = new ProductEntity();
        p2.setId(2L);
        post.setProducts(List.of(p1, p2));
        List<PostEntity> posts = List.of(post);
        Mockito.when(postService.getAllPostsWithinAdvertisement(0, 10)).thenReturn(new PageImpl<>(posts));
        posts.forEach(p -> {
            Mockito.when(postLikesRepository.countAllByPost_Id(p.getId())).thenReturn(24000L);
            Set<Long> products = p.getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet());
            AdvertisementProductEntity e1 = new AdvertisementProductEntity();
            e1.setLikes(6000);
            e1.setCoins(300);
            AdvertisementProductEntity e2 = new AdvertisementProductEntity();
            e2.setLikes(2000);
            e2.setCoins(150);
            Mockito.when(advertisementService.findAdvertisementProducts(p.getAdvertisement().getId(), products)).thenReturn(List.of(e1, e2));
        });
        Mockito.when(postTransactionsService.getPaidCoins(1L)).thenReturn(900L);
        advertisementJob.calculateLikes();
        Mockito.verify(postTransactionsService, Mockito.times(1)).saveTransactionsCoins(Mockito.anyLong(), Mockito.anyLong());
    }
}
