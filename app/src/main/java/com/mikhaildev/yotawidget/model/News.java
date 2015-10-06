package com.mikhaildev.yotawidget.model;


public class News {

    private String newsId;
    private String title;
    private String link;
    private String description;
    private String content;
    private long publicationDate;

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(long publicationDate) {
        this.publicationDate = publicationDate;
    }

    @Override
    public String toString() {
        return "News{" +
                "newsId='" + newsId + '\'' +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", content='" + content + '\'' +
                ", publicationDate=" + publicationDate +
                '}';
    }
}
