package tr.com.sgveteris.coin.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.sgveteris.coin.domain.BlockChainServiceResult;
import tr.com.sgveteris.coin.enumeration.EnumCoinConvertSymbolType;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinQueryLogEvent implements Serializable {
    private BlockChainServiceResult serviceResult;
    private EnumCoinConvertSymbolType symbolType;

}

