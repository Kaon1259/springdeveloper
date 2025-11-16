package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ymsoft.springdeveloper.com.springdeveloper.entity.Users;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByNickname(String nickname);
    Optional<Users> findByEmail(String email);
}
