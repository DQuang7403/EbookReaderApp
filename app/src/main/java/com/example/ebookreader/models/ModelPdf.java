package com.example.ebookreader.models;

public class ModelPdf {
    //variables
    String uid, id, title, description, categoryID, url;
    long timestamp;
    //constructor
    public ModelPdf() {
    }
    //contructor with all params

    public ModelPdf(String uid, String id, String title, String description, String categoryID, String url, long timestamp) {
        this.uid = uid;
        this.id = id;
        this.title = title;
        this.description = description;
        this.categoryID = categoryID;
        this.url = url;
        this.timestamp = timestamp;
    }



    /*----Getter/Setters----*/

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(String categoryID) {
        this.categoryID = categoryID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
