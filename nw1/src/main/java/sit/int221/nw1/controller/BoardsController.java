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
import sit.int221.nw1.dto.requestDTO.addCollabDTO;
import sit.int221.nw1.dto.responseDTO.*;
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
import sit.int221.nw1.services.CollabsService;
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
    private CollabsService collabsService;

    @Autowired
    private BoardStatusService boardStatusService;

    // GET /v3/boards - Get all boards accessible by the user
//    @GetMapping("/boards")
//    public ResponseEntity<Object> getAllBoards(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken) {
//        String oid = null;
//        if (rawToken != null && rawToken.startsWith("Bearer ")) {
//            String token = rawToken.substring(7);
//            try {
//                oid = jwtTokenUtil.getOid(token);
//            } catch (Exception e) {
//                // Invalid token, proceed to fetch only public boards
//                oid = null;
//            }
//        }
//
//        List<Boards> boards;
//        if (oid != null) {
//            // Fetch both public boards and user's private boards
//            boards = boardService.findAccessibleBoards(oid);
//        } else {
//            // Fetch only public boards
//            boards = boardService.findPublicBoards();
//        }
//
//        // Convert each board into BoardsResponseDTO including User information
//        List<BoardsResponseDTO> responseDTOs = boards.stream()
//                .map(board -> new BoardsResponseDTO(
//                        board.getBoardId(),
//                        board.getBoardName(),
//                        board.getVisibility(),
//                        new UserResponseDTO(board.getUser().getOid(), board.getUser().getName())
//                ))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(responseDTOs);
//    }
    // GET /v3/boards/{id} - Get a specific board by ID with visibility check

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
//        List<Boards> boards = boardService.findBoardByOid(oid);
        BoardListDTO boardDTOs = boardService.getAllBoardsByOid(oid);

//        List<ReturnBoardDTO> boardDTOs = boards.stream().map(board -> {
//            BoardOwnerDTO ownerDTO = new BoardOwnerDTO(board.getUser().getOid(), board.getUser().getName());
//
//            return new ReturnBoardDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), ownerDTO);
//        }).collect(Collectors.toList());

        return ResponseEntity.ok(boardDTOs);
    }
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<Object> getBoardById(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String rawToken,
                                               @PathVariable String boardId) {
        String userOid = null; // Initialize userOid as null
        System.out.println(rawToken.equals("Bearer null"));
        System.out.println(boardId);
        if (rawToken != null && rawToken.startsWith("Bearer ") && !rawToken.equals("Bearer null")) {
            String token = rawToken.substring(7);
            userOid = jwtTokenUtil.getOid(token); // Get OID from token
        }

        Boards board = boardService.findBoardById(boardId);

        // ตรวจสอบความเป็นเจ้าของหรือสถานะการมองเห็น
        boolean isOwner = (userOid != null && board.getUser().getOid().equals(userOid));
        boolean isCollaborator = boardService.getIsBoardCollaborator(userOid, boardId);

        if (!isOwner && board.getVisibility().equals("PRIVATE") && !isCollaborator) {
            // ใหม่: ป้องกันไม่ให้ผู้ใช้เข้าถึงบอร์ดที่เป็น 'PRIVATE'
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this board.");
        }

        // สร้าง DTO สำหรับข้อมูลเจ้าของบอร์ด
        UserResponseDTO userResponseDTO = new UserResponseDTO(board.getUser().getOid(), board.getUser().getName());

        // สร้าง DTO สำหรับข้อมูลบอร์ดที่ต้องการส่งกลับ
        BoardsResponseDTO boardsResponseDTO = new BoardsResponseDTO(board.getBoardId(), board.getBoardName(), board.getVisibility(), userResponseDTO);

        // ส่งกลับข้อมูลบอร์ดพร้อมกับข้อมูลเจ้าของ
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


    // POST /v3/boards - Create a new board with default visibility as PRIVATE
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

        // Find or create the user
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

//        if (!( board.getVisibility().equals("PRIVATE") || isOwner)) {
//            // ใหม่: ป้องกันไม่ให้ผู้ใช้เข้าถึงบอร์ดที่เป็น 'PRIVATE'
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have access to this board.");
//        }

        return ResponseEntity.status(HttpStatus.CREATED).body(returnCollab);

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
        String token = rawToken.substring(7);
        String userOid = jwtTokenUtil.getOid(token);
        // ดึงข้อมูล Token และ OID ของผู้ใช้

        // ตรวจสอบสิทธิ์ของผู้ใช้ หากผู้ใช้ไม่ใช่เจ้าของบอร์ดให้ return 403
        if (board.getVisibility().equals("PRIVATE") && !board.getUser().getOid().equals(userOid)) {
            throw new AccessDeniedException("Access denied. You do not have permission to access this private board.");
        }

        return true;
    }
}
