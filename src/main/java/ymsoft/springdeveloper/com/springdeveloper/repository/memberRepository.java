package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ymsoft.springdeveloper.com.springdeveloper.entity.Member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface memberRepository extends JpaRepository<Member, Long> {

    // 예시: 이름으로 검색
    Member findByName(String name);

    // 예시: 상태별 목록 조회
    @EntityGraph(attributePaths = {"schedules"})
    List<Member> findByStatus(Member.Status status);

    @EntityGraph(attributePaths = {"schedules"})
    List<Member> findByStatusIn(Collection<Member.Status> statuses);

    // 예시: 전화번호로 중복 체크
    boolean existsByPhone(String phone);

    @Query("""
           select distinct m
           from Member m
           left join fetch m.schedules s
           where m.id = :id
           """)
    Optional<Member> findWithSchedulesById(@Param("id") Long id);

}
