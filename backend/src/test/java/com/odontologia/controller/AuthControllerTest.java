package com.odontologia.controller;

import com.odontologia.config.SecurityConfig;
import com.odontologia.dto.AuthResponse;
import com.odontologia.dto.LoginRequest;
import com.odontologia.dto.RegisterRequest;
import com.odontologia.dto.UserMeResponse;
import com.odontologia.exception.AccountDisabledException;
import com.odontologia.exception.ConflictException;
import com.odontologia.exception.GlobalExceptionHandler;
import com.odontologia.security.JwtAuthenticationFilter;
import com.odontologia.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2, FilterChain.class);
            chain.doFilter(invocation.getArgument(0, ServletRequest.class), invocation.getArgument(1, ServletResponse.class));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 200 con token")
    void login_CredencialesValidas_DebeRetornar200() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin123");
        AuthResponse response = AuthResponse.builder().token("jwt-token").tipo("Bearer").rol("ADMIN").build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 401 con credenciales inválidas")
    void login_CredencialesInvalidas_DebeRetornar401() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrong");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Usuario o contraseña incorrectos"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login debe retornar 403 cuando la cuenta está deshabilitada")
    void login_CuentaDeshabilitada_DebeRetornar403() throws Exception {
        LoginRequest request = new LoginRequest("inactivo", "admin123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AccountDisabledException("Cuenta deshabilitada"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"inactivo","password":"admin123"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Cuenta deshabilitada"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register debe retornar 201 con usuario PACIENTE")
    void register_Valido_DebeRetornar201() throws Exception {
        RegisterRequest request = new RegisterRequest("paciente1", "paciente1@test.com", "password123");
        UserMeResponse response = UserMeResponse.builder()
                .id(10L).username("paciente1").email("paciente1@test.com").rol("PACIENTE").activo(true).build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"paciente1","email":"paciente1@test.com","password":"password123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("paciente1"))
                .andExpect(jsonPath("$.email").value("paciente1@test.com"))
                .andExpect(jsonPath("$.rol").value("PACIENTE"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register debe retornar 409 cuando username o email ya existe")
    void register_Duplicado_DebeRetornar409() throws Exception {
        RegisterRequest request = new RegisterRequest("admin", "admin@odontologia.com", "password123");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ConflictException("El username o email ya está registrado"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"admin","email":"admin@odontologia.com","password":"password123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me debe retornar 200 con usuario autenticado")
    void me_Autenticado_DebeRetornar200() throws Exception {
        UserMeResponse response = UserMeResponse.builder()
                .id(1L).username("admin").email("admin@odontologia.com").rol("ADMIN").activo(true).build();

        when(authService.getCurrentUser("admin")).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/me")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@odontologia.com"))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/auth/me sin autenticación debe retornar 401")
    void me_SinAutenticacion_DebeRetornar401() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
