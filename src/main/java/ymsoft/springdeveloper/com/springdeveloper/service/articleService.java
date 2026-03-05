package ymsoft.springdeveloper.com.springdeveloper.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ymsoft.springdeveloper.com.springdeveloper.dto.articleDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Article;
import ymsoft.springdeveloper.com.springdeveloper.mapper.ArticleMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class articleService {

    private final ArticleMapper articleMapper;

    public List<articleDto> getAllArticles() {
        return articleMapper.findAll()
                .stream()
                .map(article -> new articleDto(
                        article.getId(),
                        article.getArticleTitle(),
                        article.getArticleAuthor(),
                        article.getArticleDescription()
                )).toList();
    }

    @Transactional
    public articleDto saveArticle(articleDto dto) {
        Article entity = articleDto.toEntity(dto);
        articleMapper.insert(entity);
        return entity.toDto();
    }

    @Transactional
    public Article saveArticle(Article entity) {
        articleMapper.insert(entity);
        return entity;
    }

    public articleDto findArticleById(Long articleId) {
        return articleMapper.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + articleId))
                .toDto();
    }

    @Transactional
    public articleDto updateArticleById(Long articleId, articleDto dto) {
        return articleMapper.findById(articleId)
                .map(article -> {
                    article.setArticleTitle(dto.getArticleTitle());
                    article.setArticleAuthor(dto.getArticleAuthor());
                    article.setArticleDescription(dto.getArticleDescription());
                    articleMapper.update(article);
                    return article.toDto();
                })
                .orElse(null);
    }

    @Transactional
    public articleDto deleteArticleById(Long articleId) {
        return articleMapper.findById(articleId)
                .map(article -> {
                    articleMapper.deleteById(articleId);
                    return article.toDto();
                })
                .orElse(null);
    }
}
