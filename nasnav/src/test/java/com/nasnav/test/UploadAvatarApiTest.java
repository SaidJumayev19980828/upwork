package com.nasnav.test;

import com.google.gson.Gson;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.response.UserApiResponse;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static com.nasnav.test.commons.TestCommons.getHttpEntity;
import static com.nasnav.test.commons.TestCommons.json;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "/sql/UploadAvatar.sql" })
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = { "/sql/database_cleanup.sql" })
public class UploadAvatarApiTest extends AbstractTestWithTempBaseDir {

    private static final String USER_TOKEN = "12388";
    @Autowired
    private MockMvc mockMvc;
    private static final String TEST_PHOTO = "nasnav--Test_Photo.png";

    private static final String TEST_IMG_DIR = "src/test/resources/test_imgs_to_upload";

    private static Path img = Paths.get(TEST_IMG_DIR).resolve(TEST_PHOTO).toAbsolutePath();

    private static byte[] imgBytes;

    @Autowired
    private TestRestTemplate template;

    @BeforeClass
    public static void init() throws IOException {
        imgBytes = Files.readAllBytes(img);
    }

    @Test
    public void UploadMultipleTimesThenDownloadTest() throws Exception {
        String oldAvatarUrl = uploadAvatar("test.png", USER_TOKEN);
        assertUploadedAvatar(oldAvatarUrl, USER_TOKEN);

        String newAvatarUrl = uploadAvatar("test.png", USER_TOKEN);
        // the new file will have a different name to avoid collision
        assertFalse(newAvatarUrl.endsWith("test.png"));
        assertUploadedAvatar(newAvatarUrl, USER_TOKEN);

        // old avatar should get deleted after uploading the new one
        ResponseEntity<byte[]> response = template.exchange("/files/" + oldAvatarUrl, HttpMethod.GET,
                TestCommons.getHttpEntity(""), byte[].class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private String uploadAvatar(String fileName, String token) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", imgBytes);

        var result = mockMvc.perform(MockMvcRequestBuilders.multipart("/user/uploadAvatar").file(file)
                .header(TOKEN_HEADER, token).cookie(new Cookie(TOKEN_HEADER, token)));
        ;
        var res = result.andReturn().getResponse().getContentAsString();
        UserApiResponse responseDto = new Gson().fromJson(res, UserApiResponse.class);
        result.andExpect(status().is(200));
        return responseDto.getImageURL();
    }

    @Test
    public void uploadAvatar_Test(){
        //
        String requestBody =
                json()
                        .put("fileName","avatar.jpg")
                        .put("base64Content", "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7")
                        .toString();

        HttpEntity<?> json = getHttpEntity(requestBody, "12388");
        ResponseEntity<UserApiResponse> response = template.postForEntity("/user/userAvatar", json, UserApiResponse.class);
        assertEquals(200, response.getStatusCode().value());
    }

    private void assertUploadedAvatar(String filePath, String token) {
        UserRepresentationObject userInfo = template.exchange("/user/info", HttpMethod.GET,
                TestCommons.getHttpEntity(token), UserRepresentationObject.class).getBody();

        assertEquals(filePath, userInfo.getImage());

        ResponseEntity<byte[]> response = template.exchange("/files/" + filePath, HttpMethod.GET,
                TestCommons.getHttpEntity(""), byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(imgBytes, response.getBody());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
    }

}
