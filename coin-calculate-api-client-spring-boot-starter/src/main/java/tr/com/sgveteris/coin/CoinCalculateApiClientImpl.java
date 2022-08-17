package tr.com.sgveteris.coin;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
@ConditionalOnBean(CoinCalculateApiClient.class)
public class CoinCalculateApiClientImpl extends CoinCalculateApiClientAbstractService implements CoinCalculateApiClient {

    private final RestTemplate restTemplate;
    private final CoinCalculateApiClientConfigurationProperties config;
    private final String url;

    @SuppressWarnings("unused")
    public CoinCalculateApiClientImpl(@Qualifier("coin-calculate-api-client") RestTemplate restTemplate,
                                      CoinCalculateApiClientConfigurationProperties config) {
        this.restTemplate = restTemplate;
        this.config = config;
        this.url = String.format("%s://%s:%d%s", config.getProtocol(), config.getHost(), config.getPort(), config.getPath());

    }




}
