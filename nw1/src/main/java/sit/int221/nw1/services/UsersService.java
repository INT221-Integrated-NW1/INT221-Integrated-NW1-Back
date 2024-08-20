package sit.int221.nw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.repositories.client.UsersRepository;

import java.util.List;

@Service
public class UsersService {
    @Autowired
    private UsersRepository repository;
    private final Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(16,  // saltLength
            32,
            1,
            65536,
            4);

    public List<Users> getAllUsers() {
        return repository.findAll();
    }

    public Users login(String username, String rawPassword) {
        Users user = repository.findByUsername(username);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot modify this status");
        }
        return user;
    }

}
