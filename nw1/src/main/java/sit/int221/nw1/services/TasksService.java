package sit.int221.nw1.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.StringUtil;
import sit.int221.nw1.dto.requestDTO.addDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.entities.Status;
import sit.int221.nw1.entities.Tasks;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.repositories.StatusRepository;
import sit.int221.nw1.repositories.TasksRepository;
import org.springframework.data.annotation.CreatedDate;

import java.time.ZonedDateTime;
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


    public Tasks createTask(addDTO addDTO) {
        // Map the addDTO to Tasks entity using ModelMapper
        Tasks tasks = modelMapper.map(addDTO, Tasks.class);
        trim(tasks); // Trim strings to handle any white spaces

        // Determine status based on provided statusId
        Status status = (addDTO.getStatus() != null) ?
                statusRepository.findById(addDTO.getStatus()).orElseThrow(() ->
                        new ItemNotFoundException("Status with ID " + addDTO.getStatus() + " does not exist")) :
                statusRepository.findByName("No Status").orElseThrow(() ->
                        new ItemNotFoundException("No Status does not exist"));

        tasks.setStatus(status); // Set the status to the task
        return repository.save(tasks); // Save the task and return the saved entity
    }

    //public Tasks createTask(addDTO addDTO) {
//    // Map the addDTO to Tasks entity using ModelMapper
//    Tasks tasks = modelMapper.map(addDTO, Tasks.class);
//    trim(tasks); // Trim strings to handle any white spaces
//
//    // Determine status based on provided statusId
//    Status status = (addDTO.getStatusId() != null) ?
//            statusRepository.findById(addDTO.getStatusId()).orElseThrow(() ->
//                    new ItemNotFoundException("Status with ID " + addDTO.getStatusId() + " does not exist")) :
//            statusRepository.findByName("No Status").orElseThrow(() ->
//                    new ItemNotFoundException("No Status does not exist"));
//
//    tasks.setStatus(status); // Set the status to the task
//    return repository.save(tasks); // Save the task and return the saved entity
//}
//    public Tasks createTask(Tasks task) {
//        // Check if title, status is empty
//        if (task.getTitle() == null || task.getTitle().isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required.");
//        } else if (task.getStatus() == null) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
//        }
//        try {
//            // Save task
//            return repository.save(task);
//        } catch (DataIntegrityViolationException e) {
//            // Handle specific constraint violation (e.g., unique constraint)
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to save task. Ensure data integrity.");
//        } catch (Exception e) {
//            // Handle any other unexpected errors
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save task.", e);
//        }
//    }
    public Tasks updateTask(updateTaskDTO updateDTOTask) {
        Tasks existingTask = repository.findById(updateDTOTask.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task " + updateDTOTask.getId() + " does not exist"));

        Tasks tasks = modelMapper.map(updateDTOTask, Tasks.class);
        tasks.setCreatedOn(existingTask.getCreatedOn()); // Set 'created_on' from the existing task

        trim(tasks);

        // Fetch the Status entity from the database using the statusId provided in the updateDTO
        Status status = statusRepository.findById(updateDTOTask.getStatus())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status does not exist"));

        // Set the fetched Status entity to the status field of the Tasks entity
        tasks.setStatus(status);

        return repository.save(tasks);
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
}




