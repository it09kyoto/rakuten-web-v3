package com.example;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // ← 追加

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    void deleteByGenre(String genre);        // ← 追加
    List<Product> findByGenre(String genre); // ← 追加
}