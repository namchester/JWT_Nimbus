package vn.iotstar.controllers;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.models.*;
import vn.iotstar.services.*;
@RestController @RequestMapping("/auth")
public class AuthenticationController {
    private final AuthenticationService auth;
    private final JwtService jwt;
    public AuthenticationController(AuthenticationService auth, JwtService jwt) { this.auth = auth; this.jwt = jwt; }
    @PostMapping("/signup") public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserModel input) {
        return ResponseEntity.ok(UserResponse.from(auth.signup(input)));
    }
    @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginUserModel input) {
        return new LoginResponse(jwt.generateToken(auth.authenticate(input)), jwt.getExpirationTime());
    }
}
