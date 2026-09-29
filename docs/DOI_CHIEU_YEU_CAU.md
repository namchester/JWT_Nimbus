# Đối chiếu yêu cầu bài tập

Ảnh yêu cầu: (1) làm bài tập ví dụ trong bài giảng JWT, (2) sử dụng thư viện Nimbus thay JJWT; sinh viên tự upload GitHub và nộp link UTExLMS.

| Bước trong 04_JWT.pdf | Phần triển khai |
|---|---|
| 1. Dependency, trang 15 | `pom.xml`, Nimbus 10.10; không có dependency JJWT |
| 2. Entity, trang 16 | `entity/User.java`, đủ id/fullName/email/images/password/createdAt/updatedAt, UserDetails |
| 3. Models, trang 17 | `RegisterUserModel`, `LoginUserModel`, `LoginResponse`; thêm `UserResponse` ẩn mật khẩu |
| 4. Repository, trang 18 | `UserRepository.findByEmail`, `UserService.allUsers` |
| 4. AuthenticationService, trang 19 | Signup, BCrypt, AuthenticationManager |
| 4. JwtService, trang 20–21 | Nimbus tạo JWT HS256, kiểm chữ ký và các claim, thời hạn cấu hình |
| 4. application.properties, trang 21 | Cổng 8005, MySQL profile riêng, H2 cho chạy thử nhanh |
| 5. ApplicationConfiguration, trang 22 | UserDetailsService, BCrypt, DaoAuthenticationProvider, AuthenticationManager |
| 6. Filter, trang 23 | JwtAuthenticationFilter, Bearer header, SecurityContextHolder |
| 7. SecurityConfig, trang 24–25 | Stateless, CORS, giới hạn endpoint public, JWT filter |
| 8. RestController, trang 26–27 | POST /auth/signup, POST /auth/login, GET /users/me, GET /users và /users/ |
| 9. Test, trang 28–29 | Collection Postman, JUnit/MockMvc và kịch bản kiểm thử |
| 10. Exception, trang 30–31 | JSON 400/401/403/409; token sai/hết hạn trả 401 |
| 10. AJAX, trang 32–34 | login.html, profile.html, mainjs.js, AuthController; thêm register.html |
| Yêu cầu Nimbus trong ảnh | Import com.nimbusds.*, SignedJWT + MACSigner + MACVerifier |
| Upload GitHub/UTExLMS | README phần 9; sinh viên tự thực hiện |

## Diễn giải thay thư viện

| Trong ví dụ JJWT | Trong bài làm Nimbus |
|---|---|
| Jwts.builder() | JWTClaimsSet.Builder + new SignedJWT(...) |
| Claims | JWTClaimsSet |
| signWith(...) | SignedJWT.sign(new MACSigner(secret)) |
| compact() | SignedJWT.serialize() |
| parseSignedClaims(...) | SignedJWT.parse(...) + verify(new MACVerifier(secret)) + kiểm tra claims |
| ExpiredJwtException, SignatureException | InvalidTokenException do ứng dụng quy đổi từ kiểm tra Nimbus |

`exp`, `iat`, `nbf` trên JWT là NumericDate theo giây. Trường `expiresIn` của API giữ đơn vị mili giây giống bài giảng; không nhầm hai đơn vị này.
