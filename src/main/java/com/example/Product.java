package com.example;

import jakarta.persistence.*;
import java.util.ArrayList; // 必须导入这个，否则报错“找不到符号”
import java.util.List;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String price;
    private String imageUrl;

    @Column(length = 1000)
    private String aiPrediction;

    // 只保留这一个 reviews 定义，并整合所有注解
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> reviews = new ArrayList<>();

    // --- Getter 和 Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getReviews() { return reviews; }
    public void setReviews(List<String> reviews) { this.reviews = reviews; }

    public String getAiPrediction() { return aiPrediction; }
    public void setAiPrediction(String aiPrediction) { this.aiPrediction = aiPrediction; }
}