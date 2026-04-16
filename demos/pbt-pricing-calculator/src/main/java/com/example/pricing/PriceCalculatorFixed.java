package com.example.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Corrected pricing calculator — both bugs from {@link PriceCalculator} are fixed.
 *
 * <ul>
 *   <li><b>Bug 1 fix:</b> discount rate keeps 10 decimal places of precision
 *       instead of 2, so fractional discounts like 1.05% are computed correctly.</li>
 *   <li><b>Bug 2 fix:</b> VAT rounding uses {@link RoundingMode#HALF_UP} instead
 *       of {@link RoundingMode#FLOOR}.</li>
 * </ul>
 */
public class PriceCalculatorFixed {

    public BigDecimal calculateTotal(BigDecimal unitPrice, int quantity,
                                      BigDecimal discountPercent, BigDecimal vatRate) {
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("negative price");
        if (quantity < 0)
            throw new IllegalArgumentException("negative quantity");
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0
                || discountPercent.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("discount out of range");
        if (vatRate.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("negative VAT");

        BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantity));

        // FIX 1: keep high precision (10 d.p.) for the discount rate
        BigDecimal discountRate = discountPercent.divide(
                new BigDecimal("100"), 10, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discount);

        // FIX 2: use HALF_UP for VAT rounding
        BigDecimal vat = afterDiscount.multiply(vatRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = afterDiscount.add(vat);

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
