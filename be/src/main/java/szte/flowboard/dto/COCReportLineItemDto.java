package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class COCReportLineItemDto {
    private String name;

    private Double quantity;

    private String unit;

    private Double netPrice;

    private Double vatPrice;

    private Double grossPrice;

    private Double unitPrice;

    public void summarize(COCReportLineItemDto other) {
        this.vatPrice += other.vatPrice;
        this.grossPrice += other.grossPrice;
        this.netPrice += other.netPrice;
    }

}
