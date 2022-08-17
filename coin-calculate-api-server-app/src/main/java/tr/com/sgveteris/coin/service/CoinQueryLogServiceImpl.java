package tr.com.sgveteris.coin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tr.com.sgveteris.coin.domain.CoinQueryLog;
import tr.com.sgveteris.coin.repository.CoinQueryLogRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoinQueryLogServiceImpl implements ICoinQueryLogService{

    private final CoinQueryLogRepository coinQueryLogRepository;

    @Override
    public CoinQueryLog saveCoinQueryLogRecord(CoinQueryLog coinQueryLog){
        return coinQueryLogRepository.save(coinQueryLog);
    }
}
