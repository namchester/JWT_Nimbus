package vn.iotstar.exceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ApiError> error(int status, String message, HttpServletRequest request) {
        var builder = ResponseEntity.status(status).cacheControl(CacheControl.noStore());
        if (status == 401) builder.header(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return builder.body(ApiError.of(status, message, request.getRequestURI()));
    }
    @ExceptionHandler(AccountStatusException.class)
    ResponseEntity<ApiError> account(AccountStatusException ex, HttpServletRequest req) {
        return error(403, "Tài khoản đã bị khóa hoặc vô hiệu hóa.", req);
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiError> authentication(AuthenticationException ex, HttpServletRequest req) {
        return error(401, "Email, mật khẩu hoặc token không hợp lệ.", req);
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest req) {
        return error(403, "Bạn không có quyền truy cập.", req);
    }
    @ExceptionHandler({DuplicateEmailException.class, DataIntegrityViolationException.class})
    ResponseEntity<ApiError> duplicate(RuntimeException ex, HttpServletRequest req) {
        return error(409, "Dữ liệu bị trùng hoặc vi phạm ràng buộc. Hãy kiểm tra email.", req);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).distinct().collect(Collectors.joining("; "));
        return error(400, message, req);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> invalid(IllegalArgumentException ex, HttpServletRequest req) {
        return error(400, ex.getMessage(), req);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> json(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return error(400, "Nội dung JSON không hợp lệ.", req);
    }
}
