package com.example.hoalan.load;

import com.example.hoalan.models.Flower;
import com.example.hoalan.repository.FlowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class Loaders {

    @Autowired
    ElasticsearchOperations operations;
    @Autowired
    FlowerRepository flowerRepository;
    @PostConstruct
    public void loadAll(){

        operations.putMapping(Flower.class);
        System.out.println("Loading data");
        flowerRepository.save(getData());
        System.out.println("Loading completed");
        
    }

    private List<Flower> getData() {

        List<Flower> flowers = new ArrayList<>();
        flowers.add(new Flower("", "", ""))
    }
}
