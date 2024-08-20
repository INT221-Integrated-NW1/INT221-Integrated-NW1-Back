package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.StringUtil;
import sit.int221.nw1.dto.requestDTO.addDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.models.server.Status;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.exception.CustomFieldException;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.repositories.server.StatusRepository;
import sit.int221.nw1.repositories.server.TasksRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TasksService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private TasksRepository repository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private TasksRepository tasksRepository;


    public List<Tasks> getAllTasks() {
        return repository.findAll();
    }

    public Tasks findById(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task" + " " + id + " " + "does not exist")
        );
    }
    public List<Tasks> getTasksByStatusIds(List<Integer> statusIds) {
        return repository.findByStatusIdIn(statusIds);
    }

//    public Tasks createTask(addDTO addDTO) {
//        Tasks tasks = modelMapper.map(addDTO, Tasks.class);
//        if (tasks.getTitle() == null || addDTO.getTitle().isEmpty()) {
//            throw new CustomFieldException("title", "Title is required.");
//        }
//        if (tasks.getStatus() == null) {
//            Status defaultStatus = statusRepository.findById(1)
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Default status does not exist"));
//            tasks.setStatus(defaultStatus);
//        }
//        Status status = statusRepository.findById(addDTO.getStatus())
//                .orElseThrow(() -> new ItemNotFoundException("Status with ID " + addDTO.getStatus() + " does not exist"));
//        tasks.setStatus(status);
//        try {
//            return repository.save(tasks);
//        } catch (Exception e) {
//
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save task.", e);
//        }
//    }


    public Tasks createTask(addDTO addDTO) {
        Tasks tasks = modelMapper.map(addDTO, Tasks.class);

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        if (tasks.getTitle() == null || addDTO.getTitle().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("title", "Title is required."));
        }

        // Check default status existence and handle errors
        if (addDTO.getStatus() == null) {
            try {
                Status defaultStatus = statusRepository.findById(1)
                        .orElseThrow(() -> new Exception("Default status does not exist"));
                tasks.setStatus(defaultStatus);
            } catch (Exception e) {
                errors.add(new MultiFieldException.FieldError("status", "Default status does not exist"));
            }
        } else {
            try {
                Status status = statusRepository.findById(addDTO.getStatus())
                        .orElseThrow(() -> new Exception("Status with ID " + addDTO.getStatus() + " does not exist"));
                tasks.setStatus(status);
            } catch (Exception e) {
                errors.add(new MultiFieldException.FieldError("status", "Status with ID " + addDTO.getStatus() + " does not exist"));
            }
        }

        if (tasks.getAssignees() != null && tasks.getAssignees().length() > 30) {
            errors.add(new MultiFieldException.FieldError("assignees", "Assignees must be between 0 and 30 characters."));
        }

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        try {
            return repository.save(tasks);
        } catch (Exception e) {
            throw new CustomFieldException("internal", "Failed to save task: " + e.getMessage());
        }
    }
    public Tasks updateTask(Integer id, updateTaskDTO updateTaskDTO) {
        Tasks existingTask = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " does not exist."));
        existingTask.setTitle(updateTaskDTO.getTitle());
        existingTask.setDescription(updateTaskDTO.getDescription());
        existingTask.setAssignees(updateTaskDTO.getAssignees());
        Status status = statusRepository.findById(updateTaskDTO.getStatus())
                .orElseThrow(() -> new ItemNotFoundException("Status with ID " + updateTaskDTO.getStatus() + " does not exist"));
        existingTask.setStatus(status);
        try {
            return repository.save(existingTask);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update task.", e);
        }
    }

    private void trim(Tasks tasks) {
        tasks.setTitle(StringUtil.trimToNull(tasks.getTitle()));
        tasks.setDescription(StringUtil.trimToNull(tasks.getDescription()));
        tasks.setAssignees(StringUtil.trimToNull(tasks.getAssignees()));
    }


    // task(id) does not exist, e.g. has already been deleted by another user returns 404 from TaskNotFoundException class
    public Tasks deleteTask(Integer id) {
        Tasks task = repository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("NOT FOUND"
                ));
        repository.deleteById(id);
        return task;
    }

    public void transferTasks(Integer oldStatusId, Integer newStatusId) {
        Status oldStatus = statusRepository.findById(oldStatusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Old Status does not exist"));
        Status newStatus = statusRepository.findById(newStatusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New Status does not exist"));

        List<Tasks> tasksToTransfer = tasksRepository.findByStatus(oldStatus);
        for (Tasks task : tasksToTransfer) {
            task.setStatus(newStatus);
            tasksRepository.save(task);
        }
    }

    public List<Tasks> getTasksByStatusNames(List<String> statusNames) {
        return repository.findByStatusNameIn(statusNames);
    }
}




