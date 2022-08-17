package tr.com.sgveteris.coin;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tr.com.sgveteris.coin.logging.CorrelationData;

import java.util.Collections;
import java.util.Optional;

public abstract  class CoinCalculateApiClientAbstractService {

    protected HttpHeaders getHttpHeaderObject() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        if(Optional.ofNullable(CorrelationData.getId()).isPresent()) {
            headers.set(CorrelationData.CORRELATION_ID, CorrelationData.getId());
        }
        return headers;

    }
}
