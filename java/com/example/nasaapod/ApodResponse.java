package com.example.nasaapod;

import com.google.gson.annotations.SerializedName;

public class ApodResponse {

    private String date;
    private String explanation;
    private String hdurl;

    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("service_version")
    private String serviceVersion;

    private String title;
    private String url;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    private String copyright;

    public String getDate() {
        return date;
    }

    public String getExplanation() {
        return explanation;
    }

    public String getHdurl() {
        return hdurl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getMedia_type() {
        return mediaType;
    }

    public String getThumbnail_url() {
        return thumbnailUrl;
    }
}
