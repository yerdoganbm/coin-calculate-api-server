package tr.com.sgveteris.coin.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoinCalculateResponse implements Serializable {

    private BigDecimal receiveCoin;

    private LocalDateTime receiveCoinDate;

}
