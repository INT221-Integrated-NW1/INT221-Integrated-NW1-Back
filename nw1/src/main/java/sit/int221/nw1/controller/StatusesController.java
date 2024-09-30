package sit.int221.nw1.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.deleteStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusesRespondDTO;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.services.BoardStatusService;
import sit.int221.nw1.services.BoardsService;
import sit.int221.nw1.services.StatusesService;
import sit.int221.nw1.services.TasksService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw3.sit.kmutt.ac.th:3333", "http://intproj23.sit.kmutt.ac.th"})

@RequestMapping("/v3/boards/{boardId}/statuses")

public class StatusesController {
    @Autowired
    StatusesService statusesService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TasksService tasksService;
    @Autowired
    BoardStatusService boardStatusService;
    @Autowired
    BoardsService boardsService;
    @Autowired
    BoardsRepository boardsRepository;


    @GetMapping("")
    public ResponseEntity<Object> getAllStatus(@PathVariable String boardId) {
        Boards board = boardsRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        List<BoardStatus> boardStatuses = boardStatusService.getAllStatusByBoardId(boardId);
        List<Statuses> statuses = new ArrayList<>();
        for (BoardStatus bs : boardStatuses) {
            Statuses status = statusesService.getStatusById(bs.getStatus().getId());
            statuses.add(status);
        }
        return ResponseEntity.ok(statuses);
    }

    @GetMapping("/{id}")
    public ResponseEntity getStatusById(@PathVariable String id) {
        return ResponseEntity.ok(modelMapper.map(statusesService.getStatusById(id), StatusDTO.class));
    }

    @PostMapping("")
    public ResponseEntity<Statuses> createStatus(@PathVariable String boardId, @RequestBody addStatusDTO addStatusDTO) {
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        Statuses status = new Statuses();
        status.setName(addStatusDTO.getName());
        status.setDescription(addStatusDTO.getDescription());

        Statuses newStatus = statusesService.createStatus(status);
        boardStatusService.createBoardStatus(board, newStatus);
        return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<updateStatusDTO> updateStatus(@PathVariable String boardId, @PathVariable String id, @RequestBody updateStatusDTO updateDTO) {
        Boards board = boardsRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        Statuses status = modelMapper.map(updateDTO, Statuses.class);
        statusesService.updateStatus(id, status);

        Statuses updatedStatus = statusesService.getStatusById(id);
        updateStatusDTO updatedStatusDTO = modelMapper.map(updatedStatus, updateStatusDTO.class);
        return ResponseEntity.ok(updatedStatusDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeStatus(@PathVariable String id) {
        statusesService.deleteStatus(id);
        return ResponseEntity.ok("{}"); // Return empty JSON object on success
    }

    @DeleteMapping("/{id}/{newId}")
    public ResponseEntity<String> transferAndDeleteStatus(@PathVariable String id, @PathVariable String newId) {
        statusesService.transferAndDeleteStatus(id, newId);
        return ResponseEntity.ok("{}"); // Return empty JSON object on success
    }

}
