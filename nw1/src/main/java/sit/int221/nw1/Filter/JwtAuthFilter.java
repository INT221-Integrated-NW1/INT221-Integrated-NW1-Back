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
import sit.int221.nw1.services.JwtUserDetailsService;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip authentication for the /api/login endpoint
        if ("/api/login".equals(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");
        String username = null;
        String jwtToken = null;

        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
            logger.error("JWT Token is missing or not well-formed");
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT Token is missing or not well-formed");
            return;
        }

        jwtToken = requestTokenHeader.substring(7);

        try {
            username = jwtTokenUtil.getUsernameFromToken(jwtToken);
        } catch (IllegalArgumentException e) {
            logger.error("Unable to get JWT Token", e);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unable to get JWT Token");
            return;
        } catch (ExpiredJwtException e) {
            logger.error("JWT Token has expired", e);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT Token has expired");
            return;
        } catch (MalformedJwtException e) {
            logger.error("JWT Token is not well-formed", e);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT Token is not well-formed");
            return;
        } catch (SignatureException e) {
            logger.error("JWT Token has been tampered with", e);
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT Token has been tampered with");
            return;
        } catch (Exception e) {
            logger.error("An error occurred while processing JWT Token", e);
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An error occurred while processing JWT Token");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.error("JWT Token is not valid");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "JWT Token is not valid");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}