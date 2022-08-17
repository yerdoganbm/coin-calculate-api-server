package tr.com.sgveteris.coin.service;

import tr.com.sgveteris.coin.rest.CoinCalculateRequest;
import tr.com.sgveteris.coin.rest.CoinCalculateResponse;

import java.util.List;

public interface ICoinCalculateService {
    CoinCalculateResponse getFetchCoinDetail(CoinCalculateRequest request);
}
