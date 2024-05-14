package sit.int221.nw1.services;



import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.StringUtil;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.entities.Status;
import sit.int221.nw1.entities.Tasks;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.repositories.StatusRepository;

import java.util.List;
import java.util.Optional;

@Service
public class StatusService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    StatusRepository repository;

    public List<Status> getAllStatus() {
        return repository.findAll();
    }

    public Status findById(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status" + " " + id + " " + "does not exist")
        );

    }


    public Status createStatus(Status status) {
        trim(status);
        return repository.save(status);
    }
//    public Status createStatus(addStatusDTO addStatusDTO){
//        if (addStatusDTO.getName() == null || addStatusDTO.getName().isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name cannot be null or empty");
//        }
//        Status status = modelMapper.map(addStatusDTO , Status.class);
//        trim(status);
//        return repository.save(status);
//    }

    //trim method
    private void trim(Status status){
        status.setName(StringUtil.trimToNull(status.getName()));
        status.setDescription(StringUtil.trimToNull(status.getDescription()));
    }
    public Status deleteStatus(Integer id) {
        Status status = repository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("NOT FOUND"
                ));
        repository.deleteById(id);
        return status;
    }


    public Status updateStatus(updateStatusDTO updateDTOStatus) {
        Integer id = updateDTOStatus.getId();

        if (id != null && id.equals(1)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update status with ID 1.");
        }

        Status existingStatus = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        if (updateDTOStatus.getName() == null || updateDTOStatus.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name cannot be null or empty");
        }

        Status status = modelMapper.map(updateDTOStatus, Status.class);
        // Set 'status_id' from the existing status
        status.setId(existingStatus.getId());
        trim(status);
        return repository.save(status);
    }

    public Status getStatusById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
    }
}
