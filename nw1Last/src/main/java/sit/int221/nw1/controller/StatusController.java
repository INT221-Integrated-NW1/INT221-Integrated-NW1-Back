package sit.int221.nw1.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.dto.requestDTO.*;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.entities.Status;
import sit.int221.nw1.entities.Tasks;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.services.StatusService;
import sit.int221.nw1.services.TasksService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw1.sit.kmutt.ac.th","http://intproj23.sit.kmutt.ac.th"})
@RequestMapping("/v2/statuses")
public class StatusController {
    @Autowired
    StatusService statusService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TasksService tasksService;

    @GetMapping("")
    public ResponseEntity<Object> getAllStatus() {
        List<Status> status = statusService.getAllStatus();
        List<StatusDTO> statusDTO = status.stream()
                .map(task -> modelMapper.map(task, StatusDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(statusDTO);
    }

    @PostMapping("")
    public ResponseEntity<Object> createStatus(@RequestBody addStatusDTO addStatusDTO) {
            Status newStatus = statusService.createStatus(addStatusDTO);
            addStatusDTO addnewStatus = modelMapper.map(newStatus, addStatusDTO.class);
            URI location = URI.create("");
            return ResponseEntity.created(location).body(addnewStatus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Integer id) {
        Status deleteStatus = statusService.deleteStatus(id);
        deleteStatusDTO delete = modelMapper.map(deleteStatus, deleteStatusDTO.class);
        return ResponseEntity.ok(delete);
    }

    @DeleteMapping("/{oldStatusId}/{newStatusId}")
    public ResponseEntity<Object> transferAndDelete(@PathVariable Integer oldStatusId, @PathVariable Integer newStatusId) {
            tasksService.transferTasks(oldStatusId, newStatusId);
            statusService.deleteStatus(oldStatusId);

            return new ResponseEntity<>("The task(s) have been transferred and the status has been deleted", HttpStatus.OK);
        }

    @GetMapping("/{id}")
    public ResponseEntity<StatusDTO> getStatusById(@PathVariable Integer id) {
        Status status = statusService.findById(id);
        if (status != null) {
            StatusDTO statusDetailDTO = modelMapper.map(status, StatusDTO.class);
            return ResponseEntity.ok(statusDetailDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<updateStatusDTO> updateStatus(@RequestBody updateStatusDTO updateDTOStatus, @PathVariable Integer id) {
        // Set the ID in the DTO
        updateDTOStatus.setId(id);

        // Call the service to update the status
        Status updatedStatus = statusService.updateStatus(updateDTOStatus);

        // Map the updated status to DTO for response
        updateStatusDTO updatedDTOStatus = modelMapper.map(updatedStatus, updateStatusDTO.class);

        return ResponseEntity.ok().body(updatedDTOStatus);
    }

}
