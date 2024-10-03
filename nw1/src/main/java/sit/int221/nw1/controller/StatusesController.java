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
import sit.int221.nw1.exception.AccessDeniedException;
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
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        if ((rawToken == null || !rawToken.startsWith("Bearer ")) && board.getVisibility().startsWith("PUBLIC")) {
            System.out.println("เข้านี่");
            return ResponseEntity.ok(statusesService.getAllStatus());
        }
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            throw new AccessDeniedException("Access denied. You must provide a valid token to access this board.");
        }

        // ดึงข้อมูล Token และ OID ของผู้ใช้
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        // ตรวจสอบสิทธิ์ของผู้ใช้ หากผู้ใช้ไม่ใช่เจ้าของบอร์ดให้ return 403
        if (board.getVisibility().equals("PRIVATE") && !board.getUser().getOid().equals(userOid)) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        // ตรวจสอบว่า userOid ตรงกับเจ้าของ board หรือไม่
//        if (!board.getUser().getOid().equals(userOid)) {
//            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "You do not have permission to access statuses for this board.", null);
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
//        }

        // ดึงสถานะทั้งหมดที่เกี่ยวข้องกับ board
        List<BoardStatus> boardStatuses = boardStatusService.getAllStatusByBoardId(boardId, userOid);

        // เก็บรวบรวม statuses จาก boardStatuses
        List<Statuses> statuses = new ArrayList<>();
        for (BoardStatus bs : boardStatuses) {
            Statuses status = statusesService.getStatusById(bs.getStatus().getId());
            statuses.add(status);
        }

        // คืนค่ารายการสถานะทั้งหมด
        System.out.println("เข้านี่เว้ยยยยย");
        return ResponseEntity.ok(statuses);
    }


    @GetMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity getStatusById(@RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false) String rawToken,
                                        @PathVariable String id,
                                        @PathVariable String boardId) {
    isUserAuthorizedForBoard(rawToken, boardId);



        return ResponseEntity.ok(modelMapper.map(statusesService.getStatusById(id), StatusDTO.class));
    }

    @PostMapping("/boards/{boardId}/statuses")
    public ResponseEntity<Statuses> createStatus(
                            @PathVariable String boardId,
                            @RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false) String rawToken,
                            @RequestBody(required = false) addStatusDTO addStatusDTO
    ) {
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        isUserAuthorizedForBoard(rawToken, boardId);
        if (!board.getUser().getOid().equals(userOid)&&board.getVisibility().startsWith("PUBLIC")) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }


        if(addStatusDTO==null||addStatusDTO.getName().isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        Statuses status = new Statuses();
        status.setName(addStatusDTO.getName());
        status.setDescription(addStatusDTO.getDescription());

        Statuses newStatus = statusesService.createStatus(status);
        boardStatusService.createBoardStatus(board, newStatus);
        return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
    }

    @PutMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity<updateStatusDTO> updateStatus(@RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false) String rawToken,
                                                        @PathVariable String boardId,
                                                        @PathVariable String id,
                                                        @RequestBody(required = false) updateStatusDTO updateDTO) {

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        isUserAuthorizedForBoard(rawToken, boardId);
        if (!board.getUser().getOid().equals(userOid)&&board.getVisibility().startsWith("PUBLIC")) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        if (updateDTO==null||updateDTO.getName().isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }

        Statuses status = modelMapper.map(updateDTO, Statuses.class);
        statusesService.updateStatus(id, status);

        Statuses updatedStatus = statusesService.getStatusById(id);
        updateStatusDTO updatedStatusDTO = modelMapper.map(updatedStatus, updateStatusDTO.class);
        return ResponseEntity.ok(updatedStatusDTO);
    }

    @DeleteMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity<Object> removeStatus(@RequestHeader(value = HttpHeaders.AUTHORIZATION,required = false) String rawToken,
                                               @PathVariable String id,
                                               @PathVariable String boardId
                                               ) {
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Token is missing or invalid.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }
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

        statusesService.deleteStatus(id);
        return ResponseEntity.ok("{}"); // Return empty JSON object on success
    }

    @DeleteMapping("/boards/{boardId}/statuses/{id}/{newId}")
    public ResponseEntity<String> transferAndDeleteStatus(@PathVariable String id, @PathVariable String newId) {
        statusesService.transferAndDeleteStatus(id, newId);
        return ResponseEntity.ok("{}"); // Return empty JSON object on success
    }



    private boolean isUserAuthorizedForBoard(String rawToken, String boardId) {
        // ค้นหาบอร์ดจาก boardId
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // ตรวจสอบว่าบอร์ดเป็น Public และไม่มีการส่ง Token หรือ Token ไม่ถูกต้อง
        if ((rawToken == null || !rawToken.startsWith("Bearer ")) && board.getVisibility().startsWith("PUBLIC")) {
            return true; // ให้สามารถเข้าถึงบอร์ด Public ได้โดยไม่ต้องใช้ Token
        }

        // หากไม่มี Token และบอร์ดเป็น Private ให้ return 403
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            throw new AccessDeniedException("Access denied. You must provide a valid token to access this board.");
        }

        // ดึงข้อมูล Token และ OID ของผู้ใช้
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        // ตรวจสอบสิทธิ์ของผู้ใช้ หากผู้ใช้ไม่ใช่เจ้าของบอร์ดให้ return 403
        if (board.getVisibility().equals("PRIVATE") && !board.getUser().getOid().equals(userOid)) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        return true;
    }
}
