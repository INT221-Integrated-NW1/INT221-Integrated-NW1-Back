package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.Utils.NanoUtil;
import sit.int221.nw1.dto.requestDTO.addStatusDTO;
import sit.int221.nw1.dto.requestDTO.updateStatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusDTO;
import sit.int221.nw1.dto.responseDTO.StatusesRespondDTO;
import sit.int221.nw1.exception.ItemNotFoundException;
import sit.int221.nw1.exception.MultiFieldException;
import sit.int221.nw1.models.server.Boards;
import sit.int221.nw1.models.server.Statuses;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.repositories.server.BoardsRepository;
import sit.int221.nw1.repositories.server.StatusesRepository;
import sit.int221.nw1.repositories.server.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    @Autowired
    private NanoUtil nanoUtil;
    private static final Logger logger = LoggerFactory.getLogger(StatusesService.class);


    public List<Statuses> getAllStatus() {
        return statusesRepository.findAll();
    }

    public Statuses getStatusById(String Id) {
        return statusesRepository.findStatusesById(Id).orElseThrow(() -> new ItemNotFoundException("NOT FOUND"));
    }

    public Statuses createStatus(Statuses status) {
        List<MultiFieldException.FieldError> errors = new ArrayList<>();


        if (statusesRepository.existsByName(status.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "must be unique"));
        }

        if (status.getName() == null || status.getName().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("name", "must not be null"));
        }


        if (status.getName() == null || status.getName().length() > 50) {
            errors.add(new MultiFieldException.FieldError("name", "size must be between 0 and 50"));
        }
        if (status.getDescription() != null && status.getDescription().length() > 200) {
            errors.add(new MultiFieldException.FieldError("description", "size must be between 0 and 200"));
        }

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        String statusId = nanoUtil.nanoIdGenerate(15);
        status.setId(statusId);
        try {
            return statusesRepository.save(status);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to insert new status.", e);
        }
    }

    public Statuses updateStatus(String id, Statuses status) {
        Statuses existingStatus = statusesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status with ID " + id + " not found"));

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        if (status.getName() == null || status.getName().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("name", "must not be null."));
        }
        if (!existingStatus.getName().equals(status.getName()) && statusesRepository.existsByName(status.getName())) {
            errors.add(new MultiFieldException.FieldError("name", "must be unique"));
        }

        if (status.getName() == null || status.getName().length() > 50) {
            errors.add(new MultiFieldException.FieldError("name", "size must be between 0 and 50"));
        }
        if (status.getDescription() != null && status.getDescription().length() > 200) {
            errors.add(new MultiFieldException.FieldError("description", "size must be between 0 and 200"));
        }
        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        existingStatus.setName(status.getName());
        existingStatus.setDescription(status.getDescription());

        try {
            return statusesRepository.save(existingStatus);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update status.", e);
        }
    }


    @Transactional(transactionManager = "serverTransactionManager")
    public Statuses deleteStatus(String id) {
        Statuses status = statusesRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status with ID " + id + " not found"));

        List<MultiFieldException.FieldError> errors = new ArrayList<>();

        if (!status.getTasks().isEmpty()) {
            errors.add(new MultiFieldException.FieldError("status", "Cannot delete status with ID " + id + " because it is referenced by other entities."));
        }

        if (!errors.isEmpty()) {
            throw new MultiFieldException(errors);
        }

        try {
            statusesRepository.deleteById(id);
            return status;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete status.", e);
        }
    }

    @Transactional(transactionManager = "serverTransactionManager")
    public void transferAndDeleteStatus(String oldStatusId, String newStatusId) {
        Statuses oldStatus = statusesRepository.findById(oldStatusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "the specified status for task transfer does not exist"));

        Statuses newStatus = statusesRepository.findById(newStatusId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "the specified status for task transfer does not exist"));


        if (newStatus.getId() == oldStatus.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "destination status for task transfer must be different from current status");
        }

        List<Tasks> tasksToUpdate = tasksRepository.findByStatus(oldStatus);

        for (Tasks task : tasksToUpdate) {
            task.setStatus(newStatus);
        }

        tasksRepository.saveAll(tasksToUpdate);

        statusesRepository.delete(oldStatus);
    }


    private void trimAndValidateStatusFields(Statuses status, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name cannot be null or empty!");
        }
        if (description != null && description.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status description cannot be empty!");
        }
        if (description != null) {
            status.setDescription(description.trim());

        } else {
            status.setName(name.trim());
        }
    }


}
