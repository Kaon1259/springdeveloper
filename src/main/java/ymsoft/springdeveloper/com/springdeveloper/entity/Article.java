package ymsoft.springdeveloper.com.springdeveloper.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import ymsoft.springdeveloper.com.springdeveloper.dto.articleDto;

@Slf4j
@NoArgsConstructor
@ToString
@Getter
@Setter
public class Article {

    private Long id;
    private String articleTitle;
    private String articleAuthor;
    private String articleDescription;

    public Article(Long id, String articleTitle, String articleAuthor, String articleDescription) {
        this.id = id;
        this.articleTitle = articleTitle;
        this.articleAuthor = articleAuthor;
        this.articleDescription = articleDescription;
    }

    public articleDto toDto() {
        return new articleDto(this.id, this.articleTitle, this.articleAuthor, this.articleDescription);
    }

    public static articleDto toDto(Article entity) {
        return new articleDto(entity.id, entity.articleTitle, entity.articleAuthor, entity.articleDescription);
    }
}
