# Kịch bản kiểm thử và chụp ảnh

Chạy app tại http://localhost:8005. Đây là **kết quả mong đợi**, không phải ảnh/kết quả chạy được dựng sẵn.

| STT | Thao tác | Kết quả mong đợi |
|---|---|---|
| 1 | Mở /register, nhập họ tên, email chưa tồn tại, mật khẩu >= 6 ký tự | Chuyển sang /login, báo đăng ký thành công |
| 2 | Đăng nhập tài khoản trên | Chuyển /user/profile, hiển thị đúng tên/email/ID |
| 3 | Nhấn Tải danh sách | Bảng người dùng được tải từ /users |
| 4 | Postman đăng nhập | HTTP 200; token 3 phần; expiresIn=3600000 |
| 5 | GET /users/me có Bearer token | HTTP 200; không có password/hash |
| 6 | GET /users không có token | HTTP 401, JSON báo cần đăng nhập |
| 7 | GET /users/me với Bearer abc | HTTP 401, không phải 500 |
| 8 | Đăng nhập sai mật khẩu | HTTP 401 |
| 9 | Đăng ký trùng email, kể cả đổi chữ hoa/thường | HTTP 409 |
| 10 | Đăng ký email sai hoặc mật khẩu quá ngắn | HTTP 400 |
| 11 | Nhấn Đăng xuất rồi mở lại /user/profile | Chuyển về login khi gọi API do không có token |
| 12 | Dừng app, đặt JWT_EXPIRATION_MS=5000, chạy lại; login và đợi >5 giây rồi gọi /users/me | HTTP 401 vì token hết hạn |

Ví dụ đặt thời hạn 5 giây trên PowerShell:

```powershell
$env:JWT_EXPIRATION_MS="5000"
mvn spring-boot:run
```

Xong thử nghiệm, dừng app, bỏ biến bằng `Remove-Item Env:JWT_EXPIRATION_MS` rồi chạy lại để trở về 1 giờ.

Nếu cần chứng minh tài khoản bị khóa (MySQL), đăng nhập rồi dùng MySQL Workbench:

```sql
UPDATE users SET account_non_locked = false WHERE email = 'nam@example.com';
```

Gọi lại `/users/me` với token cũ → 403; đăng nhập lại cũng 403. Mở khóa:

```sql
UPDATE users SET account_non_locked = true WHERE email = 'nam@example.com';
```

Các ảnh nên chụp khi tự chạy: đăng ký thành công, response login trong Postman, hồ sơ trên web, bảng user, API không token 401, token lỗi 401, terminal kết quả `mvn clean test`. Không công khai token còn hiệu lực hoặc mật khẩu MySQL trong ảnh nộp.
