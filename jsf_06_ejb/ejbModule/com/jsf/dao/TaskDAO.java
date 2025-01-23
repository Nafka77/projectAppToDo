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

    public void saveTask(Task task) {
        // Zamiast porównywać z null, sprawdzamy, czy id jest równe 0
        if (task.getId() != 0) {  // Jeśli id jest różne od 0, oznacza to, że zadanie istnieje w bazie
            entityManager.merge(task);  // Używamy merge do aktualizacji istniejącego zadania
        } else {
            entityManager.persist(task);  // Jeśli id == 0, traktujemy zadanie jako nowe i zapisujemy je
        }
    }


    public Task findTaskById(int id) {
        return entityManager.find(Task.class, id);  // Używamy int jako parametr
    }


    public void deleteTask(Task task) {
        if (entityManager.contains(task)) {
            entityManager.remove(task);
        } else {
            entityManager.remove(entityManager.merge(task));
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
    public void updateTask(Task task) {
        // Sprawdzamy, czy task ma przypisane id (sprawdzamy, czy id != 0)
        if (task.getId() != 0) {  // Jeśli id jest różne od 0, oznacza to, że zadanie już istnieje
            entityManager.merge(task);  // Używamy merge do aktualizacji istniejącego zadania
        } else {
            entityManager.persist(task);  // Jeśli id == 0, traktujemy zadanie jako nowe i zapisujemy je
        }
    }


}
