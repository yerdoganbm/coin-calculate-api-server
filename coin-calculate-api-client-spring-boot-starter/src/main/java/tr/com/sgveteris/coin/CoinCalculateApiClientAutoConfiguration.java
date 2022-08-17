package tr.com.sgveteris.coin;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestTemplate;


@Configuration
@ConditionalOnClass(CoinCalculateApiClient.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CoinCalculateApiClientAutoConfiguration {

    private final CoinCalculateApiClientConfigurationProperties configurationProperties;

    @Primary
    @Bean("coin-calculate-api-client")
    @ConditionalOnClass(CoinCalculateApiClient.class)
    @ConditionalOnProperty(prefix = "lib.coin.config.oauth", value = "enabled", havingValue = "false")
    public RestTemplate orderApiClientRestTemplate() {
        return new RestTemplate();
    }


}
