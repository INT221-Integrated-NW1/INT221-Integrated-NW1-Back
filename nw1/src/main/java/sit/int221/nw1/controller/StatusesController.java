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
import sit.int221.nw1.exception.BadRequestException;
import sit.int221.nw1.exception.ErrorResponse;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.services.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://ip23nw1.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th"})

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
    @Autowired
    CollabsService collabsService;

    @GetMapping("/boards/{boardId}/statuses")
    public ResponseEntity<Object> getAllStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String boardId) {
        isUserAuthorizedForGETBoard(rawToken, boardId);

        // ดึงสถานะทั้งหมดที่เกี่ยวข้องกับ board
        List<BoardStatus> boardStatuses = boardStatusService.getAllStatusByBoardId(boardId);

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
    public ResponseEntity getStatusById(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
                                        @PathVariable String id,
                                        @PathVariable String boardId) {
        isUserAuthorizedForGETBoard(rawToken, boardId);
        // ค้นหา BoardStatus ตาม boardId และ statusId
        BoardStatus boardStatus = boardStatusService.findBoardStatusByBoardIdAndStatusId(boardId, id);
        if (boardStatus == null) {
            throw new ItemNotFoundException("Status not found for the specified board.");
        }

        // แปลงสถานะที่พบเป็น DTO และส่งกลับ
        StatusDTO statusDTO = modelMapper.map(boardStatus.getStatus(), StatusDTO.class);
        return ResponseEntity.ok(statusDTO);
        //return ResponseEntity.ok(modelMapper.map(statusesService.getStatusById(id), StatusDTO.class));
    }

    @PostMapping("/boards/{boardId}/statuses")
    public ResponseEntity<Statuses> createStatus(
            @PathVariable String boardId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @RequestBody(required = false) addStatusDTO addStatusDTO
    ) {
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // Validate authorization with write access
        isUserAuthorizedForBoardWithWriteAccess(rawToken, boardId);

        // Check for null or empty input
        if (addStatusDTO == null || addStatusDTO.getName().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Return 400 for bad input
        }

        // Proceed with creating the status
        Statuses status = new Statuses();
        status.setName(addStatusDTO.getName());
        status.setDescription(addStatusDTO.getDescription());

        Statuses newStatus = statusesService.createStatus(status);
        boardStatusService.createBoardStatus(board, newStatus);
        return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
    }


    @PutMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity<Object> updateStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String boardId,
            @PathVariable String id,
            @RequestBody(required = false) updateStatusDTO updateDTO) {

        isUserAuthorizedForBoardWithWriteAccess(rawToken, boardId);
        Statuses existingStatus = statusesService.getStatusById(id);
        if (existingStatus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Status not found");
        }

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // Check if the token is provided and extract the user OID
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. Token is missing or invalid.");
        }
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        // Check if the user is authorized to update the status
        boolean isOwner = board.getUser().getOid().equals(userOid);
        boolean hasWriteAccess = collabsService.hasWriteAccess(userOid, boardId);
        if (!isOwner && !hasWriteAccess) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied. You do not have permission to update this status.");
        }

        // Validate the request body
        if (updateDTO == null || updateDTO.getName().isEmpty()) {
            return ResponseEntity.badRequest().body("Request body is missing or malformed");
        }

        // Check for restricted status IDs that cannot be edited
        if (id.equals("000000000000001") || id.equals("000000000000004")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The status '" + existingStatus.getName() + "' cannot be edited");
        }

        // Special logic for certain status IDs
        if (id.equals("000000000000002") || id.equals("000000000000003")) {
            BoardStatus bs = boardStatusService.findBoardStatusByBoardIdAndStatusId(boardId, id);
            Statuses s1 = new Statuses();
            s1.setName(updateDTO.getName());
            s1.setDescription(updateDTO.getDescription());
            Statuses newStatus = statusesService.createStatus(s1);
            bs.setStatus(newStatus);
            List<Tasks> tasks = tasksService.findTasksByBoardsIdAndStatusId(boardId, id);
            for (Tasks task : tasks) {
                task.setStatus(newStatus);
            }
            tasksService.saveAll(tasks);
            boardStatusService.updateBoardStatusByBoardStatusId(bs);
            return ResponseEntity.ok(newStatus);
        }

        // Update the status and return the updated DTO
        statusesService.updateStatus(id, modelMapper.map(updateDTO, Statuses.class));
        Statuses updatedStatus = statusesService.getStatusById(id);
        updateStatusDTO updatedStatusDTO = modelMapper.map(updatedStatus, updateStatusDTO.class);

        return ResponseEntity.ok(updatedStatusDTO);
    }


    @DeleteMapping("/boards/{boardId}/statuses/{id}")
    public ResponseEntity<Object> removeStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String id,
            @PathVariable String boardId
    ) {
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "Token is missing or invalid.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        // Retrieve board by ID and check for existence
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        isUserAuthorizedForBoardWithWriteAccess(rawToken,boardId);

        // Find the status on the board
        BoardStatus bs = boardStatusService.findBoardStatusByBoardIdAndStatusId(boardId, id);
        if (bs == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Status not found");
        }

        // Check if the status ID matches restricted status IDs
        if (bs.getStatus().getId().equals("000000000000001") || bs.getStatus().getId().equals("000000000000004")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The status '" + bs.getStatus().getName() + "' cannot be deleted");
        }

        // Perform deletion
        boardStatusService.deleteBoardStatusByBoardStatusId(bs.getBsId());
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

    private boolean isUserAuthorizedForGETBoard(String rawToken, String boardId) {
        // ค้นหาบอร์ดจาก boardId
        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // หากบอร์ดเป็น Public และไม่มีการส่ง Token หรือ Token ไม่ถูกต้อง ให้อนุญาตการเข้าถึง
        if ((rawToken == null || !rawToken.startsWith("Bearer ")) && board.getVisibility().equals("PUBLIC")) {
            return true; // ให้สามารถเข้าถึงบอร์ด Public ได้โดยไม่ต้องใช้ Token
        }

        // หากไม่มี Token หรือ Token ไม่ถูกต้อง และบอร์ดเป็น Private ให้โยน AccessDeniedException
        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            throw new AccessDeniedException("Access denied. You must provide a valid token to access this board.");
        }

        // ดึง Token และข้อมูล OID ของผู้ใช้จาก Token
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);

        // ตรวจสอบว่าผู้ใช้เป็นเจ้าของบอร์ด หรือเป็น Collaborator
        if (board.getVisibility().equals("PRIVATE")) {
            boolean isOwner = board.getUser().getOid().equals(userOid);
            boolean isCollaborator = boardsService.getIsBoardCollaborator(userOid, boardId);

            // หากผู้ใช้ไม่ใช่เจ้าของและไม่เป็น Collaborator ให้โยน AccessDeniedException
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
            return true; // Allow public access without a token if the board is public
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

        return true; // Indicate that the user is authorized with write access
    }
}