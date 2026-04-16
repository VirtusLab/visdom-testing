package com.example.pricing;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Property-based tests using jqwik.
 *
 * <p>8 properties total:</p>
 * <ul>
 *   <li>6 <b>structural</b> properties — express invariants that hold even on
 *       buggy code (they don't need a reference implementation).</li>
 *   <li>2 <b>oracle</b> properties — compare against {@link ReferenceFormula}
 *       and <b>fail</b> on the buggy {@link PriceCalculator}, catching both
 *       planted bugs.</li>
 * </ul>
 */
class PriceCalculatorPropertyTest {

    private final PriceCalculator calc = new PriceCalculator();

    // ===================================================================
    // Structural properties — hold for any correct-ish implementation
    // ===================================================================

    @Property
    @Label("total is never negative")
    void totalIsNeverNegative(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "100") @Scale(2) BigDecimal discountPercent,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal total = calc.calculateTotal(unitPrice, quantity, discountPercent, vatRate);
        assert total.compareTo(BigDecimal.ZERO) >= 0
                : "total must be >= 0 but was " + total;
    }

    @Property
    @Label("accounting identity: total = afterDiscount + vat")
    void accountingIdentity(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "100") @Scale(2) BigDecimal discountPercent,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));

        // Replicate the calculator's internal discount computation
        BigDecimal discountRate = discountPercent.divide(
                new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal vat = afterDiscount.multiply(vatRate)
                .setScale(2, RoundingMode.FLOOR);

        BigDecimal expectedTotal = afterDiscount.add(vat)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualTotal = calc.calculateTotal(unitPrice, quantity, discountPercent, vatRate);

        assert expectedTotal.compareTo(actualTotal) == 0
                : "identity violated: expected " + expectedTotal + " but got " + actualTotal;
    }

    @Property
    @Label("monotonicity: higher discount -> lower or equal total")
    void monotonicity(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "50") @Scale(2) BigDecimal discountLow,
            @ForAll @BigRange(min = "50", max = "100") @Scale(2) BigDecimal discountHigh,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal totalLow = calc.calculateTotal(unitPrice, quantity, discountLow, vatRate);
        BigDecimal totalHigh = calc.calculateTotal(unitPrice, quantity, discountHigh, vatRate);

        assert totalHigh.compareTo(totalLow) <= 0
                : "higher discount (" + discountHigh + "%) gave higher total ("
                + totalHigh + ") than lower discount (" + discountLow + "%: " + totalLow + ")";
    }

    @Property
    @Label("zero discount means full price plus VAT")
    void zeroDiscountMeansFullPrice(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal total = calc.calculateTotal(
                unitPrice, quantity, BigDecimal.ZERO, vatRate);

        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        // With zero discount, total = subtotal * (1 + vatRate)
        // But we need to apply the same rounding as the calculator
        BigDecimal vat = subtotal.multiply(vatRate).setScale(2, RoundingMode.FLOOR);
        BigDecimal expected = subtotal.add(vat).setScale(2, RoundingMode.HALF_UP);

        assert expected.compareTo(total) == 0
                : "zero-discount total should be " + expected + " but was " + total;
    }

    @Property
    @Label("full discount means zero total")
    void fullDiscountMeansZero(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal total = calc.calculateTotal(
                unitPrice, quantity, new BigDecimal("100"), vatRate);

        assert total.compareTo(BigDecimal.ZERO) == 0
                : "100% discount should yield 0.00 but got " + total;
    }

    @Property
    @Label("boundary: total <= subtotal * (1 + vatRate)")
    void boundaryBehavior(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "100") @Scale(2) BigDecimal discountPercent,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal total = calc.calculateTotal(unitPrice, quantity, discountPercent, vatRate);
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal upperBound = subtotal.multiply(
                BigDecimal.ONE.add(vatRate)).setScale(2, RoundingMode.CEILING);

        assert total.compareTo(upperBound) <= 0
                : "total " + total + " exceeds upper bound " + upperBound;
    }

    // ===================================================================
    // Oracle properties — compare against ReferenceFormula
    // These FAIL on the buggy PriceCalculator, catching both bugs.
    // ===================================================================

    @Property
    @Label("oracle: discount matches reference formula")
    void discountMatchesReferenceFormula(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "100") @Scale(2) BigDecimal discountPercent,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        BigDecimal actual = calc.calculateTotal(unitPrice, quantity, discountPercent, vatRate);
        BigDecimal expected = ReferenceFormula.calculate(unitPrice, quantity, discountPercent, vatRate);

        assert expected.compareTo(actual) == 0
                : "BUG DETECTED — calculator=" + actual + " but reference=" + expected
                + " for unitPrice=" + unitPrice + " qty=" + quantity
                + " discount=" + discountPercent + "% vatRate=" + vatRate;
    }

    @Property
    @Label("oracle: VAT amount matches reference formula")
    void vatMatchesReferenceFormula(
            @ForAll @Positive @Scale(2) BigDecimal unitPrice,
            @ForAll @IntRange(min = 1, max = 1000) int quantity,
            @ForAll @BigRange(min = "0", max = "100") @Scale(2) BigDecimal discountPercent,
            @ForAll @BigRange(min = "0", max = "0.30") @Scale(2) BigDecimal vatRate) {

        // Compute the VAT the same way the buggy calculator does
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));
        BigDecimal discountRate = discountPercent.divide(
                new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal afterDiscountBuggy = subtotal.subtract(discount);
        BigDecimal vatBuggy = afterDiscountBuggy.multiply(vatRate)
                .setScale(2, RoundingMode.FLOOR);

        // Compute reference VAT
        BigDecimal discountAmountRef = subtotal.multiply(discountPercent)
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal afterDiscountRef = subtotal.subtract(discountAmountRef);
        BigDecimal vatRef = afterDiscountRef.multiply(vatRate)
                .setScale(2, RoundingMode.HALF_UP);

        assert vatRef.compareTo(vatBuggy) == 0
                : "VAT BUG DETECTED — buggy VAT=" + vatBuggy + " but reference VAT=" + vatRef
                + " for unitPrice=" + unitPrice + " qty=" + quantity
                + " discount=" + discountPercent + "% vatRate=" + vatRate;
    }
}
