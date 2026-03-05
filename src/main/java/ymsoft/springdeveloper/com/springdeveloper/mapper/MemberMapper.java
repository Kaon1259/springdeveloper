package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<Member> findById(@Param("id") Long id);
    Optional<Member> findWithSchedulesById(@Param("id") Long id);
    List<Member> findAll();
    List<Member> findByStatus(@Param("status") String status);
    List<Member> findByStatusIn(@Param("statuses") List<String> statuses);
    boolean existsById(@Param("id") Long id);
    boolean existsByPhone(@Param("phone") String phone);
    void insert(Member member);
    void update(Member member);
}
