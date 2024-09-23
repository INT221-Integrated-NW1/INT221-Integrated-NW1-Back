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
    private UsersRepository userRepository;
    private final Argon2PasswordEncoder passwordEncoder = new Argon2PasswordEncoder(16,  // saltLength
            32,  // hashLength
            1,   // parallelism
            65536,  // memory
            4);

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Users login(String username, String rawPassword) {
        Users user = userRepository.findByName(username);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or password is incorrect");
        }
        return user;
    }
}
