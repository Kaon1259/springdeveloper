package ymsoft.springdeveloper.com.springdeveloper.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recipe_step")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 레시피에 속한 단계인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // Step 순서 (1, 2, 3, …)
    @Column(nullable = false)
    private Integer stepOrder;

    // 실제 레시피 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}
