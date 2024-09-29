package sit.int221.nw1.Filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.filter.OncePerRequestFilter;
import sit.int221.nw1.config.JwtTokenUtil;
import sit.int221.nw1.models.client.Users;
import sit.int221.nw1.repositories.client.UsersRepository;
import sit.int221.nw1.services.JwtUserDetailsService;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UsersRepository repository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null) {
            request.setAttribute("error", "No Authorization header found");
        } else {
            try {
                String token = header.substring(7);
                String oid = jwtTokenUtil.getOid(token);

                if (oid != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String username = repository.findByOid(oid).getUsername();
                    UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);

                    if (jwtTokenUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            } catch (io.jsonwebtoken.ExpiredJwtException ex) {
                request.setAttribute("error", "Token Expired");
            } catch (io.jsonwebtoken.MalformedJwtException ex) {
                request.setAttribute("error", "Token is not well-formed JWT");
            } catch (io.jsonwebtoken.SignatureException ex) {
                request.setAttribute("error", "Token has been tampered with");
            } catch (Exception ex) {
                request.setAttribute("error", "Access is Denied");
            }
        }
        filterChain.doFilter(request, response);
    }
}