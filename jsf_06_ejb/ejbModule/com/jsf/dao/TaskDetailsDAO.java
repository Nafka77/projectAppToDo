package com.jsf.dao;

import com.jsf.entities.TaskDetail;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class TaskDetailsDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Metoda do zapisywania szczegółów zadania
    public void save(TaskDetail taskDetails) {
        entityManager.persist(taskDetails);
    }

    // Metoda do znajdowania szczegółów zadania po id
    public TaskDetail findByTaskId(int taskId) {
        try {
            return entityManager.createQuery("SELECT td FROM TaskDetail td WHERE td.task.id = :taskId", TaskDetail.class)
                                .setParameter("taskId", taskId)
                                .getSingleResult();
        } catch (Exception e) {
            // Możesz dodać logowanie błędu lub zwrócić null, jeśli zadanie nie istnieje
            return null;
        }
    }

    // Metoda do aktualizacji szczegółów zadania
    public void update(TaskDetail taskDetails) {
        entityManager.merge(taskDetails);
    }

    // Metoda do usuwania szczegółów zadania
    public void delete(TaskDetail taskDetails) {
        // Przed usunięciem upewniamy się, że taskDetails jest zarządzane
        if (entityManager.contains(taskDetails)) {
            entityManager.remove(taskDetails);
        } else {
            entityManager.remove(entityManager.merge(taskDetails));
        }
    }
}
