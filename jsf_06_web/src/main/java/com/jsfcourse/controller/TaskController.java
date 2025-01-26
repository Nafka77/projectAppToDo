package com.jsfcourse.controller;

import com.jsf.dao.CategoryDAO;
import com.jsf.dao.TaskDAO;
import com.jsf.dao.TaskDetailsDAO;
import com.jsf.entities.Category;
import com.jsf.entities.Task;
import com.jsf.entities.TaskDetail;
import com.jsf.entities.User;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;
import java.util.List;
import java.util.Date;

@Named
@RequestScoped
public class TaskController implements Serializable {

    @Inject
    private TaskDAO taskDAO;

    @Inject
    private UserController userController;

    @Inject
    private CategoryDAO categoryDAO;

    @Inject
    private TaskDetailsDAO taskDetailsDAO;

    private int pageSize = 7;
    private int currentPage = 0;
    private String filterKeyword = "";

    private String taskTitle;
    private String taskDescription;
    private String selectedPriority;
    private Category category;

    private Task task;
    private TaskDetail taskDetail; // Dodatkowy obiekt TaskDetail

    private List<Category> availableCategories;
    private List<String> availablePriorities;
    private Date selectedDate;
    

    @PostConstruct
    public void init() {
        task = new Task();
        taskDetail = new TaskDetail(); // Inicjalizacja TaskDetail
        selectedDate = new Date(); 
        availablePriorities = List.of("Low", "Medium", "High");
        availableCategories = categoryDAO.getAllCategories();
        category = null; // Reset kategorii
    }

    // Gettery i settery
    

    public Date getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(Date selectedDate) {
        this.selectedDate = selectedDate;
    }

    public List<Task> getTasks() {
        User loggedInUser = userController.getLoggedInUser();
        if (loggedInUser == null) {
            return List.of();
        }
        if (filterKeyword == null || filterKeyword.trim().isEmpty()) {
            return taskDAO.getTasksByUserWithTaskDetails(loggedInUser);
        } else {
            return taskDAO.getTasksByUserWithPaginationAndFilter(loggedInUser, currentPage, pageSize, filterKeyword);
        }
    }

    // Dodawanie nowego zadania
    public void addTask() {
        User loggedInUser = userController.getLoggedInUser();
        if (loggedInUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik niezalogowany.", null));
            return;
        }

        if (taskTitle == null || taskTitle.isEmpty() || selectedPriority == null || selectedPriority.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wszystkie wymagane pola muszą być wypełnione.", null));
            return;
        }

        // Tworzenie nowego zadania
        Task newTask = new Task();
        newTask.setTitle(taskTitle);
        newTask.setPriority(selectedPriority);
        newTask.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        newTask.setUser(loggedInUser);

        // Ustawienie kategorii, jeśli została wybrana
        if (category != null) {
            newTask.setCategory(category);
        }

        // Tworzenie szczegółów zadania
        taskDetail.setDescription(taskDescription);
        taskDetail.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        taskDetail.setDueDate(selectedDate);
        taskDetail.setTask(newTask);

        // Zapisanie zadania i szczegółów w bazie
        taskDAO.saveTask(newTask);
        taskDetailsDAO.save(taskDetail);

        // Resetowanie pól formularza
        resetFormFields();

        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukces", "Zadanie zostało dodane."));
    }




    // Resetowanie pól formularza
    private void resetFormFields() {
        taskTitle = "";
        selectedPriority = "";
        category = null;
        taskDescription = "";
        taskDetail = new TaskDetail(); // Resetowanie obiektu TaskDetail
    }

    // Nowa metoda do zmiany statusu zadania
    public void toggleTaskCompletion(int taskDetailId) {
        TaskDetail taskDetail = taskDetailsDAO.findById(taskDetailId);
        if (taskDetail != null) {
            byte newStatus = (taskDetail.getCompleted() == 0) ? (byte) 1 : (byte) 0;
            taskDetail.setCompleted(newStatus);
            taskDetailsDAO.save(taskDetail);

            String message = (newStatus == 1) ? "Zadanie zostało ukończone." : "Zadanie zostało oznaczone jako nieukończone.";
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukces", message));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd", "Nie znaleziono szczegółów zadania."));
        }
    }

    // Usuwanie zadania
    public void deleteTask(int taskId) {
        User loggedInUser = userController.getLoggedInUser();

        if (loggedInUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik niezalogowany.", null));
            return;
        }

        try {
            // Znajdź zadanie do usunięcia
            Task task = taskDAO.findTaskById((int) taskId);
            if (task == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie znaleziono zadania.", null));
                return;
            }


            // Usuń samo zadanie
            taskDAO.deleteTask(task);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukces", "Zadanie zostało usunięte."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Błąd podczas usuwania zadania.", null));
        }
    }
    

    // Przekierowanie do edycji zadania
 // W TaskController (lub innej klasie, która wywołuje tę metodę)
    public String editTask(int taskId) {
        System.out.println("Próba edycji zadania o ID: " + taskId);
        task = taskDAO.findTaskById(taskId); 
        if (task == null) {
            System.out.println("Nie znaleziono zadania o podanym ID.");
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Zadanie nie istnieje", null));
            return null;
        }
        taskDetail = taskDetailsDAO.findByTaskId(taskId);
        return "editTask.xhtml?faces-redirect=true&taskId=" + taskId;
    }






    public void saveTask() {
        User loggedInUser = userController.getLoggedInUser();
        if (loggedInUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik niezalogowany.", null));
            return;
        }

        if (taskTitle == null || taskTitle.isEmpty() || selectedPriority == null || category == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wszystkie pola są wymagane.", null));
            return;
        }

        // Ustawienie zaktualizowanych danych w obiekcie task
        task.setTitle(taskTitle);
        task.setPriority(selectedPriority);
        task.setCategory(category);

        // Ustawienie zaktualizowanych danych w obiekcie taskDetail
        taskDetail.setDescription(taskDescription);
        taskDetail.setDueDate(selectedDate);  // Ustawienie daty terminu

        // Zapisanie zaktualizowanych danych w bazie
        taskDAO.updateTask(task);  // Teraz wywołujemy updateTask zamiast saveTask
        taskDetailsDAO.save(taskDetail);  // Zapisanie szczegółów zadania

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukces", "Zadanie zostało zaktualizowane.");
        FacesContext.getCurrentInstance().addMessage(null, message);
        
        // Przekierowanie po zapisaniu
        // Możesz dodać logikę, która przekieruje użytkownika po zapisaniu na listę zadań, jeśli chcesz.
        // return "tasks.xhtml?faces-redirect=true"; 
    }



    // Filtrowanie zadań
    public void filterTasks() {
        currentPage = 0;
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    public void nextPage() {
        currentPage++;
    }

    // Gettery i settery dla pól
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public String getFilterKeyword() {
        return filterKeyword;
    }

    public void setFilterKeyword(String filterKeyword) {
        this.filterKeyword = filterKeyword;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getSelectedPriority() {
        return selectedPriority;
    }

    public void setSelectedPriority(String selectedPriority) {
        this.selectedPriority = selectedPriority;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Category> getAvailableCategories() {
        return availableCategories;
    }

    public void setAvailableCategories(List<Category> availableCategories) {
        this.availableCategories = availableCategories;
    }

    public List<String> getAvailablePriorities() {
        return availablePriorities;
    }

    public void setAvailablePriorities(List<String> availablePriorities) {
        this.availablePriorities = availablePriorities;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public TaskDetail getTaskDetail() {
        return taskDetail;
    }

    public void setTaskDetail(TaskDetail taskDetail) {
        this.taskDetail = taskDetail;
    }
}
