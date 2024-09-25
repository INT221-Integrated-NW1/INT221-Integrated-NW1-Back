package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.StringUtil;
import sit.int221.nw1.dto.requestDTO.addDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
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
    private static final Logger logger = LoggerFactory.getLogger(TasksService.class);
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    private TasksRepository repository;
    @Autowired
    private StatusesRepository statusesRepository;
    @Autowired
    private TasksRepository tasksRepository;
    @Autowired
    private BoardsRepository boardsRepository;

    //==============GetMethod GetAlltasks==================//
    // public List<Tasks> getAllTasks() {
    //     return repository.findAll();
    // }

    public List<TaskDTO> getAllTasksByBoardId(String boardId, List<String> filterStatuses) {
        logger.info("Fetching tasks for boardId: {}, filterStatuses: {}", boardId, filterStatuses);

        // Determine the sort order

        List<Tasks> tasks = tasksRepository.findByBoardsBoardId(boardId);

        // If filterStatuses is empty, return all tasks
        if (filterStatuses == null || filterStatuses.isEmpty()) {
            return tasks.stream().map(task -> {
                TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
                taskDTO.setBoardName(task.getBoards().getBoard_name()); // Set board name

                return taskDTO;
            }).collect(Collectors.toList());
        }

        // Filter tasks by statuses if filterStatuses is provided
        List<Tasks> filteredTasks = tasks.stream()
                .filter(task -> filterStatuses.contains(task.getStatus().getName())) // Filtering by status name
                .collect(Collectors.toList());

        return filteredTasks.stream().map(task -> {
            TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
            taskDTO.setBoardName(task.getBoards().getBoard_name()); // Set board name

            return taskDTO;
        }).collect(Collectors.toList());
    }


    public TasksDTO getTaskByBoardIdAndByTaskID(String boardId, Integer tasksId) {
        Tasks task = tasksRepository.findByIdAndBoardsBoardId(tasksId, boardId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        TasksDTO tasksDTO = modelMapper.map(task, TasksDTO.class);
        tasksDTO.setStatus(task.getStatus().getName());
        tasksDTO.setBoardName(task.getBoards().getBoard_name());

        return tasksDTO;
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
                Statuses defaultStatus = statusesRepository.findById(1)
                        .orElseThrow(() -> new Exception("Default status does not exist"));
                tasks.setStatus(defaultStatus);
            } catch (Exception e) {
                errors.add(new MultiFieldException.FieldError("status", "Default status does not exist"));
            }
        } else {
            try {
                // Fetch the status based on the ID provided in the DTO
                Statuses status = statusesRepository.findByIdAndBoardsBoardId(addDTO.getStatus(),boardsId)
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
            Boards boards = boardsRepository.findById(addDTO.getBoards()).orElseThrow(ItemNotFoundException::new);
            tasks.setBoards(boards);
            // Save the task to the repository
            return tasksRepository.save(tasks);
        } catch (Exception e) {
            throw new CustomFieldException("internal", "Failed to save task: " + e.getMessage());
        }
    }
//eiei


    public Tasks updateTask(Integer id, String boardId, updateTaskDTO updateTaskDTO) {
        // ตรวจสอบว่า id ไม่เป็น null
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The task ID must not be null.");
        }

        // ดึง Task ที่มีอยู่ตาม id
        Tasks existingTask = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task with ID " + id + " does not exist."));

        // ดึง Board จาก boardId ที่ส่งผ่าน path variable
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        // ดึง Status จาก updateTaskDTO
        Statuses statuses = statusesRepository.findById(updateTaskDTO.getStatus())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        // อัปเดตฟิลด์ต่าง ๆ
        existingTask.setTitle(updateTaskDTO.getTitle());
        existingTask.setDescription(updateTaskDTO.getDescription());
        existingTask.setAssignees(updateTaskDTO.getAssignees());
        existingTask.setBoards(board);
        existingTask.setStatus(statuses);

        // บันทึก Task ที่อัปเดตแล้ว
        return repository.save(existingTask);
    }


    private void trim(Tasks tasks) {
        tasks.setTitle(StringUtil.trimToNull(tasks.getTitle()));
        tasks.setDescription(StringUtil.trimToNull(tasks.getDescription()));
        tasks.setAssignees(StringUtil.trimToNull(tasks.getAssignees()));
    }


    // task(id) does not exist, e.g. has already been deleted by another user returns 404 from TaskNotFoundException class
    public Tasks deleteTask(Integer id,String boardId) {
        Tasks task = repository.findByIdAndBoardsBoardId(id,boardId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        repository.delete(task);
        return task;
    }

    public void transferTasks(Integer oldStatusId, Integer newStatusId) {
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

    public List<Tasks> getTasksByStatusNames(List<String> statusNames) {
        return repository.findByStatusNameIn(statusNames);
    }
}




