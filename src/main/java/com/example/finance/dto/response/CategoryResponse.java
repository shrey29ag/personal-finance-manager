package com.example.finance.dto.response;

import com.example.finance.entity.Category;
import com.example.finance.entity.CategoryType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A category as returned by the API. The custom flag is emitted under both {@code custom} and
 * {@code isCustom}, because clients use both names.
 */
@JsonPropertyOrder({"name", "type", "custom", "isCustom"})
public class CategoryResponse {

    private final String name;
    private final CategoryType type;
    private final boolean custom;

    /**
     * Creates the response.
     *
     * @param name   category name
     * @param type   INCOME or EXPENSE
     * @param custom whether it is user-defined
     */
    public CategoryResponse(String name, CategoryType type, boolean custom) {
        this.name = name;
        this.type = type;
        this.custom = custom;
    }

    /**
     * Maps an entity to its response.
     *
     * @param category the entity
     * @return the response
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getName(), category.getType(), category.isCustom());
    }

    /** @return the name */
    public String getName() {
        return name;
    }

    /** @return the type */
    public CategoryType getType() {
        return type;
    }

    /** @return whether it is user-defined, serialized as {@code custom} */
    @JsonProperty("custom")
    public boolean isCustom() {
        return custom;
    }

    /** @return whether it is user-defined, serialized as {@code isCustom} */
    @JsonProperty("isCustom")
    public boolean getIsCustom() {
        return custom;
    }
}
