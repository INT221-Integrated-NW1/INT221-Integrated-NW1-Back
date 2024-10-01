package sit.int221.nw1.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.deleteStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusesRespondDTO;
import sit.int221.nw1.exception.ErrorResponse;
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

@RequestMapping("/v3")

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
    @Autowired
    JwtTokenUtil jwtTokenUtil;


    @GetMapping("/boards/{boardId}/statuses")
    public ResponseEntity<Object> getAllStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String boardId) {

        // ตรวจสอบว่า rawToken ถูกส่งมาและเป็น Bearer token หรือไม่
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Token is missing or invalid.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        // ตัดเอาส่วน "Bearer " ออกและดึง token
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);  // ดึง userOid จาก token

        // ค้นหา board ตาม boardId
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // ตรวจสอบว่า userOid ตรงกับเจ้าของ board หรือไม่
        if (!board.getUser().getOid().equals(userOid)) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to access statuses for this board.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        // ดึงสถานะทั้งหมดที่เกี่ยวข้องกับ board
        List<BoardStatus> boardStatuses = boardStatusService.getAllStatusByBoardId(boardId, userOid);

        // เก็บรวบรวม statuses จาก boardStatuses
        List<Statuses> statuses = new ArrayList<>();
        for (BoardStatus bs : boardStatuses) {
            Statuses status = statusesService.getStatusById(bs.getStatus().getId());
            statuses.add(status);
        }

        // คืนค่ารายการสถานะทั้งหมด
        return ResponseEntity.ok(statuses);
    }


    @GetMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity getStatusById(@RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false) String rawToken,@PathVariable String id) {
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Token is missing or invalid.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        return ResponseEntity.ok(modelMapper.map(statusesService.getStatusById(id), StatusDTO.class));
    }

    @PostMapping("/boards/{boardId}/statuses")
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

    @PutMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity<updateStatusDTO> updateStatus(@PathVariable String boardId, @PathVariable String id, @RequestBody updateStatusDTO updateDTO) {
        Boards board = boardsRepository.findById(boardId).orElseThrow(() -> new ItemNotFoundException("Board not found"));
        Statuses status = modelMapper.map(updateDTO, Statuses.class);
        statusesService.updateStatus(id, status);

        Statuses updatedStatus = statusesService.getStatusById(id);
        updateStatusDTO updatedStatusDTO = modelMapper.map(updatedStatus, updateStatusDTO.class);
        return ResponseEntity.ok(updatedStatusDTO);
    }

    @DeleteMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity<String> removeStatus(@PathVariable String id) {
        statusesService.deleteStatus(id);
        return ResponseEntity.ok("{}"); // Return empty JSON object on success
    }

    @DeleteMapping("/boards/{boardId}/statuses/{id}/{newId}")
    public ResponseEntity<String> transferAndDeleteStatus(@PathVariable String id, @PathVariable String newId) {
        statusesService.transferAndDeleteStatus(id, newId);
        return ResponseEntity.ok("{}"); // Return empty JSON object on success
    }

}
