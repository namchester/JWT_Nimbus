package vn.iotstar.models;
import vn.iotstar.entity.User;
import java.time.Instant;
// Chi tra du lieu can hien thi; khong tra password hay hash BCrypt.
public record UserResponse(Integer id, String fullName, String email, String images,
                           Instant createdAt, Instant updatedAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(),
                user.getImages(), user.getCreatedAt(), user.getUpdatedAt());
    }
}
