package com.usmartcareer.successstorymemorial2017.model;

/**
 * Created by Tunabutter on 5/25/2016.
 */
public class Story {

    private String title;
    private String category;
    private String content;
    private int number; // start with 0

    public Story() { };

    public Story(String title, String category, String content, int number) {
        this.title = title;
        this.category = category;
        this.content = content;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public int getNumber() {
        return number;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "Story [title=" + title +
                ", category=" + category +
                ", content=" + content +
                ", number=" + Integer.toString(number) +
                "]";
    }
}
