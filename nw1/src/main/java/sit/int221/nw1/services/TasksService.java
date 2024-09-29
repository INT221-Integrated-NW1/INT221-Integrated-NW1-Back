package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.StringUtil;
import sit.int221.nw1.dto.requestDTO.addDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskResponse;
import sit.int221.nw1.dto.responseDTO.TasksDTO;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Status;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.exception.CustomFieldException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.exception.ItemNotFoundException;

import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.repositories.server.StatusRepository;
import sit.int221.nw1.repositories.server.StatusesRepository;
import sit.int221.nw1.repositories.server.TasksRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TasksService {

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private StatusesRepository statusesRepository;

    @Autowired
    private BoardsRepository boardsRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<Tasks> getAllTask() {
        return tasksRepository.findAll();
    }
    public List<TaskDTO> getAllTasksByBoardId(String boardId, List<String> filterStatuses) {

        // Determine the sort order

        List<Tasks> tasks = tasksRepository.findByBoardsBoardId(boardId);


        // If filterStatuses is empty, return all tasks
        if (filterStatuses == null || filterStatuses.isEmpty()) {
            return tasks.stream().map(task -> {
                TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
                taskDTO.setBoardName(task.getBoards().getBoardName()); // Set board name
                taskDTO.setStatus(task.getStatus().getName());
                return taskDTO;
            }).collect(Collectors.toList());
        }

        // Filter tasks by statuses if filterStatuses is provided
        List<Tasks> filteredTasks = tasks.stream()
                .filter(task -> filterStatuses.contains(task.getStatus().getName())) // Filtering by status name
                .collect(Collectors.toList());

        return filteredTasks.stream().map(task -> {
            TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
            taskDTO.setBoardName(task.getBoards().getBoardName()); // Set board name
            taskDTO.setStatus(task.getStatus().getName());
            return taskDTO;
        }).collect(Collectors.toList());
    }


    public TasksDTO findTasksById(Integer tasksId) {
        Tasks task = tasksRepository.findTasksById(tasksId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task ID " + tasksId+ " Not Found"));

//        TasksDTO tasksDTO = mapper.map(task, TasksDTO.class);
        TasksDTO tasksDTO = new TasksDTO();
        tasksDTO.setId(task.getId());
        tasksDTO.setTitle(task.getTitle());
        tasksDTO.setDescription(task.getDescription());
        tasksDTO.setAssignees(task.getAssignees());
        tasksDTO.setStatus(task.getStatus().getName());
        tasksDTO.setBoardName(task.getBoards().getBoardName());
        tasksDTO.setCreatedOn(task.getCreatedOn());
        tasksDTO.setUpdatedOn(task.getUpdatedOn());

        return tasksDTO;
    }

    public Tasks updateTask(Integer id, updateTaskDTO updateTaskDTO) {
        // Find the existing task
        Tasks existingTask = tasksRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " does not exist."));

        // Set values from DTO
        existingTask.setTitle(updateTaskDTO.getTitle());
        existingTask.setDescription(updateTaskDTO.getDescription());
        existingTask.setAssignees(updateTaskDTO.getAssignees());

        // Trim values
        trim(existingTask);

        // Validate the task
        List<MultiFieldException.FieldError> errors = validateTaskForUpdate(updateTaskDTO);

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        try {
            Statuses status = statusesRepository.findById(updateTaskDTO.getStatus())
                    .orElseThrow(() -> new Exception("Status with ID " + updateTaskDTO.getStatus() + " does not exist"));
            existingTask.setStatus(status);
        } catch (Exception e) {
            errors.add(new MultiFieldException.FieldError("status", "Status with ID " + updateTaskDTO.getStatus() + " does not exist"));
        }

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        try {
            return tasksRepository.save(existingTask);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update task.", e);
        }
    }
    public Tasks createTask(addDTO addDTO, String boardsId) {
        Tasks tasks = modelMapper.map(addDTO, Tasks.class);

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        // Validate the title
        if (tasks.getTitle() == null || tasks.getTitle().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("title", "Title is required."));
        }

        // Check if the status is null and handle accordingly
        if (addDTO.getStatus() == null) {
            try {
                // Fetch the default status (ID = 1) from the Statuses repository
                Statuses defaultStatus = statusesRepository.findById("000000000000001")
                        .orElseThrow(() -> new Exception("Default status does not exist"));
                tasks.setStatus(defaultStatus);
            } catch (Exception e) {
                errors.add(new MultiFieldException.FieldError("status", "Default status does not exist"));
            }
        } else {
            try {
                // Fetch the status based on the ID provided in the DTO
                Statuses status = statusesRepository.findStatusesById(addDTO.getStatus())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status Not Found"));
                tasks.setStatus(status);
            } catch (Exception e) {
                errors.add(new MultiFieldException.FieldError("status", "Status with ID " + addDTO.getStatus() + " does not exist"));
            }
        }

        // Validate the assignees length
        if (tasks.getAssignees() != null && tasks.getAssignees().length() > 30) {
            errors.add(new MultiFieldException.FieldError("assignees", "Assignees must be between 0 and 30 characters."));
        }

        // If there are any validation errors, throw them
        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        try {
//            tasks.setStatus(statusesRepository.findByStatusIdAndBoardsBoardId(addDTO.getStatus(),boardsId)
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status Not Found")));
            // Fetch the board by ID
            Boards boards = boardsRepository.findById(addDTO.getBoards()).orElseThrow(() -> new ItemNotFoundException("Board not found"));
            tasks.setBoards(boards);
            // Save the task to the repository
            return tasksRepository.save(tasks);
        } catch (Exception e) {
            throw new CustomFieldException("internal", "Failed to save task: " + e.getMessage());
        }
    }


    private void trim(Tasks tasks) {
        tasks.setTitle(StringUtil.trimToNull(tasks.getTitle()));
        tasks.setDescription(StringUtil.trimToNull(tasks.getDescription()));
        tasks.setAssignees(StringUtil.trimToNull(tasks.getAssignees()));
    }


    // task(id) does not exist, e.g. has already been deleted by another user returns 404 from TaskNotFoundException class
    public Tasks deleteTask(Integer id,String boardId) {
        Tasks task = tasksRepository.findByIdAndBoardsBoardId(id,boardId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        tasksRepository.delete(task);
        return task;
    }

    public void transferTasks(String oldStatusId, String newStatusId) {
        Statuses oldStatus = statusesRepository.findById(oldStatusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Old Status does not exist"));
        Statuses newStatus = statusesRepository.findById(newStatusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New Status does not exist"));

        List<Tasks> tasksToTransfer = tasksRepository.findByStatus(oldStatus);
        for (Tasks task : tasksToTransfer) {
            task.setStatus(newStatus);
            tasksRepository.save(task);
        }
    }
    @Transactional(transactionManager = "serverTransactionManager")
    public TaskResponse deleteTasks(Integer id) {
        Tasks tasks = tasksRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        tasksRepository.delete(tasks);
        return modelMapper.map(tasks, TaskResponse.class);
    }
    private List<MultiFieldException.FieldError> validateTaskForUpdate(updateTaskDTO updateTaskDTO) {
        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        // Validate title
        if (updateTaskDTO.getTitle() == null || updateTaskDTO.getTitle().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("title", "must not be null"));
        }
        if (updateTaskDTO.getTitle() != null && updateTaskDTO.getTitle().length() > 100) {
            errors.add(new MultiFieldException.FieldError("title", "size must be between 0 and 100"));
        }

        // Validate description
        if (updateTaskDTO.getDescription() != null && updateTaskDTO.getDescription().length() > 500) {
            errors.add(new MultiFieldException.FieldError("description", "size must be between 0 and 500"));
        }

        // Validate assignees
        if (updateTaskDTO.getAssignees() != null && updateTaskDTO.getAssignees().length() > 30) {
            errors.add(new MultiFieldException.FieldError("assignees", "size must be between 0 and 30"));
        }

        // Validate status
        if (updateTaskDTO.getStatus() == null) {
            errors.add(new MultiFieldException.FieldError("status", "Status is required."));
        }

        return errors;
    }
//    public List<Tasks> getTasksByStatusNames(List<String> statusNames) {
//        return repository.findByStatusNameIn(statusNames);
//    }
}



