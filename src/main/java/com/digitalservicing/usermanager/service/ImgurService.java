package com.digitalservicing.usermanager.service;

import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.File;

public interface ImgurService {
    String uploadImage(File aFile) throws UnirestException;
}
