package sit.int221.nw1;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.int221.nw1.dto.responseDTO.addDTORespond;
import sit.int221.nw1.models.server.Tasks;
import sit.int221.nw1.models.server.Status;
import sit.int221.nw1.services.ListMapper;

@Configuration
public class ApplicationConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Create an empty type map for Tasks to addDTORespond to avoid implicit mappings
        modelMapper.createTypeMap(Tasks.class, addDTORespond.class)
                .addMappings(mapper -> {
                    mapper.map(Tasks::getId, addDTORespond::setId);
                    mapper.map(Tasks::getTitle, addDTORespond::setTitle);
                    mapper.map(Tasks::getAssignees, addDTORespond::setAssignees);
                    mapper.map(Tasks::getDescription, addDTORespond::setDescription);

                    // Explicitly map nested Status fields to avoid skipping status mapping
//                    mapper.map(src -> src.getStatus().getId(), addDTORespond::setId);
//                    mapper.map(src -> src.getStatus().getName(), addDTORespond::setName);
                });

        return modelMapper;
    }

    @Bean
    public ListMapper listMapper() {
        return ListMapper.getInstance();
    }
}
