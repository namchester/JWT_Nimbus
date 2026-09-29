package vn.iotstar;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import vn.iotstar.exceptions.InvalidTokenException;
import vn.iotstar.services.JwtService;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final byte[] KEY = new byte[64];
    private final JwtService service = new JwtService(Base64.getEncoder().encodeToString(KEY), 3600000, "issuer", "aud");
    private JWTClaimsSet.Builder claims() {
        Instant now = Instant.now();
        return new JWTClaimsSet.Builder().subject("nam@example.com").issuer("issuer").audience("aud")
            .issueTime(Date.from(now.minusSeconds(1))).notBeforeTime(Date.from(now.minusSeconds(1)))
            .expirationTime(Date.from(now.plusSeconds(3600))).jwtID("test-id");
    }
    private String signed(JWTClaimsSet claims, byte[] key, JWSAlgorithm algorithm) throws Exception {
        var jwt = new SignedJWT(new JWSHeader.Builder(algorithm).type(JOSEObjectType.JWT).build(), claims);
        jwt.sign(new MACSigner(key)); return jwt.serialize();
    }
    @Test void validTokenHasThreePartsAndCorrectClaims() throws Exception {
        var user = User.withUsername("nam@example.com").password("unused").authorities(Collections.emptyList()).build();
        String token = service.generateToken(user);
        assertEquals(3, token.split("\\.").length);
        var result = service.validateToken(token);
        assertEquals("nam@example.com", result.getSubject());
        assertEquals(3600000, result.getExpirationTime().getTime() - result.getIssueTime().getTime());
    }
    @Test void expiredTokenRejected() throws Exception {
        String token = signed(claims().expirationTime(Date.from(Instant.now().minusSeconds(10))).build(), KEY, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void missingExpiryRejected() throws Exception {
        String token = signed(claims().expirationTime(null).build(), KEY, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void forgedSignatureRejected() throws Exception {
        byte[] wrongKey = new byte[64]; Arrays.fill(wrongKey, (byte) 9);
        String token = signed(claims().build(), wrongKey, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void unexpectedAlgorithmRejectedEvenWithValidSignature() throws Exception {
        String token = signed(claims().build(), KEY, JWSAlgorithm.HS512);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void wrongIssuerRejected() throws Exception {
        String token = signed(claims().issuer("other").build(), KEY, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void wrongAudienceRejected() throws Exception {
        String token = signed(claims().audience("other").build(), KEY, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void futureNotBeforeRejected() throws Exception {
        String token = signed(claims().notBeforeTime(Date.from(Instant.now().plusSeconds(100))).build(), KEY, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void missingSubjectRejected() throws Exception {
        String token = signed(claims().subject(null).build(), KEY, JWSAlgorithm.HS256);
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void unsignedTokenRejected() {
        String token = new PlainJWT(claims().build()).serialize();
        assertThrows(InvalidTokenException.class, () -> service.validateToken(token));
    }
    @Test void malformedTokenRejected() {
        assertThrows(InvalidTokenException.class, () -> service.validateToken("not.a.jwt"));
    }
    @Test void weakKeyRejected() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService("YWJj", 1000, "i", "a"));
    }
}
