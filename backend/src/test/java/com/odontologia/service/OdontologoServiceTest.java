package com.odontologia.service;

import com.odontologia.dto.OdontologoRequestDTO;
import com.odontologia.dto.OdontologoResponseDTO;
import com.odontologia.exception.ConflictException;
import com.odontologia.model.Odontologo;
import com.odontologia.model.Rol;
import com.odontologia.model.Usuario;
import com.odontologia.repository.OdontologoRepository;
import com.odontologia.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OdontologoServiceTest {

    @Autowired
    private OdontologoService odontologoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OdontologoRepository odontologoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    @DisplayName("El alta debe crear usuario ODONTOLOGO con hash BCrypt(10) vinculado al odontólogo")
    void crear_Valido_DebeCrearUsuarioYProfesionalVinculados() {
        String username = "odontologo_svc_alta";
        String matricula = "MAT-SVC-001";
        String password = "password123";

        OdontologoResponseDTO respuesta = odontologoService.crear(request(username, matricula, password));

        Usuario usuario = usuarioRepository.findByUsernameIncludingInactive(username).orElseThrow();
        Odontologo odontologo = odontologoRepository.findByMatricula(matricula).orElseThrow();

        assertThat(usuario.getRol()).isEqualTo(Rol.ODONTOLOGO);
        assertThat(usuario.getPasswordHash()).matches("\\$2[aby]\\$10\\$[./A-Za-z0-9]{53}");
        assertThat(passwordEncoder.matches(password, usuario.getPasswordHash())).isTrue();
        assertThat(odontologo.getUsuario().getId()).isEqualTo(usuario.getId());
        assertThat(respuesta.getId()).isEqualTo(odontologo.getId());
        assertThat(respuesta.getUsername()).isEqualTo(username);
        assertThat(respuesta.getMatricula()).isEqualTo(matricula);
        assertThat(Arrays.stream(OdontologoResponseDTO.class.getDeclaredFields()).map(Field::getName))
                .doesNotContain("password", "passwordHash");
    }

    @Test
    @DisplayName("Si falla el segundo insert, la transacción debe hacer rollback sin dejar registros parciales")
    void crear_FallaSegundoInsert_DebeHacerRollback() {
        transactionTemplate.executeWithoutResult(status -> {
            Usuario usuario = usuarioRepository.save(Usuario.builder()
                    .username("odontologo_svc_preexistente")
                    .email("odontologo_svc_preexistente@clinica.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .rol(Rol.ODONTOLOGO)
                    .build());
            odontologoRepository.save(Odontologo.builder()
                    .nombre("Pre")
                    .apellido("Existente")
                    .matricula("MAT-SVC-DUP")
                    .especialidad("Ortodoncia")
                    .usuario(usuario)
                    .build());
        });

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(
                status -> odontologoService.crear(request("odontologo_svc_rollback", "MAT-SVC-DUP", "password123"))))
                .isInstanceOf(RuntimeException.class);

        assertThat(usuarioRepository.findByUsernameIncludingInactive("odontologo_svc_rollback")).isEmpty();
    }

    @Test
    @DisplayName("Matrícula repetida debe lanzar ConflictException sin dejar registros parciales")
    void crear_MatriculaDuplicada_DebeLanzarConflictException() {
        guardarProfesionalPreexistente("odontologo_svc_mat_existente", "odontologo_svc_mat_existente@clinica.com", "MAT-SVC-DUP2");

        assertThatThrownBy(() -> odontologoService.crear(
                request("odontologo_svc_mat_nuevo", "MAT-SVC-DUP2", "password123")))
                .isInstanceOf(ConflictException.class);

        assertThat(usuarioRepository.findByUsernameIncludingInactive("odontologo_svc_mat_nuevo")).isEmpty();
        assertThat(odontologoRepository.findByMatriculaIncludingInactive("MAT-SVC-DUP2")).isPresent();
    }

    @Test
    @DisplayName("Username de cuenta inactiva debe lanzar ConflictException")
    void crear_UsernameInactivo_DebeLanzarConflictException() {
        Usuario inactivo = guardarUsuario("odontologo_svc_user_inactivo", "odontologo_svc_user_inactivo@clinica.com");
        usuarioRepository.deleteById(inactivo.getId());

        assertThat(usuarioRepository.findByUsernameIncludingInactive("odontologo_svc_user_inactivo"))
                .isPresent()
                .get()
                .extracting(Usuario::getActivo)
                .isEqualTo(false);

        assertThatThrownBy(() -> odontologoService.crear(request(
                "odontologo_svc_user_inactivo", "odontologo_svc_user_nuevo@clinica.com", "MAT-SVC-NEW-USER", "password123")))
                .isInstanceOf(ConflictException.class);

        assertThat(odontologoRepository.findByMatricula("MAT-SVC-NEW-USER")).isEmpty();
    }

    @Test
    @DisplayName("Email de cuenta inactiva debe lanzar ConflictException")
    void crear_EmailInactivo_DebeLanzarConflictException() {
        Usuario inactivo = guardarUsuario("odontologo_svc_email_inactivo", "odontologo_svc_email_inactivo@clinica.com");
        usuarioRepository.deleteById(inactivo.getId());

        assertThatThrownBy(() -> odontologoService.crear(request(
                "odontologo_svc_email_nuevo", "odontologo_svc_email_inactivo@clinica.com",
                "MAT-SVC-NEW-EMAIL", "password123")))
                .isInstanceOf(ConflictException.class);

        assertThat(usuarioRepository.findByUsernameIncludingInactive("odontologo_svc_email_nuevo")).isEmpty();
        assertThat(odontologoRepository.findByMatricula("MAT-SVC-NEW-EMAIL")).isEmpty();
    }

    private void guardarProfesionalPreexistente(String username, String email, String matricula) {
        transactionTemplate.executeWithoutResult(status -> {
            Usuario usuario = usuarioRepository.save(Usuario.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode("password123"))
                    .rol(Rol.ODONTOLOGO)
                    .build());
            odontologoRepository.save(Odontologo.builder()
                    .nombre("Pre")
                    .apellido("Existente")
                    .matricula(matricula)
                    .especialidad("Ortodoncia")
                    .usuario(usuario)
                    .build());
        });
    }

    private Usuario guardarUsuario(String username, String email) {
        return usuarioRepository.save(Usuario.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode("password123"))
                .rol(Rol.ODONTOLOGO)
                .build());
    }

    private OdontologoRequestDTO request(String username, String matricula, String password) {
        return request(username, username + "@clinica.com", matricula, password);
    }

    private OdontologoRequestDTO request(String username, String email, String matricula, String password) {
        return OdontologoRequestDTO.builder()
                .nombre("Ana")
                .apellido("Pérez")
                .matricula(matricula)
                .especialidad("Ortodoncia")
                .username(username)
                .email(email)
                .password(password)
                .build();
    }
}
