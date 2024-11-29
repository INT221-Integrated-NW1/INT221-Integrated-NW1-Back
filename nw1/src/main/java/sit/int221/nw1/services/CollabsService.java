package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.dto.requestDTO.addCollabDTO;
import sit.int221.nw1.dto.responseDTO.CollabDTO;
import sit.int221.nw1.dto.responseDTO.ReturnCollabDTO;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Collabs;
import sit.int221.nw1.models.server.User;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.repositories.server.CollabsBoardsRepository;
import sit.int221.nw1.repositories.server.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CollabsService {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserRepository myUserRepository;

    @Autowired
    private CollabsBoardsRepository collabsRepository;

    @Autowired
    private BoardsService boardService;

    @Autowired
    private ModelMapper mapper;

    public boolean existsByOidAndBoardId(String oid, String boardId) {
        return collabsRepository.existsByOidAndBoardBoardId(oid, boardId);
    }

    public ReturnCollabDTO addCollab(String boardId, addCollabDTO requestCollabsDTO) {
        Users addUser = usersRepository.findByEmail(requestCollabsDTO.getEmail());
        Boards board = boardService.findBoardById(boardId);
        if (addUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User to add not found");
        }
        boolean isCollaborator = existsByOidAndBoardId(addUser.getOid(), boardId);
        boolean isOwner = board.getUser().getOid().equals(addUser.getOid());
        if (isCollaborator || isOwner) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists"); //409
        }

        User myUserAddUser = mapper.map(addUser, User.class);
        myUserRepository.save(myUserAddUser);

        Collabs collab = new Collabs();
        collab.setOid(addUser.oid);
        collab.setBoardId(boardId);
        collab.setOid(myUserAddUser.getOid());
        collab.setAccessRight(requestCollabsDTO.getAccessRight().equals("WRITE") ? Collabs.AccessRight.WRITE : Collabs.AccessRight.READ);
        collab.setEmail(myUserAddUser.getEmail());

        collabsRepository.save(collab);

        ReturnCollabDTO returnCollabsDTO = new ReturnCollabDTO();
        returnCollabsDTO.setOid(addUser.oid);
        returnCollabsDTO.setBoardID(boardId);
        returnCollabsDTO.setCollaboratorName(addUser.getName());
        returnCollabsDTO.setCollaboratorEmail(addUser.getEmail());
        returnCollabsDTO.setAccessRight(requestCollabsDTO.getAccessRight());

        return returnCollabsDTO;
    }

    public List<CollabDTO> getBoardCollabs (String boardId) {
        Boards board = boardService.findBoardById(boardId);
        List<Collabs> boardCollabs = board.getCollaborators();
        List<CollabDTO> collabs = new ArrayList<>();
        for (Collabs collab : boardCollabs) {
            CollabDTO collabsDTO = mapper.map(collab, CollabDTO.class);
            collabsDTO.setName(collab.getUser().getName());
            collabs.add(collabsDTO);
        }
        return collabs;
    }

    public CollabDTO getBoardCollabByOid (String oid, String boardId) {
        Collabs collab = collabsRepository.findCollabsByOidAndBoardId(oid, boardId);
        if (collab == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collab not found");
        }
        CollabDTO collabDTO = mapper.map(collab, CollabDTO.class);
        collabDTO.setName(collab.getUser().getName());

        return collabDTO;
    }
    public CollabDTO updateCollaboratorAccessRight(String boardId, String collabOid, String accessRight) {
        Collabs collab = collabsRepository.findCollabsByOidAndBoardId(collabOid, boardId);
        if (collab == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found");
        }

        if (!accessRight.equals("READ") && !accessRight.equals("WRITE")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid access right");
        }

        collab.setAccessRight(accessRight.equals("WRITE") ? Collabs.AccessRight.WRITE : Collabs.AccessRight.READ);
        Collabs updatedCollab = collabsRepository.save(collab);

        CollabDTO collabDTO = mapper.map(updatedCollab, CollabDTO.class);
        collabDTO.setName(updatedCollab.getUser().getName());
        return collabDTO;
    }

    public void removeCollaborator(String boardId, String collabOid) {
        Collabs collab = collabsRepository.findCollabsByOidAndBoardId(collabOid, boardId);
        if (collab == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found");
        }

        collabsRepository.delete(collab);
    }

    public boolean isCollaborator(String oid, String boardId) {
        return collabsRepository.existsByOidAndBoardBoardId(oid, boardId);
    }

    public boolean hasWriteAccess(String oid, String boardId) {
        Collabs collab = collabsRepository.findCollabsByOidAndBoardId(oid, boardId);
        return collab != null && collab.getAccessRight() == Collabs.AccessRight.WRITE;
    }
}
