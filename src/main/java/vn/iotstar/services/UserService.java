package vn.iotstar.services;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.models.UserResponse;
import vn.iotstar.repository.UserRepository;
import java.util.List;
@Service
public class UserService {
    private final UserRepository repository;
    public UserService(UserRepository repository) { this.repository = repository; }
    @Transactional(readOnly=true)
    public List<UserResponse> allUsers() {
        return repository.findAll(Sort.by("id")).stream().map(UserResponse::from).toList();
    }
}
