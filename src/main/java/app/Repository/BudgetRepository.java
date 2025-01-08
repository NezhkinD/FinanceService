package app.Repository;

import app.Entity.BudgetEntity;
import app.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {
    @Query("SELECT b.id, b.sum, c.limitC " +
            "FROM BudgetEntity b " +
            "JOIN b.category c " +
            "WHERE c.id = :categoryId AND c.user = :user")
    List<Object[]> findByCategoryAndUser(
            @Param("categoryId") Long categoryId,
            @Param("user") UserEntity user
    );
}
