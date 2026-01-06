package ma.albaraka.bank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationResponse {

    private Long id;
    private String type;
    private String amount;
    private String status;
    private Long sourceAccountId;
    private String sourceAccountNumber;
    private Long destinationAccountId;
    private String destinationAccountNumber;
    private Long createdById;
    private String createdByName;
    private Long approvedById;
    private String approvedByName;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime executedAt;
    private String note;
}
