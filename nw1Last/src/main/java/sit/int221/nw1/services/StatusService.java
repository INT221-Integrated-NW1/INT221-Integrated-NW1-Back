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
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.repositories.StatusRepository;

import java.util.ArrayList;
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

    public Status createStatus(addStatusDTO addStatusDTO) {
        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        // Validate REQUIRED statusName
        if (addStatusDTO.getName() == null || addStatusDTO.getName().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("name", "must not be null"));
        }

        // Validate non-UNIQUE statusName
        if (repository.existsByName(addStatusDTO.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "must be unique"));
        }
        if (addStatusDTO.getName() == null || addStatusDTO.getName().length() > 50) {
            errors.add(new MultiFieldException.FieldError("name", "size must be between 0 and 50"));
        }
        if (addStatusDTO.getDescription() != null && addStatusDTO.getDescription().length() > 200) {
            errors.add(new MultiFieldException.FieldError("description", "size must be between 0 and 200"));
        }


        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        Status status = modelMapper.map(addStatusDTO, Status.class);
        trim(status);
        return repository.save(status);
    }

    public Status updateStatus(updateStatusDTO updateDTOStatus) {
        Status existingStatus = repository.findById(updateDTOStatus.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        // Cannot Edit or Delete "No Status" and "Done"
        if (isNoStatus(existingStatus.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "The status name 'No Status' cannot be changed"));
        }
        if (isDone(existingStatus.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "The status name 'Done' cannot be changed"));
        }

        // Validate REQUIRED statusName
        if (updateDTOStatus.getName() == null || updateDTOStatus.getName().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("name", "must not be null."));
        }
        // Validate non-UNIQUE statusName
        if (!existingStatus.getName().equals(updateDTOStatus.getName()) &&
                repository.existsByName(updateDTOStatus.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "must be unique"));
        }

        if (updateDTOStatus.getName() == null || updateDTOStatus.getName().length() > 50) {
            errors.add(new MultiFieldException.FieldError("name", "size must be between 0 and 50"));
        }
        if (updateDTOStatus.getDescription() != null && updateDTOStatus.getDescription().length() > 200) {
            errors.add(new MultiFieldException.FieldError("description", "size must be between 0 and 200"));
        }
        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        Status status = modelMapper.map(updateDTOStatus, Status.class);
        status.setId(existingStatus.getId());
        trim(status);
        return repository.save(status);
    }

    //trim method
    private void trim(Status status) {

        status.setName(StringUtil.trimToNull(status.getName()));
        status.setDescription(StringUtil.trimToNull(status.getDescription()));
    }


    public Status deleteStatus(Integer id) {
        Status status = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        if (isNoStatus(status.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "The status 'No Status' cannot be deleted"));
        }
        if (isDone(status.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "The status 'Done' cannot be deleted"));
        }
        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }
        repository.deleteById(id);
        return status;
    }


    public Status getStatusById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));
    }

    public Status findById(Integer id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status" + " " + id + " " + "does not exist")
        );
    }

    private boolean isNoStatus(String statusName) {
        return statusName.equals("No Status");
    }

    private boolean isDone(String statusName) {
        return statusName.equals("Done");
    }
}
