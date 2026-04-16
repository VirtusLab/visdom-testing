package com.example.pricing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Traditional unit tests — 10 tests, all using round prices and integer
 * discount percentages.
 *
 * <p><b>Every single test passes on the buggy {@link PriceCalculator}.</b></p>
 *
 * <p>Why?  Both planted bugs are invisible for these inputs:</p>
 * <ul>
 *   <li>Bug 1 (early rounding) is lossless for integer discount percentages
 *       because, e.g., 10/100 = 0.10 exactly in 2 d.p.</li>
 *   <li>Bug 2 (FLOOR vs HALF_UP) agrees with HALF_UP whenever the VAT amount's
 *       third decimal digit is 0–4, which happens for all the round-price
 *       examples below.</li>
 * </ul>
 */
class PriceCalculatorTraditionalTest {

    private final PriceCalculator calc = new PriceCalculator();

    // --- happy-path tests with integer discounts and round prices ---

    @Test
    @DisplayName("10.00 x 1, 10% off, 20% VAT -> 10.80")
    void basicCalculation() {
        // subtotal=10.00, discount=1.00, afterDisc=9.00, vat=1.80 -> 10.80
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("10.00"), 1,
                new BigDecimal("10"), new BigDecimal("0.20"));
        assertEquals(new BigDecimal("10.80"), result);
    }

    @Test
    @DisplayName("25.00 x 2, 20% off, 23% VAT -> 49.20")
    void multipleQuantity() {
        // subtotal=50.00, discount=10.00, afterDisc=40.00, vat=9.20 -> 49.20
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("25.00"), 2,
                new BigDecimal("20"), new BigDecimal("0.23"));
        assertEquals(new BigDecimal("49.20"), result);
    }

    @Test
    @DisplayName("50.00 x 1, 0% off, 20% VAT -> 60.00")
    void noDiscount() {
        // subtotal=50.00, discount=0.00, afterDisc=50.00, vat=10.00 -> 60.00
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("50.00"), 1,
                new BigDecimal("0"), new BigDecimal("0.20"));
        assertEquals(new BigDecimal("60.00"), result);
    }

    @Test
    @DisplayName("100.00 x 3, 15% off, 21% VAT -> 308.55")
    void largerOrder() {
        // subtotal=300.00, discount=45.00, afterDisc=255.00, vat=53.55 -> 308.55
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("100.00"), 3,
                new BigDecimal("15"), new BigDecimal("0.21"));
        assertEquals(new BigDecimal("308.55"), result);
    }

    @Test
    @DisplayName("10.00 x 1, 50% off, 20% VAT -> 6.00")
    void halfPriceDiscount() {
        // subtotal=10.00, discount=5.00, afterDisc=5.00, vat=1.00 -> 6.00
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("10.00"), 1,
                new BigDecimal("50"), new BigDecimal("0.20"));
        assertEquals(new BigDecimal("6.00"), result);
    }

    @Test
    @DisplayName("10.00 x 1, 100% off, 20% VAT -> 0.00")
    void fullDiscount() {
        // subtotal=10.00, discount=10.00, afterDisc=0.00, vat=0.00 -> 0.00
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("10.00"), 1,
                new BigDecimal("100"), new BigDecimal("0.20"));
        assertEquals(new BigDecimal("0.00"), result);
    }

    // --- boundary tests ---

    @Test
    @DisplayName("zero quantity -> 0.00")
    void zeroQuantity() {
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("10.00"), 0,
                new BigDecimal("10"), new BigDecimal("0.20"));
        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("zero price -> 0.00")
    void zeroPrice() {
        BigDecimal result = calc.calculateTotal(
                new BigDecimal("0.00"), 5,
                new BigDecimal("10"), new BigDecimal("0.20"));
        assertEquals(new BigDecimal("0.00"), result);
    }

    // --- validation tests ---

    @Test
    @DisplayName("negative price is rejected")
    void rejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculateTotal(
                        new BigDecimal("-1.00"), 1,
                        new BigDecimal("10"), new BigDecimal("0.20")));
    }

    @Test
    @DisplayName("negative quantity is rejected")
    void rejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                calc.calculateTotal(
                        new BigDecimal("10.00"), -1,
                        new BigDecimal("10"), new BigDecimal("0.20")));
    }
}
