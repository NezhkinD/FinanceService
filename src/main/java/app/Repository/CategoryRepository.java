package app.Repository;

import app.Entity.CategoryEntity;
import app.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByUser(UserEntity user);

    Optional<CategoryEntity> findOneByNameAndTypeAndUser(String name, String type, UserEntity user);
}
