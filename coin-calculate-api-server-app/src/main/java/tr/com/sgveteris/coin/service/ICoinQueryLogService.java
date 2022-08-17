package tr.com.sgveteris.coin.service;

import tr.com.sgveteris.coin.domain.CoinQueryLog;

public interface ICoinQueryLogService {
    CoinQueryLog saveCoinQueryLogRecord(CoinQueryLog coinQueryLog);
}
