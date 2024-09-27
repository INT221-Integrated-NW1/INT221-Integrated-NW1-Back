package sit.int221.nw1.controller;


import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.dto.requestDTO.BoardsAddRequestDTO;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.responseDTO.BoardsResponseDTO;
import sit.int221.nw1.dto.responseDTO.OwnerDTO;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.services.BoardsService;
import sit.int221.nw1.services.StatusesService;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;


import java.net.URI;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw3.sit.kmutt.ac.th:3333", "http://intproj23.sit.kmutt.ac.th"})

@RequestMapping("/v3")
public class BoardsController {
    @Autowired
    private BoardsService boardsService;

    @Autowired
    private StatusesService statusesService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/boards")
    public List<BoardsResponseDTO> getAllBoards() {
        return boardsService.getAllBoards();
    }

//        @PostMapping("/boards")
//    public ResponseEntity<BoardsAddRequestDTO> addBoards(@Valid @RequestBody BoardsAddRequestDTO boardsAddRequestDTO,
//                                                         @RequestHeader("Authorization") String token) {
//        String oid = jwtTokenUtil.getOid(token.replace("Bearer ", ""));
//        Users user = boardsService.findByOid(oid);
//
//        String generatedBoardId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, 10);
//        boardsAddRequestDTO.setBoardId(generatedBoardId);
//        boardsAddRequestDTO.setOid(user.getOid());
//
//        Boards createBoard = boardsService.createBoards(boardsAddRequestDTO);
//        BoardsAddRequestDTO addRequestDTO = modelMapper.map(createBoard, BoardsAddRequestDTO.class);
//
//        createAndAddStatus("No Status", "The default status", generatedBoardId);
//        createAndAddStatus("To do", "To do this task", generatedBoardId);
//        createAndAddStatus("Doing", "Doing this task", generatedBoardId);
//        createAndAddStatus("Done", "Finished", generatedBoardId);
//
//
//        URI location = URI.create("/boards/" + generatedBoardId);
//        return ResponseEntity.created(location).body(addRequestDTO);
//    }
    @PostMapping("/boards")
    public ResponseEntity<BoardsResponseDTO> addBoards(@Valid @RequestBody BoardsAddRequestDTO boardsAddRequestDTO,
                                                       @RequestHeader("Authorization") String token) {
        String oid = jwtTokenUtil.getOid(token.replace("Bearer ", ""));
        Users user = boardsService.findByOid(oid);

        String generatedBoardId = NanoIdUtils.randomNanoId(NanoIdUtils.DEFAULT_NUMBER_GENERATOR, NanoIdUtils.DEFAULT_ALPHABET, 10);
        boardsAddRequestDTO.setBoardId(generatedBoardId);
        boardsAddRequestDTO.setOid(user.getOid());

        Boards createBoard = boardsService.createBoards(boardsAddRequestDTO);
        BoardsAddRequestDTO addRequestDTO = modelMapper.map(createBoard, BoardsAddRequestDTO.class);

        createAndAddStatus("No Status", "The default status", generatedBoardId);
        createAndAddStatus("To do", "To do this task", generatedBoardId);
        createAndAddStatus("Doing", "Doing this task", generatedBoardId);
        createAndAddStatus("Done", "Finished", generatedBoardId);
        // Return response with board and owner details
        BoardsResponseDTO responseDTO = new BoardsResponseDTO();
        responseDTO.setBoardId(createBoard.getBoardId());
        responseDTO.setBoard_name(createBoard.getBoard_name());

        OwnerDTO ownerDTO = new OwnerDTO();
        ownerDTO.setOid(user.getOid());
        ownerDTO.setName(user.getUsername());
        responseDTO.setOwner(ownerDTO);

        URI location = URI.create("/boards/" + generatedBoardId);
        return ResponseEntity.created(location).body(responseDTO);
    }
    @GetMapping("/boards/{boardId}")
    public ResponseEntity<BoardsResponseDTO> getBoardById(@PathVariable String boardId) {
        BoardsResponseDTO board = boardsService.getBoardById(boardId);
        if (board != null) {
            return ResponseEntity.ok(board);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    private void createAndAddStatus(String name, String description, String boardId) {
        addStatusDTO createStatus = new addStatusDTO();
        createStatus.setName(name);
        createStatus.setDescription(description);
        createStatus.setBoards(boardId);

        statusesService.createNewStatus(createStatus);
    }
}