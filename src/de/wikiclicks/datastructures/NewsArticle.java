package de.wikiclicks.datastructures;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewsArticle implements Serializable {
    private String title;
    private String content;

    private String source;
    private String published;

    @JsonProperty(value = "media-type")
    private String mediaType;

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }

    public String getPublished() {
        return published;
    }

    public String getMediaType() {
        return mediaType;
    }

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();

        builder.append(title).append(":")
                .append(source).append(", ")
                .append(published);

        return builder.toString();
    }
}
