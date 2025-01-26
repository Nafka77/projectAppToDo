package com.jsf.dao;

import java.util.List;
import com.jsf.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class UserDAO {

    @PersistenceContext
    private EntityManager em;
    
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

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
    // Metoda do usuwania użytkownika
    public void deleteUser(User user) {
        try {
            em.remove(em.merge(user));  // Usuwamy użytkownika z bazy danych
        } catch (Exception e) {
            throw new RuntimeException("Błąd usuwania użytkownika: " + e.getMessage(), e);
        }
    }
}

