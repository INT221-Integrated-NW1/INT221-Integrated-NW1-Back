package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.nw1.dto.responseDTO.*;
import sit.int221.nw1.exception.AccessDeniedException;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Collabs;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.repositories.server.BoardsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sit.int221.nw1.repositories.server.CollabsBoardsRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// BoardsService.java
@Service
public class BoardsService {
    @Autowired
    private BoardsRepository boardsRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CollabsBoardsRepository collabsRepository;

    private static final Logger logger = LoggerFactory.getLogger(BoardsService.class);

    // Find boards accessible by the user (both PUBLIC and PRIVATE owned by the user)

    public List<CollabsBoardsDTO> findCollaborateBoardsByOid (String oid) {
        List<Collabs> allCollabs = collabsRepository.findCollabsByOid(oid);
        List<CollabsBoardsDTO> boards = new ArrayList<>();
        for (Collabs collab : allCollabs) {
            Boards board = findBoardById(collab.getBoardId());
            CollabsBoardsDTO collabsBoardsDTO = mapper.map(board, CollabsBoardsDTO.class);

            collabsBoardsDTO.setAccessRight(collab.getAccessRight().name().equals("WRITE") ? "WRITE" : "READ");
            collabsBoardsDTO.setAddedOn(collab.getAddedOn());
            OwnerDTO ownerDTO = new OwnerDTO(board.getUser().getOid(), board.getUser().getName());
            collabsBoardsDTO.setOwner(ownerDTO);
            boards.add(collabsBoardsDTO);
        }
        return boards;
    }


    public BoardListDTO getAllBoardsByOid(String oid) {
        List<BoardsResponseDTO> personalBoards = new ArrayList<>();
        List<Boards> allPersonalBoards =  findBoardByOid(oid);
        for (Boards board : allPersonalBoards) {
            BoardsResponseDTO boardsResponseDTO = mapper.map(board, BoardsResponseDTO.class);
            UserResponseDTO userResponseDTO= new UserResponseDTO(board.getUser().getOid(), board.getUser().getName());
            boardsResponseDTO.setUser(userResponseDTO);

            personalBoards.add(boardsResponseDTO);
        }

        List<CollabsBoardsDTO> allCollaborateBoards = findCollaborateBoardsByOid(oid);

        BoardListDTO allBoards = new BoardListDTO();
        allBoards.setPERSONAL_BOARD(personalBoards);
        allBoards.setCOLLABORATE_BOARD(allCollaborateBoards);
        return allBoards;
    }

    public boolean getIsBoardCollaborator(String oid, String boardId) {
        return collabsRepository.existsByOidAndBoardBoardId(oid, boardId);
    }


    public List<Boards> findAccessibleBoards(String oid) {
        List<Boards> userBoards = boardsRepository.findByUserOid(oid);
        List<Boards> publicBoards = boardsRepository.findByVisibility("PUBLIC");

        // Combine the two lists, avoiding duplicates if any
        Set<String> boardIds = new HashSet<>();
        List<Boards> accessibleBoards = new ArrayList<>();

        for (Boards board : userBoards) {
            accessibleBoards.add(board);
            boardIds.add(board.getBoardId());
        }

        for (Boards board : publicBoards) {
            if (!boardIds.contains(board.getBoardId())) {
                accessibleBoards.add(board);
            }
        }

        return accessibleBoards;
    }

    // Find only public boards
    public List<Boards> findPublicBoards() {
        return boardsRepository.findByVisibility("PUBLIC");
    }

    // Create a new board
    public Boards createBoard(Boards board) {
        return boardsRepository.save(board);
    }

    // Find board by ID and perform visibility check
    public Boards findBoardByIdWithVisibilityCheck(String id, String oid) {
        Boards board = boardsRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Board not found"));

        if ("PUBLIC".equals(board.getVisibility())) {
            return board;
        }

        if (oid != null && oid.equals(board.getUser().getOid())) {
            return board;
        }

        throw new AccessDeniedException("Access denied to this board");
    }

    // Update board visibility
    public Boards updateBoardVisibility(String id, String visibility, String oid) {
        Boards board = boardsRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Board not found"));

        if (!oid.equals(board.getUser().getOid())) {
            throw new AccessDeniedException("Only the board owner can change visibility");
        }

        if (!"PRIVATE".equals(visibility) && !"PUBLIC".equals(visibility)) {
            throw new IllegalArgumentException("Invalid visibility value");
        }

        board.setVisibility(visibility);
        return boardsRepository.save(board);
    }

    // Other existing methods...
    public List<Boards> findBoardByOid(String oid) {
        return boardsRepository.findByUserOid(oid);
    }

    public Boards findBoardById(String id) {
        return boardsRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Board not found"));
    }




    public Users findByOid(String oid) {
        Users user = usersRepository.findByOid(oid);
        if (user == null) {
            throw new ItemNotFoundException("");
        }
        return user;
    }



}
