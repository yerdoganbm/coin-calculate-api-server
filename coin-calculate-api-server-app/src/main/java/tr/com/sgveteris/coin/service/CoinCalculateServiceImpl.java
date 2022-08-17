package tr.com.sgveteris.coin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tr.com.sgveteris.coin.domain.BlockChainServiceResult;
import tr.com.sgveteris.coin.enumeration.EnumCoinConvertSymbolType;
import tr.com.sgveteris.coin.enumeration.EnumResponseCode;
import tr.com.sgveteris.coin.events.CoinQueryLogEvent;
import tr.com.sgveteris.coin.rest.CoinCalculateRequest;
import tr.com.sgveteris.coin.rest.CoinCalculateResponse;
import tr.com.sgveteris.coin.util.CoinCalculateAPIConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinCalculateServiceImpl implements ICoinCalculateService {

    private final IBlockChainService blockChainService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CoinCalculateResponse getFetchCoinDetail(CoinCalculateRequest request) {

        var coinCalculateResponse = CoinCalculateResponse.builder().build();

        var convertSymbolData = EnumCoinConvertSymbolType.getValueByName(String.join(CoinCalculateAPIConstants.STR_JOIN_FORMAT,
                request.getReceiveSymbol(),
                request.getSpendSymbol()));

        BlockChainServiceResult blockChainServiceResult = null;
        if (Objects.nonNull(convertSymbolData)) {
            blockChainServiceResult = blockChainService.callGetTickersService(convertSymbolData);

            if (ObjectUtils.allNotNull(blockChainServiceResult, blockChainServiceResult.getTickersData())) {
                var ticker = blockChainServiceResult.getTickersData();
                coinCalculateResponse.setReceiveCoin(this.calculateReceivedCoinAmount(request.getSpendAmount(), ticker.getLast_trade_price()));
                coinCalculateResponse.setReceiveCoinDate(blockChainServiceResult.getRequestTimestamp());

                //We record requests made to the service...
                eventPublisher.publishEvent(CoinQueryLogEvent.builder()
                        .symbolType(convertSymbolData)
                        .serviceResult(blockChainServiceResult)
                        .build());
            }
        }


        return coinCalculateResponse;
    }

    private BigDecimal calculateReceivedCoinAmount(BigDecimal spendAmount, BigDecimal lastTradePrice) {
        return spendAmount.divide(lastTradePrice, 8, RoundingMode.HALF_EVEN);
    }

}
