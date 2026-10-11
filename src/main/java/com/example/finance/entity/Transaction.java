package com.example.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single income or expense entry. Its type is derived from its category.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "txn_date", nullable = false)
    private LocalDate date;

    private String description;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Creates an empty transaction (required by JPA). */
    public Transaction() {
    }

    /**
     * Creates a transaction.
     *
     * @param amount      positive amount, scale 2
     * @param date        transaction date
     * @param description optional description
     * @param category    category (determines the type)
     * @param user        owning user
     */
    public Transaction(BigDecimal amount, LocalDate date, String description, Category category, User user) {
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.category = category;
        this.user = user;
    }

    /** @return the id */
    public Long getId() {
        return id;
    }

    /** @param id the id */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the amount */
    public BigDecimal getAmount() {
        return amount;
    }

    /** @param amount the amount */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /** @return the date */
    public LocalDate getDate() {
        return date;
    }

    /** @return the description */
    public String getDescription() {
        return description;
    }

    /** @param description the description */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return the category */
    public Category getCategory() {
        return category;
    }

    /** @param category the category */
    public void setCategory(Category category) {
        this.category = category;
    }

    /** @return the owning user */
    public User getUser() {
        return user;
    }
}
