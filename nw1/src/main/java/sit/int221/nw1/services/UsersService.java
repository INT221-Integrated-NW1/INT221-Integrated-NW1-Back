package sit.int221.nw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.repositories.client.UsersRepository;

import java.util.List;
@Service
public class UsersService {
    @Autowired
    private UsersRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Users> getAllUsers() {
        return repository.findAll();
    }

    public Users login(String name) {
        Users user = repository.findByName(name);
        return user;
    }
}

