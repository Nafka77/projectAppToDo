package com.jsf.dao;

import com.jsf.entities.Task;
import com.jsf.entities.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class TaskDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Metoda do zapisywania zadania
    public void saveTask(Task task) {
        if (task.getId() == 0) {
            entityManager.persist(task); // Zapisanie nowego zadania
        } else {
            entityManager.merge(task); // Aktualizacja istniejącego zadania
        }
    }

    // Metoda do usuwania zadania
    public void deleteTask(Long taskId) {
        Task task = entityManager.find(Task.class, taskId);
        if (task != null) {
            entityManager.remove(task);  // Usuń zadanie
        }
    }

    // Metoda do pobierania zadań z użytkownikiem z załadowanymi taskDetails
    public List<Task> getTasksByUserWithTaskDetails(User user) {
        return entityManager.createQuery(
            "SELECT t FROM Task t LEFT JOIN FETCH t.taskDetails WHERE t.user = :user", Task.class)
            .setParameter("user", user)
            .getResultList();
    }

    // Metoda z paginacją i filtracją
    public List<Task> getTasksByUserWithPaginationAndFilter(User user, int currentPage, int pageSize, String filterKeyword) {
        return entityManager.createQuery(
            "SELECT t FROM Task t LEFT JOIN FETCH t.taskDetails WHERE t.user = :user AND t.title LIKE :filter", Task.class)
            .setParameter("user", user)
            .setParameter("filter", "%" + filterKeyword + "%")
            .setFirstResult(currentPage * pageSize)
            .setMaxResults(pageSize)
            .getResultList();
    }
}
