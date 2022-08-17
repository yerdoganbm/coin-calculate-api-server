package tr.com.sgveteris.coin.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tr.com.sgveteris.coin.config.CoinCalculatorApiServerConfigurationProperties;
import tr.com.sgveteris.coin.domain.BlockChainServiceResult;
import tr.com.sgveteris.coin.domain.TickersData;
import tr.com.sgveteris.coin.enumeration.EnumCoinConvertSymbolType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BlockChainServiceImpl implements IBlockChainService {

    private final CoinCalculatorApiServerConfigurationProperties configurationProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public BlockChainServiceImpl(ObjectMapper objectMapper, @Qualifier("blockChainRestTemplate") RestTemplate restTemplate, CoinCalculatorApiServerConfigurationProperties configurationProperties) {
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.configurationProperties = configurationProperties;
    }

    private static final String BLOCK_CHAIN_SERVICE_HANDLE_ERROR = "[ERROR]";
    private static final String BLOCK_CHAIN_SERVICE_HANDLE_TIMEOUT = "[TIMEOUT]";
    private static final String BLOCK_CHAIN_SERVICE_HANDLE_N_A = " [N/A]";
    private static final long BLOCK_CHAIN_SERVICE_HANDLE_TIMEOUT_VALUE = -1L;

    @HystrixCommand(fallbackMethod = "callGetTicketsServiceHystrixFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "120000"),
    })
    @Override
    public BlockChainServiceResult callGetTickersService(EnumCoinConvertSymbolType inSymbolType) {

        log.info("Pre-processing is done before shipping process for transfer transaction to blockchain getTickers service.");

        BlockChainServiceResult.BlockChainServiceResultBuilder resultBuilder = BlockChainServiceResult.builder();
        LocalDateTime startDate = LocalDateTime.now();
        resultBuilder.requestTimestamp(startDate);

        StopWatch sw = StopWatch.createStarted();

        TickersData[] resultTickersDataList = null;
        try {
            resultTickersDataList = restTemplate.getForObject(
                    configurationProperties.getBlockChainGetTickersService().getAddress(),
                    TickersData[].class);

            if (log.isDebugEnabled())
                log.debug("Result from blockChain service for getTickers notification: {}", objectMapper.writeValueAsString(resultTickersDataList));

            resultBuilder.responseBody(Objects.nonNull(resultTickersDataList) ? objectMapper.writeValueAsString(resultTickersDataList) : BLOCK_CHAIN_SERVICE_HANDLE_N_A);
            resultBuilder.responseTimestamp(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Exception occurred while retrieving blockChain data: {}", e.getMessage());
            resultBuilder.responseBody(String.format("%s Get_Tickers-Service-Response: %s", BLOCK_CHAIN_SERVICE_HANDLE_ERROR, e.getMessage()));
        }

        if (Objects.nonNull(resultTickersDataList)) {
            var tickersData = this.searchGetTickersData(resultTickersDataList,inSymbolType);
            tickersData.ifPresent(resultBuilder::tickersData);
        }
        sw.stop();
        resultBuilder.metric(sw.getTime(TimeUnit.MILLISECONDS));

        return resultBuilder.build();
    }

    private BlockChainServiceResult callGetTicketsServiceHystrixFallback(EnumCoinConvertSymbolType inSymbolType) {

        log.info("There was a timeout timeout when giving notice of tickers.");

        BlockChainServiceResult.BlockChainServiceResultBuilder resultBuilder = BlockChainServiceResult.builder();
        resultBuilder.requestTimestamp(LocalDateTime.now());

        resultBuilder
                .responseBody(BLOCK_CHAIN_SERVICE_HANDLE_TIMEOUT)
                .metric(BLOCK_CHAIN_SERVICE_HANDLE_TIMEOUT_VALUE);

        return resultBuilder.build();
    }


    private Optional<TickersData> searchGetTickersData(TickersData[] tickersDataList,EnumCoinConvertSymbolType inSymbolType){
        return Arrays.stream(tickersDataList).filter(item -> inSymbolType.equals(EnumCoinConvertSymbolType.getValueByName(item.getSymbol())))
                .findFirst();
    }


}
