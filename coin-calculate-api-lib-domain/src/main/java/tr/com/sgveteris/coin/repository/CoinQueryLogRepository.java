package tr.com.sgveteris.coin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.sgveteris.coin.domain.CoinQueryLog;

@Repository
public interface CoinQueryLogRepository extends JpaRepository<CoinQueryLog, String> {

}
