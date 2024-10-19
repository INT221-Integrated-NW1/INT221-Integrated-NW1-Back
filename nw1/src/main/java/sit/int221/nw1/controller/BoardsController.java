package sit.int221.nw1.controller;


import jakarta.validation.Valid;
import net.minidev.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.Utils.NanoUtil;
import sit.int221.nw1.config.AuthUser;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.dto.requestDTO.UpdateVisibilityRequest;
import sit.int221.nw1.dto.responseDTO.BoardCollabResponse;
import sit.int221.nw1.dto.responseDTO.BoardNameRequestDTO;
import sit.int221.nw1.dto.responseDTO.BoardsResponseDTO;
import sit.int221.nw1.dto.responseDTO.UserResponseDTO;
import sit.int221.nw1.exception.AccessDeniedException;
import sit.int221.nw1.exception.ErrorResponse;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.User;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.repositories.server.UserRepository;
import sit.int221.nw1.services.BoardStatusService;
import sit.int221.nw1.services.BoardsService;
import sit.int221.nw1.services.StatusesService;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


// BoardsController.java
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://ip23nw1.sit.kmutt.ac.th:3333", "https://intproj23.sit.kmutt.ac.th"})
@RequestMapping("/v3")
public class BoardsController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BoardsService boardService;

    @Autowired
    private NanoUtil nanoUtil;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardsRepository boardsRepository;

    @Autowired
    private StatusesService statusesService;

    @Autowired
    private BoardStatusService boardStatusService;

    // GET /v3/boards - Get all boards accessible by the user
    @GetMapping("/boards")
    public ResponseEntity<Object> getAllBoards(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken) {
        String oid = null;
        if (rawToken != null && rawToken.startsWith("Bearer ")) {
            String token = rawToken.substring(7);
            try {
                oid = jwtTokenUtil.getOid(token);
            } catch (Exception e) {
                // Invalid token, proceed to fetch only public boards
                oid = null;
            }
        }

        List<Boards> boards;
        if (oid != null) {
            // Fetch both public boards and user's private boards
            boards = boardService.findAccessibleBoards(oid);
        } else {
            // Fetch only public boards
            boards = boardService.findPublicBoards();
        }

        // Convert each board into BoardsResponseDTO including User information
        List<BoardsResponseDTO> responseDTOs = boards.stream()
                .map(board -> new BoardsResponseDTO(
                        board.getBoardId(),
                        board.getBoardName(),
                        board.getVisibility(),
                        new UserResponseDTO(board.getUser().getOid(), board.getUser().getName())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    // GET /v3/boards/{id} - Get a specific board by ID with visibility check
    @GetMapping("/boards/{id}")
    public ResponseEntity<Object> getBoardById(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
            @PathVariable String id) {

        try {
            // ตรวจสอบสิทธิ์การเข้าถึงบอร์ดโดยใช้ฟังก์ชัน isUserAuthorizedForBoard
            isUserAuthorizedForBoard(rawToken, id);

            // หากสามารถเข้าถึงได้, ดึงข้อมูลบอร์ดตาม ID
            Boards board = boardService.findBoardById(id);

            // สร้าง DTO เพื่อส่งข้อมูลบอร์ดกลับไป
            BoardsResponseDTO returnBoardDTO = new BoardsResponseDTO(
                    board.getBoardId(),
                    board.getBoardName(),
                    board.getVisibility(),
                    new UserResponseDTO(board.getUser().getOid(), board.getUser().getName())
            );

            return ResponseEntity.ok(returnBoardDTO);

        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Board not found");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this board");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }


    // POST /v3/boards - Create a new board with default visibility as PRIVATE
    @PostMapping("/boards")
    public ResponseEntity<Object> createBoard(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @Valid @RequestBody(required = false) BoardNameRequestDTO boardName
    ) {
        if (boardName == null || boardName.getName() == null) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "boardName is missing", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }


        if (rawToken == null || !rawToken.startsWith("Bearer ")) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
        }

        String token = rawToken.substring(7);
        String oid;
        try {
            oid = jwtTokenUtil.getOid(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        String boardId = nanoUtil.nanoIdGenerate(10);
        if (boardId.length() > 10) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Generated boardId exceeds the allowed length", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        if (boardName.getName().length() > 120) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "boardName length exceeds the maximum limit of 120.", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Find or create the user
        Optional<User> userOptional = userRepository.findById(oid);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            user = new User(oid);
            user.setName(authUser.getName());
            userRepository.save(user);
        }

        // Create the board with default visibility as PRIVATE
        Boards board = new Boards(boardId, boardName.getName(), "PRIVATE", user);
        List<BoardStatus> boardStatuses = boardStatusService.createDefaultBoardStatus(board);
        board.setBoardStatuses(boardStatuses);

        // Save the created board
        Boards createdBoard = boardService.createBoard(board);
        boardStatusService.SaveDefaultBoardStatus(boardStatuses);

        // Return board details including the user information and visibility
        BoardsResponseDTO returnBoardDTO = new BoardsResponseDTO(
                createdBoard.getBoardId(),
                createdBoard.getBoardName(),
                createdBoard.getVisibility(),
                new UserResponseDTO(user.getOid(), user.getName())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(returnBoardDTO);
    }

    // PATCH /v3/boards/{id}/visibility - Update board visibility
    @PatchMapping("/boards/{id}")
    public ResponseEntity<Object> updateBoardVisibility(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String id,
            @Valid @RequestBody(required = false) UpdateVisibilityRequest request
    ) {
        Boards board = boardsRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        String oid;
        isUserAuthorizedForBoard(rawToken, id);
        if (!board.getUser().getOid().equals(userOid) && board.getVisibility().startsWith("PUBLIC")) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        if (request == null || request.getVisibility() == null) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Visibility is missing");
        }

        try {
            oid = jwtTokenUtil.getOid(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        try {
            Boards updatedBoard = boardService.updateBoardVisibility(id, request.getVisibility(), oid);
            BoardsResponseDTO returnBoardDTO = new BoardsResponseDTO(
                    updatedBoard.getBoardId(),
                    updatedBoard.getBoardName(),
                    updatedBoard.getVisibility(),
                    new UserResponseDTO(updatedBoard.getUser().getOid(), updatedBoard.getUser().getName())
            );
            return ResponseEntity.ok(returnBoardDTO);
        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Board not found");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the board owner can change visibility");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid visibility value");
        }
    }

    private boolean isUserAuthorizedForBoard(String rawToken, String boardId) {

        Boards board = boardsRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));


        if ((rawToken == null || !rawToken.startsWith("Bearer ")) && board.getVisibility().startsWith("PUBLIC")) {
            return true;
        }

        if (rawToken == null || !rawToken.startsWith("Bearer ")) {
            throw new AccessDeniedException("Access denied. You must provide a valid token to access this board.");
        }
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);


        if (board.getVisibility().equals("PRIVATE") && !board.getUser().getOid().equals(userOid)) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        return true;
    }

}
