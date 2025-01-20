package com.jsfcourse.controller;

import org.mindrot.jbcrypt.BCrypt;

import com.jsf.dao.UserDAO;
import com.jsf.entities.User;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.sql.Timestamp;

@Named
@SessionScoped
public class UserController implements Serializable {

    @Inject
    private UserDAO userDAO;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

    private User loggedInUser;

    // Gettery i settery
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public void setLoggedInUser(User loggedInUser) {
        this.loggedInUser = loggedInUser;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    // Rejestracja użytkownika
    public String register() {
        // Sprawdzamy, czy hasła się zgadzają
        if (!password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hasła się nie zgadzają", ""));
            return null;
        }

        // Sprawdzamy, czy użytkownik już istnieje
        User existingUser = userDAO.findUserByUsername(username);
        if (existingUser != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik o tej nazwie już istnieje", ""));
            return null;
        }

        // Hashowanie hasła przed zapisaniem w bazie
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Tworzymy nowego użytkownika
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(hashedPassword);  // Zapisujemy zaszyfrowane hasło

        // Ustawienie daty rejestracji
        newUser.setCreatedAt(new Timestamp(System.currentTimeMillis()));  // Ustawiamy datę rejestracji

        // Rejestrujemy użytkownika
        try {
            userDAO.registerUser(newUser);  // Wywołanie metody rejestracji w UserDAO
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Rejestracja zakończona sukcesem! Możesz teraz się zalogować.", ""));
            return "login.xhtml?faces-redirect=true";  // Przekierowanie do strony logowania po udanej rejestracji
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd rejestracji. Spróbuj ponownie.", ""));
            return null;
        }
    }

    // Logowanie użytkownika
    public String login() {
        User user = userDAO.findUserByUsername(username);

        if (user != null) {
            // Porównanie hasła w postaci czystego tekstu
            if (password.equals(user.getPassword())) {
                loggedInUser = user;
                return "home.xhtml?faces-redirect=true";
            } else {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błędne hasło", ""));
                return null;
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik nie znaleziony", ""));
            return null;
        }
    }


    // Metoda logout (wylogowanie użytkownika)
    public String logout() {
        loggedInUser = null;  // Usuwamy zalogowanego użytkownika z sesji
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Wylogowano pomyślnie!", ""));
        return "index.xhtml?faces-redirect=true";  // Przekierowanie na stronę główną po wylogowaniu
    }
}

