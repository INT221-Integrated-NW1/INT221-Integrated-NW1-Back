package sit.int221.nw1.controller;


import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.Utils.NanoUtil;
import sit.int221.nw1.config.AuthUser;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.dto.requestDTO.BoardsAddRequestDTO;
import sit.int221.nw1.dto.requestDTO.UpdateVisibilityRequest;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.responseDTO.BoardNameResponseDTO;
import sit.int221.nw1.dto.responseDTO.BoardsResponseDTO;
import sit.int221.nw1.dto.responseDTO.OwnerDTO;
import sit.int221.nw1.dto.responseDTO.UserResponseDTO;
import sit.int221.nw1.exception.AccessDeniedException;
import sit.int221.nw1.exception.ErrorResponse;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.models.server.User;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.repositories.server.UserRepository;
import sit.int221.nw1.services.BoardStatusService;
import sit.int221.nw1.services.BoardsService;
import sit.int221.nw1.services.StatusesService;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;


import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


// BoardsController.java
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw3.sit.kmutt.ac.th:3333", "http://intproj23.sit.kmutt.ac.th"})
@RequestMapping("/v3/boards")
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
    private StatusesService statusesService;

    @Autowired
    private BoardStatusService boardStatusService;

    // GET /v3/boards - Get all boards accessible by the user
    @GetMapping("")
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
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBoardById(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
                                               @PathVariable String id) {
        String oid = null;
        if (rawToken != null && rawToken.startsWith("Bearer ")) {
            String token = rawToken.substring(7);
            try {
                oid = jwtTokenUtil.getOid(token);
            } catch (Exception e) {
                // Invalid token, proceed as unauthenticated
                oid = null;
            }
        }

        try {
            Boards board = boardService.findBoardByIdWithVisibilityCheck(id, oid);

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
        }
    }

    // POST /v3/boards - Create a new board with default visibility as PRIVATE
    @PostMapping("")
    public ResponseEntity<Object> createBoard(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @Valid @RequestBody BoardNameResponseDTO boardName
    ) {
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
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Object> updateBoardVisibility(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String id,
            @Valid @RequestBody UpdateVisibilityRequest request
    ) {
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
}
