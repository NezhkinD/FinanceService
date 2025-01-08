package app.Controller;

import app.Entity.CategoryEntity;
import app.Entity.UserEntity;
import app.Repository.CategoryRepository;
import app.Util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthUtil authUtil;

    @Operation(summary = "Добавить новую категорию")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password,
            @RequestParam @Parameter(description = "Название категории") String name,
            @RequestParam @Parameter(description = "Тип категории") String type,
            @RequestParam @Parameter(description = "Лимит по категории") Float limit
    ) {
        try {
            UserEntity user = authUtil.auth(login, password);
            CategoryEntity save = categoryRepository.save(CategoryEntity.create(name, type, user, limit));

            return ResponseEntity.status(HttpStatus.OK).body("Категория сохранена: " + save.getName());
        } catch (RuntimeException ex) {

            if (Pattern.compile("Duplicate entry.*?category\\.unique_category").matcher(ex.getMessage()).find()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: категория " + name + " с типом " + type + " уже существует");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }

    @Operation(summary = "Обновить категорию")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @PutMapping
    public ResponseEntity<?> update(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password,
            @RequestParam @Parameter(description = "ID категории") Long id,
            @RequestParam @Parameter(required = false, description = "Новое название категории") Optional<String> name,
            @RequestParam @Parameter(required = false, description = "Новый тип категории") Optional<String> type,
            @RequestParam @Parameter(required = false, description = "Новый лимит по категории") Optional<Float> limit
    ) {
        try {
            UserEntity user = authUtil.auth(login, password);

            Optional<CategoryEntity> categoryEntity = categoryRepository.findById(id);
            if (categoryEntity.isEmpty()) {
                throw new RuntimeException("Не найдена категория с id " + id);
            }

            if (!Objects.equals(categoryEntity.get().getUser().getId(), user.getId())) {
                throw new RuntimeException("Категория принадлежит другому пользователю. Можно редактировать только свои категории");
            }

            name.ifPresent(s -> categoryEntity.get().setName(s));
            type.ifPresent(s -> categoryEntity.get().setType(s));
            limit.ifPresent(s -> categoryEntity.get().setLimit(s));

            categoryRepository.save(categoryEntity.get());

            return ResponseEntity.status(HttpStatus.OK).body("Категория обновлена: " + name.orElse(categoryEntity.get().getName()) + " " + type.orElse(categoryEntity.get().getType()) + " " + limit.orElse(categoryEntity.get().getLimit()) );
        } catch (RuntimeException ex) {

            if (Pattern.compile("Duplicate entry.*?category\\.unique_category").matcher(ex.getMessage()).find()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: категория " + name + " с типом " + type + " уже существует");
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }

    @Operation(summary = "Получение всех категорий для конкретного пользователя")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @GetMapping
    public List<CategoryEntity> get(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password
    ) {
        UserEntity user = authUtil.auth(login, password);
        List<CategoryEntity> categories = categoryRepository.findByUser(user);
        return categories.stream().peek(category -> category.getUser().setPassword("****")).collect(Collectors.toList());
    }
}
