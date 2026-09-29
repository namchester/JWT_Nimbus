package vn.iotstar.services;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.User;
import vn.iotstar.exceptions.DuplicateEmailException;
import vn.iotstar.models.*;
import vn.iotstar.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class AuthenticationService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;
    public AuthenticationService(UserRepository repository, PasswordEncoder encoder, AuthenticationManager manager) {
        this.repository = repository; this.encoder = encoder; this.manager = manager;
    }
    @Transactional
    public User signup(RegisterUserModel input) {
        checkPasswordBytes(input.password());
        String email = normalize(input.email());
        if (repository.existsByEmail(email)) throw new DuplicateEmailException();
        User user = new User();
        user.setEmail(email); user.setFullName(input.fullName().trim());
        user.setPassword(encoder.encode(input.password()));
        return repository.saveAndFlush(user);
    }
    public User authenticate(LoginUserModel input) {
        checkPasswordBytes(input.password());
        var authentication = manager.authenticate(
                new UsernamePasswordAuthenticationToken(normalize(input.email()), input.password()));
        return (User) authentication.getPrincipal();
    }
    private static String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private static void checkPasswordBytes(String password) {
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new IllegalArgumentException("Mật khẩu tối đa 72 byte UTF-8 (giới hạn BCrypt).");
    }
}
