package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.nw1.dto.requestDTO.BoardsAddRequestDTO;
import sit.int221.nw1.dto.responseDTO.BoardsResponseDTO;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.repositories.server.BoardsRepository;

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


    public List<BoardsResponseDTO> getAllBoards(){
        List<Boards> boards = boardsRepository.findAll();
        return boards.stream().map(board ->
                modelMapper.map(board, BoardsResponseDTO.class)
        ).collect(Collectors.toList());
    }

    public Boards createBoards(BoardsAddRequestDTO boardsAddRequestDTO){
        Boards boards = modelMapper.map(boardsAddRequestDTO, Boards.class);

        return boardsRepository.save(boards);
    }

    public Users findByOid(String oid) {
        Users user = usersRepository.findByOid(oid);
        if (user == null) {
            throw new ItemNotFoundException();
        }
        return user;
    }
}
