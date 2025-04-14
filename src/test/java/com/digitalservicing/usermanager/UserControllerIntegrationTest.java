package com.digitalservicing.usermanager;

import com.digitalservicing.usermanager.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){

    }

    @Test
    void testVersion(){
       ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/api/v1/version",String.class);
       Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    void testLogin()  {
        ResponseEntity<String> responseEntity = testRestTemplate.getForEntity("/api/v1/user/login?userName=JOHN DOE&password=abcd",
                String.class);
        Assertions.assertEquals(HttpStatus.OK,  responseEntity.getStatusCode());
    }

    @Test
    void testAddProfileToUser() throws MalformedURLException {
        URI url = URI.create("/api/v1/user/profile?userid=299999&profileUrl=http://google.com");
        testRestTemplate.put(url, null);
        ResponseEntity<UserDto> responseEntity = testRestTemplate.getForEntity("/api/v1/user/299999/profile", UserDto.class);
        Assertions.assertEquals(299999, responseEntity.getBody().getUserId() );
        Assertions.assertEquals( URI.create("http://google.com").toURL(), responseEntity.getBody().getProfileUri() );
    }

    @Test
    void testGetUser()  {
        ResponseEntity<UserDto> responseEntity = testRestTemplate.getForEntity("/api/v1/user/199999/profile", UserDto.class);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode() );
        Assertions.assertEquals(199999, responseEntity.getBody().getUserId() );
        Assertions.assertEquals("JOHN DOE", responseEntity.getBody().getUserName() );
    }

    @Test
    void testUploadImage() throws FileNotFoundException {
        File imageFile = ResourceUtils.getFile(
                "classpath:sample.jpg");
        URI postUrl = URI.create("/api/v1/user/image?fileName=" + imageFile.getAbsolutePath());

        ResponseEntity<URL> responseEntity = testRestTemplate.postForEntity(postUrl, null, URL.class);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode() );
        Assertions.assertNotNull(responseEntity.getBody());
    }

}
