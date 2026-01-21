package com.app.service;

import com.app.model.Task;
import com.app.model.Employee;
import com.app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    public List<Task> getTasksByEmployee(Employee employee) {
        return taskRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }
    
    public List<Task> getTasksByEmployeeAndStatus(Employee employee, String status) {
        return taskRepository.findByEmployeeAndStatus(employee, status);
    }
    
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }
    
    public Task saveTask(Task task) {
        if ("COMPLETED".equals(task.getStatus()) && task.getCompletedDate() == null) {
            task.setCompletedDate(LocalDate.now());
        }
        return taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
    
    public Task updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
        task.setStatus(status);
        if ("COMPLETED".equals(status)) {
            task.setCompletedDate(LocalDate.now());
        }
        return taskRepository.save(task);
    }
}

