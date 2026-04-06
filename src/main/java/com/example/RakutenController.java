package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.*;

@Controller
public class RakutenController {

    @Autowired
    private RakutenSpider rakutenSpider;

    @Autowired
    private ProductRepository productRepository; // 核心：注入数据库工具

    @GetMapping("/")
    public String index(@RequestParam(name = "lang", defaultValue = "cn") String lang, Model model) {
        model.addAttribute("lang", lang);
        return "index";
    }

    @GetMapping("/analysis")
    public String analysis(
            @RequestParam(name = "genre", defaultValue = "0") String genre,
            @RequestParam(name = "lang", defaultValue = "cn") String lang,
            Model model) {

        // 1. 更加彻底的清空方式
        productRepository.deleteAllInBatch(); // 批量删除，比一条条删快得多

        // 2. 开始抓取（只抓5个）
        rakutenSpider.fetchTrendingData(genre);

        // 3. 获取最新的 5 个
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("lang", lang);

        return "analysis";
    }


}