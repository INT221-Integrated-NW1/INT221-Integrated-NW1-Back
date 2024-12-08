package sit.int221.nw1.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.dto.requestDTO.addDTO;
import sit.int221.nw1.dto.requestDTO.deleteTaskDTO;
import sit.int221.nw1.dto.requestDTO.updateTaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
import sit.int221.nw1.dto.responseDTO.TaskResponse;
import sit.int221.nw1.dto.responseDTO.TasksDTO;
import sit.int221.nw1.dto.responseDTO.addDTORespond;
import sit.int221.nw1.exception.AccessDeniedException;
import sit.int221.nw1.exception.ErrorResponse;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.repositories.server.TasksRepository;
import sit.int221.nw1.services.BoardsService;
import sit.int221.nw1.services.CollabsService;
import sit.int221.nw1.services.ListMapper;
import sit.int221.nw1.services.TasksService;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.models.server.Boards;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:5173", "https://ip23nw1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})
@RestController
@RequestMapping("/v3")
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

    @Autowired
    TasksRepository tasksRepository;

    @Autowired
    BoardsService boardService;
    @Autowired
    CollabsService collabsService;

    @GetMapping("/boards/{boardId}/tasks")
    public ResponseEntity<Object> getAllTasks(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String boardId,
            @RequestParam(value = "filterStatuses", required = false) List<String> filterStatuses) {

        isUserAuthorizedForGETBoard(rawToken, boardId);


        List<TaskDTO> tasks = tasksService.getAllTasksByBoardId(boardId, filterStatuses);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/boards/{boardId}/tasks/{taskId}")
    public ResponseEntity<Object> getTasksById(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestParam(value = "filterStatuses", required = false) List<String> filterStatuses) {


        isUserAuthorizedForGETBoard(rawToken, boardId);
        TasksDTO tasks = tasksService.findTasksById(taskId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/boards/{boardId}/tasks")
    public ResponseEntity<Object> createTask(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @RequestBody(required = false) addDTO addDTO,
            @PathVariable String boardId) {

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        isUserAuthorizedForBoardWithWriteAccess(rawToken, boardId);
        if (addDTO == null) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Request body is missing or malformed", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        addDTO.setBoards(boardId);
        Tasks tasks = tasksService.createTask(addDTO, boardId);
        addDTORespond addDTORespond = modelMapper.map(tasks, addDTORespond.class);
        URI location = URI.create("/" + boardId + "/tasks/");
        return ResponseEntity.created(location).body(addDTORespond);
    }

    @PutMapping("/boards/{boardId}/tasks/{taskId}")
    public ResponseEntity<Object> updateTask(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestBody(required = false) updateTaskDTO updateTaskDTO) {

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        isUserAuthorizedForBoardWithWriteAccess(rawToken, boardId);
        TasksDTO task = tasksService.findTasksById(taskId);
        if (task == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Task not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        boolean isCollaborator = boardService.getIsBoardCollaborator(userOid, boardId);

        if (isCollaborator) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Collaborators can read but cannot modify tasks",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        boolean isUserAuthorizedForUpdateTask = isUserAuthorizedForUpdateTask(rawToken, boardId, userOid, board);

        if (!isUserAuthorizedForUpdateTask) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "You do not have permission to edit tasks from this board",
                    null
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        if (updateTaskDTO == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Request body is missing or malformed",
                    null
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        updateTaskDTO.setId(taskId);
        Tasks updatedTask = tasksService.updateTask(taskId, updateTaskDTO);
        addDTORespond addDTORespond = modelMapper.map(updatedTask, addDTORespond.class);
        return ResponseEntity.ok(addDTORespond);
    }


    @DeleteMapping("/boards/{boardId}/tasks/{taskId}")
    public ResponseEntity<Object> deleteTasks(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @PathVariable Integer taskId) {
        isUserAuthorizedForBoardWithWriteAccess(rawToken, boardId);
        TasksDTO task = tasksService.findTasksById(taskId);
        if (task == null) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Task not found",
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }


        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        if (!board.getUser().getOid().equals(userOid) && board.getVisibility().startsWith("PUBLIC")) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }


        TaskResponse deletedTask = tasksService.deleteTasks(taskId);
        return ResponseEntity.ok(deletedTask);
    }
    
    private boolean isUserAuthorizedForUpdateTask(String rawToken, String boardId, String userOid, Boards board) {
        if (board.getUser().getOid().equals(userOid)) {
            return true;
        }

        if (board.getVisibility().equals("PUBLIC")) {
            return false;
        }

        boolean isCollaborator = boardService.getIsBoardCollaborator(userOid, boardId);
        return isCollaborator;
    }

    private boolean isUserAuthorizedForGETBoard(String rawToken, String boardId) {

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));


        if ((rawToken == null || !rawToken.startsWith("Bearer ")) && board.getVisibility().equals("PUBLIC")) {
            return true;
        }


        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            throw new AccessDeniedException("Access denied. You must provide a valid token to access this board.");
        }


        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);


        if (board.getVisibility().equals("PRIVATE")) {
            boolean isOwner = board.getUser().getOid().equals(userOid);
            boolean isCollaborator = boardService.getIsBoardCollaborator(userOid, boardId);

  
            if (!isOwner && !isCollaborator) {
                throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
            }
        }

        return true;
    }

    private boolean isUserAuthorizedForBoardWithWriteAccess(String rawToken, String boardId) {
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        
        if ((rawToken == null || !rawToken.startsWith("Bearer ")) && board.getVisibility().equals("PUBLIC")) {
            return true; 
        }

        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            throw new AccessDeniedException("Access denied. You must provide a valid token to access this board.");
        }

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        
        boolean isOwner = board.getUser().getOid().equals(userOid);
        boolean hasWriteAccess = collabsService.hasWriteAccess(userOid, boardId);
        
        if (!isOwner && !hasWriteAccess) {
            throw new AccessDeniedException("Access denied. You do not have permission to create tasks on this board.");
        }

        return true;
    }
}
