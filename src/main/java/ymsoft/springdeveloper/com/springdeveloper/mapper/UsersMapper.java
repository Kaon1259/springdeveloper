package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.Users;

import java.util.Optional;

@Mapper
public interface UsersMapper {
    Optional<Users> findByNickname(@Param("nickname") String nickname);
    Optional<Users> findByEmail(@Param("email") String email);
    void insert(Users users);
}
