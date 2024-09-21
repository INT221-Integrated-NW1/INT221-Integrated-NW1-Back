package sit.int221.nw1.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.dto.requestDTO.JwtDTO;
import sit.int221.nw1.dto.responseDTO.JwtResponseDTO;
import sit.int221.nw1.dto.responseDTO.UsersDTO;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.services.JwtUserDetailsService;
import sit.int221.nw1.services.UsersService;
import sit.int221.nw1.exception.MultiFieldException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23nw1.sit.kmutt.ac.th","http://intproj23.sit.kmutt.ac.th"})
@RequestMapping("/api")

public class UsersController {
    @Autowired
    private UsersService usersService;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @GetMapping("/users")
    public List<UsersDTO> getAllUsers() {
        List<Users> users = usersService.getAllUsers();
        return users.stream()
                .map(us -> modelMapper.map(us, UsersDTO.class))
                .collect(Collectors.toList());

    }
//    @PostMapping("/login")
//    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody JwtDTO jwtRequestDTO) {
//        Users users = usersRepository.findByName(jwtRequestDTO.getUserName());
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(jwtRequestDTO.getUserName(), jwtRequestDTO.getPassword());
//        Authentication authentication = authenticationManager.authenticate(authenticationToken);
//        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(jwtRequestDTO.getUserName());
//        String token = jwtTokenUtil.generateToken(userDetails , users);
//        JwtResponseDTO jwtResponseDTO = JwtResponseDTO.builder().accessToken(token).build();
//        return ResponseEntity.ok(jwtResponseDTO);
//    }
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody JwtDTO jwtRequestDTO) {
        Users users = usersRepository.findByName(jwtRequestDTO.getUserName());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(jwtRequestDTO.getUserName(), jwtRequestDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(jwtRequestDTO.getUserName());
        String token = jwtTokenUtil.generateToken(userDetails,users);
        JwtResponseDTO jwtResponseDTO = JwtResponseDTO.builder().accessToken(token).build();
        return ResponseEntity.ok(jwtResponseDTO);
    }
}


