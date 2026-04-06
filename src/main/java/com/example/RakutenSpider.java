package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.*;

@Service
public class RakutenSpider {

    @Autowired
    private ProductRepository productRepository;

    public List<Map<String, String>> fetchTrendingData(String genre) {
        List<Map<String, String>> products = new ArrayList<>();
        try {
            // 1. 清理数据库
            productRepository.deleteAll();

            // 2. 读取排行榜镜像 (确保根目录下有 rakuten.html)
            File rankFile = new File("rakuten.html");
            if (!rankFile.exists()) {
                System.err.println("❌ 错误：请确保项目根目录下存在 rakuten.html");
                return products;
            }

            Document doc = Jsoup.parse(rankFile, "UTF-8", "https://ranking.rakuten.co.jp/");
            Elements items = doc.select(".rnkRanking_itemName a");
            int limit = Math.min(items.size(), 5);

            for (int i = 0; i < limit; i++) {
                Element itemAnchor = items.get(i);
                String productName = itemAnchor.text();

                System.out.println("🔍 正在解析真实数据 (" + (i + 1) + "/5): " + productName);

                List<String> reviews = new ArrayList<>();

                // --- 核心逻辑：读取对应的详情页镜像 ---
                // 请将详情页另存为 1.html, 2.html 等，放在项目根目录
                File detailFile = new File((i + 1) + ".html");

                if (detailFile.exists()) {
                    Document docDetail = Jsoup.parse(detailFile, "UTF-8");

                    // 乐天真实评价选择器：针对 PC 版详情页
                    // 常见的类名包含 revRvwUserReviewSctn 或 review_item_text
                    Elements rews = docDetail.select(".revRvwUserReviewSctn p, .review_item_text, .revRvwUserReviewSctn p");

                    for (Element r : rews) {
                        String text = r.text().trim();
                        if (text.length() > 10 && reviews.size() < 3) {
                            reviews.add(text);
                        }
                    }
                    System.out.println("✅ 已从本地镜像提取 " + reviews.size() + " 条真实评价");
                } else {
                    System.err.println("⚠️ 未找到本地镜像 " + detailFile.getName() + "，将使用演示占位符");
                    reviews.add("由于乐天详情页反爬严重，请手动下载详情页并命名为 " + (i+1) + ".html 放入项目根目录以提取真实数据。");
                }

                // 3. 封装并持久化
                Product product = new Product();
                product.setName(productName);
                product.setPrice("真实数据");
                product.setReviews(reviews);

                // 模拟简单的 AI 分析建议
                String advice = "该商品在乐天排行榜位居前列，评论显示用户对“" +
                        (productName.contains("靴") ? "穿着舒适度" : "产品品质") +
                        "”认可度极高。建议关注大促期间的库存水位。";
                product.setAiPrediction(advice);

                productRepository.save(product);
                System.out.println("💾 真实数据已同步至数据库。");
            }
        } catch (Exception e) {
            System.err.println("❌ 发生解析错误: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }
}