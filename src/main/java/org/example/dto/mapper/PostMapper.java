package org.example.dto.mapper;

import org.example.dto.PostPreviewDto;
import org.example.model.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PostMapper {
    @Mapping(source = "post.id", target = "postId")
    @Mapping(source = "post.userId", target = "userId")
    @Mapping(source = "post.title",target = "title")
    @Mapping(source = "post.content",target = "content",qualifiedByName = "shortContent")
    @Mapping(source = "post.createdAt",target = "createdAt")
    @Mapping(source = "username", target = "username")
    @Mapping(source = "avatarId", target = "avatarId")
    @Mapping(source = "likeCount", target = "likeCount")
    PostPreviewDto toDto(Post post, String username, Long avatarId, long likeCount);
    @Named("shortContent")
    static String shortContent(String content) {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}
