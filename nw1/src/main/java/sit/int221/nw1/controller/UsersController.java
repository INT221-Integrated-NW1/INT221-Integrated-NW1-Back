package sit.int221.nw1.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://ip23sy3.sit.kmutt.ac.th","http://intproj23.sit.kmutt.ac.th"})
@RequestMapping("")

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
    private UsersRepository repository;

    @Autowired
    AuthenticationManager authenticationManager;

    @GetMapping("/users")
    public List<UsersDTO> getAllUsers() {
        List<Users> users = usersService.getAllUsers();
        return users.stream()
                .map(us -> modelMapper.map(us, UsersDTO.class))
                .collect(Collectors.toList());

    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody JwtDTO jwtRequestDTO) {
        // Authenticate user
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(jwtRequestDTO.getUserName(), jwtRequestDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // Optional: set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(jwtRequestDTO.getUserName());
        Users user = repository.findByName(jwtRequestDTO.getUserName());

        // Ensure user is found
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 401 if user not found
        }

        // Generate access and refresh tokens
        String accessToken = jwtTokenUtil.generateToken(userDetails, user);
        String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails, user); // New method for refresh token

        // Return both tokens
        JwtResponseDTO jwtResponseDTO = JwtResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken) // Include refresh token in response
                .build();

        return ResponseEntity.ok(jwtResponseDTO);
    }
    @PostMapping("/token")
    public ResponseEntity<JwtResponseDTO> refreshAccessToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 401 if no token provided
        }

        String refreshToken = authHeader.substring(7);
        try {
            // Validate the refresh token
            if (jwtTokenUtil.isTokenExpired(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 401 if token is expired
            }

            // Extract OID from the refresh token
            String oid = jwtTokenUtil.getOid(refreshToken);

            // Use loadUserByOid to load the user
            UserDetails userDetails = jwtUserDetailsService.loadUserByOid(oid);

            // Generate a new access token
            String newAccessToken = jwtTokenUtil.generateToken(userDetails, (Users) userDetails);

            JwtResponseDTO jwtResponseDTO = JwtResponseDTO.builder()
                    .accessToken(newAccessToken)
                    .build();

            return ResponseEntity.ok(jwtResponseDTO);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 401 if user not found
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

}
