package tr.com.sgveteris.coin.exception;


import tr.com.sgveteris.coin.enumeration.EnumResponseCode;

public class CoinCalculateException extends RuntimeException {
    public final EnumResponseCode rc;

    public CoinCalculateException(String message) {
        super(message);
        this.rc = EnumResponseCode.RC_ERROR;
    }

    public CoinCalculateException(String message, Throwable cause){
        super(message,cause);
        this.rc = EnumResponseCode.RC_ERROR;
    }
    public CoinCalculateException(String message, EnumResponseCode responseCode){
        super(message);
        this.rc = responseCode;
    }

    public EnumResponseCode getRc() {
        return rc;
    }
}
