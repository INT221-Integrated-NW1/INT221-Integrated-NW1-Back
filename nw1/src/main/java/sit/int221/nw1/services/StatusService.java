package sit.int221.nw1.services;



import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.StringUtil;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.models.server.Status;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.repositories.server.StatusRepository;

import java.util.ArrayList;
import java.util.List;

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
            errors.add(new MultiFieldException.FieldError("name", "Status name is required."));
        }

        // Validate non-UNIQUE statusName
        if (repository.existsByName(addStatusDTO.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "Status name must be unique."));
        }

        // Validate maximum field size
        validateFieldSize(addStatusDTO.getName(), "name", 1, 50, errors);
        validateFieldSize(addStatusDTO.getDescription(), "description", 0, 255, errors);

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        Status status = modelMapper.map(addStatusDTO, Status.class);
        trim(status);
        return repository.save(status);
    }

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
        Status existingStatus = repository.findById(updateDTOStatus.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found"));

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        // Cannot Edit or Delete "No Status" and "Done"
        if (isSpecialStatus(existingStatus.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "Cannot edit or delete special statuses."));
        }

        // Validate REQUIRED statusName
        if (updateDTOStatus.getName() == null || updateDTOStatus.getName().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("name", "Status name is required."));
        }

        // Validate non-UNIQUE statusName
        if (!existingStatus.getName().equals(updateDTOStatus.getName()) &&
                repository.existsByName(updateDTOStatus.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "Status name must be unique."));
        }

        // Validate maximum field size
        validateFieldSize(updateDTOStatus.getName(), "name", 1, 50, errors);
        validateFieldSize(updateDTOStatus.getDescription(), "description", 0, 255, errors);

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        Status status = modelMapper.map(updateDTOStatus, Status.class);
        status.setId(existingStatus.getId());
        trim(status);
        return repository.save(status);
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
    private void validateFieldSize(String value, String fieldName, int minSize, int maxSize, List<MultiFieldException.FieldError> errors) {
        if (value != null && (value.length() < minSize || value.length() > maxSize)) {
            errors.add(new MultiFieldException.FieldError(fieldName, "Size must be between " + minSize + " and " + maxSize));
        }
    }

    private boolean isSpecialStatus(String statusName) {
        return statusName.equals("No Status") || statusName.equals("Done");
    }
}
