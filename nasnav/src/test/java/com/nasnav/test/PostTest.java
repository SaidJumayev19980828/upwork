package com.nasnav.test;

import com.nasnav.dao.FollowerRepository;
import com.nasnav.dao.PostClicksRepository;
import com.nasnav.dao.PostLikesRepository;
import com.nasnav.dao.PostRepository;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

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
        String requestBody =
                json()
                        .put("isReview", false)
                        .put("description", "description msg")
                        .put("organizationId", 99001L)
                        .put("productsIds", Arrays.asList(1001))
                        .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "123");
        ResponseEntity<Void> response = template.postForEntity("/post", json, Void.class);
        assertEquals(200, response.getStatusCode().value());
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
}
