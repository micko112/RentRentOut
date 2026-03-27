package org.landm.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    // Mora biti min 32 znaka (256 bita) za HMAC-SHA256
    private static final String TEST_SECRET = "test-secret-key-must-be-32chars!";

    private JwtUtil jwtUtil;

    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 900_000L);    // 15 min
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604_800_000L); // 7 dana

        // Inicijalizuj signingKey (simulira @PostConstruct)
        ReflectionTestUtils.invokeMethod(jwtUtil, "init");

        Role role = new Role("ROLE_USER");
        user = new User();
        user.setId(42L);
        user.setEmail("test@test.com");
        user.setFirstname("Test");
        user.setLastname("User");
        user.setRole(role);
    }

    // ─── generateAccessToken() ────────────────────────────────────────────────

    @Test
    void generateAccessToken_vrataNeNullToken() {
        String token = jwtUtil.generateAccessToken(user);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateAccessToken_tokenSadrziForrmatJWT() {
        String token = jwtUtil.generateAccessToken(user);
        // JWT ima tačno dva tačkica — header.payload.signature
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    void generateAccessToken_userId_mozeSeBitiExtraktovan() {
        String token = jwtUtil.generateAccessToken(user);
        Long extractedId = jwtUtil.extractUserId(token);
        assertThat(extractedId).isEqualTo(42L);
    }

    @Test
    void generateAccessToken_roles_mozuSeBitiExtraktovane() {
        String token = jwtUtil.generateAccessToken(user);
        List<GrantedAuthority> roles = jwtUtil.extractRoles(token);
        assertThat(roles).isNotEmpty();
        assertThat(roles.stream().map(GrantedAuthority::getAuthority).toList())
                .contains("ROLE_USER");
    }

    @Test
    void generateAccessToken_razlicitiKorisnici_generiseSuRazlicitiTokeni() {
        User user2 = new User();
        user2.setId(99L);
        user2.setRole(new Role("ROLE_USER"));

        String token1 = jwtUtil.generateAccessToken(user);
        String token2 = jwtUtil.generateAccessToken(user2);

        assertThat(token1).isNotEqualTo(token2);
    }

    // ─── generateRefreshToken() ───────────────────────────────────────────────

    @Test
    void generateRefreshToken_vrataValidanToken() {
        String token = jwtUtil.generateRefreshToken(user);
        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateRefreshToken_sadrziKorektnogKorisnika() {
        String token = jwtUtil.generateRefreshToken(user);
        Long userId = jwtUtil.extractUserId(token);
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    void generateRefreshToken_duzeTrajanjeOdAccessTokena() {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        Date accessExp = jwtUtil.extractExpiration(accessToken);
        Date refreshExp = jwtUtil.extractExpiration(refreshToken);

        assertThat(refreshExp).isAfter(accessExp);
    }

    // ─── generateToken() (alias) ──────────────────────────────────────────────

    @Test
    void generateToken_aliasZaGenerateAccessToken_vrataEkvivalentanToken() {
        String token = jwtUtil.generateToken(user);
        Long extractedId = jwtUtil.extractUserId(token);
        assertThat(extractedId).isEqualTo(42L);
    }

    // ─── validateToken() ──────────────────────────────────────────────────────

    @Test
    void validateToken_validanToken_vrataTrue() {
        String token = jwtUtil.generateAccessToken(user);
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_nevalijanToken_vrataFalse() {
        assertThat(jwtUtil.validateToken("ovo.nije.validan.token")).isFalse();
    }

    @Test
    void validateToken_prazanString_vrataFalse() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    void validateToken_modifikovaniToken_vrataFalse() {
        String token = jwtUtil.generateAccessToken(user);
        // Izmeni payload
        String[] parts = token.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "TAMPERED." + parts[2];
        assertThat(jwtUtil.validateToken(tampered)).isFalse();
    }

    @Test
    void validateToken_pogresniPotpisniKljuc_vrataFalse() {
        // Generiši token sa drugim JwtUtil instancom (drugi secret)
        JwtUtil otherJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(otherJwtUtil, "secret", "other-secret-key-32charslong!!!!!");
        ReflectionTestUtils.setField(otherJwtUtil, "accessExpiration", 900_000L);
        ReflectionTestUtils.setField(otherJwtUtil, "refreshExpiration", 604_800_000L);
        ReflectionTestUtils.invokeMethod(otherJwtUtil, "init");

        String tokenSaDrugimKljecem = otherJwtUtil.generateAccessToken(user);

        // Validacija sa originalnim JwtUtil mora da vrati false
        assertThat(jwtUtil.validateToken(tokenSaDrugimKljecem)).isFalse();
    }

    @Test
    void validateToken_istekliToken_vrataFalse() throws Exception {
        // Postavi accessExpiration na -1 ms (odma istekne)
        JwtUtil expiredJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(expiredJwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(expiredJwtUtil, "accessExpiration", -1_000L); // već istekao
        ReflectionTestUtils.setField(expiredJwtUtil, "refreshExpiration", 604_800_000L);
        ReflectionTestUtils.invokeMethod(expiredJwtUtil, "init");

        String expiredToken = expiredJwtUtil.generateAccessToken(user);

        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }

    // ─── extractUserId() ──────────────────────────────────────────────────────

    @Test
    void extractUserId_vrataKorektnuVrednost() {
        user.setId(123L);
        String token = jwtUtil.generateAccessToken(user);
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(123L);
    }

    @Test
    void extractUserId_nevalijanToken_bacaJwtException() {
        assertThatThrownBy(() -> jwtUtil.extractUserId("nevalidan.token.vrednost"))
                .isInstanceOf(JwtException.class);
    }

    // ─── extractRoles() ───────────────────────────────────────────────────────

    @Test
    void extractRoles_korisnikSaJednomRolom_vrataListuSaJednomGrantedAuthority() {
        user.setRole(new Role("ROLE_ADMIN"));
        String token = jwtUtil.generateAccessToken(user);

        List<GrantedAuthority> roles = jwtUtil.extractRoles(token);

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void extractRoles_refreshToken_nemaClaims_vrataPreazanuListu() {
        // Refresh token ne nosi roles
        String refreshToken = jwtUtil.generateRefreshToken(user);
        List<GrantedAuthority> roles = jwtUtil.extractRoles(refreshToken);
        assertThat(roles).isEmpty();
    }

    // ─── extractExpiration() ──────────────────────────────────────────────────

    @Test
    void extractExpiration_accessToken_datumJeUBudućnosti() {
        String token = jwtUtil.generateAccessToken(user);
        Date exp = jwtUtil.extractExpiration(token);
        assertThat(exp).isAfter(new Date());
    }

    @Test
    void extractExpiration_accessTokenJeKraci_odRefreshTokena() {
        String access = jwtUtil.generateAccessToken(user);
        String refresh = jwtUtil.generateRefreshToken(user);

        assertThat(jwtUtil.extractExpiration(access))
                .isBefore(jwtUtil.extractExpiration(refresh));
    }

    // ─── višestruki korisnici ─────────────────────────────────────────────────

    @Test
    void generateToken_viseKorisnika_ispravnoMapiraUserId() {
        for (long id = 1; id <= 5; id++) {
            User u = new User();
            u.setId(id);
            u.setRole(new Role("ROLE_USER"));

            String token = jwtUtil.generateAccessToken(u);
            assertThat(jwtUtil.extractUserId(token)).isEqualTo(id);
        }
    }
}
