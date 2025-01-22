package com.jsf.dao;

import com.jsf.entities.Category;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class CategoryDAO {

    @PersistenceContext
    private EntityManager entityManager;
    
    // Dodajemy metodę do pobierania wszystkich kategorii
    public List<Category> getAllCategories() {
        return entityManager.createQuery("SELECT c FROM Category c", Category.class)
                            .getResultList();
    }

    // Pobieranie wszystkich kategorii
    public List<Category> findAll() {
        return entityManager.createQuery("SELECT c FROM Category c", Category.class).getResultList();
    }

    // Znajdź kategorię po ID
    public Category findById(int id) {
        return entityManager.find(Category.class, id);
    }

    // Zapisz nową kategorię
    public void save(Category category) {
        if (category.getId() == 0) {
            entityManager.persist(category);
        } else {
            entityManager.merge(category);
        }
    }

    // Usuń kategorię
    public void delete(int categoryId) {
        Category category = findById(categoryId);
        if (category != null) {
            entityManager.remove(category);
        }
    }
}
