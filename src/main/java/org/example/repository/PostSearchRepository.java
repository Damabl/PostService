package org.example.repository;

import org.example.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostSearchRepository extends ElasticsearchRepository<Post, Long> {
    Page<Post> findByContentContaining(String keyword, Pageable pageable);
}
