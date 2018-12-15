package com.example.hoalan.models;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(indexName = "flowers", type = "flowers", shards = 1)
public class Flower {

    private String name;
    private Long id;
    private Integer price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Flower(String name, Long id, Integer price) {
        this.name = name;
        this.id = id;
        this.price = price;
    }

    public Flower() {
    }
}
