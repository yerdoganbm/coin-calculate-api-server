mvn clean compile install

#Dockerfile : 
docker build --build-arg JAR_FILE=coin-calculate-api-server-app/target/*.jar -t springbootapp .

docker run -p 8088:8080 springbootapp  

#Docker-Compose:

docker-compose -f docker-compose.yml up


package tr.gov.tcmb.ogmdfif.service.impl;

import com.itextpdf.text.PageSize;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tcmb.log.logger.PlatformLogger;
import tr.gov.tcmb.log.logger.PlatformLoggerFactory;
import tr.gov.tcmb.mgmosyp.modul.mesaj.musteriHesabaOdeme.MusteriHesabaOdeme;
import tr.gov.tcmb.ogmdfif.constant.KararTipiEnum;
import tr.gov.tcmb.ogmdfif.constant.MektupTipEnum;
import tr.gov.tcmb.ogmdfif.constant.ServisTip;
import tr.gov.tcmb.ogmdfif.constant.SubeKoduEnum;
import tr.gov.tcmb.ogmdfif.exception.GecersizVeriException;
import tr.gov.tcmb.ogmdfif.exception.GenelException;
import tr.gov.tcmb.ogmdfif.exception.ValidationException;
import tr.gov.tcmb.ogmdfif.model.entity.BorcBilgiArsiv;
import tr.gov.tcmb.ogmdfif.model.dto.servisDto.MuhasebeBilgiDTO;
import tr.gov.tcmb.ogmdfif.model.entity.*;
import tr.gov.tcmb.ogmdfif.orkestrator.ServisTaslak;
import tr.gov.tcmb.ogmdfif.repository.EftBilgisiYonetimArsivRepository;
import tr.gov.tcmb.ogmdfif.repository.EftBilgisiYonetimRepository;
import tr.gov.tcmb.ogmdfif.service.*;
import tr.gov.tcmb.ogmdfif.util.Constants;
import tr.gov.tcmb.ogmdfif.util.ExportedFile;
import tr.gov.tcmb.ogmdfif.util.ModulUtil;
import tr.gov.tcmb.ogmdfif.util.StringUtil;
import tr.gov.tcmb.ogmdfif.ws.client.EFTClientService;
import tr.gov.tcmb.ogmdfif.ws.client.MuhasebeClientService;
import tr.gov.tcmb.ogmdfif.ws.client.impl.EpostaGonderimService;
import tr.gov.tcmb.ogmdfif.ws.request.Attachment;
import tr.gov.tcmb.ogmdfif.ws.request.EPostaDTO;
import tr.gov.tcmb.submuhm.pikur.PikurDocument;
import tr.gov.tcmb.submuhm.pikur.model.veri.DocGrupVeri;
import tr.gov.tcmb.submuhm.pikur.model.veri.DocVeri;
import tr.gov.tcmb.submuhm.pikur.model.veri.YeniSayfaVeri;
import tr.gov.tcmb.submuhm.pikur.service.PikurIslemService;

import javax.print.attribute.standard.OrientationRequested;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = {Exception.class})
public class MektupServiceImpl implements MektupService {

    @Autowired
    private KararIslemleriService kararIslemleriService;

    @Autowired
    private KullaniciBilgileriService kullaniciBilgileriService;

    @Autowired
    private ProvizyonIslemleriService provizyonIslemleriService;

    @Autowired
    private HakedisIslemleriService hakedisIslemleriService;

    @Autowired
    private BankaSubeService bankaSubeService;

    @Autowired
    private BorcBilgiService borcBilgiService;

    @Autowired
    private EFTClientService eftClientService;

    @Autowired
    private PikurIslemService pikurIslemService;

    @Autowired
    private EpostaGonderimService epostaGonderimService;

    @Autowired
    private MuhasebeClientService muhasebeClientService;

    private static final PlatformLogger logger = PlatformLoggerFactory.getLogger(MektupServiceImpl.class);

    private static final String HAKEDIS_DAVET_MEKTUP_BODY = "%s sayılı karar kapsamındaki hak ediş belgesine ilişkin bilgilendirme mektubu ekte yer almaktadır."
            + "Hak ediş belgesinin teslim alınması ve mahsup işlemlerinin yapılabilmesi için Türkiye Cumhuriyet Merkez Bankası %s Şubesine başvurulması gerekmektedir.";


    private static final String HAKEDIS_DEVIR_MEKTUP_BODY = "%s sayılı karar kapsamındaki hak ediş devrine ilişkin bilgilendirme mektubu ekte yer almaktadır."
            + "Hak ediş belgesinin teslim alınması ve mahsup işlemlerinin yapılabilmesi için Türkiye Cumhuriyet Merkez Bankası %s Şubesine başvurulması gerekmektedir.";

    private static final String STR_ODEME_MEKTUP = "Ödeme Mektupları";
    private static final String STR_DAVET_MEKTUP = "Davet Mektupları";
    private static final String STR_HAKEDIS_DEVIR_MEKTUP = "Hakedis Devir Mektupları";


    String milatTarihiStr = "20/01/2025";
    private static final SimpleDateFormat SDF_TARIH_DD_MM_YYYY = new SimpleDateFormat("dd/MM/yyyy");

    private static final String ihracatciZimmetMektubuPikurXMLPath = "print/IHRACATCIZIMMETMEKTUP.xml";
    private static final String ihracatciDevirMektubuPikurXMLPath = "print/IHRACATCIDEVIRMEKTUP.xml";
    private static final String ihracatciHakedisBelgesiPikurXMLPath1 = "print/HAKEDISBELGESI1.xml";
    private static final String genelOdemeListePikurXMLPath = "print/GENELODEMELST.xml";
    private static final String hakedisZimmetListeXMLPath = "print/HAKEDISZIMMETLST.xml";
    private static final String ihracatciDavetMektup = "print/IHRACATCIDAVETMEKTUP.xml";
    private static final String borcsuzIhracatciDavetMektup = "print/BORCSUZIHRACATCIDAVETMEKTUP.xml";
    private static final String ihracatciNakitOdemeMektubuPikurXMLPath = "print/IHRACATCINAKITODEMEMEKTUP.xml";


    @Autowired
    private EftBilgisiYonetimRepository eftBilgisiYonetimRepository;
    @Autowired
    private EftBilgisiYonetimArsivRepository eftBilgisiYonetimArsivRepository;

    private void nakitKontrolYap(String kararNo) throws ValidationException {
        Karar karar = kararIslemleriService.getKararByKararNoAndSube(kararNo, SubeKoduEnum.IDARE_MERKEZI.getSubeId());

        if (karar == null) {
            throw new ValidationException("Aradığınız karar bulunamamıştır. Karar No: " + kararNo);
        } else {
            if (!karar.isNakitKarar()) {
                throw new ValidationException("Ödeme mektupları sadece nakit ödemeler için üretilmektedir.");
            }
        }
    }

    private void tarimMahsupKontrolYap(String kararNo) throws ValidationException {
        Karar karar = kararIslemleriService.getKararByKararNoAndSube(kararNo, SubeKoduEnum.IDARE_MERKEZI.getSubeId());
        if (karar == null) {
            throw new ValidationException("Aradığınız karar bulunamamıştır. Karar No: " + kararNo);
        } else {
            if (!karar.isMahsupKarar()) {
                throw new ValidationException("Bu belge yalnızca tarım&mahsup ödemeler için üretilmektedir.");
            }
        }

    }

    @Override
    public void sendIhracatciMektupMailRouter(KararTipiEnum belgeTip, Integer belgeNo, Integer belgeYil, String kararNo, LocalDate odemeTarihiDate, String vkn, String tckn, MektupTipEnum mektupTip) throws Exception {
        this.parametreKontrolleriYap(belgeTip, belgeNo, belgeYil, kararNo, odemeTarihiDate, mektupTip);
        switch (mektupTip) {
            case ODEME_MEKTUPLARI:
                if (StringUtils.isNotEmpty(kararNo)) {
                    this.nakitKontrolYap(kararNo);
                }
                Date odemeTarihi = Date.from(odemeTarihiDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date milatTarihi = MektupServiceImpl.SDF_TARIH_DD_MM_YYYY.parse(milatTarihiStr);
                if (odemeTarihi.after(milatTarihi)) {
                    this.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(belgeTip, belgeYil, belgeNo, kararNo, odemeTarihiDate, vkn, tckn);
                } else {
                    this.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(belgeTip, belgeYil, belgeNo, kararNo, odemeTarihiDate, vkn, tckn);
                }
                break;
            case IHRACATCI_DAVET_MEKTUPLARI:
                if (StringUtils.isNotEmpty(kararNo)) {
                    tarimMahsupKontrolYap(kararNo);
                }

                this.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(belgeTip, belgeYil, belgeNo, kararNo, odemeTarihiDate, vkn, tckn);
                break;
            case HAKEDIS_DEVIR_MEKTUPLARI:
                this.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(odemeTarihiDate);
                break;
            default:
                throw new GecersizVeriException("Mektup tipi boş olamaz.");
        }
    }

    private String handleExportFileName(LocalDate odemeTarihiDate, MektupTipEnum mektupTip) {
        Date odemeTarihi = Date.from(odemeTarihiDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat sdfTarih = new SimpleDateFormat("dd/MM/yyyy");
        String odemeTarihStr = sdfTarih.format(odemeTarihi);
        return odemeTarihStr + "_" + mektupTip.getAdi();
    }


    private void parametreKontrolleriYap(KararTipiEnum belgeTip, Integer belgeNo, Integer yil, String kararNo, LocalDate odemeTarihiDate, MektupTipEnum mektupTip) throws ValidationException, GecersizVeriException {
        if (mektupTip == null) {
            throw new GecersizVeriException("Mektup tipi boş olamaz.");
        }

        if (odemeTarihiDate == null) {
            throw new GecersizVeriException("Ödeme tarihi boş olamaz.");
        }

        if (!((belgeTip == null && yil == null && belgeNo == null || ((belgeTip != null && yil != null && belgeNo != null))))) {
            throw new GecersizVeriException("Paket tipi, belge no ve yıl birlikte kullanılmalıdır.");
        }
    }


    @Override
    public ExportedFile mektupYazdir(KararTipiEnum belgeTip, Integer belgeNo, Integer yil, String kararNo, LocalDate odemeTarihiDate, String vkn, String tckn, MektupTipEnum mektupTip) throws Exception {
        logger.info("mektupYazdir", "Mektup yazdırma işlemi başladı.");

        parametreKontrolleriYap(belgeTip, belgeNo, yil, kararNo, odemeTarihiDate, mektupTip);

        Date odemeTarihi = Date.from(odemeTarihiDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        SimpleDateFormat sdfTarih = new SimpleDateFormat("dd/MM/yyyy");
        String odemeTarihStr = sdfTarih.format(odemeTarihi);
        String dosyaAdi = odemeTarihStr + "_" + mektupTip.getAdi();

        String kullaniciSubeId = kullaniciBilgileriService.getKullaniciSubeId();

        DocVeri mektupTaslaklari;
        PikurDocument pd;

        if (mektupTip.equals(MektupTipEnum.IHRACATCI_DAVET_MEKTUPLARI)) {
            if (kararNo != null) {
                tarimMahsupKontrolYap(kararNo);
            }
            pd = pikurIslemService.xmlYukle(ihracatciDavetMektup);
            mektupTaslaklari = ihracatciDavetMektuplariniCikar(belgeTip, yil, belgeNo, kararNo, odemeTarihiDate, vkn, tckn, kullaniciSubeId);
        } else if (mektupTip.equals(MektupTipEnum.ODEME_MEKTUPLARI)) {
            if (StringUtils.isNotBlank(kararNo)) {
                nakitKontrolYap(kararNo);
            }
            pd = pikurIslemService.xmlYukle(ihracatciNakitOdemeMektubuPikurXMLPath);
            Date milatTarihi = sdfTarih.parse(milatTarihiStr);
            if (odemeTarihi.after(milatTarihi)) {
                mektupTaslaklari = ihracatciOdemeMektuplariniCikar(belgeTip, yil, belgeNo, kararNo, odemeTarihiDate, vkn, tckn);
            } else {
                mektupTaslaklari = ihracatciOdemeMektuplariniCikarEski(belgeTip, yil, belgeNo, kararNo, odemeTarihiDate, vkn, tckn);
            }
        } else if (mektupTip.equals(MektupTipEnum.HAKEDIS_DEVIR_MEKTUPLARI)) {
            if (odemeTarihiDate == null) {
                MuhasebeBilgiDTO muhasebeBilgiDTO = muhasebeClientService.loadMuhasebeBilgi(SubeKoduEnum.ANKARA.getKod());
                odemeTarihiDate = muhasebeBilgiDTO.getMuhasebeTarih().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            pd = pikurIslemService.xmlYukle(ihracatciDevirMektubuPikurXMLPath);
            mektupTaslaklari = getHakedisDevirMektuplariniTopluAl(odemeTarihiDate);
        } else {
            throw new GecersizVeriException("Mektup tipi hatalıdır.");
        }

        ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, mektupTaslaklari, PageSize.A4, OrientationRequested.PORTRAIT);
        ExportedFile file = outputAsPDF(baos, dosyaAdi);
        return file;
    }

    private DocVeri getHakedisDevirMektuplariniTopluAl(LocalDate odemeTarihiDate) throws Exception {
        List<Ihracatci> ihracatcilar = hakedisIslemleriService.getDeviriOlanIhracatcilar(odemeTarihiDate);
        if (ihracatcilar == null || ihracatcilar.isEmpty())
            throw new GecersizVeriException("İlgili tarihte deviri olan ihracatçı bulunamadı");

        DocVeri tumMektuplar = new DocVeri();
        List<Hakedis> devirdenOlusanHakedisler = null;
        List<DocGrupVeri> veriler = null;
        ServisTaslak servis;
        String subeKodu = kullaniciBilgileriService.getKullaniciSubeKodu();
        if (SubeKoduEnum.IDARE_MERKEZI.getKod().equals(subeKodu)) {
            throw new GenelException("İdare merkezi kullanıcısı bu işleme devam edemez.");
        }
        if (SubeKoduEnum.ANKARA.equals(SubeKoduEnum.getBykod(String.valueOf(subeKodu)))) {
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), Constants.FON_ODEMELERI);
        } else if (SubeKoduEnum.getBykod(String.valueOf(subeKodu)).equals(SubeKoduEnum.IZMIR))
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), ServisTip.KAMBIYO);
        else if (SubeKoduEnum.getBykod(String.valueOf(subeKodu)).equals(SubeKoduEnum.ISTANBUL))
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), ServisTip.FON_ODEMELERI);
        else {
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), ServisTip.BANKACILIK);
        }
        for (Ihracatci ihracatciTmp : ihracatcilar) {
            devirdenOlusanHakedisler = hakedisIslemleriService.getDevirdenOlusmusHakedisler(ihracatciTmp, odemeTarihiDate);
            veriler = hakedisDevirMektubuCikar(servis, devirdenOlusanHakedisler);
            tumMektuplar.addGrupVeriAll(veriler);
        }
        return tumMektuplar;
    }

    @Override
    public ExportedFile hakedisDevirMektubuCikar(DocVeri docVeri, String dosyaAdi) throws Exception {
        logger.info("hakedisDevirMektubuCikar", "Hakediş devir mektubu çıkarma işlemi başladı.");
        PikurDocument pd = pikurIslemService.xmlYukle(ihracatciDevirMektubuPikurXMLPath);
        ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.PORTRAIT);
        ExportedFile file = outputAsPDF(baos, dosyaAdi);
        return file;
    }

    private List<DocGrupVeri> hakedisDevirMektubuCikar(ServisTaslak servis, List<Hakedis> hakedisler) throws GecersizVeriException {

        if (hakedisler == null || hakedisler.isEmpty())
            throw new GecersizVeriException("Mektubu alınacak hakediş bulunamadı");

        List<DocGrupVeri> veriler = new ArrayList<DocGrupVeri>();

        DocGrupVeri detayGrup = new DocGrupVeri();
        detayGrup.setGrupAd("DETAY");

        Hakedis hakedis = hakedisler.get(0);
        if (SubeKoduEnum.ANKARA.getSubeId().equals(hakedis.getKarar().getSubeId())) {
            detayGrup.addAlanVeri("TAAHHUTSATIR", " ");
        } else {
            detayGrup.addAlanVeri("TAAHHUTSATIR", "İADELİ-TAAHHÜTLÜ");
        }

        detayGrup.addAlanVeri("KARARNO", hakedis.getKarar().getKararNo());
        Date duzenlemeTarihi = Date.from(hakedis.getDuzenlemeTarihi().toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Calendar cal = Calendar.getInstance();
        cal.setTime(duzenlemeTarihi);
        detayGrup.addAlanVeri("TARIH", ModulUtil.fromCalendarToDateString(cal));

        detayGrup.addAlanVeri("IHRACATCIADI", hakedis.getIhracatci().getAd());

        String adres1 = hakedis.getIhracatci().getAdres().trim();
        String adres2 = "";
        String adres3 = "";
        if (adres1.length() > 50) {
            if (adres1.length() > 100) {
                adres3 = adres1.substring(100);
                adres2 = adres1.substring(50, 100);
            } else {
                adres2 = adres1.substring(50);
                adres1 = adres1.substring(0, 50);
            }
        }
        detayGrup.addAlanVeri("IHRACATCIADRES1", adres1);
        detayGrup.addAlanVeri("IHRACATCIADRES2", adres2);
        detayGrup.addAlanVeri("IHRACATCIADRES3", adres3);

        if (servis.isOgmServisiMi()) {
            detayGrup.addAlanVeri("TCMBSUBEADI", "OPERASYON GENEL MÜDÜRLÜĞÜ");
        } else {
            SubeKoduEnum subeKoduEnum = SubeKoduEnum.getById(hakedis.getIhracatci().getSubeId());
            detayGrup.addAlanVeri("TCMBSUBEADI", subeKoduEnum.getAdi());
        }

        List<DocGrupVeri> devirler = new ArrayList<DocGrupVeri>();

        for (Hakedis devirHakedis : hakedisler) {
            DocGrupVeri detay = new DocGrupVeri();
            detay.setGrupAd("DEVIRBILGILERI");
            detay.addAlanVeri("KARAR", devirHakedis.getKarar().getKararNo() + "-" + hakedis.getKarar().getAd());
            Hakedis devredenHakedis = hakedisIslemleriService.getHakedisById(devirHakedis.getDevredenHakedisId());
            detay.addAlanVeri("DEVREDENIHRACATCI", devredenHakedis.getIhracatci().getAd());
            detay.addAlanVeri("DEVREDILENTUTAR", devirHakedis.getTutarTl());
            devirler.add(detay);
        }
        // once detay eklenecek
        veriler.add(detayGrup);
        veriler.addAll(devirler);
        veriler.add(new YeniSayfaVeri());
        return veriler;
    }

    @Override
    public ExportedFile hakedisTeslimMektubuCikar(DocVeri docVeri, String dosyaAdi) throws Exception {
        logger.info("hakedisTeslimMektubuCikar", "Hakediş teslim mektubu çıkarma işlemi başladı.");
        PikurDocument pd = pikurIslemService.xmlYukle(ihracatciHakedisBelgesiPikurXMLPath1);
        ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.LANDSCAPE);
        ExportedFile file = outputAsPDF(baos, dosyaAdi);
        return file;
    }

    @Override
    public ExportedFile hakedisZimmetListesiCiktiHazirla(DocVeri docVeri, String dosyaAdi) throws Exception {
        logger.info("hakedisZimmetListesiCiktiHazirla", "Hakediş zimmet listesi çıkarma işlemi başladı.");
        PikurDocument pd = pikurIslemService.xmlYukle(hakedisZimmetListeXMLPath);
        ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.LANDSCAPE);
        ExportedFile file = outputAsPDF(baos, dosyaAdi);
        return file;
    }

    @Override
    public ExportedFile genelOdemeListesiCiktiHazirla(DocVeri docVeri, String dosyaAdi) throws Exception {
        logger.info("genelOdemeListesiCiktiHazirla", "Genel ödeme listesi çıkarma işlemi başladı.");
        PikurDocument pd = pikurIslemService.xmlYukle(genelOdemeListePikurXMLPath);
        ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A3, OrientationRequested.PORTRAIT);
        ExportedFile file = outputAsPDF(baos, dosyaAdi);
        return file;
    }

    /*private DocVeri ihracatciDavetMektuplariniCikarIfos(String tip, String yil, String belgeNo, Karar karar, Date odemeTarihi, String vkn, String tckn, String subeId) throws Exception {
        logger.info("ihracatciDavetMektuplariniCikarIfos", "İhracatçı davet mektuplarını çıkarma işlemi başladı.");
        List<Provizyon> provizyonList = null;
        if(StringUtil.isNullOrTrimEmpty(vkn) && StringUtil.isNullOrTrimEmpty(tckn)){
            provizyonList = provizyonIslemleriService.getProvizyonList(karar, tip, yil, belgeNo, odemeTarihi);
        }else if(!StringUtil.isNullOrTrimEmpty(vkn)){
            provizyonList = provizyonIslemleriService.getProvizyonList(karar, tip, yil, belgeNo, odemeTarihi, vkn, subeId);
        } else {
            provizyonList = provizyonIslemleriService.getProvizyonList(karar, tip, yil, belgeNo, odemeTarihi, tckn, subeId);
        }

        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı.");
        }

        DocVeri docVeri = new DocVeri();
        List<DocGrupVeri> veriler = new ArrayList<>();

        for(Provizyon provizyon : provizyonList) {
            String eftBankaSube = "-";
            String sorguNo = "-";
            BigDecimal eftTutari = null;
            SimpleDateFormat sdfTarih = new SimpleDateFormat("dd/MM/yyyy");

            ProvizyonOdeme provizyonOdeme = provizyonTalepOdemeService.getProvizyonOdemeByOdemeTarih(provizyon, provizyon.getOdemeTarih());
            if(provizyonOdeme != null) {
                MusteriHesabaOdeme eftMesaj = (MusteriHesabaOdeme) eftClientService.getKasMesajByHareketId(provizyonOdeme.getMuhasebeHareketId().toString());

                if (eftMesaj == null) {
                    continue;
                }
                String bad = null;
                if(provizyon.getEftBankaKod() != null){
                    bad = bankaSubeService.getBankaForBankaKodu(provizyon.getEftBankaKod()).getAd();
                }else{
                    bad = "-";
                }

                String sad = null;
                if(provizyon.getEftBankaKod() != null && provizyon.getEftSubeKod() != null){
                    sad = bankaSubeService.getBankaSube(provizyon.getEftBankaKod(), provizyon.getEftSubeKod()).getAd();
                }else{
                    sad = "-";
                }

                bad = bad.length() > 35 ? bad.substring(0, 35) : bad;

                eftBankaSube = provizyon.getEftBankaKod() + "-" + bad + "/"
                        + provizyon.getEftSubeKod() + "-"
                        + sad.trim();
                eftBankaSube = (eftBankaSube.length() > 100 ? eftBankaSube.substring(0, 100) : eftBankaSube)
                        + " Şubesi'ne";
                eftTutari = new BigDecimal(StringUtil.formatVirgulToNokta(eftMesaj.getTtr()));

                String subeKod = kullaniciBilgileriService.getSubeKodu(karar.getSubeId());
                MuhasebeBilgiDTO muhasebeBilgiDTO = muhasebeClientService.loadMuhasebeBilgi(subeKod);
                Date muhasebeTarih = muhasebeBilgiDTO.getMuhasebeTarih();
                int muhasebeTarihYear = muhasebeTarih.getYear() + 1900;
                //TODO: servis değiştirilebilir
                ServisTaslak servis = muhasebeClientService.findServis(Integer.parseInt(subeKod), Constants.FON_HESAPLARI_VE_BANKACILIK_OGM);
                long muhaberatNo = muhasebeClientService.getServisTakipNo(subeKod, servis.getServisKod(), muhasebeTarihYear, ServisTakipNoTipEnum.MUHABERAT_NO.getAdi());
                DocGrupVeri detayGrup = new DocGrupVeri();

                Ihracatci ihracatci = provizyon.getIhracatci();
                detayGrup.addAlanVeri("IHRACATCIADI", ihracatci.getAd());
                String adres1 = ihracatci.getAdres().trim();
                String adres2 = StringUtils.EMPTY;
                String adres3 = StringUtils.EMPTY;
                if (adres1.length() > 50) {
                    adres2 = adres1.substring(50);
                    adres1 = adres1.substring(0, 50);
                }
                detayGrup.addAlanVeri("IHRACATCIADRES1", adres1);
                detayGrup.addAlanVeri("IHRACATCIADRES2", adres2);
                detayGrup.addAlanVeri("IHRACATCIADRES3", adres3);
                detayGrup.addAlanVeri("KARARNO", provizyon.getKarar().getKararNo());
                detayGrup.addAlanVeri("TARIH", sdfTarih.format(new Date()));
                detayGrup.setGrupAd("DETAY");
                detayGrup.addAlanVeri("EFTBANKASUBE", eftBankaSube);
                detayGrup.addAlanVeri("ODEMETARIH", sdfTarih.format(odemeTarihi));
                detayGrup.addAlanVeri("EFTSORGUNO", eftMesaj.getSN());
                detayGrup.addAlanVeri("MUHABERATNO", muhaberatNo);
                detayGrup.addAlanVeri("FUAR", provizyon.getFuar());
                detayGrup.addAlanVeri("TUTAR", eftTutari);

                String subeKodu = kullaniciBilgileriService.getSubeKodu(karar.getSubeId());
                detayGrup.addAlanVeri("TCMBSUBEADI", SubeKoduEnum.getBykod(subeKodu).getAdi());

                veriler.add(detayGrup);
                veriler.add(new YeniSayfaVeri());
            }
        }
        docVeri.addGrupVeriAll(veriler);
        return docVeri;
    }

*/

    private List<DocGrupVeri> getOdemeMektupBorcBilgileri(Provizyon provizyon, Boolean sadeceBorcYazdir) throws Exception {
        List<DocGrupVeri> borclar = new ArrayList<DocGrupVeri>();
        // borcu olmayan ihracatcıya mektup gitmeyecek
        List<EftBilgiYonetim> eftBilgiYonetimList = eftBilgisiYonetimRepository.getEftBilgiYonetimsByProvizyonId(BigDecimal.valueOf(provizyon.getId()));
        Map<BigDecimal, EftBilgiYonetim> eftBilgiYonetimMap = new HashMap<>();
        for (EftBilgiYonetim eftBilgiYonetim : eftBilgiYonetimList) {
            eftBilgiYonetimMap.put(eftBilgiYonetim.getBorcId(), eftBilgiYonetim);
        }
        List<BigDecimal> borcIdList = eftBilgiYonetimList.stream().map(EftBilgiYonetim::getBorcId).sorted().collect(Collectors.toList());
        List<BorcBilgi> borcBilgiList = provizyon.getBorcBilgiList();
        for (BigDecimal borcId : borcIdList) {
            if (sadeceBorcYazdir && borcBilgiList.stream().noneMatch(borcBilgi -> new BigDecimal(borcBilgi.getId()).equals(borcId))) {
                continue;
            }
            EftBilgiYonetim eftBilgiYonetim = eftBilgiYonetimMap.get(borcId);
            if (eftBilgiYonetim.getKasTarih() == null) {
                continue;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(eftBilgiYonetim.getKasTarih(), formatter);
            MusteriHesabaOdeme eftMesaj = (MusteriHesabaOdeme) eftClientService.getGunlukKasMesajBySorguNoAndOdemeTarihi(eftBilgiYonetim.getKasSorguNo(), localDate);

            DocGrupVeri detayBorclar = new DocGrupVeri();
            detayBorclar.setGrupAd("BORCBILGILERI");
            detayBorclar.addAlanVeri("BORCALICISI", eftMesaj.getAlAd());
            detayBorclar.addAlanVeri("BORCTUTARI", new BigDecimal(StringUtil.formatVirgulToNokta(eftMesaj.getTtr())));
            String eftBankaKoduAdi = eftMesaj.getAlKK() + "-"
                    + bankaSubeService.getBankaForBankaKodu(eftMesaj.getAlKK()).getAd();

            StringBuilder sb = new StringBuilder(eftBankaKoduAdi.trim());
            if (sb.length() > 30) {
                sb.setLength(30);
            }
            detayBorclar.addAlanVeri("EFTBANKAKODUADI", sb.toString());
            detayBorclar.addAlanVeri("EFTHESAP", eftMesaj.getAlHesN());
            detayBorclar.addAlanVeri("EFTTARIHI", eftMesaj.getTrh());
            detayBorclar.addAlanVeri("EFTSORGUNO", eftMesaj.getSN());
            detayBorclar.addAlanVeri("EFTACIKLAMA", eftMesaj.getAcklm());


            borclar.add(detayBorclar);
        }
        return borclar;
    }

    private List<DocGrupVeri> getOdemeMektupBorcBilgileri(ProvizyonArsiv provizyon, Boolean sadeceBorcYazdir) throws Exception {
        List<DocGrupVeri> borclar = new ArrayList<DocGrupVeri>();
        // borcu olmayan ihracatcıya mektup gitmeyecek
        List<EftBilgiYonetimArsiv> eftBilgiYonetimList = eftBilgisiYonetimArsivRepository.getEftBilgiYonetimArsivsByProvizyonId(BigDecimal.valueOf(provizyon.getId()));
        Map<BigDecimal, EftBilgiYonetimArsiv> eftBilgiYonetimMap = new HashMap<>();
        for (EftBilgiYonetimArsiv eftBilgiYonetim : eftBilgiYonetimList) {
            eftBilgiYonetimMap.put(eftBilgiYonetim.getBorcId(), eftBilgiYonetim);
        }
        List<BigDecimal> borcIdList = eftBilgiYonetimList.stream().map(EftBilgiYonetimArsiv::getBorcId).sorted().collect(Collectors.toList());
        List<BorcBilgiArsiv> borcBilgiList = borcBilgiService.getBorcBilgiArsivList(provizyon);
        for (BigDecimal borcId : borcIdList) {
            if (sadeceBorcYazdir && borcBilgiList.stream().noneMatch(borcBilgi -> new BigDecimal(borcBilgi.getId()).equals(borcId))) {
                continue;
            }
            EftBilgiYonetimArsiv eftBilgiYonetim = eftBilgiYonetimMap.get(borcId);
            if (eftBilgiYonetim.getKasTarih() == null) {
                continue;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(eftBilgiYonetim.getKasTarih(), formatter);
            MusteriHesabaOdeme eftMesaj = (MusteriHesabaOdeme) eftClientService.getGunlukKasMesajBySorguNoAndOdemeTarihi(eftBilgiYonetim.getKasSorguNo(), localDate);

            DocGrupVeri detayBorclar = new DocGrupVeri();
            detayBorclar.setGrupAd("BORCBILGILERI");
            detayBorclar.addAlanVeri("BORCALICISI", eftMesaj.getAlAd());
            detayBorclar.addAlanVeri("BORCTUTARI", new BigDecimal(StringUtil.formatVirgulToNokta(eftMesaj.getTtr())));
            String eftBankaKoduAdi = eftMesaj.getAlKK() + "-"
                    + bankaSubeService.getBankaForBankaKodu(eftMesaj.getAlKK()).getAd();
            StringBuilder sb = new StringBuilder(eftBankaKoduAdi.trim());
            if (sb.length() > 30) {
                sb.setLength(30);
            }
            detayBorclar.addAlanVeri("EFTBANKAKODUADI", sb.toString());
            detayBorclar.addAlanVeri("EFTHESAP", eftMesaj.getAlHesN());
            detayBorclar.addAlanVeri("EFTTARIHI", eftMesaj.getTrh());
            detayBorclar.addAlanVeri("EFTSORGUNO", eftMesaj.getSN());
            detayBorclar.addAlanVeri("EFTACIKLAMA", eftMesaj.getAcklm());

            borclar.add(detayBorclar);
        }
        return borclar;
    }

    private BigDecimal getProvizyonArsivToplamTutar(ProvizyonArsiv provizyon, boolean sadeceBorcYazdir) {
        BigDecimal toplamTutar = BigDecimal.ZERO;

        List<EftBilgiYonetimArsiv> eftBilgiYonetimList = eftBilgisiYonetimArsivRepository.getEftBilgiYonetimArsivsByProvizyonId(BigDecimal.valueOf(provizyon.getId()));
        Map<BigDecimal, EftBilgiYonetimArsiv> eftBilgiYonetimMap = new HashMap<>();
        for (EftBilgiYonetimArsiv eftBilgiYonetim : eftBilgiYonetimList) {
            eftBilgiYonetimMap.put(eftBilgiYonetim.getBorcId(), eftBilgiYonetim);
        }
        List<BigDecimal> borcIdList = eftBilgiYonetimList.stream().map(EftBilgiYonetimArsiv::getBorcId).sorted().collect(Collectors.toList());
        List<BorcBilgiArsiv> borcBilgiList = borcBilgiService.getBorcBilgiArsivList(provizyon);
        for (BigDecimal borcId : borcIdList) {
            if (sadeceBorcYazdir && borcBilgiList.stream().noneMatch(borcBilgi -> new BigDecimal(borcBilgi.getId()).equals(borcId))) {
                continue;
            }
            EftBilgiYonetimArsiv eftBilgiYonetim = eftBilgiYonetimMap.get(borcId);
            if (eftBilgiYonetim.getKasTarih() == null) {
                continue;
            }
            toplamTutar = toplamTutar.add(eftBilgiYonetim.getTutar());
        }
        return toplamTutar;
    }

    private List<DocGrupVeri> getOdemeMektupDetayByProvizyon(Provizyon provizyon) throws Exception {
        SimpleDateFormat sdfTarih = new SimpleDateFormat("dd/MM/yyyy");
        List<DocGrupVeri> veriler = new ArrayList<>();
        List<DocGrupVeri> borclar = getOdemeMektupBorcBilgileri(provizyon, false);
        if (CollectionUtils.isEmpty(borclar)) {
            return new ArrayList<>();
        }
        DocGrupVeri detayGrup = new DocGrupVeri();
        detayGrup.setGrupAd("DETAY");
        Ihracatci ihracatci = provizyon.getIhracatci();
        detayGrup.addAlanVeri("IHRACATCIADI", ihracatci.getAd());
        String adres1 = ihracatci.getAdres().trim();
        String adres2 = StringUtils.EMPTY;
        String adres3 = StringUtils.EMPTY;
        if (adres1.length() > 50) {
            if (adres1.length() > 100) {
                adres3 = adres1.substring(100);
                adres2 = adres1.substring(50, 100);
            } else {
                adres2 = adres1.substring(50);
                adres1 = adres1.substring(0, 50);
            }
        }

        detayGrup.addAlanVeri("IHRACATCIADRES1", adres1);
        detayGrup.addAlanVeri("IHRACATCIADRES2", adres2);
        detayGrup.addAlanVeri("IHRACATCIADRES3", adres3);
        detayGrup.addAlanVeri("TARIH", sdfTarih.format(new Date()));
        detayGrup.addAlanVeri("KARARNO", provizyon.getKarar().getKararNo());
        String kararAraMetin = "sayılı %s ";
        detayGrup.addAlanVeri("KARARADI", String.format(kararAraMetin, provizyon.getKarar().getAd()));
        detayGrup.addAlanVeri("PROVIZYONTUTAR", provizyon.getTutar());
        detayGrup.addAlanVeri("ODEMETARIH", sdfTarih.format(provizyon.getOdemeTarih()));

        SubeKoduEnum subeKoduEnum = SubeKoduEnum.getById(provizyon.getKarar().getSubeId());
        if (SubeKoduEnum.ANKARA.equals(subeKoduEnum) && !KararTipiEnum.TARIMSAL.equals(KararTipiEnum.getBykod(provizyon.getKarar().getTip()))) {
            subeKoduEnum = SubeKoduEnum.IDARE_MERKEZI;
        }
        detayGrup.addAlanVeri("TCMBSUBEADI", subeKoduEnum.getAdi());

        veriler.add(detayGrup);
        veriler.addAll(borclar);
        return veriler;
    }

    private List<DocGrupVeri> getOdemeMektupDetayByProvizyon(ProvizyonArsiv provizyonArsiv) throws Exception {
        SimpleDateFormat sdfTarih = new SimpleDateFormat("dd/MM/yyyy");
        List<DocGrupVeri> veriler = new ArrayList<>();
        List<DocGrupVeri> borclar = getOdemeMektupBorcBilgileri(provizyonArsiv, false);
        if (CollectionUtils.isEmpty(borclar)) {
            return new ArrayList<>();
        }
        DocGrupVeri detayGrup = new DocGrupVeri();
        detayGrup.setGrupAd("DETAY");
        Ihracatci ihracatci = provizyonArsiv.getIhracatci();
        detayGrup.addAlanVeri("IHRACATCIADI", ihracatci.getAd());
        String adres1 = ihracatci.getAdres().trim();
        String adres2 = StringUtils.EMPTY;
        String adres3 = StringUtils.EMPTY;
        if (adres1.length() > 50) {
            if (adres1.length() > 100) {
                adres3 = adres1.substring(100);
                adres2 = adres1.substring(50, 100);
            } else {
                adres2 = adres1.substring(50);
                adres1 = adres1.substring(0, 50);
            }
        }

        detayGrup.addAlanVeri("IHRACATCIADRES1", adres1);
        detayGrup.addAlanVeri("IHRACATCIADRES2", adres2);
        detayGrup.addAlanVeri("IHRACATCIADRES3", adres3);
        detayGrup.addAlanVeri("TARIH", sdfTarih.format(new Date()));
        detayGrup.addAlanVeri("KARARNO", provizyonArsiv.getKarar().getKararNo());
        String kararAraMetin = "sayılı %s ";
        detayGrup.addAlanVeri("KARARADI", String.format(kararAraMetin, provizyonArsiv.getKarar().getAd()));
        detayGrup.addAlanVeri("PROVIZYONTUTAR", getProvizyonArsivToplamTutar(provizyonArsiv, false));
        detayGrup.addAlanVeri("ODEMETARIH", sdfTarih.format(provizyonArsiv.getOdemeTarih()));
        SubeKoduEnum subeKoduEnum = SubeKoduEnum.getById(provizyonArsiv.getKarar().getSubeId());
        if (SubeKoduEnum.ANKARA.equals(subeKoduEnum) && !KararTipiEnum.TARIMSAL.equals(KararTipiEnum.getBykod(provizyonArsiv.getKarar().getTip()))) {
            subeKoduEnum = SubeKoduEnum.IDARE_MERKEZI;
        }
        detayGrup.addAlanVeri("TCMBSUBEADI", subeKoduEnum.getAdi());
        veriler.add(detayGrup);
        veriler.addAll(borclar);
        return veriler;
    }

    private List<DocGrupVeri> getDavetMektupDetayByProvizyon(Provizyon provizyon) throws Exception {
        SimpleDateFormat sdfTarih = new SimpleDateFormat("dd/MM/yyyy");

        List<DocGrupVeri> veriler = new ArrayList<>();

        List<DocGrupVeri> borclar = getOdemeMektupBorcBilgileri(provizyon, true);

        DocGrupVeri detayGrup = new DocGrupVeri();
        detayGrup.setGrupAd("DETAY");
        Ihracatci ihracatci = provizyon.getIhracatci();
        detayGrup.addAlanVeri("IHRACATCIADI", ihracatci.getAd());
        String adres1 = ihracatci.getAdres().trim();
        String adres2 = StringUtils.EMPTY;
        if (adres1.length() > 100) {
            adres2 = adres1.substring(100);
            adres1 = adres1.substring(0, 100);
        }

        detayGrup.addAlanVeri("IHRACATCIADRES1", adres1);
        detayGrup.addAlanVeri("IHRACATCIADRES2", adres2);
        detayGrup.addAlanVeri("TARIH", sdfTarih.format(new Date()));
        detayGrup.addAlanVeri("KARARNO", provizyon.getKarar().getKararNo());
        detayGrup.addAlanVeri("TUTAR", provizyon.getHakedisTutari());
        String subeKodu = kullaniciBilgileriService.getSubeKodu(provizyon.getKarar().getSubeId());

        DocGrupVeri ekBilgilendirme = new DocGrupVeri();
        ekBilgilendirme.setGrupAd("EKBILGILENDIRMEYAZISI");
        ekBilgilendirme.addAlanVeri("TCMBSUBEADI", SubeKoduEnum.getBykod(subeKodu).getAdi());

        veriler.add(detayGrup);

        if (!borclar.isEmpty()) {
            DocGrupVeri borcBilgilendirme = new DocGrupVeri();
            borcBilgilendirme.setGrupAd("BORCBILGILENDIRMEYAZISI");
            veriler.add(borcBilgilendirme);
            veriler.add(ekBilgilendirme);
            veriler.addAll(borclar);
        } else {
            veriler.add(ekBilgilendirme);
        }

        return veriler;
    }

    private void kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(KararTipiEnum tip, Integer yil, Integer belgeNo, String kararNo, LocalDate odemeTarihi, String vkn, String tckn) throws Exception {
        logger.info("odeme mektuplarini eposta ile gonder", "Kep bilgisi olan ihracatçılara mail ile mektup gönderme işlemi başladı");
        List<Provizyon> provizyonList = provizyonIslemleriService.listProvizyon(odemeTarihi, odemeTarihi, tip, belgeNo, yil, kararNo, vkn, tckn, null, null);
        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Gönderilecek ödeme mektubu bulunamadı");
        }
        List<EPostaDTO> ePostaDTOList = new ArrayList<>();
        for (Provizyon provizyon : provizyonList) {
            // Sadece nakit ödemeler için mektup çıkartılacaktır. Idare merkezi ödemeleri zaten nakit olduğu için kontrole gerek yoktur. Tarım ödemesi ise ödeme mektubu çıkartmak için nakit olması gerekir.
            if (!provizyon.getKarar().isNakitKarar()) {
                continue;
            }

            // borcu olmayan ihracatcıya mektup gitmeyecek
            List<BorcBilgi> borcList = borcBilgiService.getOdenmisBorclarByProvizyon(provizyon);
            if (borcList == null || borcList.isEmpty()) {
                continue;
            }

            if (provizyon.getIhracatci().getEmail() == null) {
                continue;
            }
            List<DocGrupVeri> provizyonVeri = getOdemeMektupDetayByProvizyon(provizyon);
            if (CollectionUtils.isEmpty(provizyonVeri)) {
                continue;
            }
            List<DocGrupVeri> veriler = new ArrayList<>(provizyonVeri);
            DocVeri docVeri = new DocVeri();
            docVeri.addGrupVeriAll(veriler);
            PikurDocument pd = pikurIslemService.xmlYukle(ihracatciNakitOdemeMektubuPikurXMLPath);
            ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.PORTRAIT);
            ExportedFile file = outputAsPDF(baos, this.handleExportFileName(odemeTarihi, MektupTipEnum.ODEME_MEKTUPLARI));
            List<String> toList = new ArrayList<>();
            if (StringUtils.isNotEmpty(provizyon.getIhracatci().getEmail())) {
                toList.add(provizyon.getIhracatci().getEmail());
            } else {
                continue;
            }
            EPostaDTO ePostaDTO = new EPostaDTO();
            ePostaDTO.setFrom("ogmfon@tcmb.gov.tr");
            ePostaDTO.setTo(toList);
            ePostaDTO.setCc(Collections.singletonList("ogmfon@tcmb.gov.tr"));
            ePostaDTO.setSubject("DFİF Kapsamında Hakediş Ödeme Bilgileri");
            ePostaDTO.setBody(provizyon.getKarar().getKararNo() + " sayılı karar kapsamında hakettiğiniz tutara ait bilgiler ekteki dokümanda yer almaktadır.");
            ePostaDTO.setContentType("text/plain; charset=utf-8");
            ePostaDTO.setApplicationName("ODMDFIFSE");
            Attachment attachment = new Attachment();
            attachment.setName(file.getFileName() + ".pdf");
            attachment.setContent(baos.toByteArray());
            List<Attachment> attachmentList = new ArrayList<>();
            attachmentList.add(attachment);
            ePostaDTO.setAttachment(attachmentList);
            ePostaDTOList.add(ePostaDTO);
        }
        if (ePostaDTOList.isEmpty()) {
            throw new GecersizVeriException("Gönderilecek ödeme mektubu bulunamadı.");
        } else {
            this.handleSendEposta(ePostaDTOList, MektupServiceImpl.STR_ODEME_MEKTUP);
        }
        logger.info("odeme mektuplarini eposta ile gonder", "Kep bilgisi olan ihracatçılara mail ile mektup gönderme işlemi bitti");
    }


    private void handleSendEposta(List<EPostaDTO> ePostaDTOList, String mektupAd) throws ValidationException {
        Map<String,String> errorMap =  epostaGonderimService.sendEposta(ePostaDTOList);
        if(!errorMap.isEmpty()) {
            String mailBodyHtml = this.buildErrorTableHtml(errorMap, mektupAd);

            EPostaDTO hataBildirimMail = new EPostaDTO();

            hataBildirimMail.setFrom("ogmfon@tcmb.gov.tr");
            hataBildirimMail.setCc(Collections.singletonList("ogmfon@tcmb.gov.tr"));
            hataBildirimMail.setSubject("OGMDFIF-E-Posta Gönderiminde Hata Alındı");
            hataBildirimMail.setBody(mailBodyHtml);
            hataBildirimMail.setContentType("text/html; charset=utf-8");
            hataBildirimMail.setTo(Collections.singletonList("ogmfon@tcmb.gov.tr"));
            hataBildirimMail.setApplicationName("ODMDFIFSE");

            epostaGonderimService.sendEposta(List.of(hataBildirimMail));

            logger.info("E-Posta hata bildirim maili","E-Posta gönderiminde bir hata alındı, hata bildirim maili gönderildi");

            throw new ValidationException(String.join("\n", "E-Posta gönderimi sırasında bir hata meydana geldi"));
        }
    }


    private String buildErrorTableHtml(Map<String, String> errorMap, String mektupAd) {

        String tarih = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date());


        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<p><b>").append(tarih).append("</b> tarihinde <b>")
                        .append(mektupAd)
                                .append("</b> gönderimi sırasında bir hata meydana geldi.</p>");
        sb.append("<h3>E-posta Gönderim Hataları</h3>");
        sb.append("<table border='1' cellspacing='0' cellpadding='5' style='border-collapse:collapse;'>");
        sb.append("<tr style='background-color:#f2f2f2'>")
                .append("<th>Hata Mesaj ID </th>")
                .append("<th>Tarih</th>")
                .append("<th>Hata Detayı</th>")
                .append("</tr>");

        errorMap.forEach((messageId, error) -> {
            sb.append("<tr>")
                    .append("<td>").append(messageId).append("</td>")
                    .append("<td>").append(tarih).append("</td>")
                    .append("<td>").append(error.replace("\n","<br/>")).append("</td>")
                    .append("</tr>");

        });
        sb.append("</table>");
        sb.append("</body></html>");
        return sb.toString();


    }

    private void kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(KararTipiEnum tip, Integer yil, Integer belgeNo, String kararNo, LocalDate odemeTarihi, String vkn, String tckn) throws Exception {
        List<ProvizyonArsiv> provizyonList = provizyonIslemleriService.listProvizyonArsiv(odemeTarihi, odemeTarihi, tip, belgeNo, yil, kararNo, vkn, tckn, null, null);
        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Gönderilecek ödeme mektubu bulunamadı");
        }
        List<EPostaDTO> ePostaDTOList = new ArrayList<>();
        for (ProvizyonArsiv provizyon : provizyonList) {
            // Sadece nakit ödemeler için mektup çıkartılacaktır. Idare merkezi ödemeleri zaten nakit olduğu için kontrole gerek yoktur. Tarım ödemesi ise ödeme mektubu çıkartmak için nakit olması gerekir.
            if (!provizyon.getKarar().isNakitKarar()) {
                continue;
            }

            // borcu olmayan ihracatcıya mektup gitmeyecek
            List<BorcBilgiArsiv> borcList = borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon);
            if (borcList == null || borcList.isEmpty()) {
                continue;
            }

            if (provizyon.getIhracatci().getEmail() == null) {
                continue;
            }
            List<DocGrupVeri> provizyonVeri = getOdemeMektupDetayByProvizyon(provizyon);
            if (CollectionUtils.isEmpty(provizyonVeri)) {
                continue;
            }
            List<DocGrupVeri> veriler = new ArrayList<>(provizyonVeri);
            DocVeri docVeri = new DocVeri();
            docVeri.addGrupVeriAll(veriler);
            PikurDocument pd = pikurIslemService.xmlYukle(ihracatciNakitOdemeMektubuPikurXMLPath);
            ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.PORTRAIT);
            ExportedFile file = outputAsPDF(baos, handleExportFileName(odemeTarihi, MektupTipEnum.ODEME_MEKTUPLARI));

            List<String> toList = new ArrayList<>();
            if (StringUtils.isNotBlank(provizyon.getIhracatci().getEmail())) {
                toList.add(provizyon.getIhracatci().getEmail());
            } else {
                continue;
            }
            EPostaDTO ePostaDTO = new EPostaDTO();
            ePostaDTO.setFrom("ogmfon@tcmb.gov.tr");
            ePostaDTO.setTo(toList);
            ePostaDTO.setCc(Collections.singletonList("ogmfon@tcmb.gov.tr"));
            ePostaDTO.setSubject("DFİF Kapsamında Hakediş Ödeme Bilgileri");
            ePostaDTO.setBody(provizyon.getKarar().getKararNo() + " sayılı karar kapsamında hakettiğiniz tutara ait bilgiler ekteki dokümanda yer almaktadır.");
            ePostaDTO.setContentType("text/plain; charset=utf-8");
            ePostaDTO.setApplicationName("ODMDFIFSE");
            Attachment attachment = new Attachment();
            attachment.setName(file.getFileName() + ".pdf");
            attachment.setContent(baos.toByteArray());

            List<Attachment> attachmentList = new ArrayList<>();
            attachmentList.add(attachment);
            ePostaDTO.setAttachment(attachmentList);
            ePostaDTOList.add(ePostaDTO);
        }
        if (ePostaDTOList.isEmpty()) {
            throw new GecersizVeriException("Gönderilecek ödeme mektubu bulunamadı.");
        } else {
            this.handleSendEposta(ePostaDTOList, MektupServiceImpl.STR_ODEME_MEKTUP);
        }
    }

    private DocVeri ihracatciOdemeMektuplariniCikar(KararTipiEnum tip, Integer yil, Integer belgeNo, String kararNo, LocalDate odemeTarihi, String vkn, String tckn) throws Exception {
        List<Provizyon> provizyonList = provizyonIslemleriService.listProvizyon(odemeTarihi, odemeTarihi, tip, belgeNo, yil, kararNo, vkn, tckn, null, null);
        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı");
        }
        DocVeri docVeri = new DocVeri();
        List<DocGrupVeri> veriler = new ArrayList<>();
        for (Provizyon provizyon : provizyonList) {
            // Sadece nakit ödemeler için mektup çıkartılacaktır. Idare merkezi ödemeleri zaten nakit olduğu için kontrole gerek yoktur. Tarım ödemesi ise ödeme mektubu çıkartmak için nakit olması gerekir.
            if (!provizyon.getKarar().isNakitKarar()) {
                continue;
            }
            // borcu olmayan ihracatcıya mektup gitmeyecek
            List<BorcBilgi> borcList = borcBilgiService.getOdenmisBorclarByProvizyon(provizyon);
            if (borcList == null || borcList.isEmpty()) {
                continue;
            }
            if (StringUtils.isNotBlank(provizyon.getIhracatci().getEmail())) {
                continue;
            }
            List<DocGrupVeri> provizyonVeri = getOdemeMektupDetayByProvizyon(provizyon);
            if (CollectionUtils.isEmpty(provizyonVeri)) {
                continue;
            }
            veriler.addAll(provizyonVeri);
            veriler.add(new YeniSayfaVeri());
        }
        docVeri.addGrupVeriAll(veriler);
        if (docVeri.getDocGrupSayi() == 0) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı");
        }
        return docVeri;
    }

    private DocVeri ihracatciOdemeMektuplariniCikarEski(KararTipiEnum tip, Integer yil, Integer belgeNo, String kararNo, LocalDate odemeTarihi, String vkn, String tckn) throws Exception {
        List<ProvizyonArsiv> provizyonList = provizyonIslemleriService.listProvizyonArsiv(odemeTarihi, odemeTarihi, tip, belgeNo, yil, kararNo, vkn, tckn, null, null);
        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı");
        }
        DocVeri docVeri = new DocVeri();
        List<DocGrupVeri> veriler = new ArrayList<>();
        for (ProvizyonArsiv provizyon : provizyonList) {
            // Sadece nakit ödemeler için mektup çıkartılacaktır. Idare merkezi ödemeleri zaten nakit olduğu için kontrole gerek yoktur. Tarım ödemesi ise ödeme mektubu çıkartmak için nakit olması gerekir.
            if (!provizyon.getKarar().isNakitKarar()) {
                continue;
            }
            // borcu olmayan ihracatcıya mektup gitmeyecek
            List<BorcBilgiArsiv> borcList = borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon);
            if (borcList == null || borcList.isEmpty()) {
                continue;
            }
            if (StringUtils.isNotBlank(provizyon.getIhracatci().getEmail())) {
                continue;
            }
            List<DocGrupVeri> provizyonVeri = getOdemeMektupDetayByProvizyon(provizyon);
            if (CollectionUtils.isEmpty(provizyonVeri)) {
                continue;
            }
            veriler.addAll(provizyonVeri);
            veriler.add(new YeniSayfaVeri());
        }
        docVeri.addGrupVeriAll(veriler);
        if (docVeri.getDocGrupSayi() == 0) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı");
        }
        return docVeri;
    }

    private DocVeri ihracatciDavetMektuplariniCikar(KararTipiEnum tip, Integer yil, Integer belgeNo, String kararNo, LocalDate odemeTarihi, String vkn, String tckn, String subeId) throws Exception {
        logger.info("ihracatciDavetMektuplariniCikar", "İhracatçı davet mektuplarını çıkarma işlemi başladı.");
        if (kullaniciBilgileriService.idareMerkeziKullanicisiMi()) {
            throw new ValidationException("Sadece şube kullanıcıları bu mektup tipinde belge oluşturabilir");
        }
        List<Provizyon> provizyonList = provizyonIslemleriService.listProvizyon(odemeTarihi, odemeTarihi, tip, belgeNo, yil, kararNo, vkn, tckn, null, null);
        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı.");
        }
        DocVeri docVeri = new DocVeri();
        List<DocGrupVeri> veriler = new ArrayList<>();
        for (Provizyon provizyon : provizyonList) {
            if (!provizyon.getKarar().isMahsupKarar()) {
                continue;
            }
            List<DocGrupVeri> provizyonVeri = getDavetMektupDetayByProvizyon(provizyon);
            veriler.addAll(provizyonVeri);
            veriler.add(new YeniSayfaVeri());
        }
        docVeri.addGrupVeriAll(veriler);
        if (docVeri.getDocGrupSayi() == 0) {
            throw new GecersizVeriException("Çıktı alınacak mektup bulunamadı");
        }
        return docVeri;
    }


    private ExportedFile outputAsPDF(ByteArrayOutputStream baos, String dosyaAdi) throws IOException {
        logger.info("outputAsPDF", "PDF olarak çıkarma işlemi başladı.");
        ExportedFile file = new ExportedFile();
        byte[] byteArray = baos.toByteArray();
        file.setData(byteArray);
        file.setFileName(dosyaAdi);
        file.setMimeType(ExportedFile.Types.Pdf.mimeType);
        return file;
    }


    private void kepAdresiOlanIhracatcilaraDavetMektuplariGonder(KararTipiEnum tip, Integer yil, Integer belgeNo, String kararNo, LocalDate odemeTarihi, String vkn, String tckn) throws Exception {

        logger.info("Davet mektuplarini eposta ile gonder", "Kep bilgisi olan ihracatçılara mail ile davet mektubu gönderme işlemi başladı");

        if (kullaniciBilgileriService.idareMerkeziKullanicisiMi()) {
            throw new ValidationException("Sadece şube kullanıcıları bu mektup tipinde belge oluşturabilir");
        }

        List<Provizyon> provizyonList = provizyonIslemleriService.listProvizyon(odemeTarihi, odemeTarihi, tip, belgeNo, yil, kararNo, vkn, tckn, null, null);
        if (provizyonList == null || provizyonList.isEmpty()) {
            throw new GecersizVeriException("Gönderilecek mektup bulunamadı");
        }
        List<EPostaDTO> ePostaDTOList = new ArrayList<>();
        for (Provizyon provizyon : provizyonList) {
            if (!provizyon.getKarar().isMahsupKarar() || provizyon.getIhracatci().getEmail() == null) {
                continue;
            }
            List<DocGrupVeri> provizyonVeri = getDavetMektupDetayByProvizyon(provizyon);
            List<DocGrupVeri> veriler = new ArrayList<>(provizyonVeri);
            if (!veriler.isEmpty()) {
                DocVeri docVeri = new DocVeri();
                docVeri.addGrupVeriAll(veriler);
                PikurDocument pd = pikurIslemService.xmlYukle(ihracatciDavetMektup);
                ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.PORTRAIT);
                ExportedFile file = outputAsPDF(baos, this.handleExportFileName(odemeTarihi, MektupTipEnum.IHRACATCI_DAVET_MEKTUPLARI));
                List<String> toList = new ArrayList<>();
                if (StringUtils.isNotEmpty(provizyon.getIhracatci().getEmail())) {
                    toList.add(provizyon.getIhracatci().getEmail());
                } else {
                    continue;
                }

                try {
                    EPostaDTO ePostaDTO = new EPostaDTO();
                    ePostaDTO.setFrom("ogmfon@tcmb.gov.tr");
                    ePostaDTO.setTo(toList);
                    ePostaDTO.setCc(Collections.singletonList("ogmfon@tcmb.gov.tr"));
                    ePostaDTO.setSubject("DFİF Kapsamındaki Hak Ediş Bilgisi");
                    ePostaDTO.setBody(String.format(MektupServiceImpl.HAKEDIS_DAVET_MEKTUP_BODY, provizyon.getKarar().getKararNo(),
                            Objects.requireNonNull(SubeKoduEnum.getById(provizyon.getKarar().getSubeId())).getAdi()));
                    ePostaDTO.setContentType("text/plain; charset=utf-8");
                    ePostaDTO.setApplicationName("ODMDFIFSE");
                    Attachment attachment = new Attachment();
                    attachment.setName(file.getFileName() + ".pdf");
                    attachment.setContent(baos.toByteArray());

                    List<Attachment> attachmentList = new ArrayList<>();
                    attachmentList.add(attachment);
                    ePostaDTO.setAttachment(attachmentList);
                    ePostaDTOList.add(ePostaDTO);
                } catch (Exception e) {
                    logger.error("Hakediş mail göndeirmi", "Hak ediş davet mektubu gönderimi sırasında bir hata oluştu." + e.getMessage());
                }
            }
        }

        if (ePostaDTOList.isEmpty()) {
            throw new GecersizVeriException("Davet mektubu gönderilecek eposta bulunamadı.");
        } else {
            this.handleSendEposta(ePostaDTOList, MektupServiceImpl.STR_DAVET_MEKTUP);
        }

        logger.info("Davet mektuplarini eposta ile gonder", "Kep bilgisi olan ihracatçılara mail ile davet mektubu gönderme işlemi bitti");
    }


    private void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(LocalDate odemeTarihi) throws Exception {

        logger.info("Hakediş Devir mektuplarini eposta ile gonder", "Kep bilgisi olan ihracatçılara mail ile hakediş-devir mektubu gönderme işlemi başladı");

        List<Ihracatci> ihracatcilar = hakedisIslemleriService.getDeviriOlanIhracatcilar(odemeTarihi);
        if (ihracatcilar == null || ihracatcilar.isEmpty())
            throw new GecersizVeriException("İlgili tarihte deviri olan ihracatçı bulunamadı");


        ServisTaslak servis;
        String subeKodu = kullaniciBilgileriService.getKullaniciSubeKodu();
        if (SubeKoduEnum.IDARE_MERKEZI.getKod().equals(subeKodu)) {
            throw new GenelException("İdare merkezi kullanıcısı bu işleme devam edemez.");
        }
        if (SubeKoduEnum.ANKARA.equals(SubeKoduEnum.getBykod(String.valueOf(subeKodu)))) {
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), Constants.FON_ODEMELERI);
        } else if (Objects.equals(SubeKoduEnum.getBykod(String.valueOf(subeKodu)), SubeKoduEnum.IZMIR))
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), ServisTip.KAMBIYO);
        else if (Objects.equals(SubeKoduEnum.getBykod(String.valueOf(subeKodu)), SubeKoduEnum.ISTANBUL))
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), ServisTip.FON_ODEMELERI);
        else {
            servis = muhasebeClientService.findServis(Integer.parseInt(String.valueOf(subeKodu)), ServisTip.BANKACILIK);
        }

        List<EPostaDTO> ePostaDTOList = new ArrayList<>();
        for (Ihracatci ihracatciTmp : ihracatcilar) {
            if (Objects.isNull(ihracatciTmp.getEmail())) {
                continue;
            }

            List<Hakedis> devirdenOlusanHakedisler = hakedisIslemleriService.getDevirdenOlusmusHakedisler(ihracatciTmp, odemeTarihi);


            for (Hakedis hakedis : devirdenOlusanHakedisler) {
                List<DocGrupVeri> veriler = hakedisDevirMektubuCikar(servis, List.of(hakedis));

                DocVeri docVeri = new DocVeri();
                docVeri.addGrupVeriAll(veriler);

                PikurDocument pd = pikurIslemService.xmlYukle(ihracatciDevirMektubuPikurXMLPath);
                ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.PORTRAIT);
                ExportedFile file = outputAsPDF(baos, this.handleExportFileName(odemeTarihi, MektupTipEnum.HAKEDIS_DEVIR_MEKTUPLARI));

                List<String> toList = new ArrayList<>();
                if (StringUtils.isNotEmpty(ihracatciTmp.getEmail())) {
                    toList.add(ihracatciTmp.getEmail());
                } else {
                    continue;
                }

                try {

                    EPostaDTO ePostaDTO = new EPostaDTO();
                    ePostaDTO.setFrom("ogmfon@tcmb.gov.tr");
                    ePostaDTO.setTo(toList);
                    ePostaDTO.setCc(Collections.singletonList("ogmfon@tcmb.gov.tr"));
                    ePostaDTO.setSubject("DFİF Kapsamındaki Hak Ediş Bilgisi (Devir)");
                    ePostaDTO.setBody(String.format(MektupServiceImpl.HAKEDIS_DEVIR_MEKTUP_BODY, hakedis.getKarar().getKararNo(),
                            Objects.requireNonNull(SubeKoduEnum.getById(hakedis.getSubeId())).getAdi()));
                    ePostaDTO.setContentType("text/plain; charset=utf-8");
                    ePostaDTO.setApplicationName("ODMDFIFSE");
                    Attachment attachment = new Attachment();
                    attachment.setName(file.getFileName() + ".pdf");
                    attachment.setContent(baos.toByteArray());

                    List<Attachment> attachmentList = new ArrayList<>();
                    attachmentList.add(attachment);
                    ePostaDTO.setAttachment(attachmentList);
                    ePostaDTOList.add(ePostaDTO);

                } catch (Exception e) {
                    logger.error("Hakediş devir mail göndeirimi", "Hak ediş devir mektubu gönderimi sırasında bir hata oluştu." + e.getMessage());
                }
            }
        }


        if (ePostaDTOList.isEmpty()) {
            throw new GecersizVeriException("Hakediş Devir mektubu gönderilecek eposta bulunamadı.");
        } else {
            this.handleSendEposta(ePostaDTOList, MektupServiceImpl.STR_HAKEDIS_DEVIR_MEKTUP);
        }

        logger.info("Davet mektuplarini eposta ile gonder", "Kep bilgisi olan ihracatçılara mail ile davet mektubu gönderme işlemi bitti");
    }

}




