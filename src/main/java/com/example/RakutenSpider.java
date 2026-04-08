package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class RakutenSpider {

    @Autowired
    private ProductRepository productRepository;

    // 品类ID与对应的文件名映射
    private static final Map<String, String> GENRE_FILE_MAP = Map.of(
            "0",      "rakuten_overall.html",
            "551167", "rakuten_electronics.html",
            "100371", "rakuten_fashion.html",
            "216129", "rakuten_beauty.html"
    );

    public List<Map<String, String>> fetchTrendingData(String genre) {
        List<Map<String, String>> products = new ArrayList<>();
        try {
            // 1. 清理该品类的旧数据
            productRepository.deleteByGenre(genre);

            // 2. 定位资源文件
            String fileName = GENRE_FILE_MAP.getOrDefault(genre, "rakuten_overall.html");
            ClassPathResource mainResource = new ClassPathResource("static_data/" + fileName);

            if (!mainResource.exists()) {
                System.err.println("❌ [错误] 找不到文件: src/main/resources/static_data/" + fileName);
                return products;
            }

            System.out.println("📂 [读取] 正在解析真实榜单: " + fileName + " (大小: " + mainResource.contentLength() + " bytes)");

            try (InputStream is = mainResource.getInputStream()) {
                // 使用 UTF-8 解析，baseUri 设为空
                Document doc = Jsoup.parse(is, "UTF-8", "");

                // --- 强力选择器升级 ---
                // 尝试抓取：乐天标准的 class、包含 itemName 的任何 class、或者包含商品链接的 a 标签
                Elements items = doc.select(".rnkRanking_itemName a, [class*='itemName'] a, .itemName a, a[href*='item.rakuten.co.jp']");

                if (items.isEmpty()) {
                    System.err.println("⚠️ [警告] 抓取不到商品！HTML 结构可能已变。");
                    // 调试：打印前 300 个字符看看内容
                    System.out.println("DEBUG HTML: " + doc.text().substring(0, Math.min(300, doc.text().length())));
                }

                int limit = Math.min(items.size(), 5);
                for (int i = 0; i < limit; i++) {
                    Element itemAnchor = items.get(i);
                    String productName = itemAnchor.text().trim();

                    // 过滤掉过短的无意义字符
                    if (productName.length() < 2) continue;

                    int rank = i + 1;
                    System.out.println("🔍 [解析成功] 第 " + rank + " 名: " + productName);

                    List<String> reviews = new ArrayList<>();

                    // 3. 处理详情页评价
                    String detailPath = "static_data/detail/" + genre + "/" + rank + ".html";
                    ClassPathResource detailResource = new ClassPathResource(detailPath);

                    if (detailResource.exists()) {
                        try (InputStream dis = detailResource.getInputStream()) {
                            Document docDetail = Jsoup.parse(dis, "UTF-8", "");
                            // 抓取乐天评价文本常用的选择器
                            Elements rews = docDetail.select(".revRvwUserReviewSctn p, .review_item_text, [class*='reviewText']");
                            for (Element r : rews) {
                                String text = r.text().trim();
                                if (text.length() > 5 && reviews.size() < 3) {
                                    reviews.add(text);
                                }
                            }
                        }
                    }

                    // 4. 保存到数据库
                    Product product = new Product();
                    product.setName(productName);
                    product.setPrice("Ranking Price");
                    product.setReviews(reviews.isEmpty() ? List.of("暂无详细评价") : reviews);
                    product.setGenre(genre);
                    product.setAiPrediction(buildAdvice(productName, genre));

                    productRepository.save(product);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ [系统崩溃] 爬虫异常: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    private String buildAdvice(String productName, String genre) {
        return "【AI 建议】根据「" + productName + "」的市场反馈，该产品在当前品类中表现强劲，建议关注其价格波动。";
    }
}