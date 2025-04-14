package com.digitalservicing.usermanager.service;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public interface ImgurService {
    URL uploadImage(File aFile) throws UnirestException, IOException;

    int getImage(String imageHash) throws UnirestException;

    int deleteImage(String imageHash) throws UnirestException;
}
