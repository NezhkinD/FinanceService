package app.Service;

import app.Entity.BudgetEntity;
import app.Entity.CategoryEntity;
import app.Entity.UserEntity;
import app.Repository.BudgetRepository;
import app.Repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Object[]> getByUser(UserEntity user) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);

        Root<BudgetEntity> budgetRoot = query.from(BudgetEntity.class);
        Join<BudgetEntity, CategoryEntity> categoryJoin = budgetRoot.join("category");
        Join<CategoryEntity, UserEntity> userJoin = categoryJoin.join("user");

        query.multiselect(
                budgetRoot.get("id"),
                budgetRoot.get("sum"),
                categoryJoin.get("id"),
                categoryJoin.get("name"),
                categoryJoin.get("type"),
                categoryJoin.get("limitC"),
                userJoin.get("id"),
                userJoin.get("login")
        );

        Predicate userCondition = cb.equal(userJoin.get("id"), user.getId());
        query.where(cb.and(userCondition));
        query.orderBy(cb.asc(budgetRoot.get("id")));

        return entityManager.createQuery(query).getResultList();
    }

    public Float calculateBudgetByCategory(CategoryEntity categoryEntity, UserEntity user) {
        List<Object[]> budgets = budgetRepository.findByCategoryAndUser(categoryEntity.getId(), user);

        float totalSum = 0.0F;

        for (Object[] row : budgets) {
            Float budgetSum = (Float) row[1];
            totalSum += budgetSum;
        }

        return totalSum;
    }

    public Float calculateTotalBudget(UserEntity user) {
        List<Object[]> budgets = getByUser(user);

        float totalIncome = 0.0F;
        float totalCost = 0.0F;

        for (Object[] row : budgets) {
            Float budgetSum = (Float) row[1];
            String categoryType = (String) row[4];

            if ("income".equals(categoryType)) {
                totalIncome += budgetSum;
            } else if ("cost".equals(categoryType)) {
                totalCost += budgetSum;
            }
        }
        return totalIncome - totalCost;
    }

    public void transferToUser(UserEntity from, UserEntity to, Float sum) {
        Float availableBudgetFrom = calculateTotalBudget(from);
        if (sum > availableBudgetFrom) {
            throw new RuntimeException("Сумма перевода (" + sum + ") больше чем текущий доступный бюджет пользователя " + from.getLogin() + " (" + availableBudgetFrom + ")");
        }

        String categoryTransferFromName = "Перевод пользователю " + to.getLogin();
        Optional<CategoryEntity> categoryTransferFrom = categoryRepository.findOneByNameAndTypeAndUser(categoryTransferFromName, CategoryEntity.CATEGORY_TYPE_COST, from);
        if (categoryTransferFrom.isEmpty()) {
            CategoryEntity categoryEntity = CategoryEntity.create(categoryTransferFromName, CategoryEntity.CATEGORY_TYPE_COST, from, 0.0F);
            CategoryEntity save = categoryRepository.save(categoryEntity);
            categoryTransferFrom = Optional.of(save);
        }

        String categoryTransferToName = "Перевод от пользователя " + from.getLogin();
        Optional<CategoryEntity> categoryTransferTo = categoryRepository.findOneByNameAndTypeAndUser(categoryTransferToName, CategoryEntity.CATEGORY_TYPE_INCOME, to);
        if (categoryTransferTo.isEmpty()) {
            CategoryEntity categoryEntity = CategoryEntity.create(categoryTransferToName, CategoryEntity.CATEGORY_TYPE_INCOME, to, 0.0F);
            CategoryEntity save = categoryRepository.save(categoryEntity);
            categoryTransferTo = Optional.of(save);
        }

        budgetRepository.save(BudgetEntity.create(categoryTransferFrom.get(), sum));
        budgetRepository.save(BudgetEntity.create(categoryTransferTo.get(), sum));
    }
}