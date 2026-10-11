package com.example.finance.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void normalizeSetsScaleTwoWithHalfUp() {
        assertThat(Money.normalize(new BigDecimal("5000"))).hasToString("5000.00");
        assertThat(Money.normalize(new BigDecimal("1.005"))).hasToString("1.01");
        assertThat(Money.normalize(null)).isNull();
    }

    @Test
    void totalReturnsPlainZeroForZeroAndNull() {
        assertThat(Money.total(new BigDecimal("0.00"))).isSameAs(BigDecimal.ZERO).hasToString("0");
        assertThat(Money.total(null)).isSameAs(BigDecimal.ZERO);
    }

    @Test
    void totalKeepsScaleTwoForNonZero() {
        assertThat(Money.total(new BigDecimal("6550"))).hasToString("6550.00");
        assertThat(Money.total(new BigDecimal("-12.5"))).hasToString("-12.50");
    }
}
