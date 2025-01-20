/*
 * package com.jsf.dao;
 * 
 * import com.jsf.entities.Task; import com.jsf.entities.User; import
 * jakarta.persistence.EntityManager; import
 * jakarta.persistence.PersistenceContext; import
 * jakarta.persistence.TypedQuery; import java.util.List;
 * 
 * public class TaskDAO {
 * 
 * @PersistenceContext private EntityManager em;
 * 
 * // Zaktualizowana metoda z paginacją public List<Task> findTasksByUser(User
 * user, int page, int pageSize) { String query =
 * "SELECT t FROM Task t WHERE t.user = :user ORDER BY t.dueDate DESC";
 * TypedQuery<Task> typedQuery = em.createQuery(query, Task.class);
 * typedQuery.setParameter("user", user); typedQuery.setFirstResult(page *
 * pageSize); // set the offset for pagination
 * typedQuery.setMaxResults(pageSize); // set the page size return
 * typedQuery.getResultList(); } }
 */
package com.jsfcourse.controller;

import com.jsf.dao.TaskDAO;
import com.jsf.entities.Task;
import com.jsf.entities.TaskDetail;
import com.jsf.entities.User;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@RequestScoped
public class TaskController implements Serializable {

    @Inject
    private TaskDAO taskDAO;

    @Inject
    private UserController userController;

    private int pageSize = 10;  // Domyślna liczba zadań na stronie
    private int currentPage = 0;  // Domyślna strona
    private String filterKeyword;  // Fraza do filtracji

    // Getter i setter dla filterKeyword
    public String getFilterKeyword() {
        return filterKeyword;
    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

    // Getter i setter dla pageSize
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    // Getter i setter dla currentPage
    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    // Metoda do pobierania zadań z taskDetails (Eager Fetch)
    public List<Task> getTasks() {
        User loggedInUser = userController.getLoggedInUser();
        if (loggedInUser == null) {
            return List.of();  // Użytkownik jest niezalogowany, zwracamy pustą listę
        }

        if (filterKeyword == null || filterKeyword.trim().isEmpty()) {
            return taskDAO.getTasksByUserWithTaskDetails(loggedInUser);
        } else {
            return taskDAO.getTasksByUserWithPaginationAndFilter(loggedInUser, currentPage, pageSize, filterKeyword);
        }
    }

    // Metoda do filtrowania (np. resetowanie paginacji)
    public void filterTasks() {
        currentPage = 0;  // Resetujemy stronę przy filtracji
    }
}

/*
 * // Metoda do pobierania statusu zadania w widoku public String
 * getTaskStatus(Task task) { if (task.getTaskDetails() == null ||
 * task.getTaskDetails().isEmpty()) { return "Brak szczegółów"; } TaskDetail
 * firstDetail = task.getTaskDetails().get(0); return firstDetail.getCompleted()
 * == 1 ? "Ukończone" : "Nieukończone"; }
 * 
 * 
 */
