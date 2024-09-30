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
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.responseDTO.BoardNameResponseDTO;
import sit.int221.nw1.dto.responseDTO.BoardsResponseDTO;
import sit.int221.nw1.dto.responseDTO.OwnerDTO;
import sit.int221.nw1.exception.ErrorResponse;
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

//    @GetMapping("")
//    public List<BoardsResponseDTO> getAllBoards() {
//        return boardsService.getAllBoards();
//    }


    @GetMapping("")
    public ResponseEntity<Object> getAllBoards(@RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken) {
        String token = rawToken.substring(7);
        String oid = jwtTokenUtil.getOid(token);
        List<Boards> boards = boardService.findBoardByOid(oid);

        return ResponseEntity.ok(boards);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Object> getBoardById(@RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken, @PathVariable String id) {
        String token = rawToken.substring(7);
        Boards boards = boardService.findBoardById(id);
        BoardsResponseDTO returnBoardDTO = new BoardsResponseDTO(boards.getBoardId(), boards.getBoardName());

        return ResponseEntity.ok(returnBoardDTO);
    }

    @PostMapping("")
    public ResponseEntity<Object> createBoard(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String rawToken,
            @Valid @RequestBody BoardNameResponseDTO boardName // Use @Valid here
    ) {
        String token = rawToken.substring(7);
        String oid = jwtTokenUtil.getOid(token);

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
            userRepository.save(user);
        }

        Boards board = new Boards(boardId, boardName.getName(), user);
        List<BoardStatus> boardStatuses = boardStatusService.createDefaultBoardStatus(board);
        board.setBoardStatuses(boardStatuses);
        Boards createdBoard = boardService.createBoard(board);
        BoardsResponseDTO returnBoardDTO = new BoardsResponseDTO(board.getBoardId(), createdBoard.getBoardName());
        boardStatusService.SaveDefaultBoardStatus(boardStatuses);

        return ResponseEntity.status(HttpStatus.CREATED).body(returnBoardDTO);
    }


}