package tr.com.sgveteris.coin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TickersData implements Serializable {

    private String symbol;

    private BigDecimal price_24h;

    private BigDecimal volume_24h;

    private BigDecimal last_trade_price;

}
