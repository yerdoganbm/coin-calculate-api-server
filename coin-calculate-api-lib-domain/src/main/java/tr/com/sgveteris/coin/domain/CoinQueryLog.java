package tr.com.sgveteris.coin.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.NumberFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Slf4j
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@Table(name = "COIN_QUERY_LOG")
public class CoinQueryLog implements Serializable {

    @Id
    @NotNull
    @Column(name = "TRAN_ID", nullable = false)
    private String tranId;

    @NotNull
    @Column(name = "SYMBOL_TYPE", nullable = false)
    private String symbolType;

    @Column(name = "RESPONSE_TEXT",length = 4000)
    private String responseText;

    @NotNull
    @Column(name = "REQUEST_TIME")
    private LocalDateTime requestTime;

    @Column(name = "RESPONSE_TIME")
    private LocalDateTime responseTime;

    @Column(name = "SYSTEM_METRIC", nullable = false)
    @NumberFormat(style= NumberFormat.Style.NUMBER)
    private Long systemMetric;




}