package tr.com.sgveteris.coin;


import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConditionalOnBean(CoinCalculateApiClient.class)
@ConfigurationProperties(prefix = "lib.coin.config")
public class CoinCalculateApiClientConfigurationProperties {

    private String protocol;

    private String host;

    private Integer port;

    private String path = "/";


    @Data
    public static class OAuth {

        private Boolean enabled = false;

        private String resourceId = "coin-calculate-api-server";


    }
}

