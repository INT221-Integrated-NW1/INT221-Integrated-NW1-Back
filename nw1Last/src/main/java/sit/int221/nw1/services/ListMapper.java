package sit.int221.nw1.services;


import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;
import sit.int221.nw1.entities.Status;
import sit.int221.nw1.entities.Tasks;

public class ListMapper {
    private static final ListMapper listMapper = new ListMapper();

    private ListMapper() {
    }
    public static ListMapper getInstance() {
        return listMapper;
    }

}
