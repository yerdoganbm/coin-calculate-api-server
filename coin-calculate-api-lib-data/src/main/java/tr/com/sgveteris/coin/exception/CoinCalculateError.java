package tr.com.sgveteris.coin.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoinCalculateError {
    private String responseCode;

    private String responseDesc;

    private Integer stan;

    private String transactionGroupId;

    private String correlationId;

    private LocalDateTime requestTimestamp;

    private String errorDetails;
}
