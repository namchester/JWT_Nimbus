package vn.iotstar.filter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.iotstar.exceptions.SecurityErrorWriter;
import vn.iotstar.services.JwtService;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService users;
    private final SecurityErrorWriter errors;
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService users, SecurityErrorWriter errors) {
        this.jwtService = jwtService; this.users = users; this.errors = errors;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            try {
                String subject = jwtService.validateToken(header.substring(7).trim()).getSubject();
                UserDetails user = users.loadUserByUsername(subject);
                new AccountStatusUserDetailsChecker().check(user);
                var auth = UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (AccountStatusException ex) {
                SecurityContextHolder.clearContext();
                errors.write(request, response, 403, "Tài khoản đã bị khóa hoặc vô hiệu hóa.");
                return;
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                errors.write(request, response, 401, "JWT không hợp lệ, đã hết hạn hoặc tài khoản không tồn tại.");
                return;
            }
        }
        // Keep downstream application errors out of the JWT exception handler.
        chain.doFilter(request, response);
    }
}
