package ymsoft.springdeveloper.com.springdeveloper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ymsoft.springdeveloper.com.springdeveloper.dto.articleDto;
import ymsoft.springdeveloper.com.springdeveloper.entity.Article;
import ymsoft.springdeveloper.com.springdeveloper.repository.articleRepository;

import java.util.List;
import java.util.Optional;

@Service
public class articleService {
    @Autowired
    private articleRepository articleRepository;

    public List<articleDto> getAllArticles() {
       return articleRepository.findAll()
                .stream()
                .map(article-> new articleDto(
                        article.getId(),
                        article.getArticleTitle(),
                        article.getArticleAuthor(),
                        article.getArticleDescription()
                )).toList();
    }

    public articleDto saveArticle(articleDto dto){
        return articleRepository.save(articleDto.toEntity(dto)).toDto();
    }
    public Article saveArticle(Article entity){
        return articleRepository.save(entity);
    }

    public articleDto findArticleById(Long articleId){
        return  articleRepository.findById(articleId).stream().findFirst().get().toDto();
    }

    public articleDto updateArticleById(Long articleId, articleDto articleDto){
        Article article = articleRepository.findById(articleId).orElse(null);

        if(article != null){
            article.setArticleTitle(articleDto.getArticleTitle());
            article.setArticleAuthor(articleDto.getArticleAuthor());
            article.setArticleDescription(articleDto.getArticleDescription());
            return articleRepository.save(article).toDto();
        }

        return null;
    }

    public articleDto deleteArticleById(Long articleId){
        return articleRepository.findById(articleId)
                .map(article ->
                        {
                            articleRepository.delete(article);
                            return article.toDto();
                        })
                .orElse(null);
    }
}
