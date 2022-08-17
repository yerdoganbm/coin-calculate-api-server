package tr.com.sgveteris.coin.exception;



public class CoinCalculateExceptionContext {
    private static ThreadLocal<CoinCalculateError> coinCalculateError = new ThreadLocal<>();

    private CoinCalculateExceptionContext() {
    }

    public static CoinCalculateError getCoinCalculateError() {
        return coinCalculateError.get();
    }

    public static void setCoinCalculateError(CoinCalculateError error) {
        coinCalculateError.set(error);
    }

    public static void clear() {
        coinCalculateError.remove();
    }
}
