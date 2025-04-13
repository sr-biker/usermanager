package com.digitalservicing.usermanager.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class ImgurServiceImpl implements ImgurService{

    @Value("${imgur.clientid:fb0dd7f3de3e69b}")
    private String imgurClientId;

    private String BASE_IMAGE_URL="https://api.imgur.com/3/image/";

    @Override
    public String uploadImage(File aFile) throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<JsonNode> response = Unirest.post("https://api.imgur.com/3/image")
                .header("Authorization", "Client-ID " + imgurClientId)
                .field("file", aFile)
                .field("type", "image")
                .field("title", "UserManager upload")
                .field("description", "UserManager upload")
                .asJson();
        return response.getBody().getObject().getString("link");
    }


    public void getImage() throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.get(BASE_IMAGE_URL + "{{imageHash}}")
                .header("Authorization", "Client-ID " + imgurClientId).asString();
    }

    @Override
    public int deleteImage(File aFile) throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<JsonNode> jsonResponse = Unirest.delete("https://api.imgur.com/3/image/{{imageHash}}")
                .header("Authorization", "Bearer {{accessToken}}")
                .field("file", aFile)
                .asJson();

        return jsonResponse.getStatus();
    }
}
