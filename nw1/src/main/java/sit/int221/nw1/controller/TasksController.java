package sit.int221.nw1.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sit.int221.nw1.dto.requestDTO.addDTO;
//import sit.int221.nw1.dto.requestDTO.deleteDTO;
import sit.int221.nw1.dto.requestDTO.deleteTaskDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskResponse;
import sit.int221.nw1.dto.responseDTO.TasksDTO;
import sit.int221.nw1.dto.responseDTO.addDTORespond;
import sit.int221.nw1.exception.ErrorResponse;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.services.ListMapper;
import sit.int221.nw1.services.TasksService;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.models.server.Boards;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw1.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3/boards/{boardId}/tasks")
public class TasksController {
    @Autowired
    TasksService tasksService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    ListMapper listMapper;
     @Autowired
    JwtTokenUtil jwtTokenUtil;
     @Autowired
     BoardsRepository boardsRepository;



 @GetMapping("")
    public ResponseEntity<Object> getAllTasks(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @RequestParam(value = "filterStatuses", required = false) List<String> filterStatuses) {

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        if (!board.getUser().getOid().equals(userOid)) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to access tasks for this board.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        List<TaskDTO> tasks = tasksService.getAllTasksByBoardId(boardId, filterStatuses);
        return ResponseEntity.ok(tasks);
    }

   @GetMapping("/{taskId}")
    public ResponseEntity<Object> getTasksById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestParam(value = "filterStatuses", required = false) List<String> filterStatuses) {

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getUser().getOid().equals(userOid)) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to access tasks for this board.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
//        Tasks tasks = new Tasks();
        TasksDTO tasks = tasksService.findTasksById(taskId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("")
    public ResponseEntity<Object> createTask(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @RequestBody addDTO addDTO,
            @PathVariable String boardId) {

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getUser().getOid().equals(userOid)) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to add tasks to this board.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        addDTO.setBoards(boardId);
        Tasks tasks = tasksService.createTask(addDTO, boardId);
        addDTORespond addDTORespond = modelMapper.map(tasks, addDTORespond.class);
        URI location = URI.create("/" + boardId + "/tasks/");
        return ResponseEntity.created(location).body(addDTORespond);
    }

        @PutMapping("/{taskId}")
    public ResponseEntity<Object> updateTask(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestBody updateTaskDTO updateTaskDTO) {

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getUser().getOid().equals(userOid)) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to edit tasks from this board.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
        updateTaskDTO.setId(taskId);
        Tasks updatedTask = tasksService.updateTask(taskId, updateTaskDTO);
        addDTORespond addDTORespond = modelMapper.map(updatedTask, addDTORespond.class);
        return ResponseEntity.ok(addDTORespond);
    }
    // @PutMapping("/{tasksId}")
    // public ResponseEntity<TaskDTO> updateTask(
    //         @PathVariable("boardId") String boardId, // ดึง boardId จาก path variable
    //         @PathVariable("tasksId") Integer id,      // ดึง taskId จาก path variable
    //         @RequestBody updateTaskDTO updateTaskDTO) {
    //     Tasks updatedTask = service.updateTask(id, boardId, updateTaskDTO); // ส่ง boardId ไปยัง service
    //     TaskDTO responseDTO = modelMapper.map(updatedTask, TaskDTO.class);
    //     return ResponseEntity.ok(responseDTO);
    // }
@DeleteMapping("/{taskId}")
    public ResponseEntity<Object> deleteTasks(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @PathVariable Integer taskId) {

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        if (!board.getUser().getOid().equals(userOid)) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to remove tasks from this board.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        TaskResponse deletedTask = tasksService.deleteTasks(taskId);
        return ResponseEntity.ok(deletedTask);
    }

    // @DeleteMapping("/{taskId}")
    // public ResponseEntity<deleteTaskDTO> deleteTask(@PathVariable Integer taskId, @PathVariable String boardId) {
    //     Tasks deletedTask = service.deleteTask(taskId, boardId);
    //     deleteTaskDTO delete = modelMapper.map(deletedTask, deleteTaskDTO.class);
    //     delete.setStatus(deletedTask.getStatus().getName());
    //     return ResponseEntity.ok(delete);
    // }

}
