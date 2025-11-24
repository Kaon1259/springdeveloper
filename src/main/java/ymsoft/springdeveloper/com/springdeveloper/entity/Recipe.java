package ymsoft.springdeveloper.com.springdeveloper.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import ymsoft.springdeveloper.com.springdeveloper.enums.CupSize;
import ymsoft.springdeveloper.com.springdeveloper.enums.RecipeCategory;
import ymsoft.springdeveloper.com.springdeveloper.enums.Temperature;


@Entity
@Table(name = "recipe")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String menuName;

    @Column(nullable = false, length = 50)
    private String author;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean visible = false;   // 기본 비노출

    @Column(nullable = false)
    private boolean template = false;   // 기본 비노출

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Temperature temperature;      // HOT / ICE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CupSize cupSize;              // OZ20 / OZ24 / OZ32

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecipeCategory category;      // HOT_COFFEE 등 Enum 이름

    @OneToMany(mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<RecipeStep> steps = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addStep(RecipeStep step) {
        steps.add(step);
        step.setRecipe(this);
    }

    public void removeStep(RecipeStep step) {
        steps.remove(step);
        step.setRecipe(null);
    }
}
