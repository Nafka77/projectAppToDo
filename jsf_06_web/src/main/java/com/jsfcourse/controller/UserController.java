package com.jsfcourse.controller;

import com.jsf.dao.UserDAO;
import com.jsf.entities.User;
import com.jsf.entities.Role;
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

    private Role role = Role.USER;  // Domyślnie ustawiamy rolę na USER

    private String newPassword;
    private String confirmNewPassword;

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

    // Gettery i settery dla nowych danych
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
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

        // Tworzymy nowego użytkownika (hasło przechowywane jako zwykły tekst)
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(password); // Hasło przechowywane jako tekst
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
        FacesContext context = FacesContext.getCurrentInstance();
        System.out.println("Zaczynam logowanie...");  // Logowanie do konsoli, aby sprawdzić, czy funkcja jest wywoływana

        // Sprawdzamy, czy użytkownik istnieje
        User user = userDAO.findUserByUsername(username);
        if (user != null) {
            System.out.println("Użytkownik znaleziony");  // Logowanie
            if (password.equals(user.getPassword())) {
                loggedInUser = user;
                System.out.println("Zalogowano pomyślnie");  // Logowanie
                return "home.xhtml?faces-redirect=true"; // Przekierowanie po poprawnym logowaniu
            } else {
                // Błędne hasło
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błędne hasło", ""));
                return null;
            }
        } else {
            // Użytkownik nie znaleziony
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik nie znaleziony", ""));
            return null;
        }
    }


    // Wylogowanie użytkownika
    public String logout() {
        loggedInUser = null;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Wylogowano pomyślnie!", ""));
        return "index.xhtml?faces-redirect=true"; // Przekierowanie na stronę główną
    }

    // Zmiana hasła użytkownika
    public String changePassword() {
        FacesContext context = FacesContext.getCurrentInstance();

        // Sprawdzamy, czy nowe hasło i potwierdzenie pasują
        if (!newPassword.equals(confirmNewPassword)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nowe hasła się nie zgadzają", ""));
            return null;
        }

        // Zmieniamy hasło w bazie (hasło pozostaje jako zwykły tekst)
        try {
            loggedInUser.setPassword(newPassword); // Ustawiamy nowe hasło
            userDAO.updateUser(loggedInUser); // Zaktualizuj dane użytkownika w bazie
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Hasło zostało zmienione.", ""));
            return "home.xhtml?faces-redirect=true"; // Przekierowanie na stronę główną
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd zmiany hasła: " + e.getMessage(), ""));
            return null;
        }
    }

    // Edytowanie danych użytkownika (np. email)
    public String updateProfile() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            // Jeśli nowe hasło zostało wprowadzone i jest zgodne z potwierdzeniem
            if (newPassword != null && !newPassword.isEmpty() && newPassword.equals(confirmNewPassword)) {
                loggedInUser.setPassword(newPassword);  // Ustawiamy nowe hasło
            }

            // Aktualizacja e-maila
            loggedInUser.setEmail(email);

            // Aktualizacja użytkownika w bazie danych
            userDAO.updateUser(loggedInUser);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Dane zostały zaktualizowane.", ""));
            return "home.xhtml?faces-redirect=true"; // Powrót na stronę główną
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd aktualizacji danych: " + e.getMessage(), ""));
            return null;
        }
    }
}

