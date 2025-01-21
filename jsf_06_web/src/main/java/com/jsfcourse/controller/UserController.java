package com.jsfcourse.controller;

import com.jsf.dao.UserDAO;
import com.jsf.entities.User;
import com.jsf.entities.Role;  // Dodaj import dla Role

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
    
    @NotEmpty
    private String email;
    
    // Zmieniamy z String na Role
    private Role role = Role.USER;  // Domyślnie ustawiamy rolę na USER
    
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
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
        FacesContext context = FacesContext.getCurrentInstance();

        // Sprawdzamy, czy użytkownik już istnieje
        User existingUser = userDAO.findUserByUsername(username);
        if (existingUser != null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik o tej nazwie już istnieje", ""));
            return null;
        }

        // Sprawdzamy, czy hasła się zgadzają
        if (!password.equals(confirmPassword)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hasła się nie zgadzają", ""));
            return null;
        }

        // Tworzymy nowego użytkownika
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password); // W produkcji hasło należy haszować
        newUser.setRole(role);  // Ustawiamy rolę na podstawie wartości enum
        newUser.setCreatedAt(new Timestamp(System.currentTimeMillis())); // Ustawienie daty rejestracji

        // Rejestrujemy użytkownika
        try {
            userDAO.registerUser(newUser);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Rejestracja zakończona sukcesem! Możesz się teraz zalogować.", ""));
            return "login.xhtml?faces-redirect=true"; // Przekierowanie do strony logowania
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd rejestracji: " + e.getMessage(), ""));
            return null;
        }
    }




    // Logowanie użytkownika
    public String login() {
        User user = userDAO.findUserByUsername(username);

        if (user != null) {
            if (password.equals(user.getPassword())) {
                loggedInUser = user;
                return "home.xhtml?faces-redirect=true"; // Przekierowanie po poprawnym logowaniu
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

    // Wylogowanie użytkownika
    public String logout() {
        loggedInUser = null;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Wylogowano pomyślnie!", ""));
        return "index.xhtml?faces-redirect=true"; // Przekierowanie na stronę główną
    }
}


