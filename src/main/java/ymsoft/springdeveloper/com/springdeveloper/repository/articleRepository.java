package ymsoft.springdeveloper.com.springdeveloper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ymsoft.springdeveloper.com.springdeveloper.entity.Article;

@Repository
public interface articleRepository extends JpaRepository<Article, Long> {
}
