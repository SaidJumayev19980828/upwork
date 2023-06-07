package com.nasnav.test;

import com.google.gson.Gson;
import com.nasnav.NavBox;
import com.nasnav.response.UserApiResponse;
import com.nasnav.test.commons.TestCommons;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;

import net.jcip.annotations.NotThreadSafe;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NavBox.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureMockMvc
@PropertySource("classpath:test.database.properties")
@NotThreadSafe
@Sql(executionPhase = BEFORE_TEST_METHOD, scripts = {"/sql/UploadAvatar.sql"})
@Sql(executionPhase = AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
public class UploadAvatarApiTest extends AbstractTestWithTempBaseDir {

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
    public void uploadUserAvatar() throws IOException, Exception {

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", imgBytes);

        var result = uploadAvatar("/user/uploadAvatar", "12388", file);
        var res = result.andReturn().getResponse().getContentAsString();
        UserApiResponse responseDto = new Gson().fromJson(res, UserApiResponse.class);
        result.andExpect(status().is(200));
        assertEquals("customers/88/test.png", responseDto.getImageURL());
    }

    private ResultActions uploadAvatar(String url, String token, MockMultipartFile filePart) throws Exception {

        var result = mockMvc.perform(MockMvcRequestBuilders.multipart(url).file(filePart).header(TOKEN_HEADER, token).cookie(new Cookie(TOKEN_HEADER, token)));
        return result;
    }

    @Test
    public void theUserAvatarUploadToDownloadTestProcess() throws Exception {

        MockMultipartFile file = new MockMultipartFile("file", "Test.png", "image/png", imgBytes);

        var result = uploadAvatar("/user/uploadAvatar", "12388", file);
        var res = result.andReturn().getResponse().getContentAsString();
        UserApiResponse responseDto = new Gson().fromJson(res, UserApiResponse.class);
        result.andExpect(status().is(200));
        assertEquals("customers/88/test.png", responseDto.getImageURL());

        String expectedUrl = responseDto.getImageURL();

        ResponseEntity<byte[]> response = template.exchange("/files/"+ expectedUrl, HttpMethod.GET, TestCommons.getHttpEntity(""), byte[].class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(imgBytes, response.getBody());
        assertEquals("image/png", response.getHeaders().getContentType().toString());
    }

}


