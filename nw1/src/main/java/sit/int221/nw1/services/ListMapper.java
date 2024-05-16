package sit.int221.nw1.services;


public class ListMapper {
    private static final ListMapper listMapper = new ListMapper();

    private ListMapper() {
    }
    public static ListMapper getInstance() {
        return listMapper;
    }

}
