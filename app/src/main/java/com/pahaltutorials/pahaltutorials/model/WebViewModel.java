package com.pahaltutorials.pahaltutorials.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.List;

public class WebViewModel {

    /*private String link;
    private String filePath;
    private String downloadLink;
    private String description;
    private List<String> words;
    private List<String> downloadLinks;
    private String docLink;
    private int startPageNumber;
    private int numberOfPages;*/

    private List<Exercise> exercise;
    private int numOfPages;
    private int startPageNum;
    private String storagePath;
    private String fileName;
    private String downloadLink;

    @ServerTimestamp
    private Date uploadDate;

    public WebViewModel() {
    }

    /*public WebViewModel(String link, String filePath, String downloadLink, String description, List<String> words) {
        this.link = link;
        this.filePath = filePath;
        this.downloadLink = downloadLink;
        this.description = description;
        this.words = words;
    }*/

    /*public WebViewModel(String docLink, String description, List<String> downloadLinks, int startPageNumber, int numberOfPages) {
        this.docLink = docLink;
        this.description = description;
        this.downloadLinks = downloadLinks;
        this.startPageNumber = startPageNumber;
        this.numberOfPages = numberOfPages;
    }*/

    public WebViewModel(List<Exercise> exercise, int numOfPages, int startPageNum, String storagePath,
                        String fileName, String downloadLink) {
        this.exercise = exercise;
        this.numOfPages = numOfPages;
        this.startPageNum = startPageNum;
        this.storagePath = storagePath;
        this.fileName = fileName;
        this.downloadLink = downloadLink;
    }

    public WebViewModel(List<Exercise> exercise, int numOfPages, int startPageNum, String storagePath) {
        this.exercise = exercise;
        this.numOfPages = numOfPages;
        this.startPageNum = startPageNum;
        this.storagePath = storagePath;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public List<Exercise> getExercise() {
        return exercise;
    }

    public void setExercise(List<Exercise> exercise) {
        this.exercise = exercise;
    }

    public int getNumOfPages() {
        return numOfPages;
    }

    public void setNumOfPages(int numOfPages) {
        this.numOfPages = numOfPages;
    }

    public int getStartPageNum() {
        return startPageNum;
    }

    public void setStartPageNum(int startPageNum) {
        this.startPageNum = startPageNum;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }
}
