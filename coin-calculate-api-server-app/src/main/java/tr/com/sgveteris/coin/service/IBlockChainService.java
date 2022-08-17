package tr.com.sgveteris.coin.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import tr.com.sgveteris.coin.domain.BlockChainServiceResult;
import tr.com.sgveteris.coin.enumeration.EnumCoinConvertSymbolType;

public interface IBlockChainService {

    @HystrixCommand(fallbackMethod = "callGetTicketsServiceHystrixFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "120000"),
    })
    BlockChainServiceResult callGetTickersService(EnumCoinConvertSymbolType inSymbolType);
}
