package tr.com.sgveteris.coin.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockChainServiceResult implements Serializable {

    private LocalDateTime requestTimestamp;

    private LocalDateTime responseTimestamp;

    private Long metric;

    private String responseBody;

    private TickersData tickersData;

}
