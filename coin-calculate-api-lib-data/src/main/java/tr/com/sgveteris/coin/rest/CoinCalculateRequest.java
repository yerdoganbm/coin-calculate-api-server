package tr.com.sgveteris.coin.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoinCalculateRequest implements Serializable {

    @NotNull(message = "spendSymbol cannot be null")
    private String spendSymbol;

    @NotNull(message = "receiveSymbol cannot be null")
    private String receiveSymbol;

    @NotNull(message = "spendAmount cannot be null")
    private BigDecimal spendAmount;

}
