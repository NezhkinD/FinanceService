package app.Entity;


import jakarta.persistence.*;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "budget")
public class BudgetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private Float sum;

    public static BudgetEntity create(CategoryEntity category, Float sum) {
        BudgetEntity obj = new BudgetEntity();
        obj.category = category;
        obj.sum = sum;

        return obj;
    }
}
