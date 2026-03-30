package fr.fms.spring_things;

import fr.fms.spring_things.entities.Article;
import fr.fms.spring_things.entities.Category;
import fr.fms.spring_things.services.DataService;
import fr.fms.spring_things.utils.AppLogger;
import fr.fms.spring_things.utils.Helpers;
import fr.fms.spring_things.utils.Helpers.LogLevel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class SpringThingsApplication implements CommandLineRunner {

	@Autowired
	private DataService dataService;

	public static void main(String[] args) {
		SpringApplication.run(SpringThingsApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		try (Scanner scanner = new Scanner(System.in)) {

			// 🚀
			AppLogger.setVerbose(true);
			Helpers.title("🌿MYSDF SPRING THINGS🌿");
			AppLogger.info("Démarrage du système de configuration...");

			// Spring chhuuuuuutttt
			dataService.changeLogLevel("ERROR");

			// Premier tour de config
			runConfigurationCycle(scanner);

			// Fin de la config, on fait des vrais trucs
			Helpers.spacer();
			Helpers.printlnColor(Helpers.GREEN, "**************************************************");
			Helpers.printlnColor(Helpers.GREEN, "   CONFIGURATION TERMINEE ");
			Helpers.printlnColor(Helpers.GREEN, "**************************************************");

			displayMainMenu(scanner);
		}
	}

	/**
	 * Configuration menu
	 */
	private void runConfigurationCycle(Scanner scanner) {
		int step = 1;

		while (step <= 3) {
			Helpers.spacer();
			Helpers.printlnColor(Helpers.CYAN, "======= ETAPE [" + step + "/3] =======");
			Helpers.spacer();

			if (step == 1) {
				boolean showSql = Helpers.confirm(scanner, "> Activer les logs SQL ?");
				dataService.setSqlVisibility(showSql);
				step++;

			} else if (step == 2) {
				Helpers.printlnColor(Helpers.YELLOW, "1:🔴 ERROR | 2:🟡 WARN | 3:🔵 INFO | 4:🟣 DEBUG | 0:Retour");
				Integer choice = Helpers.askIntOrBack(scanner, "LOG LEVEL : ");

				if (choice == null || choice == 0) {
					if (step > 1)
						step--;
					continue;
				}

				LogLevel selectedLevel = LogLevel.fromId(choice);
				dataService.changeLogLevel(selectedLevel.name());
				step++;

			} else if (step == 3) {
				boolean reset = Helpers.confirm(scanner, "> Réinitialiser la BDD ?");

				if (reset) {
					AppLogger.info("Nettoyage en cours...🧹");
					dataService.clearData();
					dataService.generateMockData();
					AppLogger.rocket("BDD toute neuve");
				} else {
					AppLogger.info("On garde les données actuelles.");
				}
				step++;
			}
		}
	}

	/**
	 * Ajouter un article (1.5/1.7)
	 */
	private void adminAddArticle(Scanner scanner) {
		Helpers.title("➕ AJOUTER UN ARTICLE");

		// Marque
		System.out.print("Marque : ");
		String brand = scanner.nextLine();

		// Description
		System.out.print("Description : ");
		String description = scanner.nextLine();

		// Prix
		int priceInt = Helpers.askInt(scanner, "Prix (en Flouz) : ");
		double price = (double) priceInt;

		// Catégorie
		List<Category> categories = dataService.getAllCategories();

		Integer catId = Helpers.paginateWithSelection(
				scanner,
				"Choisir une catégorie pour l'article",
				categories,
				10,
				sub -> sub.forEach(c -> System.out.printf("[%d] %s%n", c.getId(), c.getName())),
				id -> categories.stream().anyMatch(c -> c.getId().equals((long) id)),
				true);

		if (catId != null) {
			Category selectedCat = categories.stream()
					.filter(c -> c.getId().equals((long) catId))
					.findFirst()
					.orElse(null);

			// Assemblage
			Article newArticle = new Article(null, description, brand, price, selectedCat);

			// Save
			dataService.saveArticle(newArticle);
		} else {
			AppLogger.warn("Ajout annulé : aucune catégorie sélectionnée.");
		}
	}

	/**
	 * Recherche & Lecture
	 */
	private void adminSearchAndRead(Scanner scanner) {
		Helpers.title("🔍 RECHERCHER UN ARTICLE");

		System.out.print("Mot-clé à rechercher (Marque/Description) : ");
		String keyword = scanner.nextLine();
		List<Article> results = dataService.searchArticles(keyword);

		Integer selectedId = Helpers.paginateWithSelection(
				scanner,
				"Recherche - Résultats pour : " + "'" + keyword + "'"
						+ " - ",
				results,
				5,
				subList -> subList
						.forEach(a -> System.out.printf("%s%-4d %s| %-10s | %-15s | %s%10.2f Flouz %s| %s%s%s%n",
								Helpers.YELLOW, a.getId(), Helpers.RESET,
								a.getBrand(), a.getDescription(),
								Helpers.GREEN, a.getPrice(), Helpers.RESET,
								Helpers.CYAN, a.getCategory().getName(), Helpers.RESET)),
				id -> results.stream().anyMatch(a -> a.getId().equals((long) id)),
				true);

		if (selectedId != null) {
			displayArticleDetail((long) selectedId, scanner);
		}
	}

	/**
	 * Suppression
	 */
	private void adminDeleteArticle(Scanner scanner) {
		Helpers.title("❌ SUPPRIMER UN ARTICLE");

		System.out.print("Entrez le nom de l'article à SUPPRIMER : ");
		String keyword = scanner.nextLine();
		List<Article> results = dataService.searchArticles(keyword);

		Integer selectedId = Helpers.paginateWithSelection(
				scanner,
				"Suppression - Résultats pour : " + "'" + keyword + "'" + " - ",
				results,
				5,
				subList -> subList
						.forEach(a -> System.out.printf("%s%-4d %s| %-10s | %-15s | %s%10.2f Flouz %s| %s%s%s%n",
								Helpers.YELLOW, a.getId(), Helpers.RESET,
								a.getBrand(), a.getDescription(),
								Helpers.GREEN, a.getPrice(), Helpers.RESET,
								Helpers.CYAN, a.getCategory().getName(), Helpers.RESET)),
				id -> results.stream().anyMatch(a -> a.getId().equals((long) id)),
				true);

		if (selectedId != null) {
			if (Helpers.confirm(scanner,
					"🔥 ATTENTION : Voulez-vous vraiment supprimer l'article : " + selectedId + " ?")) {
				dataService.deleteArticle((long) selectedId);
			}
		}
	}

	private void adminUpdateArticle(Scanner scanner) {
		Helpers.title("🔧 MODIFIER UN ARTICLE");

		// Recherche par mot-clef
		System.out.print("recherche par mot-clef : ");
		String keyword = scanner.nextLine();
		List<Article> articles = dataService.searchArticles(keyword);

		if (articles.isEmpty())
			return;

		Integer selectedId = Helpers.paginateWithSelection(
				scanner,
				"Choisir l'article à modifier",
				articles,
				10,
				sub -> sub.forEach(
						a -> System.out.printf("[%d] %-12s | %s%n", a.getId(), a.getBrand(), a.getDescription())),
				val -> articles.stream().anyMatch(a -> a.getId().equals((long) val)),
				true);

		if (selectedId != null) {
			Article article = dataService.getArticleById((long) selectedId);
			Helpers.spacer();
			System.out.println("--- Édition de l'article n°" + selectedId + " ---");

			// MARQUE
			System.out.print("Nouvelle Marque [" + article.getBrand() + "] : ");
			String brand = scanner.nextLine();
			if (!brand.isBlank())
				article.setBrand(brand);

			// DESCRIPTION
			System.out.print("Nouvelle Description/Nom [" + article.getDescription() + "] : ");
			String desc = scanner.nextLine();
			if (!desc.isBlank())
				article.setDescription(desc);

			// PRIX
			System.out.print("Nouveau Prix [" + article.getPrice() + "] : ");
			String priceStr = scanner.nextLine();
			if (!priceStr.isBlank()) {
				try {
					article.setPrice(Double.parseDouble(priceStr));
				} catch (NumberFormatException e) {
					AppLogger.error("Prix non valide, on garde l'ancien.");
				}
			}

			dataService.saveArticle(article);
		}
	}

	private void adminAddCategory(Scanner scanner) {
		Helpers.title("➕ NOUVELLE CATÉGORIE");

		System.out.print("Nom : ");
		String name = scanner.nextLine();

		if (!name.isBlank()) {
			Category cat = new Category();
			cat.setName(name);
			dataService.saveCategory(cat);
		} else {
			AppLogger.error("Le nom ne peut pas être vide !");
		}
	}

	private void adminDeleteCategory(Scanner scanner) {
		Helpers.title("❌ SUPPRIMER UNE CATEGORIE");

		List<Category> categories = dataService.getAllCategories();
		Integer id = Helpers.paginateWithSelection(
				scanner, "Supprimer une catégorie", categories, 10,
				sub -> sub.forEach(c -> System.out.printf("[%d] %s%n", c.getId(), c.getName())),
				val -> categories.stream().anyMatch(c -> c.getId().equals((long) val)), true);

		if (id != null && Helpers.confirm(scanner, "Confirmer la suppression de la catégorie " + id + " ?")) {
			dataService.deleteCategory((long) id);
		}
	}

	private void adminFilterByCategory(Scanner scanner) {
		Helpers.title("🔽 FILTRER PAR CATEGORIE");

		List<Category> categories = dataService.getAllCategories();
		Integer catId = Helpers.paginateWithSelection(
				scanner, "Filtrer par catégorie", categories, 10,
				sub -> sub.forEach(c -> System.out.printf("[%d] %s%n", c.getId(), c.getName())),
				val -> categories.stream().anyMatch(c -> c.getId().equals((long) val)), true);

		if (catId != null) {
			List<Article> results = dataService.getArticlesByCategoryId((long) catId);
			Helpers.title("Articles de la catégorie " + catId);
			if (results.isEmpty()) {
				AppLogger.warn("Aucun article dans cette catégorie.");
			} else {
				// On réutilise ton affichage compact qu'on a fait tout à l'heure !
				results.forEach(
						a -> System.out.printf("%-4d | %-12s | %s%n", a.getId(), a.getBrand(), a.getDescription()));
			}
			System.out.println("\nAppuyez sur Entrée pour continuer...");
			scanner.nextLine();
		}
	}

	private void adminUpdateCategory(Scanner scanner) {
		Helpers.title("🔧 MODIFIER UNE CATEGORIE");
		List<Category> categories = dataService.getAllCategories();
		Integer id = Helpers.paginateWithSelection(
				scanner, "Modifier une catégorie", categories, 10,
				sub -> sub.forEach(c -> System.out.printf("[%d] %s%n", c.getId(), c.getName())),
				val -> categories.stream().anyMatch(c -> c.getId().equals((long) val)), true);

		if (id != null) {
			Category cat = categories.stream().filter(c -> c.getId().equals((long) id)).findFirst().get();
			System.out.print("Nouveau nom [" + cat.getName() + "] : ");
			String newName = scanner.nextLine();

			if (!newName.isBlank()) {
				cat.setName(newName);
				dataService.saveCategory(cat);
			}
		}
	}

	/**
	 * CONSOLE D'ADMINISTRATION
	 */
	private void displayMainMenu(Scanner scanner) {
		boolean running = true;
		while (running) {
			Helpers.title("CONSOLE D'ADMINISTRATION MYSDF");
			System.out.println("1  : 📋 Afficher tous les articles");
			System.out.println("2  : 🔍 Rechercher un article");
			System.out.println("3  : 🔽 Filtrer les articles par catégorie");
			System.out.println("4  : ⚙️  Paramétres de la console");
			Helpers.title("Admin");
			System.out.println("5  : ➕ Ajouter un article");
			System.out.println("6  : ❌ Supprimer un article");
			System.out.println("7  : 🔧 Modifier un article");
			System.out.println("8  : ➕ Ajouter une catégorie");
			System.out.println("9  : ❌ Supprimer une catégorie");
			System.out.println("10 : 🔧 Modifier une catégorie");
			System.out.println("0  : 🚪 Quitter");

			Integer choice = Helpers.askIntOrBack(scanner, "Choix : ");

			if (choice == null || choice == 0) {
				AppLogger.info("Fermeture de 🌿MYSDF SPRING THINGS🌿... À bientôt Marie-Lorraine !");
				running = false;
			} else {
				switch (choice) {
					case 1 -> {
						Integer id = dataService.listArticlesWithPagination(scanner);
						if (id != null)
							displayArticleDetail((long) id, scanner);
					}
					case 2 -> adminSearchAndRead(scanner);
					case 3 -> adminFilterByCategory(scanner);
					case 4 -> runConfigurationCycle(scanner);
					case 5 -> adminAddArticle(scanner);
					case 6 -> adminDeleteArticle(scanner);
					case 7 -> adminUpdateArticle(scanner);
					case 8 -> adminAddCategory(scanner);
					case 9 -> adminDeleteCategory(scanner);
					case 10 -> adminUpdateCategory(scanner);
					default -> AppLogger.warn("🚧 WIP");
				}
			}
		}
	}

	/**
	 * Affiche les détails d'un article (Exercice 1.1 / 1.2)
	 */
	private void displayArticleDetail(Long id, Scanner scanner) {
		Article a = dataService.getArticleById(id);
		if (a != null) {
			Helpers.spacer();
			Helpers.printlnColor(Helpers.PURPLE, "--- 🔍 FICHE DÉTAILLÉE ---");
			System.out.println("ID        : " + a.getId());
			System.out.println("MARQUE    : " + a.getBrand());
			System.out.println("DESC      : " + a.getDescription());
			System.out.println("PRIX      : " + a.getPrice() + " Flouz");
			System.out.println("CATÉGORIE : " + a.getCategory().getName());
			Helpers.spacer();
			System.out.println("Appuyez sur Entrée pour continuer...");
			scanner.nextLine();
		}
	}

}
