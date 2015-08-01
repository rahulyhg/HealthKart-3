package com.tarunsoft.healthkartapp.modal;

public class FeedItem {
    private int id;
    private String name, status, image, profilePic, brandName, url, brandInstName, brandCategory;

    public FeedItem() {
    }


    public FeedItem(int id, String name, String status, String image,
                    String profilePic, String brandName, String url,
                    String brandInstName, String brandCategory) {
        super();
        this.id = id;
        this.name = name;
        this.status = status;
        this.image = image;
        this.profilePic = profilePic;
        this.brandName = brandName;
        this.url = url;
        this.brandInstName = brandInstName;
        this.brandCategory = brandCategory;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBrandInstName() {
        return brandInstName;
    }

    public void setBrandInstName(String brandInstName) {
        this.brandInstName = brandInstName;
    }

    public String getBrandCategory() {
        return brandCategory;
    }

    public void setBrandCategory(String brandCategory) {
        this.brandCategory = brandCategory;
    }


}
