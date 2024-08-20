package sit.int221.nw1.controller;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import sit.int221.nw1.dto.responseDTO.UsersDTO;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.services.UsersService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5174", "http://ip23nw1.sit.kmutt.ac.th","http://intproj23.sit.kmutt.ac.th"})
@RequestMapping("/api")

public class UsersController {
    @Autowired
    private UsersService usersService;

//    @Autowired
//    private JwtUserDetailsService jwtUserDetailsService;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/users")
    public List<UsersDTO> getAllUsers() {
        List<Users> users = usersService.getAllUsers();
        return users.stream()
                .map(us -> modelMapper.map(us, UsersDTO.class))
                .collect(Collectors.toList());

    }

//    @PostMapping("/login")
//    public ResponseEntity<String> login(@Valid @RequestBody JwtRequestDTO jwtRequestDTO) {
//        Users user = usersService.login(jwtRequestDTO.getUsername(), jwtRequestDTO.getPassword());
////        if (user == null) { เปิดนะ
////            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
////        }
//        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(jwtRequestDTO.getUsername());
//        String token = jwtTokenUtil.generateToken(userDetails);
//        return ResponseEntity.ok(token);
//    }

}
