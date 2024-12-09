package sit.int221.nw1.controller;


import jakarta.validation.Valid;
import net.minidev.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.NanoUtil;
import sit.int221.nw1.config.AuthUser;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.dto.requestDTO.UpdateVisibilityRequest;
import sit.int221.nw1.dto.requestDTO.addCollabDTO;
import sit.int221.nw1.dto.responseDTO.*;
import sit.int221.nw1.exception.AccessDeniedException;
import sit.int221.nw1.exception.ErrorResponse;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.BoardStatus;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.User;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.repositories.server.CollabsBoardsRepository;
import sit.int221.nw1.repositories.server.UserRepository;
import sit.int221.nw1.services.BoardStatusService;
import sit.int221.nw1.services.BoardsService;
import sit.int221.nw1.services.CollabsService;
import sit.int221.nw1.services.StatusesService;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://ip23nw1.sit.kmutt.ac.th:3333", "https://intproj23.sit.kmutt.ac.th"})
@RequestMapping("/v3")
public class BoardsController {

    @Autowired
    private CollabsBoardsRepository collabsBoardsRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private BoardsService boardService;

    @Autowired
    private NanoUtil nanoUtil;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BoardsRepository boardsRepository;

    @Autowired
    private CollabsService collabsService;

    @Autowired
    private BoardStatusService boardStatusService;

    @GetMapping("/boards")
    public ResponseEntity<Object> getAllBoards(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken) {
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = rawToken.substring(7);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String oid = jwtTokenUtil.getOid(token);
        BoardListDTO boardDTOs = boardService.getAllBoardsByOid(oid);


        return ResponseEntity.ok(boardDTOs);
    }
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<Object> getBoardById(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
                                               @PathVariable String boardId) {
        String userOid = null;

        if (rawToken != null && rawToken.startsWith("Bearer ") && !rawToken.equals("Bearer null")) {
            String token = rawToken.substring(7);
            userOid = jwtTokenUtil.getOid(token);
        }

        Boards board = boardService.findBoardById(boardId);
        List<CollabDTO> collaborators = collabsService.getBoardCollabs(boardId);

        boolean isOwner = (userOid != null && board.getUser().getOid().equals(userOid));
        boolean isCollaborator = boardService.getIsBoardCollaborator(userOid, boardId);

        if (board.getVisibility().equals("PRIVATE") && !isOwner && !isCollaborator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this board.");
        }

        UserResponseDTO userResponseDTO = new UserResponseDTO(board.getUser().getOid(), board.getUser().getName());
        BoardsResponseDTO boardsResponseDTO = new BoardsResponseDTO(
                board.getBoardId(),
                board.getBoardName(),
                board.getVisibility(),
                board.getCreated_On(),
                userResponseDTO,
                collaborators
        );

        boardsResponseDTO.setCollaborators(collaborators);

        return ResponseEntity.ok(boardsResponseDTO);
    }
    @GetMapping("/boards/{boardId}/collabs")
    public ResponseEntity<Object> getBoardCollabs(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId
    ) {
        Boards board = boardService.findBoardById(boardId);
        List<CollabDTO> collaborators = collabsService.getBoardCollabs(boardId);
        if (board.getVisibility().equals("PUBLIC")){
            return ResponseEntity.ok(collaborators);
        }
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = rawToken.substring(7);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userOid = jwtTokenUtil.getOid(token);

        boolean isOwner = board.getUser().getOid().equals(userOid);
        boolean isCollabs = collabsService.existsByOidAndBoardId(userOid, boardId);

        if (!isOwner && !isCollabs && !board.getVisibility().equals("PUBLIC")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }


        return ResponseEntity.ok(collaborators);


    }

