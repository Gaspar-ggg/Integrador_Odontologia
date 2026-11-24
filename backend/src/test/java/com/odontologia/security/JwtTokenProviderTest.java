package com.odontologia.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "test-super-secret-key-for-jwt-authentication-odontologia-2026-32bytes-min";
    private static final long EXPIRATION_MS = 86_400_000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS);
    }

    @Test
    @DisplayName("Debe generar token con claims sub, id, rol y expiración")
    void generateToken_DebeIncluirClaimsYExpiracion() {
        String token = jwtTokenProvider.generateToken("admin", 1L, "ADMIN");

        Claims claims = jwtTokenProvider.getClaims(token);

        assertThat(claims.getSubject()).isEqualTo("admin");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("rol", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration().getTime() - claims.getIssuedAt().getTime())
                .isLessThanOrEqualTo(EXPIRATION_MS);
    }

    @Test
    @DisplayName("Debe validar token válido")
    void isTokenValid_TokenValido_DebeRetornarTrue() {
        String token = jwtTokenProvider.generateToken("recepcion", 2L, "RECEPCION");

        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("recepcion");
    }

    @Test
    @DisplayName("Debe rechazar token expirado")
    void isTokenValid_TokenExpirado_DebeRetornarFalse() {
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1_000L);
        String token = expiredProvider.generateToken("admin", 1L, "ADMIN");

        assertThat(jwtTokenProvider.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("Debe rechazar token inválido o malformado")
    void isTokenValid_TokenInvalido_DebeRetornarFalse() {
        assertThat(jwtTokenProvider.isTokenValid("token.invalido.malformado")).isFalse();
        assertThat(jwtTokenProvider.isTokenValid("")).isFalse();
        assertThat(jwtTokenProvider.isTokenValid(null)).isFalse();
    }

    @Test
    @DisplayName("Debe aplicar tope de expiración máximo de 24 horas")
    void generateToken_DebeAplicarTope24Horas() {
        JwtTokenProvider longProvider = new JwtTokenProvider(SECRET, 172_800_000L);
        String token = longProvider.generateToken("admin", 1L, "ADMIN");

        Claims claims = longProvider.getClaims(token);
        long duration = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();

        assertThat(duration).isLessThanOrEqualTo(EXPIRATION_MS);
    }
}
