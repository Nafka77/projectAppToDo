package com.jsf.dao;

import com.jsf.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Stateless
public class UserDAO {

    @PersistenceContext
    private EntityManager em;

    // Metoda do rejestracji użytkownika
    public void registerUser(User user) {
        try {
            em.persist(user);  // Tworzymy nowego użytkownika w bazie
        } catch (Exception e) {
            throw new RuntimeException("Błąd rejestracji użytkownika", e);
        }
    }

    // Metoda do znalezienia użytkownika po nazwie użytkownika
    public User findUserByUsername(String username) {
        try {
            TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
            query.setParameter("username", username);
            return query.getSingleResult();  // Zwracamy znalezionego użytkownika
        } catch (NoResultException e) {
            return null;  // Jeśli użytkownik nie został znaleziony, zwracamy null
        }
    }
}

