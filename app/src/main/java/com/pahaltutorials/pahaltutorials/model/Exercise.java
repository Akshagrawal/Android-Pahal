package com.pahaltutorials.pahaltutorials.model;

public class Exercise {

    private int ansPageNum;
    private String num;
    private int pageNum;

    public Exercise() {
    }

    public Exercise(int ansPageNum, String num, int pageNum) {
        this.ansPageNum = ansPageNum;
        this.num = num;
        this.pageNum = pageNum;
    }

    public int getAnsPageNum() {
        return ansPageNum;
    }

    public void setAnsPageNum(int ansPageNum) {
        this.ansPageNum = ansPageNum;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }
}
