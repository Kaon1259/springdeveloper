package ymsoft.springdeveloper.com.springdeveloper.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ymsoft.springdeveloper.com.springdeveloper.dto.articleDto;
import ymsoft.springdeveloper.com.springdeveloper.service.articleService;

import java.util.List;

@Slf4j
@Controller
public class ArticleController {

    @Autowired
    articleService articleService;

    @GetMapping("/articles")
    public String articlesList(Model model) {

        List<articleDto> articleDtoList = articleService.getAllArticles();
        model.addAttribute("articles", articleDtoList);
        return "articles/list";
    }

    @GetMapping("/articles/{articleId}/edit")
    public String articlesEdit(@PathVariable("articleId") Long articleId, Model model) {

        articleDto dto = articleService.findArticleById(articleId);
        model.addAttribute("article", dto);
        return "articles/edit";
    }

    @GetMapping("/articles/new")
    public String newArticle() {
        return "articles/new";
    }

    @PostMapping("/articles/create")
    public String createArticle(articleDto dto, Model model, RedirectAttributes redirectAttributes) {

        articleDto newDto = articleService.saveArticle(dto);
        redirectAttributes.addFlashAttribute("message",
                "도서가 등록되었습니다. (title: " + newDto.getArticleTitle() + ")");
        return "redirect:/articles";
    }

    @PostMapping("/articles/{articleId}/update")
    public String articlesUpdate(@PathVariable("articleId") Long articleId, articleDto dto, RedirectAttributes redirectAttributes) {

        articleDto updatedDto = articleService.updateArticleById(articleId, dto);
        redirectAttributes.addFlashAttribute("message",
                "도서정보가 수정되었습니다. (title: " + updatedDto.getArticleTitle() + ")");
        return "redirect:/articles";
    }

    @DeleteMapping("/articles/{articleId}/delete")
    public ResponseEntity<Void> articlesDelete(@PathVariable("articleId") Long articleId, RedirectAttributes redirectAttributes) {

        articleDto deletedDto = articleService.deleteArticleById(articleId);

        if (deletedDto == null) {
            redirectAttributes.addFlashAttribute("message",
                    " 도서정보를 찾을 수 없습니다.");
            return ResponseEntity.notFound().build();
        }else {
            redirectAttributes.addFlashAttribute("message",
                    deletedDto.getArticleTitle() + " 도서정보가 삭제되었습니다.");
            return ResponseEntity.ok().build();
        }
    }
}
