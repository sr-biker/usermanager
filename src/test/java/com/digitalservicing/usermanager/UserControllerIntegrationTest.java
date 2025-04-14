package com.digitalservicing.usermanager;

import com.digitalservicing.usermanager.dto.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
    void testUploadImage() throws FileNotFoundException, URISyntaxException {
        File imageFile = ResourceUtils.getFile(
                "classpath:sample.jpg");
        URI postUrl = URI.create("/api/v1/user/image?fileName=" + imageFile.getAbsolutePath());

        ResponseEntity<URL> responseEntity = testRestTemplate.postForEntity(postUrl, null, URL.class);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode() );
        Assertions.assertNotNull(responseEntity.getBody());
        ResponseEntity<String> response = testRestTemplate.exchange(responseEntity.getBody().toURI(), HttpMethod.HEAD, null, String.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testDeleteImage() throws FileNotFoundException, URISyntaxException {
        File imageFile = ResourceUtils.getFile(
                "classpath:sample.jpg");
        URI postUrl = URI.create("/api/v1/user/image?fileName=" + imageFile.getAbsolutePath());

        ResponseEntity<URL> responseEntity = testRestTemplate.postForEntity(postUrl, null, URL.class);
        Assertions.assertEquals(HttpStatus.ACCEPTED, responseEntity.getStatusCode() );
        Assertions.assertNotNull(responseEntity.getBody());
        ResponseEntity<String> response = testRestTemplate.exchange(responseEntity.getBody().toURI(), HttpMethod.HEAD, null, String.class);
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());


        String[] segments =  responseEntity.getBody().toURI().getPath().split("/");
        String lastSegment = segments[segments.length - 1];
        String imageHash = lastSegment.split(".jpeg")[0];
        URI deleteUrl = URI.create("/api/v1/user/image?imageHash=" + imageHash);
        testRestTemplate.delete(deleteUrl);

        ResponseEntity<String> response2 =  testRestTemplate.exchange(responseEntity.getBody().toURI(), HttpMethod.HEAD,
                null, String.class);
        Assertions.assertNotEquals (200,response2.getStatusCode().is4xxClientError());
    }

}
