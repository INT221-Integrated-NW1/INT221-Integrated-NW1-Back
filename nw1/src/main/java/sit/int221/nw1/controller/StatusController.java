package sit.int221.nw1.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.dto.requestDTO.*;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusDetailDTO;
import sit.int221.nw1.dto.responseDTO.TasksDTO;
import sit.int221.nw1.entities.Status;
import sit.int221.nw1.entities.Tasks;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.services.StatusService;
import sit.int221.nw1.services.TasksService;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw1.sit.kmutt.ac.th"})
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
    @GetMapping("/{id}")
    public ResponseEntity<StatusDetailDTO> getStatusById(@PathVariable Integer id) {
        Status status = statusService.findById(id);
        if (status != null) {
            StatusDetailDTO statusDetailDTO = modelMapper.map(status, StatusDetailDTO.class);
            return ResponseEntity.ok(statusDetailDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




    @PostMapping("")
    public ResponseEntity<Status> createStatus(@RequestBody addStatusDTO addStatusDTO) {
        Status status = new Status();
        status.setName(addStatusDTO.getName());
        status.setDescription(addStatusDTO.getDescription());

        Status newStatus = statusService.createStatus(status);
        return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteTask(@PathVariable Integer id) {
        Status deleteStatus = statusService.deleteStatus(id);
        deleteStatusDTO delete = modelMapper.map(deleteStatus, deleteStatusDTO.class);
        return ResponseEntity.ok(delete);
    }

    @DeleteMapping("/{oldStatusId}/{newStatusId}")
    public ResponseEntity<Object> transferAndDelete(@PathVariable Integer oldStatusId, @PathVariable Integer newStatusId) {
        try {
            // Retrieve the old status
            Status oldStatus = statusService.getStatusById(oldStatusId);
            // Check if the old status exists
            if (oldStatus == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The status does not exist.");
            }
            // Transfer tasks from the to-delete status to the destination status
            tasksService.transferTasks(oldStatusId, newStatusId);

            // Delete the to-delete status
            statusService.deleteStatus(oldStatusId);

            return new ResponseEntity<>("The task(s) have been transferred and the status has been deleted", HttpStatus.OK);
        } catch (ItemNotFoundException e) {
            return new ResponseEntity<>("An error has occurred, the status does not exist.", HttpStatus.NOT_FOUND);
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
