package com.jsf.dao;

import com.jsf.entities.TaskDetail;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class TaskDetailsDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(TaskDetail taskDetail) {
        entityManager.persist(taskDetail);
    }

    public TaskDetail findById(int id) {
        return entityManager.find(TaskDetail.class, id);
    }

    public TaskDetail findByTaskId(int taskId) {
        try {
            return entityManager.createQuery("SELECT td FROM TaskDetail td WHERE td.task.id = :taskId", TaskDetail.class)
                                .setParameter("taskId", taskId)
                                .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public void delete(TaskDetail taskDetail) {
        if (entityManager.contains(taskDetail)) {
            entityManager.remove(taskDetail);
        } else {
            entityManager.remove(entityManager.merge(taskDetail));
        }
    }

    public void deleteByTaskId(int taskId) {
        entityManager.createQuery("DELETE FROM TaskDetail td WHERE td.task.id = :taskId")
                     .setParameter("taskId", taskId)
                     .executeUpdate();
    }
}
