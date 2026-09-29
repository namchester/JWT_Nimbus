# Kết quả kiểm tra gói bàn giao

Ngày kiểm tra: 29/09/2026.

## Kiểm chứng thực tế

- Đã biên dịch toàn bộ mã nguồn và mã kiểm thử, đóng gói executable JAR thành công: **BUILD SUCCESS**.
- Runtime Java 17.0.20, Maven 3.9.9, Spring Boot 3.5.16, Nimbus 10.10.
- Môi trường kiểm tra chỉ có JRE; dùng Eclipse compiler qua một POM kiểm chứng tạm (không nằm trong gói nộp). POM bàn giao dùng compiler mặc định; người chạy cần JDK 17/21 theo README.
- Chạy unit test và integration test với H2 trong RAM, qua Spring Security và MockMvc thật; không giả lập kết quả xác thực.
- Khởi động JAR và kiểm tra qua HTTP thật: các trang/tài nguyên trả 200; đăng ký và đăng nhập 200; API hồ sơ/danh sách có Bearer token trả 200; thiếu token hoặc token sai trả 401.
- JavaScript đã qua `node --check`.

| Test suite | Số test | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| vn.iotstar.AuthenticationIntegrationTest | 13 | 0 | 0 | 0 |
| vn.iotstar.JwtServiceTest | 12 | 0 | 0 | 0 |

**Tổng cộng: 25/25 đạt.** Bộ kiểm thử nằm trong `src/test`. Dùng `mvn clean test` để chạy lại trên máy có JDK; GitHub Actions cũng chạy `mvn verify` khi push.

## Phạm vi chưa kiểm chứng

- Chưa chạy kết nối với một MySQL server thực; đã cung cấp JDBC driver, profile MySQL và SQL tạo database. Các kiểm thử tích hợp hiện chạy H2.
- Chưa kiểm thử thao tác click/render bằng Chromium: môi trường tải browser không thành công. Không kèm ảnh chụp giả hoặc ảnh kết quả chưa chạy.
- Chưa tạo/push repository, chưa chạy GitHub Actions trên tài khoản sinh viên và chưa nộp UTExLMS; sinh viên tự thực hiện theo README.

File `KICH_BAN_KIEM_THU.md` ghi các bước để sinh viên tự thử và chụp ảnh trên máy của mình.
