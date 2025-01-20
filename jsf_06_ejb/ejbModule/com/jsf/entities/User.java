package com.jsf.entities;

import java.io.Serializable;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the users database table.
 * 
 */
@Entity
@Table(name = "users")
@NamedQuery(name = "User.findAll", query = "SELECT u FROM User u")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "created_at")
    private Timestamp createdAt;

    private String email;

    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    private String username;

    // bi-directional many-to-one association to Category
    @OneToMany(mappedBy = "user")
    private List<Category> categories;

    // bi-directional many-to-one association to Task
    @OneToMany(mappedBy = "user")
    private List<Task> tasks;

    // Konstruktor bezparametrowy
    public User() {}

    // Konstruktor z parametrami
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;  // Pamiętaj, aby hasło było zaszyfrowane
        this.role = role;
    }

    // Gettery i settery
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return this.role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Category> getCategories() {
        return this.categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public Category addCategory(Category category) {
        getCategories().add(category);
        category.setUser(this);
        return category;
    }

    public Category removeCategory(Category category) {
        getCategories().remove(category);
        category.setUser(null);
        return category;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Task addTask(Task task) {
        getTasks().add(task);
        task.setUser(this);
        return task;
    }

    public Task removeTask(Task task) {
        getTasks().remove(task);
        task.setUser(null);
        return task;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "', role=" + role + "}";
    }
}
