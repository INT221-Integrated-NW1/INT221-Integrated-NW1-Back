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
import sit.int221.nw1.dto.responseDTO.addDTORespond;
import sit.int221.nw1.dto.responseDTO.filteredTaskDTO;
import sit.int221.nw1.entities.Tasks;
import sit.int221.nw1.services.ListMapper;
import sit.int221.nw1.services.TasksService;

import java.net.URI;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5174", "http://ip23nw1.sit.kmutt.ac.th","http://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v2/tasks")
public class TasksController {
    @Autowired
    TasksService service;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    ListMapper listMapper;



//    @GetMapping("")
//    public ResponseEntity<Object> getAllTasks() {
//        List<Tasks> tasks = service.getAllTasks();
//        List<TaskDTO> tasksDTO = tasks.stream()
//                .map(task -> {
//                    TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
//                    taskDTO.setStatus(task.getStatus());
//                    return taskDTO;
//                })
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(tasksDTO);
//    }

    // get + filter
// TasksController.java
    @GetMapping("")
    public ResponseEntity<Object> getAllTasks(@RequestParam(value = "filterStatuses", required = false) List<String> filterStatuses,
                                              @RequestParam(defaultValue = "default") String sortBy) {
        List<Tasks> tasks;
        if (filterStatuses == null || filterStatuses.isEmpty()) {
            tasks = service.getAllTasks();
        } else {
            tasks = service.getTasksByStatusNames(filterStatuses);
        }

        if (sortBy.equalsIgnoreCase("reverse")) {
            tasks.sort(Comparator.comparing(task -> task.getStatus().getName(), Comparator.reverseOrder()));
        } else if (sortBy.equalsIgnoreCase("status.name")) {
            tasks.sort(Comparator.comparing(task -> task.getStatus().getName()));

        } else {
            tasks.sort(Comparator.comparing(Tasks::getCreatedOn));

        }

        List<TaskDTO> tasksDTO = tasks.stream()
                .map(task -> modelMapper.map(task, TaskDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tasksDTO);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TasksDTO> getTaskById(@PathVariable Integer id) {
        Tasks task = service.findById(id);
        if (task != null) {
            TasksDTO tasksDTO = modelMapper.map(task, TasksDTO.class);
            tasksDTO.setStatus(task.getStatus().getName());
            return ResponseEntity.ok(tasksDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("") public ResponseEntity<addDTORespond> createTask(@RequestBody addDTO addDTO) {
            Tasks tasks = service.createTask(addDTO);
            addDTORespond addDTORespond = modelMapper.map(tasks, addDTORespond.class);
            URI location = URI.create("");
            return ResponseEntity.created(location).body(addDTORespond);
        }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updatetask(@RequestBody updateTaskDTO updateTaskDTO, @PathVariable Integer id) {
        return ResponseEntity.ok(modelMapper.map(service.updateTask(id, updateTaskDTO), TaskDTO.class));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Integer id) {
        Tasks deletedTask = service.deleteTask(id);
        deleteTaskDTO delete = modelMapper.map(deletedTask, deleteTaskDTO.class);
        delete.setStatus(deletedTask.getStatus().getName());
        return ResponseEntity.ok(delete);
    }
}
