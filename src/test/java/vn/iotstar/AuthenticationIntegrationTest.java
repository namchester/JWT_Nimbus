package vn.iotstar;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.repository.UserRepository;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class AuthenticationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;
    private final String registration = """
        {"email":"nam@example.com","password":"123456","fullName":"Nguyễn Thành Nam"}
        """;
    @BeforeEach void clean() { users.deleteAll(); }
    private void signup() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(registration))
            .andExpect(status().isOk()).andExpect(jsonPath("$.email").value("nam@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist()).andExpect(jsonPath("$.images").value("/images/avatar.svg"));
    }
    private String login() throws Exception {
        String response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nam@example.com\",\"password\":\"123456\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(3600000))
            .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("token").asText();
    }
    @Test void signupLoginProfileAndUserList() throws Exception {
        signup();
        var stored = users.findByEmail("nam@example.com").orElseThrow();
        assertNotEquals("123456", stored.getPassword());
        assertTrue(encoder.matches("123456", stored.getPassword()));
        String token = login();
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Nguyễn Thành Nam"))
            .andExpect(jsonPath("$.password").doesNotExist());
        for (String path : List.of("/users", "/users/")) {
            mvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].email").value("nam@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
        }
    }
    @Test void missingTokenReturns401() throws Exception {
        for (String path : List.of("/users/me", "/users", "/users/"))
            mvc.perform(get(path)).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"));
    }
    @Test void malformedTokenReturns401Not500() throws Exception {
        mvc.perform(get("/users/me").header("Authorization", "Bearer invalid"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }
    @Test void badPasswordReturns401() throws Exception {
        signup();
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nam@example.com\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isUnauthorized());
    }
    @Test void duplicateEmailCaseInsensitiveReturns409() throws Exception {
        signup();
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content(registration.replace("nam@example.com", "NAM@EXAMPLE.COM")))
            .andExpect(status().isConflict());
    }
    @Test void invalidInputReturns400() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"bad\",\"password\":\"12\",\"fullName\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
    @Test void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content("{"))
            .andExpect(status().isBadRequest());
    }
    @Test void utf8PasswordOverBcryptLimitReturns400() throws Exception {
        String input = json.writeValueAsString(Map.of("email", "nam@example.com", "password", "ậ".repeat(30), "fullName", "Nam"));
        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON).content(input))
            .andExpect(status().isBadRequest());
    }
    @Test void lockedAccountReturns403ForLoginAndExistingToken() throws Exception {
        signup(); String token = login();
        var user = users.findByEmail("nam@example.com").orElseThrow();
        user.setAccountNonLocked(false); users.saveAndFlush(user);
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token)).andExpect(status().isForbidden());
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nam@example.com\",\"password\":\"123456\"}"))
            .andExpect(status().isForbidden());
    }
    @Test void deletedUserCannotReuseToken() throws Exception {
        signup(); String token = login(); users.deleteAll();
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + token)).andExpect(status().isUnauthorized());
    }
    @Test void expiredTokenReturns401() throws Exception {
        signup(); Instant past = Instant.now().minusSeconds(120);
        var claims = new JWTClaimsSet.Builder().subject("nam@example.com").issuer("jwt-nimbus-23162061")
            .audience("jwt-nimbus-web").issueTime(Date.from(past)).notBeforeTime(Date.from(past))
            .expirationTime(Date.from(past.plusSeconds(60))).jwtID("expired-test").build();
        var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims);
        jwt.sign(new MACSigner(Base64.getDecoder().decode("AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=")));
        mvc.perform(get("/users/me").header("Authorization", "Bearer " + jwt.serialize())).andExpect(status().isUnauthorized());
    }
    @Test void publicHtmlAndStaticAssetsAccessible() throws Exception {
        for (String path : List.of("/login", "/register", "/user/profile", "/js/mainjs.js", "/css/style.css", "/images/avatar.svg"))
            mvc.perform(get(path)).andExpect(status().isOk());
    }
    @Test void apiDoesNotCreateSession() throws Exception {
        signup(); String token = login();
        var result = mvc.perform(get("/users/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk()).andReturn();
        assertNull(result.getRequest().getSession(false));
        assertNull(result.getResponse().getCookie("JSESSIONID"));
    }
}