    @GetMapping("/boards/{boardId}/collabs/{collab_oid}")
    public ResponseEntity<Object> getBoardCollabsByOid(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String boardId,
            @PathVariable String collab_oid
    ) {
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = rawToken.substring(7);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userOid = jwtTokenUtil.getOid(token);

        Boards board = boardService.findBoardById(boardId);
        boolean isOwner = board.getUser().getOid().equals(userOid);
        boolean isCollabs = collabsService.existsByOidAndBoardId(userOid, boardId);

        if (!isOwner && !isCollabs && !board.getVisibility().equals("PUBLIC")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        CollabDTO collaborator = collabsService.getBoardCollabByOid(collab_oid, boardId);

        return ResponseEntity.ok(collaborator);


    }

    @PostMapping("/boards")
    public ResponseEntity<Object> createBoard(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @Valid @RequestBody(required = false) BoardNameRequestDTO boardName
    ) {
        if (boardName==null || boardName.getName()==null) {
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


        Optional<User> userOptional = userRepository.findById(oid);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            user = new User(oid);
            user.setName(authUser.getName());
            user.setEmail(authUser.getEmail());
            userRepository.save(user);
        }


        Boards board = new Boards(boardId, boardName.getName(), "PRIVATE", user);
        List<BoardStatus> boardStatuses = boardStatusService.createDefaultBoardStatus(board);
        board.setBoardStatuses(boardStatuses);


        Boards createdBoard = boardService.createBoard(board);
        boardStatusService.SaveDefaultBoardStatus(boardStatuses);


        addBoardDTOResponse returnBoardDTO = new addBoardDTOResponse(
                createdBoard.getBoardId(),
                createdBoard.getBoardName(),
                createdBoard.getVisibility(),
                createdBoard.getCreated_On(),
                new UserResponseDTO(user.getOid(), user.getName())
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(returnBoardDTO);
    }

    @PostMapping("/boards/{id}/collabs")
    public ResponseEntity<Object> addCollaborator(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String id,
            @RequestBody(required = false) addCollabDTO requestCollab
    ) {
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = rawToken.substring(7);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userOid = jwtTokenUtil.getOid(token);
        Boards board = boardService.findBoardById(id);


        boolean isOwner = (board.getUser().getOid().equals(userOid));
        if (!isOwner || requestCollab.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        if ((requestCollab.getAccessRight() == null) || (!requestCollab.getAccessRight().equals("READ") && !requestCollab.getAccessRight().equals("WRITE"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access Right must be READ or WRITE");
        }


        ReturnCollabDTO returnCollab = collabsService.addCollab(id, requestCollab);


        return ResponseEntity.status(HttpStatus.CREATED).body(returnCollab);

    }


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
        if (!board.getUser().getOid().equals(userOid)&&board.getVisibility().startsWith("PUBLIC")) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        if (request==null || request.getVisibility()==null) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Visibility is missing");
        }

        try {
            oid = jwtTokenUtil.getOid(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        try {
            Boards updatedBoard = boardService.updateBoardVisibility(id, request.getVisibility(), oid);
            addBoardDTOResponse returnBoardDTO = new addBoardDTOResponse(
                    updatedBoard.getBoardId(),
                    updatedBoard.getBoardName(),
                    updatedBoard.getVisibility(),
                    updatedBoard.getCreated_On(),
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
    @PatchMapping("/boards/{id}/collabs/{collab_oid}")
    public ResponseEntity<Object> updateCollaboratorAccess(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String id,
            @PathVariable String collab_oid,
            @RequestBody Map<String, String> request
    ) {
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = rawToken.substring(7);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userOid = jwtTokenUtil.getOid(token);
        Boards board = boardService.findBoardById(id);


        if (!board.getUser().getOid().equals(userOid)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only board owner can update collaborator access");
        }

        String accessRight = request.get("accessRight");
        if (accessRight == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Access right is required");
        }

        try {
            CollabDTO updatedCollab = collabsService.updateCollaboratorAccessRight(id, collab_oid, accessRight);
            return ResponseEntity.ok(updatedCollab);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @DeleteMapping("/boards/{id}/collabs/{collab_oid}")
    public ResponseEntity<Object> removeOrLeaveCollaboration(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @PathVariable String id,
            @PathVariable String collab_oid
    ) {
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = rawToken.substring(7);
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userOid = jwtTokenUtil.getOid(token);
        Boards board = boardService.findBoardById(id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users currentUser = usersRepository.findByName(username);


        boolean isOwner = board.getUser().getOid().equals(userOid);
        boolean isCollaboratorLeaving = collab_oid.equals(userOid);
        boolean isCollaborator = collabsBoardsRepository.existsByOidAndBoardBoardId(currentUser.getOid(), id);

        if (!isOwner && !isCollaboratorLeaving) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only board owner or the collaborator themselves can remove collaboration");
        }
        if (!isOwner && !(isCollaborator && currentUser.getOid().equals(collab_oid))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to remove this collaborator.");
        }
            collabsService.removeCollaborator(id, collab_oid);
            return ResponseEntity.ok().build();

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
    private boolean canManageTasksAndStatuses(String userOid, String boardId) {
        Boards board = boardService.findBoardById(boardId);
        return board.getUser().getOid().equals(userOid) ||
                collabsService.hasWriteAccess(userOid, boardId);
    }
}
