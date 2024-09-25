package sit.int221.nw1.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.deleteStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusesRespondDTO;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.services.StatusesService;

import java.net.URI;
import java.util.List;


@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://ip23nw3.sit.kmutt.ac.th:3333","http://intproj23.sit.kmutt.ac.th"})

@RequestMapping("/v3/boards")

public class StatusesController {
    @Autowired
    private StatusesService statusesService;

    @Autowired
    private ModelMapper modelMapper;

//    @Autowired
//    private BoardsService boardsService;


    @GetMapping("/{boardId}/statuses")
    public List<StatusDTO> getAllStatus(@PathVariable String boardId){
        List<StatusDTO> statuses = statusesService.getAllStatusesByBoardId(boardId);
        return statuses;
    }
    @GetMapping("/{boardId}/statuses/{statusId}")
    public StatusesRespondDTO getStatuses(@PathVariable String boardId, @PathVariable String statusId){
        return statusesService.getStatusesByBoard_idAndByStatusID(boardId, Integer.valueOf(statusId));
    }
    @PostMapping("/{boardId}/statuses")
    public ResponseEntity<addStatusDTO> addStatuses(@Valid @RequestBody addStatusDTO statusAddRequestDTO, @PathVariable String boardId){
        statusAddRequestDTO.setBoards(boardId);
        Statuses createStatus = statusesService.createNewStatus(statusAddRequestDTO);
        addStatusDTO addRequestDTO = modelMapper.map(createStatus, addStatusDTO.class);
        URI location = URI.create("/"+boardId+"/statuses/");
        return ResponseEntity.created(location).body(addRequestDTO);
    }

    @PutMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<updateStatusDTO> updateStatuses (@RequestBody updateStatusDTO statusAddRequestDTO, @PathVariable String boardId, @PathVariable Integer statusId) {
        statusAddRequestDTO.setBoards(boardId);
        StatusesRespondDTO updatedStatus = statusesService.getStatusesByBoard_idAndByStatusID(boardId,statusId);
        updateStatusDTO updatedStatusDTO = modelMapper.map(updatedStatus, updateStatusDTO.class);
        updatedStatusDTO.setName(statusAddRequestDTO.getName());
        updatedStatusDTO.setDescription(statusAddRequestDTO.getDescription());
        updatedStatusDTO.setBoards(statusAddRequestDTO.getBoards());
        statusesService.updateStatus(updatedStatusDTO,statusId);
        return ResponseEntity.ok().body(updatedStatusDTO);
    }
// ใช้ไม่ได้
    @DeleteMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<deleteStatusDTO>
    deleteStatus(@PathVariable Integer statusId , @PathVariable  String boardId) {
        Statuses deletedStatus = statusesService.deleteStatus(statusId , boardId);
        deleteStatusDTO deletedStatusDTO = modelMapper.map(deletedStatus, deleteStatusDTO.class);
//        deletedStatus.setBoards(boardId);
        return ResponseEntity.ok().body(deletedStatusDTO);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}/{newStatusId}")
    public ResponseEntity<deleteStatusDTO> deleteStatusAndReassign(@PathVariable Integer statusId, @PathVariable Integer newStatusId ,@PathVariable String boardId) {
        Statuses deletedStatus = statusesService.reassignAndDeleteStatus(statusId, newStatusId,boardId);
        deleteStatusDTO deletedStatusDTO = modelMapper.map(deletedStatus, deleteStatusDTO.class);
        return ResponseEntity.ok().body(deletedStatusDTO);
    }
}
