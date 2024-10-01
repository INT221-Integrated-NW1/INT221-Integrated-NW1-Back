package sit.int221.nw1.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sit.int221.nw1.Filter.JwtAuthFilter;
import sit.int221.nw1.exception.AuthExceptionHandler;
import sit.int221.nw1.services.JwtUserDetailsService;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class UserSecurityConfig {
    @Autowired
    JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    JwtAuthFilter jwtAuthFilter;

    @Autowired
    AuthExceptionHandler authExceptionHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(jwtUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET,
                                "/v3/boards","/v3/boards/{id}",
                                "/v3/boards/{boardId}/tasks","/v3/boards/{boardId}/tasks/{taskId}",
                                "/v3/boards/{boardId}/statuses","/v3/boards/{boardId}/statuses/{id}").permitAll()  // อนุญาตให้เข้าถึงเฉพาะ GET requests สำหรับเส้นทางที่กำหนด
                        .requestMatchers("/login").permitAll()  // อนุญาตให้เข้าถึง /login ได้โดยไม่ต้องมีการตรวจสอบสิทธิ์
                        .anyRequest().authenticated())  // การร้องขออื่นๆ ต้องมีการตรวจสอบสิทธิ์
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authExceptionHandler))  // ใช้ authExceptionHandler เมื่อมีข้อผิดพลาดเกี่ยวกับการตรวจสอบสิทธิ์
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(withDefaults());
        return httpSecurity.build();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.csrf(csrf -> csrf.disable())
//                .cors(Customizer.withDefaults())
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/login","/v3/boards").permitAll()
//                        // Allow access to /api/login without authentication
//                        .anyRequest().authenticated())  // All other requests require authentication
//                .exceptionHandling(exceptionHandling -> exceptionHandling
//                        .authenticationEntryPoint(authExceptionHandler))  // Use injected instance
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .httpBasic(withDefaults());
//        return httpSecurity.build();
//    }
}
