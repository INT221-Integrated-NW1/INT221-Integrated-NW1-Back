package sit.int221.nw1.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sit.int221.nw1.dto.requestDTO.addDTO;
//import sit.int221.nw1.dto.requestDTO.deleteDTO;
import sit.int221.nw1.dto.requestDTO.deleteTaskDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
import sit.int221.nw1.dto.responseDTO.TasksDTO;
import sit.int221.nw1.dto.responseDTO.addDTORespond;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.services.ListMapper;
import sit.int221.nw1.services.TasksService;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards")
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
    @GetMapping("/{boardId}/tasks")
    public List<TaskDTO> getAllTasks(@PathVariable String boardId, @RequestParam(value = "filterStatuses", required = false) List<String> filterStatuses) {

//          (filterStatuses == null || filterStatuses.isEmpty()) {
        return service.getAllTasksByBoardId(boardId, filterStatuses);
    }
//        } else {
//             return service.getTasksByStatusNames(filterStatuses);
//        }


//        if (sortBy.equalsIgnoreCase("reverse")) {
//            service.sort(Comparator.comparing(task -> task.getStatus().getName(), Comparator.reverseOrder()));
//        } else if (sortBy.equalsIgnoreCase("status.name")) {
//            service.sort(Comparator.comparing(task -> task.getStatus().getName()));
//
//        } else {
//            service.sort(Comparator.comparing(Tasks::getCreatedOn));
//
//        }
//
//        List<TaskDTO> tasksDTO = tasks.stream()
//                .map(task -> modelMapper.map(task, TaskDTO.class))
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(tasksDTO);


    @GetMapping("/{boardId}/tasks/{tasksId}")
    public TasksDTO getTask(@PathVariable String boardId, @PathVariable Integer tasksId) {
        return service.getTaskByBoardIdAndByTaskID(boardId, tasksId);
    }

    @PostMapping("/{boardId}/tasks")
    public ResponseEntity<addDTORespond> createTask(@RequestBody addDTO addDTO, @PathVariable String boardId) {
        addDTO.setBoards(boardId);
        Tasks tasks = service.createTask(addDTO,boardId);
        addDTORespond addDTORespond = modelMapper.map(tasks, addDTORespond.class);
        URI location = URI.create("/"+boardId+"/tasks/");
        return ResponseEntity.created(location).body(addDTORespond);
    }
    @PutMapping("/{boardId}/tasks/{tasksId}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable("boardId") String boardId, // ดึง boardId จาก path variable
            @PathVariable("tasksId") Integer id,      // ดึง taskId จาก path variable
            @RequestBody updateTaskDTO updateTaskDTO) {
        Tasks updatedTask = service.updateTask(id, boardId, updateTaskDTO); // ส่ง boardId ไปยัง service
        TaskDTO responseDTO = modelMapper.map(updatedTask, TaskDTO.class);
        return ResponseEntity.ok(responseDTO);
    }


    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<deleteTaskDTO> deleteTask(@PathVariable Integer taskId, @PathVariable String boardId) {
        Tasks deletedTask = service.deleteTask(taskId, boardId);
        deleteTaskDTO delete = modelMapper.map(deletedTask, deleteTaskDTO.class);
        delete.setStatus(deletedTask.getStatus().getName());
        return ResponseEntity.ok(delete);
    }

}
