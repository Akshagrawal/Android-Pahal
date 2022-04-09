package com.pahaltutorials.pahaltutorials.model;

import java.util.List;

public class DataModel {

    private List<String> content;
    private List<String> links;

    public DataModel(){

    }

    public DataModel(List<String> content, List<String> links) {
        this.content = content;
        this.links = links;
    }

    public List<String> getContent() {
        return content;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public List<String> getLinks() {
        return links;
    }

    public void setLinks(List<String> links) {
        this.links = links;
    }
}
