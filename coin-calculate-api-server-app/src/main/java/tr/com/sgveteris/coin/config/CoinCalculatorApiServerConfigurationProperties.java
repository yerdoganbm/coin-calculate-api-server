package tr.com.sgveteris.coin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "api.coin")
public class CoinCalculatorApiServerConfigurationProperties {

    private Integer connectTimeout;

    private Integer readTimeout;

    private HttpProxy httpProxy = new HttpProxy();

    private BlockChainGetTickersService blockChainGetTickersService = new BlockChainGetTickersService();


    @Data
    public static class HttpProxy {
        private boolean proxyHttpEnabled;

        private boolean proxyHttpsEnabled;

        private String proxyHost;

        private Integer proxyPort;

        private String proxyUsername;

        private String proxyDomain;

        private String proxyPasso;
    }

    @Data
    public static class BlockChainGetTickersService {
        private String address;
    }


}
