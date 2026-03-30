package fr.fms.spring_things.entities;

import java.util.Collection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // @OneToMany : Une catégorie contient plusieurs articles
    // mappedBy="category" : article gère la relation
    // fetch = FetchType.LAZY : chargement des articles seulement si demandé
    // clairement (pour
    // les perfs)
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY) // LAZY -> par défaut mais c'est bien de s'en souvenir
                                                              // fetch = FetchType.EAGER -> pour tout recup
    private Collection<Article> articles;

}