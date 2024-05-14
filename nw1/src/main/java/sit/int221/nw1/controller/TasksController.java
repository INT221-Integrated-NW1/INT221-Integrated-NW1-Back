package sit.int221.nw1.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import sit.int221.nw1.dto.requestDTO.addDTO;
//import sit.int221.nw1.dto.requestDTO.deleteDTO;
import sit.int221.nw1.dto.requestDTO.deleteTaskDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
import sit.int221.nw1.dto.responseDTO.TasksDTO;
import sit.int221.nw1.dto.responseDTO.addTaskDetailDTO;
import sit.int221.nw1.entities.Tasks;
import sit.int221.nw1.services.ListMapper;
import sit.int221.nw1.services.TasksService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw1.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v2/tasks")
public class TasksController {
    @Autowired
    TasksService service;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    ListMapper listMapper;


    @GetMapping("")
    public ResponseEntity<Object> getAllTasks() {
        List<Tasks> tasks = service.getAllTasks();
        List<TaskDTO> tasksDTO = tasks.stream()
                .map(task -> {
                    TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
                    taskDTO.setStatus(task.getStatus());
                    return taskDTO;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasksDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TasksDTO> getTaskById(@PathVariable Integer id) {
        Tasks task = service.findById(id);
        if (task != null) {
            TasksDTO tasksDTO = modelMapper.map(task, TasksDTO.class);
            tasksDTO.setStatus(task.getStatus().getName()); // Add this line
            return ResponseEntity.ok(tasksDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("")
    public ResponseEntity<Object> CreateTask(@RequestBody addDTO addDTO) {
        Tasks createnewtask = service.createTask(addDTO);
        addTaskDetailDTO createdto = modelMapper.map(createnewtask, addTaskDetailDTO.class);
        URI location = URI.create("");
        return ResponseEntity.created(location).body(createdto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<updateTaskDTO> updatetask(@RequestBody addDTO adddto, @PathVariable Integer id) {
        Tasks update = service.findById(id);
        updateTaskDTO updateDTOtask = modelMapper.map(update, updateTaskDTO.class);
        updateDTOtask.setTitle(adddto.getTitle());
        updateDTOtask.setDescription(adddto.getDescription());
        updateDTOtask.setAssignees(adddto.getAssignees());
        updateDTOtask.setStatus(adddto.getStatus());
        service.updateTask(updateDTOtask);
        return ResponseEntity.ok().body(updateDTOtask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Integer id) {
        Tasks deletedTask = service.deleteTask(id);
        deleteTaskDTO delete = modelMapper.map(deletedTask, deleteTaskDTO.class);
        delete.setStatus(deletedTask.getStatus().getName()); // Add this line
        return ResponseEntity.ok(delete);
    }
}
