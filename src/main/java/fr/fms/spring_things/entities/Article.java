package fr.fms.spring_things.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity // Spring : "Table SQL Article"
@Data // Crée les getters & setters auto
@NoArgsConstructor // Constructeur vide
@AllArgsConstructor // Crée un constructeur avec tous les champs
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrément
    private Long id;

    private String description;
    private String brand;
    private double price;

    @ManyToOne
    @JoinColumn(name = "category_id") // Optionnel mais utile pour garder la main sur le nom de la colonne
    private Category category;
}