package tr.com.sgveteris.coin.enumeration;



public enum EnumResponseCode {

    RC_INVALID_REQUEST("BS-SYNC-1001", "Giris parametreleri eksik veya hatali. Lutfen giris degerlerini kontrol ediniz!"),
    RC_SUCCESS("0000",  "Basarili Islem"),
    RC_ERROR("BS-SYNC-9999", "Bilinmeyen islem hatasi.")
            ;






    private String rc;
    private String rcDesc;


    EnumResponseCode(String rc, String rcDesc) {
        this.rc = rc;
        this.rcDesc = rcDesc;
    }

    public static EnumResponseCode getByName(String name) {

        for (EnumResponseCode e : EnumResponseCode.values()) {
            if (e.name().equals(name)) {
                return e;
            }
        }
        return null;
    }


    public String getRc() {
        return rc;
    }

    public String getRcDesc() {
        return rcDesc;
    }
}
