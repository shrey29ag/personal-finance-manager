package com.example.finance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A transaction category. Default categories are global ({@code user == null}, {@code custom == false});
 * custom categories belong to a single user.
 */
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Column(nullable = false)
    private boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Creates an empty category (required by JPA). */
    public Category() {
    }

    /**
     * Creates a category.
     *
     * @param name   category name
     * @param type   INCOME or EXPENSE
     * @param custom whether it is a user-defined category
     * @param user   owning user, or {@code null} for a default category
     */
    public Category(String name, CategoryType type, boolean custom, User user) {
        this.name = name;
        this.type = type;
        this.custom = custom;
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

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @return the type */
    public CategoryType getType() {
        return type;
    }

    /** @return whether this is a user-defined category */
    public boolean isCustom() {
        return custom;
    }

    /** @return the owning user, or {@code null} for a default category */
    public User getUser() {
        return user;
    }
}
