package vn.iotstar.models;
import jakarta.validation.constraints.*;
public record RegisterUserModel(
        @NotBlank(message="Email không được để trống") @Email(message="Email không hợp lệ")
        @Size(max=100, message="Email tối đa 100 ký tự") String email,
        @NotBlank(message="Mật khẩu không được để trống")
        @Size(min=6, max=72, message="Mật khẩu từ 6 đến 72 ký tự") String password,
        @NotBlank(message="Họ tên không được để trống")
        @Size(max=50, message="Họ tên tối đa 50 ký tự") String fullName) { }
