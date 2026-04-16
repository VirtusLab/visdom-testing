package com.example.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A simple, obviously-correct reference implementation used as an oracle
 * in property-based tests.
 *
 * <p>The key difference from {@link PriceCalculator}:
 * <ul>
 *   <li>Discount is computed in a single expression with 10-digit intermediate
 *       precision — no early rounding.</li>
 *   <li>VAT uses {@link RoundingMode#HALF_UP}, the standard rounding mode.</li>
 * </ul>
 */
public class ReferenceFormula {

    public static BigDecimal calculate(BigDecimal unitPrice, int quantity,
                                        BigDecimal discountPercent, BigDecimal vatRate) {
        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));

        BigDecimal discountAmount = subtotal.multiply(discountPercent)
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        BigDecimal vat = afterDiscount.multiply(vatRate)
                .setScale(2, RoundingMode.HALF_UP);

        return afterDiscount.add(vat).setScale(2, RoundingMode.HALF_UP);
    }
}
