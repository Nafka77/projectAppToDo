package com.jsfcourse.converter;

import com.jsf.dao.CategoryDAO;
import com.jsf.entities.Category;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;

@FacesConverter(forClass = Category.class)
public class CategoryConverter implements Converter<Category> {

    @Inject
    private CategoryDAO categoryDAO= new CategoryDAO();

    @Override
    public Category getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return categoryDAO.findById(Integer.parseInt(value));
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Category category) {
        if (category == null) {
            return ""; // Jeśli kategoria jest null, zwracamy pusty string
        }
        return String.valueOf(category.getId()); // Zawsze zwracamy id jako String
    }

}
