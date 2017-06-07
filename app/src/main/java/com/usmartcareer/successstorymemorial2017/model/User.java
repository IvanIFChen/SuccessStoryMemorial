package com.usmartcareer.successstorymemorial2017.model;

/**
 * Created by Tunabutter on 6/2/2016.
 */
public class User {
    public String name;
    public String title;
    public String content;
    public String hint;
    public String key;
    public int number;

    public User() { };

    public User(String name, String title, String content, String hint, String key, int number) {
        this.name = name;
        this.title = title;
        this.content = content;
        this.hint = hint;
        this.key = key;
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "User [name= " + this.name +
                ", title= " + this.title +
                ", content= " + this.content +
                ", hint= " + this.hint +
                ", key= " + this.key +
                ", number= " + this.number +
                "]";
    }
}
