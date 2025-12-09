package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for a single line item in a COC report.
 * Represents billing information for one item including quantity, unit price, and calculated prices.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class COCReportLineItemDto {
    
    /** The name or description of the line item */
    private String name;

    /** The quantity of the item */
    private Double quantity;

    /** The unit of measurement (e.g., "óra", "story pont") */
    private String unit;

    /** The net price (before VAT) */
    private Double netPrice;

    /** The VAT amount */
    private Double vatPrice;

    /** The gross price (net price + VAT) */
    private Double grossPrice;

    /** The price per unit */
    private Double unitPrice;

    /**
     * Adds the prices from another line item to this one (for summary calculations).
     *
     * @param other the other line item to summarize
     */
    public void summarize(COCReportLineItemDto other) {
        this.vatPrice += other.vatPrice;
        this.grossPrice += other.grossPrice;
        this.netPrice += other.netPrice;
    }

}
