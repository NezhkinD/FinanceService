package app.Controller;

import app.Entity.BudgetEntity;
import app.Entity.CategoryEntity;
import app.Entity.UserEntity;
import app.Repository.BudgetRepository;
import app.Repository.CategoryRepository;
import app.Repository.UserRepository;
import app.Service.BudgetService;
import app.Util.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Посчитать бюджет")
    @ApiResponse(responseCode = "200", description = "ОК")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @GetMapping
    public String calculate(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password
    ) {
        UserEntity user = authUtil.auth(login, password);
        List<Object[]> budgets = budgetService.getByUser(user);

        JSONArray incomeList = new JSONArray();
        JSONArray costList = new JSONArray();
        JSONObject incomeByCategory = new JSONObject();
        JSONObject costByCategory = new JSONObject();
        JSONObject categoryLimits = new JSONObject();
        float totalIncome = 0.0F;
        float totalCost = 0.0F;

        for (Object[] row : budgets) {
            String categoryName = (String) row[3];
            Float budgetSum = (Float) row[1];
            String categoryType = (String) row[4];
            Float categoryLimit = (Float) row[5];

            if (CategoryEntity.CATEGORY_TYPE_INCOME.equals(categoryType)) {
                incomeList.put(new JSONObject().put("категория", categoryName).put("сумма", budgetSum));
                incomeByCategory.put(categoryName, incomeByCategory.optFloat(categoryName, 0.0F) + budgetSum);
                totalIncome += budgetSum;
            } else if (CategoryEntity.CATEGORY_TYPE_COST.equals(categoryType)) {
                costList.put(new JSONObject().put("категория", categoryName).put("сумма", budgetSum));
                float currentCost = costByCategory.optFloat(categoryName, 0.0F) + budgetSum;
                costByCategory.put(categoryName, currentCost);

                JSONObject limitInfo = categoryLimits.optJSONObject(categoryName);
                if (limitInfo == null) {
                    limitInfo = new JSONObject();
                    limitInfo.put("текущий расход", currentCost);
                } else {
                    limitInfo.put("текущий расход", currentCost);
                }
                if (categoryLimit > 0) {
                    limitInfo.put("бюджет", categoryLimit);
                    limitInfo.put("остаток бюджета", categoryLimit - currentCost);
                }
                categoryLimits.put(categoryName, limitInfo);
                totalCost += budgetSum;
            }
        }

        JSONObject result = new JSONObject();
        result.put("Доходы", incomeList);
        result.put("Расходы", costList);
        result.put("Доходы по категориям", incomeByCategory);
        result.put("Расходы по категориям", costByCategory);
        result.put("Бюджет по категориям", categoryLimits);
        result.put("Общие доходы", totalIncome);
        result.put("Общие расходы", totalCost);
        result.put("Итог", totalIncome - totalCost);

        return result.toString();
    }

    @Operation(summary = "Добавить новую статью расходов")
    @ApiResponse(responseCode = "200", description = "ОК")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @PostMapping("/c")
    public ResponseEntity<?> addCost(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password,
            @RequestParam @Parameter(description = "Id категории") Long id,
            @RequestParam @Parameter(description = "Сумма") Float sum
    ) {
        try {
            UserEntity user = authUtil.auth(login, password);
            Optional<CategoryEntity> categoryEntity = categoryRepository.findById(id);

            if (categoryEntity.isEmpty()) {
                throw new RuntimeException("Не найдена категория с id " + id);
            }

            if (!Objects.equals(categoryEntity.get().getUser().getId(), user.getId())) {
                throw new RuntimeException("Категория принадлежит другому пользователю. Можно добавлять доходы только в свою категорию");
            }

            if (!Objects.equals(categoryEntity.get().getType(), CategoryEntity.CATEGORY_TYPE_COST)) {
                throw new RuntimeException("Тип категории, в которую можно добавлять расход должен быть " + CategoryEntity.CATEGORY_TYPE_COST);
            }

            if (sum <= 0) {
                throw new RuntimeException("Сумма, должна быть больше 0");
            }


            Float categoryLimitCalculate = budgetService.calculateBudgetByCategory(categoryEntity.get(), user);
            String limitMessage = "Текущий расход по категории " + categoryLimitCalculate + ". Бюджет " + categoryEntity.get().getLimit();

            if (categoryEntity.get().getLimit() > 0 && categoryLimitCalculate > categoryEntity.get().getLimit()) {
                limitMessage = limitMessage + ". Внимание! Текущий бюджет превышен на " + (categoryLimitCalculate - categoryEntity.get().getLimit());
            }

            BudgetEntity budgetEntity = BudgetEntity.create(categoryEntity.get(), sum);
            budgetRepository.save(budgetEntity);

            return ResponseEntity.status(HttpStatus.OK).body(
                    "Расход добавлен: " + categoryEntity.get().getName() + " " + sum + ". " + limitMessage
            );
        } catch (RuntimeException ex) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }

    @Operation(summary = "Добавить новую статью доходов")
    @ApiResponse(responseCode = "200", description = "ОК")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @PostMapping("/i")
    public ResponseEntity<?> addIncome(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password,
            @RequestParam @Parameter(description = "Id категории") Long id,
            @RequestParam @Parameter(description = "Сумма") Float sum
    ) {
        try {
            UserEntity user = authUtil.auth(login, password);
            Optional<CategoryEntity> categoryEntity = categoryRepository.findById(id);

            if (categoryEntity.isEmpty()) {
                throw new RuntimeException("Не найдена категория с id " + id);
            }

            if (!Objects.equals(categoryEntity.get().getUser().getId(), user.getId())) {
                throw new RuntimeException("Категория принадлежит другому пользователю. Можно добавлять доходы только в свою категорию");
            }

            if (!Objects.equals(categoryEntity.get().getType(), CategoryEntity.CATEGORY_TYPE_INCOME)) {
                throw new RuntimeException("Тип категории, в которую можно добавлять доход должен быть " + CategoryEntity.CATEGORY_TYPE_INCOME);
            }

            if (sum <= 0) {
                throw new RuntimeException("Сумма, должна быть больше 0");
            }

            BudgetEntity budgetEntity = BudgetEntity.create(categoryEntity.get(), sum);
            budgetRepository.save(budgetEntity);

            return ResponseEntity.status(HttpStatus.OK).body("Доход добавлен: " + categoryEntity.get().getName() + " " + sum);
        } catch (RuntimeException ex) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }

    @Operation(summary = "Перевод средств другому пользователю")
    @ApiResponse(responseCode = "200", description = "ОК")
    @ApiResponse(responseCode = "400", description = "Неверный запрос")
    @PostMapping("/t")
    public ResponseEntity<?> transfer(
            @RequestHeader("login") @Parameter(description = "Логин") String login,
            @RequestHeader("password") @Parameter(description = "Пароль") String password,
            @RequestParam @Parameter(description = "Логин пользователя, кому осуществляем перевод") String toUserLogin,
            @RequestParam @Parameter(description = "Сумма") Float sum
    ) {
        try {
            UserEntity user = authUtil.auth(login, password);

            Optional<UserEntity> toUser = userRepository.findOneByLogin(toUserLogin);
            if (toUser.isEmpty()){
                throw new RuntimeException("Пользователь, которому планируется осуществить перевод средств (" + toUserLogin + ") не найден.");
            }

            if (sum <= 0) {
                throw new RuntimeException("Сумма, должна быть больше 0");
            }

            budgetService.transferToUser(user, toUser.get(), sum);

            return ResponseEntity.status(HttpStatus.OK).body("Перевод суммы " + sum + " от пользователя " + user.getLogin() + " пользователю " + toUserLogin + " выполнен.");
        } catch (RuntimeException ex) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка: " + ex.getMessage());
        }
    }
}
