package ymsoft.springdeveloper.com.springdeveloper.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ymsoft.springdeveloper.com.springdeveloper.entity.Article;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class articleDto {
    private Long articleId;
    private String articleTitle;
    private String articleAuthor;
    private String articleDescription;

    @Override
    public String toString() {
        return "articleDto [articleId=" + articleId + ", articleTitle=" + articleTitle + ", articleAuthor="  + articleAuthor + ", articleDescription=" + articleDescription + "]";
    }

    public static Article toEntity(articleDto dto) {
        return new Article(dto.articleId, dto.articleTitle, dto.articleAuthor, dto.articleDescription );
    }
}
