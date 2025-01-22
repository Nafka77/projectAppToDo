package com.jsf.dao;

import com.jsf.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserDAO {

    @PersistenceContext
    private EntityManager em;

    // Metoda do rejestracji użytkownika
    public void registerUser(User user) {
        try {
            em.persist(user); // Zapisujemy użytkownika w bazie
        } catch (Exception e) {
            throw new RuntimeException("Błąd rejestracji użytkownika: " + e.getMessage(), e);
        }
    }

    // Metoda do znalezienia użytkownika po nazwie użytkownika
    public User findUserByUsername(String username) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                     .setParameter("username", username)
                     .getSingleResult();
        } catch (Exception e) {
            return null;  // Jeśli użytkownik nie został znaleziony, zwracamy null
        }
    }

    // Metoda do aktualizacji danych użytkownika
    public void updateUser(User user) {
        try {
            em.merge(user); // Zaktualizuj użytkownika w bazie danych
        } catch (Exception e) {
            throw new RuntimeException("Błąd aktualizacji użytkownika: " + e.getMessage(), e);
        }
    }
}

