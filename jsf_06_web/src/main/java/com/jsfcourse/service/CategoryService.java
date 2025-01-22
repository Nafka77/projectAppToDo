package com.jsfcourse.service;

import com.jsf.dao.CategoryDAO;
import com.jsf.entities.Category;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;

@Stateless
public class CategoryService {

    @Inject
    private CategoryDAO categoryDAO;

    // Metoda do pobierania wszystkich kategorii
    public List<Category> findAllCategories() {
        return categoryDAO.findAll();
    }

    // Metoda do pobierania kategorii po ID
    public Category findCategoryById(int id) {
        return categoryDAO.findById(id);
    }
}
