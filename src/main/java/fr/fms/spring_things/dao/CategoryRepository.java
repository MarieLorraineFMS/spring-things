package fr.fms.spring_things.dao;

import fr.fms.spring_things.entities.Category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 1.6 Asc
    List<Category> findAllByOrderByNameAsc();

    // 1.6 Desc
    List<Category> findAllByOrderByNameDesc();
}