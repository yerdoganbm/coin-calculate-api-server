package tr.com.sgveteris.coin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tr.com.sgveteris.coin.domain.CoinQueryLog;
import tr.com.sgveteris.coin.events.CoinQueryLogEvent;
import tr.com.sgveteris.coin.service.ICoinQueryLogService;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class CoinQueryLogEventListener {

    private final ICoinQueryLogService logService;
    private static final Integer MAX_WIDTH = 1500;

    @Async
    @EventListener(classes = {CoinQueryLogEvent.class})
    public void publishCoinQueryLogTable(CoinQueryLogEvent coinQueryLogEvent) {
        if (log.isDebugEnabled()) {
            log.debug("Got notify event: {}", coinQueryLogEvent);
        }
        var serviceResult = coinQueryLogEvent.getServiceResult();

        try {
            if (ObjectUtils.allNotNull(serviceResult)) {
                //insertMetricLog-initialize record
                var queryLog = CoinQueryLog.builder()
                        .tranId(UUID.randomUUID().toString())
                        .requestTime(serviceResult.getRequestTimestamp())
                        .responseText(StringUtils.abbreviate(serviceResult.getResponseBody(),MAX_WIDTH))
                        .responseTime(serviceResult.getResponseTimestamp())
                        .symbolType(coinQueryLogEvent.getSymbolType().getName())
                        .systemMetric(serviceResult.getMetric())
                        .build();
                logService.saveCoinQueryLogRecord(queryLog);
            }
        }
        catch (Exception ex){
            log.error("An error occurred while saving to the CoinQueryLog table. {}",ex.getMessage());
        }
    }
}
