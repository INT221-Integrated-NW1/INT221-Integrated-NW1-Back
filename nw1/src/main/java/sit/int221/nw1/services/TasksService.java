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


        List<Tasks> tasks = tasksRepository.findByBoardsBoardId(boardId);


        if (filterStatuses == null || filterStatuses.isEmpty()) {
            return tasks.stream().map(task -> {
                TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
                taskDTO.setBoardName(task.getBoards().getBoardName()); 
                taskDTO.setStatus(task.getStatus().getName());
                return taskDTO;
            }).collect(Collectors.toList());
        }

        List<Tasks> filteredTasks = tasks.stream()
                .filter(task -> filterStatuses.contains(task.getStatus().getName()))
                .collect(Collectors.toList());

        return filteredTasks.stream().map(task -> {
            TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
            taskDTO.setBoardName(task.getBoards().getBoardName());
            taskDTO.setStatus(task.getStatus().getName());
            return taskDTO;
        }).collect(Collectors.toList());
    }

    public List<Tasks> findTasksByBoardsIdAndStatusId(String boardId, String statusId) {
        return tasksRepository.findTasksByBoards_BoardIdAndStatus_Id(boardId, statusId);
    }


    public TasksDTO findTasksById(Integer tasksId) {
        Tasks task = tasksRepository.findTasksById(tasksId)
                .orElseThrow(() -> new ItemNotFoundException("Task ID " + tasksId + " Not Found"));

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

    public Tasks createTask(addDTO addDTO, String boardsId) {
        Tasks tasks = modelMapper.map(addDTO, Tasks.class);

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        if (tasks.getTitle() == null || tasks.getTitle().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("title", "Title is required."));
        }

        if (addDTO.getStatus() == null) {
            try {
                Statuses defaultStatus = statusesRepository.findById("000000000000001")
                        .orElseThrow(() -> new Exception("Default status does not exist"));
                tasks.setStatus(defaultStatus);
            } catch (Exception e) {
                errors.add(new MultiFieldException.FieldError("status", "Default status does not exist"));
            }
        } else {
            try {
                Statuses status = statusesRepository.findStatusesById(addDTO.getStatus())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status Not Found"));
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
            Boards boards = boardsRepository.findById(addDTO.getBoards()).orElseThrow(() -> new ItemNotFoundException("Board not found"));
            tasks.setBoards(boards);
            return tasksRepository.save(tasks);
        } catch (Exception e) {
            throw new CustomFieldException("internal", "Failed to save task: " + e.getMessage());
        }
    }

    public Tasks updateTask(Integer id, updateTaskDTO updateTaskDTO) {
        Tasks existingTask = tasksRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " does not exist."));

        existingTask.setTitle(updateTaskDTO.getTitle());
        existingTask.setDescription(updateTaskDTO.getDescription());
        existingTask.setAssignees(updateTaskDTO.getAssignees());

        trim(existingTask);

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


    @Transactional(transactionManager = "serverTransactionManager")
    public TaskResponse deleteTasks(Integer id) {
        System.out.println("เข้าไหมถามแค่นี้");
        Tasks tasks = tasksRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
        tasksRepository.delete(tasks);
        return modelMapper.map(tasks, TaskResponse.class);
    }


    private List<MultiFieldException.FieldError> validateTaskForUpdate(updateTaskDTO updateTaskDTO) {
        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        if (updateTaskDTO.getTitle() == null || updateTaskDTO.getTitle().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("title", "must not be null"));
        }
        if (updateTaskDTO.getTitle() != null && updateTaskDTO.getTitle().length() > 100) {
            errors.add(new MultiFieldException.FieldError("title", "size must be between 0 and 100"));
        }

        if (updateTaskDTO.getDescription() != null && updateTaskDTO.getDescription().length() > 500) {
            errors.add(new MultiFieldException.FieldError("description", "size must be between 0 and 500"));
        }

        if (updateTaskDTO.getAssignees() != null && updateTaskDTO.getAssignees().length() > 30) {
            errors.add(new MultiFieldException.FieldError("assignees", "size must be between 0 and 30"));
        }

        if (updateTaskDTO.getStatus() == null) {
            errors.add(new MultiFieldException.FieldError("status", "Status is required."));
        }

        return errors;
    }

    private void trim(Tasks tasks) {
        tasks.setTitle(StringUtil.trimToNull(tasks.getTitle()));
        tasks.setDescription(StringUtil.trimToNull(tasks.getDescription()));
        tasks.setAssignees(StringUtil.trimToNull(tasks.getAssignees()));
    }

    public void saveAll(List<Tasks> tasks) {
        tasksRepository.saveAll(tasks);
    }
}

