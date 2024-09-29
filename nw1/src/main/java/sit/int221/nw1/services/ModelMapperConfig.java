package sit.int221.nw1.services;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sit.int221.nw1.dto.responseDTO.TaskDTO;
import sit.int221.nw1.models.server.Tasks;


@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.typeMap(Tasks.class, TaskDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getStatus().getName(), TaskDTO::setStatus);
        });


        return modelMapper;
    }
}
