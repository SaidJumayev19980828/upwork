package com.nasnav.test;

import com.nasnav.dao.FollowerRepository;
import com.nasnav.dao.PostClicksRepository;
import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dao.PostRepository;
import com.nasnav.dto.UserListFollowProjection;
import com.nasnav.dto.request.PostCreationDTO;
import com.nasnav.dto.response.PostResponseDTO;
import com.nasnav.dto.response.RestResponsePage;
import com.nasnav.request.ImageBase64;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Posts_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class PostTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private TestRestTemplate template;
    @Autowired
    private PostLikesRepository postLikesRepository;
    @Autowired
    private PostClicksRepository postClicksRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private FollowerRepository followerRepository;


    @Test
    public void createPostTest(){
        PostCreationDTO postCreationDTO = createPostCreationDTO();
        postCreationDTO.setIsReview(false);
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PostCreationDTO> request = new HttpEntity<>(postCreationDTO,headers);
        ResponseEntity<Void> response = template.postForEntity("/post", request, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createReviewPostTest(){
       PostCreationDTO postCreationDTO = createPostCreationDTO();
        addAttachment(postCreationDTO);
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");
        HttpEntity<PostCreationDTO> request = new HttpEntity<>(postCreationDTO,headers);
        ResponseEntity<Void> response = template.postForEntity("/post", request, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createPostTestException() {
        PostCreationDTO postCreationDTO = createPostCreationDTO();

        postCreationDTO.setShopId(501L);
        ResponseEntity<Void> response1 = sendPostRequest(postCreationDTO);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        // Set valid shopId but provide empty attachments to trigger 404 Not Found
        postCreationDTO.setShopId(5010L);
        addAttachment(postCreationDTO);
        ResponseEntity<Void> response2 = sendPostRequest(postCreationDTO);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());

        // Add an invalid productId to trigger 406 Not Acceptable
        Set<Long> productsIds = postCreationDTO.getProductsIds();
        productsIds.add(100000001L);
        postCreationDTO.setProductsIds(productsIds);
        postCreationDTO.setIsReview(false);
        ResponseEntity<Void> response3 = sendPostRequest(postCreationDTO);
        assertEquals(HttpStatus.NOT_ACCEPTABLE, response3.getStatusCode());


    }

    private PostCreationDTO createPostCreationDTO() {
        PostCreationDTO postCreationDTO = new PostCreationDTO();
        postCreationDTO.setDescription("description msg");
        postCreationDTO.setOrganizationId(99001L);
        postCreationDTO.setProductsIds(new HashSet<>(Collections.singletonList(1001L)));
        postCreationDTO.setShopId(501L);
        postCreationDTO.setRating((short) 2);
        postCreationDTO.setProductName("test");
        postCreationDTO.setIsReview(true);
        return postCreationDTO;
    }

    private void addAttachment(PostCreationDTO postCreationDTO) {
        List<ImageBase64> attachments = new ArrayList<>();
        ImageBase64 attachment = new ImageBase64();
        attachment.setFileName("avatar.jpg");
        attachment.setBase64("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
        attachment.setFileType("image/jpeg");
        attachments.add(attachment);
        postCreationDTO.setAttachment(attachments);
    }

    private ResponseEntity<Void> sendPostRequest(PostCreationDTO postCreationDTO) {
        return sendPostRequest(postCreationDTO, new HttpHeaders());
    }

    private ResponseEntity<Void> sendPostRequest(PostCreationDTO postCreationDTO, HttpHeaders headers) {
        HttpCookie cookie = new HttpCookie("User-Token", "123");
        headers.add("Cookie", cookie.toString());
        headers.add("User-Token", "123");

        HttpEntity<PostCreationDTO> request = new HttpEntity<>(postCreationDTO, headers);

        return template.postForEntity("/post", request, Void.class);
    }


    @Test
    public void likeorDislikePostTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/post/like?postId=1&likeAction=true", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L,postLikesRepository.countAllByPost_Id(1L).longValue());
    }

    @Test
    public void clickOnPostTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/post/click?postId=1", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        ResponseEntity<Void> response2 = template.postForEntity("/post/click?postId=1", json, Void.class);
        assertEquals(200, response2.getStatusCode().value());
        assertEquals(2L,postClicksRepository.getClicksCountByPost(1L).longValue());
    }

    @Test
    public void approveReviewTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "hijkllm");
        template.put("/post/approve?postId=2&postStatus=APPROVED", json, String.class);
        assertEquals(1,postRepository.findById(2L).get().getStatus().intValue());
    }

    @Test
    public void followOrUnfollowUserTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/follow?followerId=89&followAction=true", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1,followerRepository.countAllByUser_Id(89).longValue());
        ResponseEntity<Void> response2 = template.postForEntity("/follow?followerId=89&followAction=false", json, Void.class);
        assertEquals(200, response2.getStatusCode().value());
        assertEquals(0,followerRepository.countAllByUser_Id(89).longValue());
    }

    @Test
    public void userListWithFollowerOrFollowingOrBothTest(){

        HttpEntity<Object> httpEntity = getHttpEntity("123");
        ParameterizedTypeReference<RestResponsePage<UserListFollowProjection>> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<?> response = template.exchange("/follow/users/list", HttpMethod.GET, httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void savePostTest(){
        Long postId= 2L;
        HttpEntity<Object> httpEntity = getHttpEntity("123");
        ResponseEntity<Void> response = template.postForEntity("/post/save?postId=" +postId , httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }
    @Test
    public void unsavePostTest(){
        Long postId= 2L;
        HttpEntity<Object> httpEntity = getHttpEntity("123");
        ResponseEntity<Void> response = template.postForEntity("/post/unsave?postId=" +postId , httpEntity, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getSavedPostsTest(){
        HttpEntity<Object> httpEntity = getHttpEntity("123");
        ParameterizedTypeReference<RestResponsePage<PostResponseDTO>> responseType = new ParameterizedTypeReference<>() {
        };
        Integer start = 0;
        Integer count =10;
        ResponseEntity<RestResponsePage<PostResponseDTO>> response = template.exchange("/post/saved?start=" + start + "&?count=" + count  , HttpMethod.GET, httpEntity, responseType);
        assertEquals(200, response.getStatusCode().value());

    }


    @Test
    public void getSavedPostsTestWithException(){
        HttpEntity<Object> httpEntity = getHttpEntity("abcdefg");
        Integer start = 0;
        Integer count =10;
        ResponseEntity<Void> response = template.exchange("/post/saved?start=" + start + "&?count=" + count  , HttpMethod.GET, httpEntity, Void.class);
        assertEquals(403, response.getStatusCode().value());

    }
}
