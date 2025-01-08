package app.Entity;


import jakarta.persistence.*;

import java.util.Arrays;
import java.util.List;

@Entity
@Table(
        name = "category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "type", "user_id"})
        }
)
public class CategoryEntity {

    public static final String CATEGORY_TYPE_INCOME = "income";
    public static final String CATEGORY_TYPE_COST = "cost";

    public static final List<String> CATEGORY_TYPES = Arrays.asList(CATEGORY_TYPE_COST, CATEGORY_TYPE_INCOME);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 10)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Float limitC;

    public static CategoryEntity create(String name, String type, UserEntity user, Float limit) {
        if (!CATEGORY_TYPES.contains(type)) {
            throw new RuntimeException("Тип в добавляемой категории, может быть одним из значений: " + CATEGORY_TYPES);
        }

        CategoryEntity obj = new CategoryEntity();
        obj.name = name;
        obj.type = type;
        obj.user = user;
        obj.limitC = limit;

        return obj;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        if (!CATEGORY_TYPES.contains(type)) {
            throw new RuntimeException("Тип в добавляемой категории, может быть одним из значений: " + CATEGORY_TYPES);
        }
        this.type = type;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Float getLimit() {
        return limitC;
    }

    public void setLimit(Float limit) {
        this.limitC = limit;
    }
}
