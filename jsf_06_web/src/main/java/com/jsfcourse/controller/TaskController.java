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

    @PostConstruct
    public void init() {
        task = new Task();
        taskDetail = new TaskDetail(); // Inicjalizacja TaskDetail
        availablePriorities = List.of("Low", "Medium", "High");
        availableCategories = categoryDAO.getAllCategories();
    }

    // Gettery i settery

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

        if (taskTitle == null || taskTitle.isEmpty() || selectedPriority == null || category == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Wszystkie pola są wymagane.", null));
            return;
        }

        // Tworzenie zadania
        Task newTask = new Task();
        newTask.setTitle(taskTitle);
        newTask.setPriority(selectedPriority);
        newTask.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        newTask.setUser(loggedInUser);
        newTask.setCategory(category);

        // Tworzenie szczegółów zadania
        taskDetail.setDescription(taskDescription); // Dodanie opisu
        taskDetail.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        taskDetail.setDueDate(taskDetail.getDueDate()); // Przypisanie terminu
        taskDetail.setCompleted((byte) 0); // Domyślny status - do wykonania
        taskDetail.setTask(newTask); // Powiązanie szczegółów z zadaniem

        // Zapisanie zadania i szczegółów
        taskDAO.saveTask(newTask);
        taskDetailsDAO.save(taskDetail);

        // Resetowanie pól formularza
        resetFormFields();

        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukces", "Zadanie zostało dodane.");
        FacesContext.getCurrentInstance().addMessage(null, message);
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
    public void deleteTask(Long taskId) {
        User loggedInUser = userController.getLoggedInUser();

        if (loggedInUser == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Użytkownik niezalogowany.", null));
            return;
        }

        try {
            taskDAO.deleteTask(taskId);
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Sukces", "Zadanie zostało usunięte."));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Nie można usunąć zadania.", null));
        }
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
