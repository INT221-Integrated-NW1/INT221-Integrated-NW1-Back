package sit.int221.nw1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.nw1.config.AuthUser;
import sit.int221.nw1.exception.CustomGlobalExceptionHandler;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.repositories.client.UsersRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Users user = usersRepository.findByName(name);
        if(user == null) {
            System.out.println(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Username or Password is incorrect");
        }
        UserDetails userDetails = new AuthUser(name, user.getPassword());

        return userDetails;
    }
}