package com.example.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money normalisation rules shared by all services.
 */
public final class Money {

    private Money() {
    }

    /**
     * Normalises an amount to scale 2 (HALF_UP).
     *
     * @param amount the amount, may be null
     * @return the amount with scale 2, or null
     */
    public static BigDecimal normalize(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Normalises a computed total: zero becomes {@link BigDecimal#ZERO} (serialized as {@code 0}),
     * anything else gets scale 2.
     *
     * @param total the computed total, may be null (treated as zero)
     * @return the normalised total
     */
    public static BigDecimal total(BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return normalize(total);
    }
}
