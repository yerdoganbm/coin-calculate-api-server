package tr.com.sgveteris.coin.enumeration;

public enum EnumCoinConvertSymbolType {

    BTC_USD("BTC-USD",1),
    ETH_USD("ETH-USD",2),
    BTC_EUR("BTC-EUR",3),
    ETH_EUR("ETH-EUR",4);

    private String name;
    private Integer code;


    EnumCoinConvertSymbolType(String name, Integer code) {
        this.code = code;
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }

    public static EnumCoinConvertSymbolType getValueByName(String name) {
        for (EnumCoinConvertSymbolType e : EnumCoinConvertSymbolType.values()) {
            if (e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    public static EnumCoinConvertSymbolType getByCode(Integer code) {
        for (EnumCoinConvertSymbolType e : EnumCoinConvertSymbolType.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }


}
