package com.nasnav.test;

import com.nasnav.dao.FollowerRepository;
import com.nasnav.dao.PostClicksRepository;
import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dao.PostRepository;
import com.nasnav.dto.PaginatedResponse;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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
        postCreationDTO.setReview(false);
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<PostCreationDTO> request = new HttpEntity<>(postCreationDTO,headers);
        ResponseEntity<Void> response = template.postForEntity("/post", request, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createReviewPostTest(){
       PostCreationDTO postCreationDTO = createPostCreationDTO();
        addAttachment(postCreationDTO);
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Token", "123");
        HttpEntity<PostCreationDTO> request = new HttpEntity<>(postCreationDTO,headers);
        ResponseEntity<Void> response = template.postForEntity("/post", request, Void.class);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void createPostTestException() {
        String userToken = "123";
        String employeeToken = "abcdefg";
        PostCreationDTO postCreationDTO = createPostCreationDTO();

        ResponseEntity<Void> responseEmployee = sendPostRequest(postCreationDTO,employeeToken);
        assertEquals(HttpStatus.FORBIDDEN, responseEmployee.getStatusCode());


        postCreationDTO.setShopId(501L);
        ResponseEntity<Void> response1 = sendPostRequest(postCreationDTO,userToken);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        // Set valid shopId but provide empty attachments to trigger 404 Not Found
        postCreationDTO.setShopId(5010L);
        addAttachment(postCreationDTO);
        ResponseEntity<Void> response2 = sendPostRequest(postCreationDTO,userToken);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());

        // Add an invalid productId to trigger 404 Not FOUND
        Set<Long> productsIds = postCreationDTO.getProductsIds();
        productsIds.add(100000001L);
        postCreationDTO.setProductsIds(productsIds);
        postCreationDTO.setReview(false);
        ResponseEntity<Void> response3 = sendPostRequest(postCreationDTO,userToken);
        assertEquals(HttpStatus.NOT_FOUND, response3.getStatusCode());


    }

    private PostCreationDTO createPostCreationDTO() {
        PostCreationDTO postCreationDTO = new PostCreationDTO();
        postCreationDTO.setDescription("description msg");
        postCreationDTO.setOrganizationId(99001L);
        Set<Long> productsIds = new HashSet<>();
        productsIds.add(1001L);
        postCreationDTO.setProductsIds(productsIds);
        postCreationDTO.setShopId(501L);
        postCreationDTO.setRating((short) 2);
        postCreationDTO.setProductName("test");
        postCreationDTO.setReview(true);
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

    private ResponseEntity<Void> sendPostRequest(PostCreationDTO postCreationDTO , String token) {
        return sendPostRequest(postCreationDTO, new HttpHeaders(),token);
    }

    private ResponseEntity<Void> sendPostRequest(PostCreationDTO postCreationDTO, HttpHeaders headers, String token ) {
        headers.add("User-Token", token);
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

        ResponseEntity<Void> response2 = template.postForEntity("/post/like?postId=1&likeAction=true", json, Void.class);
        assertEquals(200, response2.getStatusCode().value());
    }


    @Test
    public void likeOrDislikeReviewTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Long> response = template.postForEntity("/post/review/like?review=5", json, Long.class);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(2L, Objects.requireNonNull(response.getBody()).longValue());
        ResponseEntity<Long> response2 = template.postForEntity("/post/review/like?review=5", json, Long.class);
        assertEquals(200, response2.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response2.getBody()).longValue());
    }


    @Test
    public void getReviewTest(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ParameterizedTypeReference<PostResponseDTO> responseType = new ParameterizedTypeReference<>() {
        };
        ResponseEntity<PostResponseDTO> response = template.exchange("/post/5", HttpMethod.GET, json, responseType);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1L, Objects.requireNonNull(response.getBody()).getTotalReviewLikes());


    }

    @Test
    public void likeOrDislikeReviewTestException(){
        String requestBody =
                json().toString();
        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/post/review/like?review=16", json, Void.class);
        assertEquals(404, response.getStatusCode().value());
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

    @Test
    public void getForYouUserWithException()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 9999 + "&start=" + start + "&count=" + count + "&type=reviews", HttpMethod.GET, getHttpEntity("abcdefg"),
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    public void getForYouUserWithNotException()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 9999 + "&start=" + start + "&count=" + count + "&type=reviews", HttpMethod.GET,
                getHttpEntity("123"), new ParameterizedTypeReference<>()
                {
                });
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    public void getForYouUser()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=following", HttpMethod.GET, getHttpEntity("123"),
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getForYouUserExplore()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=explore", HttpMethod.GET, getHttpEntity("123"),
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getForYouUserReview()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=reviews", HttpMethod.GET, getHttpEntity("123"),
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getForYouUserPostsOfTheWeek() {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=postsOfTheWeek", HttpMethod.GET,
                getHttpEntity("123"), new ParameterizedTypeReference<>() {
                });
        assertEquals(200, response.getStatusCode().value());
    }

    public void getForYouUserWithoutType()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count, HttpMethod.GET, getHttpEntity("123"),
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void getForYouUserWrongType()
    {
        int start = 0, count = 10;
        ResponseEntity<PaginatedResponse<PostResponseDTO>> response = template.exchange(
                "/post/filterForUser?userId=" + 88 + "&start=" + start + "&count=" + count + "&type=worng", HttpMethod.GET, getHttpEntity("123"),
                new ParameterizedTypeReference<>()
                {
                });
        assertEquals(200, response.getStatusCode().value());
    }
}
