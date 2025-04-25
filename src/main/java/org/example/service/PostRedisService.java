package org.example.service;

import org.example.model.entity.Post;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PostRedisService {

    private static final String KEY = "popular_posts";
    private final RedisTemplate<String, Object> redisTemplate;
    private final ZSetOperations<String, Object> zSetOps;

    public PostRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOps = redisTemplate.opsForZSet();
    }

    public void savePost(Post post, double score) {
        zSetOps.add(KEY, post, score);
    }

    public void clearTopPosts() {
        redisTemplate.delete(KEY);
    }

    public void cacheSearchResults(String key, List<Post> posts) {
        redisTemplate.opsForValue().set(key, posts, 300, TimeUnit.SECONDS); // 5 минут
    }

    @SuppressWarnings("unchecked")
    public List<Post> getCachedSearchResults(String key) {
        return (List<Post>) redisTemplate.opsForValue().get(key);
    }
}
