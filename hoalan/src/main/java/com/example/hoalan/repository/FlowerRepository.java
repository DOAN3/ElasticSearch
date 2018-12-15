package com.example.hoalan.repository;

import com.example.hoalan.models.Flower;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface FlowerRepository extends ElasticsearchRepository<Flower, Long> {
}
