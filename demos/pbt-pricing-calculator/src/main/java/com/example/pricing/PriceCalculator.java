package com.example.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing calculator with two planted bugs that traditional tests miss.
 *
 * <p>Both bugs are "rounding" bugs — they produce correct results for the most
 * commonly tested inputs (round prices, integer discount percentages) but
 * diverge on edge-case combinations that property-based testing discovers.</p>
 */
public class PriceCalculator {

    /**
     * Compute line-item total: (unitPrice * quantity) minus discount, plus VAT.
     *
     * <p><b>BUG 1 — Early rounding of discount rate.</b><br>
     * {@code discountPercent / 100} is rounded to 2 decimal places BEFORE
     * multiplying by subtotal.  For integer discounts (10%, 20%) this is
     * lossless.  For fractional discounts like 1.05%, the intermediate rounding
     * truncates 0.0105 to 0.01, losing the 0.05 fraction.</p>
     *
     * <p><b>BUG 2 — Wrong rounding mode on VAT.</b><br>
     * Uses {@link RoundingMode#FLOOR} instead of {@link RoundingMode#HALF_UP}.
     * For most price/rate combinations the two modes agree; they diverge on
     * small prices with odd VAT rates (e.g. unitPrice=0.03, vatRate=0.07).</p>
     *
     * @param unitPrice       non-negative unit price
     * @param quantity        non-negative quantity
     * @param discountPercent discount percentage in [0, 100]
     * @param vatRate         non-negative VAT rate (e.g. 0.20 for 20%)
     * @return the total including discount and VAT, rounded to 2 d.p.
     */
    public BigDecimal calculateTotal(BigDecimal unitPrice, int quantity,
                                      BigDecimal discountPercent, BigDecimal vatRate) {
        // --- validation ---
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

        // BUG 1: early rounding — 2 d.p. precision loses fractional discounts
        BigDecimal discountRate = discountPercent.divide(
                new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.multiply(discountRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discount);

        // BUG 2: FLOOR instead of HALF_UP
        BigDecimal vat = afterDiscount.multiply(vatRate)
                .setScale(2, RoundingMode.FLOOR);
        BigDecimal total = afterDiscount.add(vat);

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
