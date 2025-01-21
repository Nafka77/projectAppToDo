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
    public void addTask(Task task) {
        entityManager.persist(task); // Zapisanie zadania w bazie danych
    }
    // Metoda do pobierania zadań z użytkownikiem z załadowanymi taskDetails
    public List<Task> getTasksByUserWithTaskDetails(User user) {
        return entityManager.createQuery(
            "SELECT t FROM Task t LEFT JOIN FETCH t.taskDetails WHERE t.user = :user", Task.class)
            .setParameter("user", user)
            .getResultList();
    }

    // Inna metoda z paginacją i filtracją (jeśli to potrzebne)
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
