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
import java.util.List;
import java.util.stream.Collectors;

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
    private int pageSize = 5;
    private int currentPage = 0;

    private List<User> filteredUsers; // Lista przefiltrowanych użytkowników
    private String filterKeyword; // Zmienna do przechowywania słowa kluczowego do filtrowania

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

    public boolean isAdmin() {
        return loggedInUser != null && "ADMIN".equals(loggedInUser.getRole().name());  // Sprawdzamy, czy rola to ADMIN
    }

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

    // Getter i setter dla filterKeyword
    public String getFilterKeyword() {
        return filterKeyword;
    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

    // Metoda filtrowania użytkowników
    public void filterUsers() {
        if (filterKeyword != null && !filterKeyword.isEmpty()) {
            filteredUsers = userDAO.getAllUsers().stream()
                .filter(user -> user.getUsername().contains(filterKeyword) || user.getEmail().contains(filterKeyword))
                .collect(Collectors.toList());
        } else {
            filteredUsers = userDAO.getAllUsers(); // Jeśli brak filtra, zwracamy wszystkich użytkowników
        }
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

        // Sprawdzamy, czy użytkownik istnieje
        User user = userDAO.findUserByUsername(username);
        if (user != null) {
            if (password.equals(user.getPassword())) {
                loggedInUser = user;
                return "home.xhtml?faces-redirect=true"; // Przekierowanie po poprawnym logowaniu
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błędne hasło", ""));
                return null;
            }
        } else {
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

    // Metoda do pobierania użytkowników
    public List<User> getUsers() {
        try {
            if (filteredUsers == null) {  // Zmienna filteredUsers będzie przechowywać pobranych użytkowników
                filteredUsers = userDAO.getAllUsers(); // Pobranie wszystkich użytkowników
            }
            return filteredUsers; // Zwrócenie przefiltrowanej listy użytkowników
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd pobierania użytkowników: " + e.getMessage(), ""));
            return null;
        }
    }
    public void deleteUser(User user) {
        FacesContext context = FacesContext.getCurrentInstance();
        
        try {
            userDAO.deleteUser(user);  // Usuwamy użytkownika z bazy
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Użytkownik został usunięty.", ""));
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd usuwania użytkownika: " + e.getMessage(), ""));
        }

        // Przeładuj listę użytkowników
        filterUsers();  // Możemy tutaj ponownie zastosować filtrację, jeśli jest aktywna
    }

    // Getter dla filteredUsers
    public List<User> getFilteredUsers() {
        return filteredUsers;
    }
    
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    public void nextPage() {
        currentPage++;
    }
}
