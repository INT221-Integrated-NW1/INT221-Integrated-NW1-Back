package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusesRespondDTO;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.repositories.server.StatusesRepository;
import sit.int221.nw1.repositories.server.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusesService {
    @Autowired
    private StatusesRepository statusesRepository;

    @Autowired
    private TasksRepository tasksRepository;

    @Autowired
    private BoardsRepository boardsRepository;

    @Autowired
    private ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(StatusesService.class);


    public List<StatusDTO> getAllStatusesByBoardId(String boardsId) {
        Boards boards =  boardsRepository.findById(boardsId).orElseThrow(ItemNotFoundException::new);
        return boards.getStatuses().stream().sorted(Comparator.comparing(Statuses::getId)).map(status ->
                modelMapper.map(status, StatusDTO.class)
        ).collect(Collectors.toList());
    }
    public StatusesRespondDTO getStatusesByBoard_idAndByStatusID(String boardsId, Integer statusId) {
        Statuses statuses =  statusesRepository.findByIdAndBoardsBoardId(statusId,boardsId).orElseThrow(ItemNotFoundException::new);

        StatusesRespondDTO statusesRespondDTO = modelMapper.map(statuses, StatusesRespondDTO.class);
        return statusesRespondDTO;
    }

    public Statuses createNewStatus(addStatusDTO addStatusDTO) {
//        checkStatusNameExists(statusAddRequestDTO.getName());

        Statuses status = modelMapper.map(addStatusDTO, Statuses.class);
        trimAndValidateStatusFields(status, addStatusDTO.getName(), addStatusDTO.getDescription());

        Boards boards = boardsRepository.findById(addStatusDTO.getBoards()).orElseThrow(ItemNotFoundException::new);
        status.setBoards(boards);
        return statusesRepository.save(status);
    }

    public Statuses updateStatus(updateStatusDTO updateStatusDTO , Integer statusId) {
//        checkStatusNameExists(statusUpdateRequestDTO.getName());
        Statuses status = modelMapper.map(updateStatusDTO, Statuses.class);
        trimAndValidateStatusFields(status, updateStatusDTO.getName(), updateStatusDTO.getDescription());

        Statuses statuses = statusesRepository.findById(statusId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        if ("No Status".equals(statuses.getName()) || "Done".equals(statuses.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify this status");
        }
        Boards boards = boardsRepository.findById(updateStatusDTO.getBoards()).orElseThrow(ItemNotFoundException::new);
        status.setName(updateStatusDTO.getName());
        status.setDescription(updateStatusDTO.getDescription());
        status.setBoards(boards);

        return statusesRepository.save(status);
    }


    public Statuses deleteStatus(Integer statusId, String boardId) {
        Statuses status = statusesRepository.findByIdAndBoardsBoardId(statusId , boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
        if ("No Status".equals(status.getName()) || "Done".equals(status.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete this status");
        }
        if (!tasksRepository.findByStatus_IdAndBoards_BoardId(statusId ,boardId ).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete status with associated tasks");
        }
        statusesRepository.delete(status);
        return status;
    }
//    public Statuses reassignAndDeleteStatus(Integer statusId, Integer newStatusId, String boardId) {
//        if (statusId.equals(newStatusId)) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination statuses cannot be the same");
//        }
//
//        Statuses oldStatus = statusesRepository.findByIdAndBoardsBoardId(statusId,boardId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source status not found"));
//
//        Statuses newStatus = statusesRepository.findByIdAndBoardsBoardId(newStatusId,boardId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination status not found"));
//        logger.info("Old Status: ID = {}, Name = {}", oldStatus.getId(), oldStatus.getName());
//        logger.info("New Status: ID = {}, Name = {}", newStatus.getId(), newStatus.getName());
//
//
//        List<Tasks> tasksWithThisStatus = tasksRepository.findByStatus_IdAndBoards_BoardId(statusId,boardId);
//
//        if (tasksWithThisStatus.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination status not found");
//        }
//        tasksWithThisStatus.forEach(task -> {
//            task.setStatus(newStatus);
//            tasksRepository.save(task);
//        });
//
//        statusesRepository.delete(oldStatus);
//        return oldStatus;
//    }
public Statuses reassignAndDeleteStatus(Integer statusId, Integer newStatusId, String boardId) {
    if (statusId.equals(newStatusId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination statuses cannot be the same");
    }

    Statuses oldStatus = statusesRepository.findByIdAndBoardsBoardId(statusId, boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source status not found"));

    Statuses newStatus = statusesRepository.findByIdAndBoardsBoardId(newStatusId, boardId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination status not found"));

    logger.info("Reassigning tasks from status {} to {}", oldStatus.getName(), newStatus.getName());

    List<Tasks> tasksWithThisStatus = tasksRepository.findByStatus_IdAndBoards_BoardId(statusId, boardId);

    if (!tasksWithThisStatus.isEmpty()) {
        tasksWithThisStatus.forEach(task -> {
            task.setStatus(newStatus);
            tasksRepository.save(task);
        });
    }

    // Now delete the old status
    statusesRepository.delete(oldStatus);
    return oldStatus;
}





    private void trimAndValidateStatusFields(Statuses status, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name cannot be null or empty!");
        }
        if (description != null && description.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status description cannot be empty!");
        }
        if (description != null){
            status.setDescription(description.trim());

        }else {
            status.setName(name.trim());
        }
    }

    private void checkStatusNameExists(String name) {
        if (statusesRepository.findByName(name) != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name already exists!");
        }
    }

}
