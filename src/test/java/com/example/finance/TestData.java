package com.example.finance;

import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.example.finance.entity.SavingsGoal;
import com.example.finance.entity.Transaction;
import com.example.finance.entity.User;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity fixtures for unit tests.
 */
public final class TestData {

    private TestData() {
    }

    /**
     * @param id       user id
     * @param username email
     * @return a persisted-looking user
     */
    public static User user(long id, String username) {
        User user = new User(username, "hash", "Test User", "+1234567890");
        user.setId(id);
        return user;
    }

    /**
     * @param id   category id
     * @param name name
     * @param type type
     * @return a default category
     */
    public static Category defaultCategory(long id, String name, CategoryType type) {
        Category category = new Category(name, type, false, null);
        category.setId(id);
        return category;
    }

    /**
     * @param id    category id
     * @param name  name
     * @param type  type
     * @param owner owning user
     * @return a custom category
     */
    public static Category customCategory(long id, String name, CategoryType type, User owner) {
        Category category = new Category(name, type, true, owner);
        category.setId(id);
        return category;
    }

    /**
     * @param id       transaction id
     * @param amount   amount
     * @param date     ISO date
     * @param category category
     * @param owner    owning user
     * @return a transaction
     */
    public static Transaction transaction(long id, String amount, String date, Category category, User owner) {
        Transaction t = new Transaction(new BigDecimal(amount), LocalDate.parse(date), "desc", category, owner);
        t.setId(id);
        return t;
    }

    /**
     * @param id        goal id
     * @param target    target amount
     * @param startDate ISO start date
     * @param owner     owning user
     * @return a goal with target date 2099-01-01
     */
    public static SavingsGoal goal(long id, String target, String startDate, User owner) {
        SavingsGoal goal = new SavingsGoal("Goal", new BigDecimal(target), LocalDate.of(2099, 1, 1),
                LocalDate.parse(startDate), owner);
        goal.setId(id);
        return goal;
    }
}
