package sa.elm.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import sa.elm.demo.exception.InvalidJWTException;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

  @InjectMocks
  private JwtUtil jwtUtil;

  private final String jwtSecret = "2ECCD48E35ADF9C17813D4C22B4672ECCD48E35ADF9C17813D4C22B4672ECCD48E35ADF9C17813D4C22B467"; // Mocked secret key

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil();
    ReflectionTestUtils.setField(jwtUtil, "JWT_SECRET", jwtSecret);
  }

  @Test
  void testExtractUsername_ShouldSuccess() {
    String token = generateMockToken("dummy_sub");

    String username = jwtUtil.extractUsername(token);
    assertEquals("dummy_sub", username);
  }

  @Test
  void testGenerateToken_ShouldSuccess() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ADMIN");
    String subject = "dummy_sub";

    String token = jwtUtil.generateToken(claims, subject);
    assertNotNull(token);

    Claims parsedClaims = jwtUtil.extractAllClaims(token);
    assertEquals("ADMIN", parsedClaims.get("role"));
    assertEquals(subject, parsedClaims.getSubject());
  }

  @Test
  void testValidateToken_ShouldSuccess() {
    String token = generateMockToken("dummy_sub");

    boolean isValid = jwtUtil.validateToken(token);
    assertTrue(isValid);
  }

  @Test
  void testValidateToken_Expired() {
    String expiredToken = generateExpiredMockToken("dummy_sub");

    assertThrows(InvalidJWTException.class, () -> jwtUtil.validateToken(expiredToken));
  }

  @Test
  void testExtractExpiration_ShouldSuccess() {
    String token = generateMockToken("dummy_sub");

    Date expiration = jwtUtil.extractExpiration(token);
    assertNotNull(expiration);
  }

  @Test
  void testInvalidToken_SignatureException() {
    try (MockedStatic<Jwts> jwtsMockedStatic = Mockito.mockStatic(Jwts.class)) {
      jwtsMockedStatic.when(Jwts::parser)
          .thenThrow(SignatureException.class);

      String invalidToken = "invalid_token";
      assertThrows(InvalidJWTException.class, () -> jwtUtil.validateToken(invalidToken));
    }
  }

  private String generateMockToken(String subject) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ADMIN");
    SecretKey secretKey = getSecretKey();
    return Jwts.builder()
        .signWith(secretKey)
        .subject(subject)
        .claims(claims)
        .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
        .issuedAt(new Date(System.currentTimeMillis()))
        .compact();
  }

  private String generateExpiredMockToken(String subject) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", "ADMIN");
    SecretKey secretKey = getSecretKey();
    return Jwts.builder()
        .signWith(secretKey)
        .subject(subject)
        .claims(claims)
        .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
        .issuedAt(new Date(System.currentTimeMillis()))
        .compact();
  }

  private SecretKey getSecretKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

}