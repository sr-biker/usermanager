package com.digitalservicing.usermanager.service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;

@Service
@Slf4j
public class ImgurServiceImpl implements ImgurService{

    @Value("${usermanager.imgur.accessToken}")
    private String imgurAccessToken;

    @Override
    public URL uploadImage(File aFile) throws UnirestException, IOException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<JsonNode> jsonResponse = Unirest.post("https://api.imgur.com/3/upload")
                .header("Authorization", "Bearer " + imgurAccessToken)
                .header("Token", "Bearer " + imgurAccessToken)
                .field("image", imageToBase64(aFile))
                .asJson();
        log.info(jsonResponse.getStatusText());
        String link =jsonResponse.getBody().getObject().getJSONObject("data").getString ("link");
        return URI.create(link).toURL();
    }

    @Override
    public int getImage(String imageHash) throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.imgur.com/3/image/ "+ imageHash)
                .header("Authorization", "Bearer " + imgurAccessToken)
                .asJson();
        return jsonResponse.getStatus();

    }

    @Override
    public int deleteImage(String imageHash) throws UnirestException {
        Unirest.setTimeouts(0, 0);
        HttpResponse<JsonNode> jsonResponse = Unirest.delete("https://api.imgur.com/3/image/" + imageHash)
                .header("Authorization", "Bearer " + imgurAccessToken)
                .asJson();

        return jsonResponse.getStatus();
    }

    private String imageToBase64(File imageFile) throws IOException {
        byte[] fileContent = Files.readAllBytes(imageFile.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
