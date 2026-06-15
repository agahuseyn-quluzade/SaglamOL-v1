package az.saglamol.iam.controller;

import az.saglamol.iam.service.UserManagementService;
import az.saglamol.iam.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserManagementController.class)
@Import(UserManagementSecurityTest.SecurityTestConfig.class)
class UserManagementSecurityTest {

    private final MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserManagementSecurityTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void userListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/iam/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PATIENT")
    void userListRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/iam/users"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/api/v1/iam/users/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .build();
        }
    }
}
