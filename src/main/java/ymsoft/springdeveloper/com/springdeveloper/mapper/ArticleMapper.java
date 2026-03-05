package ymsoft.springdeveloper.com.springdeveloper.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import ymsoft.springdeveloper.com.springdeveloper.entity.Article;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ArticleMapper {
    List<Article> findAll();
    Optional<Article> findById(@Param("id") Long id);
    void insert(Article article);
    void update(Article article);
    void deleteById(@Param("id") Long id);
}
