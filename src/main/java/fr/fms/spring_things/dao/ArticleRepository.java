package fr.fms.spring_things.dao;

import fr.fms.spring_things.entities.Article;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // Marque qui coûtent plus cher que X
    List<Article> findByBrandAndPriceGreaterThan(String brand, double price);

    // 1.3 : Description || marque
    List<Article> findByDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(String description, String brand);

    // 1.3 : Description && marque
    List<Article> findByDescriptionContainingAndBrandContaining(String description, String brand);

    List<Article> findByCategoryId(Long categoryId);
}
