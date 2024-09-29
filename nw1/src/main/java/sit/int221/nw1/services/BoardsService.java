package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import sit.int221.nw1.dto.requestDTO.BoardsAddRequestDTO;
import sit.int221.nw1.dto.responseDTO.BoardsResponseDTO;
import sit.int221.nw1.dto.responseDTO.OwnerDTO;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.repositories.server.BoardsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardsService {
    @Autowired
    private BoardsRepository boardsRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UsersRepository usersRepository;
    private static final Logger logger = LoggerFactory.getLogger(BoardsService.class);


    public List<Boards> findBoardByOid(String oid) {
        return boardsRepository.findByUserOid(oid);
    }


    public Boards createBoard(Boards board) {
        return boardsRepository.save(board);
    }



    public Users findByOid(String oid) {
        Users user = usersRepository.findByOid(oid);
        if (user == null) {
            throw new ItemNotFoundException("");
        }
        return user;
    }
//    public BoardsResponseDTO getBoardById(String boardId) {
//        Boards board = boardsRepository.findById(boardId)
//                .orElseThrow(() -> new ItemNotFoundException("Board not found with ID: " + boardId));
//
//        // Map board entity to DTO
//        BoardsResponseDTO responseDTO = new BoardsResponseDTO();
//        responseDTO.setBoardId(board.getBoardId());
//        responseDTO.setBoard_name(board.getBoardName());
//
//        // Fetch owner details (User) by OID
//        Users owner = findByOid(board.getOid());
//        OwnerDTO ownerDTO = new OwnerDTO();
//        ownerDTO.setOid(owner.getOid());
//        ownerDTO.setName(owner.getUsername());
//        responseDTO.setOwner(ownerDTO);
//
//        return responseDTO;
//    }


}
