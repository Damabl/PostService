package org.example.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "topic")
@Getter
@Setter
public class TopicProperties {
    private String likePost;
    private String commentPost;
    private String createdPost;
}