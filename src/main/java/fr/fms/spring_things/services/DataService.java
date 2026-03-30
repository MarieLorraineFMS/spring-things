package fr.fms.spring_things.services;

import fr.fms.spring_things.dao.ArticleRepository;
import fr.fms.spring_things.dao.CategoryRepository;
import fr.fms.spring_things.entities.Article;
import fr.fms.spring_things.entities.Category;
import fr.fms.spring_things.utils.AppLogger;
import fr.fms.spring_things.utils.Helpers;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@Service
public class DataService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Recup "robinets" des logs
    private final Logger rootLogger = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
    private final Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
    private final Logger binderLogger = (Logger) LoggerFactory.getLogger("org.hibernate.type.descriptor.sql");

    /**
     * Log global & log maison
     */
    public void changeLogLevel(String levelStr) {
        Level newLevel = Level.toLevel(levelStr.toUpperCase(), Level.INFO);
        rootLogger.setLevel(newLevel);

        // ERROR || WARN, moins de "blabla" en général
        if (newLevel.isGreaterOrEqual(Level.WARN)) {
            AppLogger.setVerbose(false); // Réduction des logs "maison"
            AppLogger.setVerbose(true); // Activation des logs "maison"
        }

        Helpers.printlnColor(Helpers.GREEN, ">>> Niveau de log : " + Helpers.formatWithLevelColors(newLevel, levelStr));
        Helpers.spacer();
    }

    /**
     * Log SQL
     */
    public void setSqlVisibility(boolean visible) {
        if (visible) {
            sqlLogger.setLevel(Level.DEBUG);
            binderLogger.setLevel(Level.TRACE);
            AppLogger.warn("LOGS SQL ACTIFS");
            Helpers.spacer();

        } else {
            sqlLogger.setLevel(Level.OFF);
            binderLogger.setLevel(Level.OFF);
            AppLogger.warn("LOGS SQL INACTIFS");
            Helpers.spacer();

        }
    }

    // Nettoyage
    public void clearData() {
        articleRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // MOCK DATA
    public void generateMockData() {
        Random random = new Random();
        List<Category> categories = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            categories.add(categoryRepository.save(new Category(null, "Cat" + i, null)));
        }

        for (int i = 1; i <= 50; i++) {
            int catIdx = random.nextInt(categories.size());
            Category randomCat = categories.get(catIdx);

            String priceString = i + "." + i;
            int randomPow = random.nextInt(4) - 1;
            double rawPrice = Double.parseDouble(priceString) * Math.pow(10, randomPow);
            double finalPrice = Math.round(rawPrice * 100.0) / 100.0;

            articleRepository.save(new Article(null, "Description" + i, "Marque" + i, finalPrice, randomCat));
        }

        // Verif
        System.out.println("");
        Helpers.title("VERIF");
        System.out.println("");
        articleRepository.findAll().forEach(a -> {
            // %-10s : (%)data (-)aligné à gauche de (10)10 (s)string (marques)
            // %-15s : (%)data (-)aligné à gauche de (10)10 (s)string (description)
            // %10.2f : (%)data (10)10 espaces pour (2f)float avec 2 décimales (prix)
            // Flouz : currency
            // %s : (%)data (s)string carac (catégorie)
            // %n : saut de ligne
            System.out.printf("%-10s | %-15s | %10.2f Flouz | %s%n",
                    a.getBrand(), a.getDescription(), a.getPrice(), a.getCategory().getName());
        });

        long nbCategories = categoryRepository.count();
        long nbArticles = articleRepository.count();

        System.out.println("");
        System.out.println("");

        String nbCatColor = Helpers.formatWithLevelColors("INFO", Long.toString(nbCategories));
        String nbArticleColor = Helpers.formatWithLevelColors("INFO", Long.toString(nbArticles));
        AppLogger.info("-> Taille 'Category' : " + nbCatColor);
        AppLogger.info("-> Taille 'Articles' : " + nbArticleColor);

        System.out.println("");
        System.out.println("//////////////////////////////////////////////////");
        System.out.println("");
    }

    public Integer listArticlesWithPagination(Scanner sc) {
        List<Article> allArticles = articleRepository.findAll();

        return Helpers.paginateWithSelection(
                sc,
                "Catalogue des Articles",
                allArticles,
                5, // Taille de page
                (List<Article> subList) -> {
                    subList.forEach(a -> {
                        System.out.printf("%s%-4d %s| %-12s | %s%n",
                                Helpers.YELLOW, a.getId(), Helpers.RESET,
                                a.getBrand(), a.getDescription());
                    });
                },
                (id) -> articleRepository.existsById((long) id), // Vérif ID
                true // Help
        );
    }

    /////////////////////////////// CRUD ///////////////////////////////

    ///////////////// ARTICLES ////////////////////////
    /**
     * Récupère tous les articles
     */
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    // 1.4 : Supprimer
    public void deleteArticle(Long id) {
        if (articleRepository.existsById(id)) {
            articleRepository.deleteById(id);
            AppLogger.success("Article " + id + " supprimé avec succès.");
        } else {
            AppLogger.error("Impossible de supprimer : l'article " + id + " n'existe pas.");
        }
    }

    // 1.1 / 1.5 : Lire ou Maj
    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    // 1.7 : Créer/Modifier
    public void saveArticle(Article article) {
        articleRepository.save(article); // Save fait "Ajout" si !ID, "Update" si'ID
        AppLogger.success("Article sauvegardé avec succès : " + article.getDescription());
    }

    // 1.3 : Recherche description OU marque
    public List<Article> searchArticles(String keyword) {
        List<Article> results = articleRepository
                .findByDescriptionContainingIgnoreCaseOrBrandContainingIgnoreCase(keyword, keyword);

        if (results.isEmpty()) {
            AppLogger.warn("Aucun article ne correspond à : " + keyword);
        } else {
            AppLogger.info(results.size() + " article(s) trouvé(s) pour : " + keyword);
        }
        return results;
    }

    ///////////////// CATEGORIES ////////////////////////
    /**
     * Récupère toutes les catégories (1.6/1.7)
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Ajouter catégorie (1.6/1.7)
     */
    public void saveCategory(Category category) {
        categoryRepository.save(category);
        AppLogger.success("Catégorie '" + category.getName() + "' sauvegardé avec succès...!");
    }

    public void deleteCategory(Long id) {
        Category cat = categoryRepository.findById(id).orElse(null);
        if (cat != null) {
            // Verif si category est vide
            if (cat.getArticles() != null && !cat.getArticles().isEmpty()) {
                AppLogger.error("Suppression impossible : la catégorie '" + cat.getName()
                        + "' contient encore des articles !");
            } else {
                categoryRepository.deleteById(id);
                AppLogger.success("Catégorie supprimée avec succès.");
            }
        }
    }

    public List<Article> getArticlesByCategoryId(Long id) {
        return articleRepository.findByCategoryId(id);
    }

}
