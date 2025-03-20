
mvn clean compile install

#Dockerfile : 
docker build --build-arg JAR_FILE=coin-calculate-api-server-app/target/*.jar -t springbootapp .

docker run -p 8088:8080 springbootapp  

#Docker-Compose:

docker-compose -f docker-compose.yml up


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tr.gov.tcmb.ogmdfif.exception.GecersizVeriException;
import tr.gov.tcmb.ogmdfif.exception.GenelException;
import tr.gov.tcmb.ogmdfif.model.entity.Hakedis;
import tr.gov.tcmb.ogmdfif.model.entity.Ihracatci;
import tr.gov.tcmb.ogmdfif.service.*;
import tr.gov.tcmb.ogmdfif.util.Constants;
import tr.gov.tcmb.ogmdfif.ws.client.impl.EpostaGonderimService;
import tr.gov.tcmb.ogmdfif.ws.request.EPostaDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MektupServiceImplTest {

    @Mock
    private HakedisIslemleriService hakedisIslemleriService;

    @Mock
    private KullaniciBilgileriService kullaniciBilgileriService;

    @Mock
    private MuhasebeClientService muhasebeClientService;

    @Mock
    private PikurIslemService pikurIslemService;

    @Mock
    private EpostaGonderimService epostaGonderimService;

    @InjectMocks
    private MektupServiceImpl mektupService;
	
	@Mock
    private KullaniciBilgileriService kullaniciBilgileriService;

    @Mock
    private ProvizyonIslemleriService provizyonIslemleriService;

    @Mock
    private PikurIslemService pikurIslemService;

    @Mock
    private EpostaGonderimService epostaGonderimService;
	
	@Mock
    private BorcBilgiService borcBilgiService;
	
	@Mock
    private EpostaGonderimService epostaGonderimService;

    private PlatformLogger logger;
	
	@Mock
    private KullaniciBilgileriService kullaniciBilgileriService;

    @Mock
    private PikurIslemService pikurIslemService;

    @Mock
    private MuhasebeClientService muhasebeClientService;

    @Mock
    private KararIslemleriService kararIslemleriService;
	
	    @Mock
    private HakedisIslemleriService hakedisIslemleriService;
	
	 @Mock
    private EftBilgiYonetimRepository eftBilgisiYonetimRepository;

    @Mock
    private EFTClientService eftClientService;

    @Mock
    private BankaSubeService bankaSubeService;

    @InjectMocks
    private MektupServiceImpl mektupService;
	
	    
    @InjectMocks
    @Spy
    private DavetMektupService davetMektupService;
	
	
	@Test
    void nakitKontrolYap_KararYoksaExceptionFirlatir() {
        String kararNo = "KARAR_001";
        when(kararIslemleriService.getKararByKararNoAndSube(kararNo, "IDARE_MERKEZI")).thenReturn(null);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> mektupService.nakitKontrolYap(kararNo)
        );
        assertEquals("Aradığınız karar bulunamamıştır. Karar No: " + kararNo, exception.getMessage());
    }

    @Test
    void nakitKontrolYap_NakitDegilseExceptionFirlatir() {
        String kararNo = "KARAR_002";
        Karar karar = new Karar();
        karar.setNakitKarar(false);
        when(kararIslemleriService.getKararByKararNoAndSube(kararNo, "IDARE_MERKEZI")).thenReturn(karar);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> mektupService.nakitKontrolYap(kararNo)
        );
        assertEquals("Ödeme mektupları sadece nakit ödemeler için üretilmektedir.", exception.getMessage());
    }

    @Test
    void nakitKontrolYap_NakitKararIseHataYok() {
        String kararNo = "KARAR_003";
        Karar karar = new Karar();
        karar.setNakitKarar(true);
        when(kararIslemleriService.getKararByKararNoAndSube(kararNo, "IDARE_MERKEZI")).thenReturn(karar);

        assertDoesNotThrow(() -> mektupService.nakitKontrolYap(kararNo));
    }
	
	

    @Test
    void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder_IhracatciYoksaHata() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(date)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GecersizVeriException.class, 
            () -> mektupService.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(date)
        );
    }

    @Test
    void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder_IdareMerkeziKullanicisiHata() {
        // Arrange
        LocalDate date = LocalDate.now();
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(date)).thenReturn(List.of(new Ihracatci()));
        when(kullaniciBilgileriService.getKullaniciSubeKodu()).thenReturn("IDARE_MERKEZI");

        // Act & Assert
        assertThrows(GenelException.class, 
            () -> mektupService.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(date)
        );
    }

    @Test
    void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder_AnkaraSubesiServisTipi() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setEmail("test@tcmb.gov.tr");
        
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(date)).thenReturn(List.of(ihracatci));
        when(kullaniciBilgileriService.getKullaniciSubeKodu()).thenReturn("ANKARA");
        when(muhasebeClientService.findServis(anyInt(), eq(Constants.FON_ODEMELERI))).thenReturn(new ServisTaslak());
        when(hakedisIslemleriService.getDevirdenOlusmusHakedisler(any(), any())).thenReturn(List.of(new Hakedis()));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(date);

        // Assert
        verify(pikurIslemService).pdfDocOlustur(any(), any(), any(), any());
    }

    @Test
    void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder_EmailYoksaAtla() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        Ihracatci ihracatci = new Ihracatci(); // Email null
        
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(date)).thenReturn(List.of(ihracatci));
        when(kullaniciBilgileriService.getKullaniciSubeKodu()).thenReturn("ISTANBUL");

        // Act & Assert
        assertThrows(GecersizVeriException.class, 
            () -> mektupService.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(date)
        );
    }

    @Test
    void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder_EpostaHazirlamaHatasi() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setEmail("test@tcmb.gov.tr");
        
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(date)).thenReturn(List.of(ihracatci));
        when(kullaniciBilgileriService.getKullaniciSubeKodu()).thenReturn("IZMIR");
        when(hakedisIslemleriService.getDevirdenOlusmusHakedisler(any(), any())).thenThrow(new RuntimeException("DB Error"));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(date);

        // Assert
        verify(epostaGonderimService).sendEposta(anyList(), anyString());
    }

    @Test
    void kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder_BasariliGonderim() throws Exception {
        // Arrange
        LocalDate date = LocalDate.now();
        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setEmail("test@tcmb.gov.tr");
        Hakedis hakedis = new Hakedis();
        hakedis.setSubeId("ANKARA");
        
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(date)).thenReturn(List.of(ihracatci));
        when(kullaniciBilgileriService.getKullaniciSubeKodu()).thenReturn("ISTANBUL");
        when(hakedisIslemleriService.getDevirdenOlusmusHakedisler(any(), any())).thenReturn(List.of(hakedis));
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        mektupService.kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(date);

        // Assert
        verify(epostaGonderimService).sendEposta(anyList(), eq("Hakedis Devir Mektupları"));
    }
	
	
	@Test
    void davetMektubuGonder_IdareMerkeziKullanicisiHata() {
        // Arrange
        when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_001", LocalDate.now(), "VKN_001", "TCKN_001"
            )
        );
        verify(kullaniciBilgileriService).idareMerkeziKullanicisiMi();
    }

    @Test
    void davetMektubuGonder_ProvizyonListesiBosIseHata() {
        // Arrange
        when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_002", LocalDate.now(), "VKN_002", "TCKN_002"
            )
        );
    }

    @Test
    void davetMektubuGonder_GecerliProvizyonYoksaHata() {
        // Arrange
        Provizyon provizyon = new Provizyon();
        provizyon.setMahsupKarar(false); // Mahsup değil
        provizyon.getIhracatci().setEmail(null); // Email yok

        when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_003", LocalDate.now(), "VKN_003", "TCKN_003"
            )
        );
    }

    @Test
    void davetMektubuGonder_BasariliEpostaGonderimi() throws Exception {
        // Arrange
        Provizyon validProvizyon = new Provizyon();
        validProvizyon.setMahsupKarar(true);
        validProvizyon.getIhracatci().setEmail("ihracatci@tcmb.gov.tr");
        validProvizyon.getKarar().setSubeId("ANKARA");

        when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(validProvizyon));
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        mektupService.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_004", LocalDate.now(), "VKN_004", "TCKN_004"
        );

        // Assert
        verify(epostaGonderimService).sendEposta(anyList(), eq("Davet Mektupları"));
        verify(pikurIslemService).pdfDocOlustur(any(), any(), eq(PageSize.A4), eq(OrientationRequested.PORTRAIT));
    }

    @Test
    void davetMektubuGonder_PdfOlusturmaHatasi() throws Exception {
        // Arrange
        Provizyon validProvizyon = new Provizyon();
        validProvizyon.setMahsupKarar(true);
        validProvizyon.getIhracatci().setEmail("ihracatci@tcmb.gov.tr");

        when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(validProvizyon));
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenThrow(new RuntimeException("PDF Hatası"));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005"
        );

        // Assert
        verify(epostaGonderimService).sendEposta(anyList(), anyString()); // Hata durumunda bile çağrılır
    }

    @Test
    void davetMektubuGonder_KarmaSenaryo() throws Exception {
        // Arrange
        Provizyon validProvizyon = new Provizyon();
        validProvizyon.setMahsupKarar(true);
        validProvizyon.getIhracatci().setEmail("valid@tcmb.gov.tr");

        Provizyon invalidProvizyon = new Provizyon();
        invalidProvizyon.setMahsupKarar(false); // Geçersiz
        invalidProvizyon.getIhracatci().setEmail("invalid@tcmb.gov.tr");

        when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(validProvizyon, invalidProvizyon));
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        mektupService.kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_006", LocalDate.now(), "VKN_006", "TCKN_006"
        );

        // Assert
        verify(epostaGonderimService).sendEposta(argThat(list -> list.size() == 1), anyString()); // Sadece 1 email gönderilmeli
    }
	
	@Test
    void outputAsPDF_GecerliByteArrayIleDosyaOlusturur() throws IOException {
        // Arrange
        String testData = "PDF içeriği";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(testData.getBytes());
        String dosyaAdi = "test_dosya";

        // Act
        ExportedFile file = mektupService.outputAsPDF(baos, dosyaAdi);

        // Assert
        assertAll(
            () -> assertNotNull(file),
            () -> assertEquals(dosyaAdi, file.getFileName()),
            () -> assertEquals("application/pdf", file.getMimeType()),
            () -> assertArrayEquals(testData.getBytes(), file.getData())
        );
    }

    @Test
    void outputAsPDF_BosByteArrayIleDosyaOlusturur() throws IOException {
        // Arrange
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String dosyaAdi = "bos_dosya";

        // Act
        ExportedFile file = mektupService.outputAsPDF(baos, dosyaAdi);

        // Assert
        assertAll(
            () -> assertEquals(0, file.getData().length),
            () -> assertEquals(dosyaAdi, file.getFileName())
        );
    }

    @Test
    void outputAsPDF_DosyaAdiNullIseHataYok() throws IOException {
        // Arrange
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Act
        ExportedFile file = mektupService.outputAsPDF(baos, null);

        // Assert
        assertNull(file.getFileName());
    }

    @Test
    void outputAsPDF_DosyaAdiBosIseHataYok() throws IOException {
        // Arrange
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Act
        ExportedFile file = mektupService.outputAsPDF(baos, "");

        // Assert
        assertEquals("", file.getFileName());
    }
	
	@Test
    void odemeMektubuGonderEski_ProvizyonListesiBosIseHata() {
        // Arrange
        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_001", LocalDate.now(), "VKN_001", "TCKN_001"
            )
        );
    }

    @Test
    void odemeMektubuGonderEski_NakitKararDegilseAtla() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(false); // Nakit değil
        provizyon.getIhracatci().setEmail("test@tcmb.gov.tr");

        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_002", LocalDate.now(), "VKN_002", "TCKN_002"
        );

        // Assert
        verify(epostaGonderimService, never()).sendEposta(anyList(), anyString()); // Email gönderilmedi
    }

    @Test
    void odemeMektubuGonderEski_BorcYoksaAtla() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(true);
        provizyon.getIhracatci().setEmail("test@tcmb.gov.tr");
        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon)).thenReturn(Collections.emptyList());

        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_003", LocalDate.now(), "VKN_003", "TCKN_003"
        );

        // Assert
        verify(pikurIslemService, never()).pdfDocOlustur(any(), any(), any(), any()); // PDF oluşturulmadı
    }

    @Test
    void odemeMektubuGonderEski_EmailYoksaAtla() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(true);
        provizyon.getIhracatci().setEmail(null); // Email null
        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon)).thenReturn(List.of(new BorcBilgiArsiv()));

        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_004", LocalDate.now(), "VKN_004", "TCKN_004"
        );

        // Assert
        verify(epostaGonderimService, never()).sendEposta(anyList(), anyString());
    }

    @Test
    void odemeMektubuGonderEski_GecerliProvizyonIleEmailGonder() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(true);
        provizyon.getIhracatci().setEmail("valid@tcmb.gov.tr");
        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon)).thenReturn(List.of(new BorcBilgiArsiv()));
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005"
        );

        // Assert
        verify(epostaGonderimService).sendEposta(anyList(), eq("Ödeme Mektupları")); // Email gönderildi
    }

    @Test
    void odemeMektubuGonderEski_TumProvizyonlarGecersizIseHata() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(false); // Tüm provizyonlar geçersiz

        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_006", LocalDate.now(), "VKN_006", "TCKN_006"
            )
        );
    }

    @Test
    void odemeMektubuGonderEski_PdfOlusturmaHatasi() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(true);
        provizyon.getIhracatci().setEmail("test@tcmb.gov.tr");
        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon)).thenReturn(List.of(new BorcBilgiArsiv()));
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenThrow(new RuntimeException("PDF Hatası"));

        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(RuntimeException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_007", LocalDate.now(), "VKN_007", "TCKN_007"
            )
        );
    }
	
	@Test
    void handleSendEposta_HataYoksaIslemYapma() throws ValidationException {
        // Arrange
        Map<String, String> emptyErrorMap = new HashMap<>();
        when(epostaGonderimService.sendEposta(anyList())).thenReturn(emptyErrorMap);

        // Act & Assert
        assertDoesNotThrow(() -> 
            mektupService.handleSendEposta(List.of(new EPostaDTO()), "Test Mektup")
        );
        verify(epostaGonderimService, never()).sendEposta(argThat(list -> list.size() == 1)); // Hata maili gönderilmedi
    }

    @Test
    void handleSendEposta_HataVarsaBildirimGonder() {
        // Arrange
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("MSG_001", "E-posta gönderilemedi");
        when(epostaGonderimService.sendEposta(anyList())).thenReturn(errorMap);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
            () -> mektupService.handleSendEposta(List.of(new EPostaDTO()), "Test Mektup")
        );

        // Hata mailinin gönderildiğini kontrol et
        verify(epostaGonderimService, times(2)).sendEposta(anyList()); // 1. Gönderim + Hata maili
        assertEquals("E-Posta gönderimi sırasında bir hata meydana geldi", exception.getMessage());
    }

    @Test
    void handleSendEposta_HataMailiIcerikKontrolu() {
        // Arrange
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("MSG_002", "Sunucu hatası");
        when(epostaGonderimService.sendEposta(anyList())).thenReturn(errorMap);

        // Act
        assertThrows(ValidationException.class,
            () -> mektupService.handleSendEposta(List.of(new EPostaDTO()), "Test Mektup")
        );

        // Hata mailinin içeriğini kontrol et
        verify(epostaGonderimService).sendEposta(argThat(list -> {
            EPostaDTO hataMaili = list.get(0);
            return hataMaili.getSubject().equals("OGMDFIF-E-Posta Gönderiminde Hata Alındı") &&
                   hataMaili.getTo().contains("ogmfon@tcmb.gov.tr") &&
                   hataMaili.getBody().contains("Sunucu hatası");
        }));
    }

    @Test
    void handleSendEposta_LoglamaKontrolu() {
        // Arrange
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("MSG_003", "Zaman aşımı");
        when(epostaGonderimService.sendEposta(anyList())).thenReturn(errorMap);

        // Act
        assertThrows(ValidationException.class,
            () -> mektupService.handleSendEposta(List.of(new EPostaDTO()), "Test Mektup")
        );

        // Log mesajının doğruluğunu kontrol et
        verify(logger).info(eq("E-Posta hata bildirim maili"), eq("E-Posta gönderiminde bir hata alındı, hata bildirim maili gönderildi"));
    }

    @Test
    void handleSendEposta_HataMailiContentTypeKontrolu() {
        // Arrange
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("MSG_004", "Geçersiz format");
        when(epostaGonderimService.sendEposta(anyList())).thenReturn(errorMap);

        // Act
        assertThrows(ValidationException.class,
            () -> mektupService.handleSendEposta(List.of(new EPostaDTO()), "Test Mektup")
        );

        // Content-Type ve uygulama adı kontrolü
        verify(epostaGonderimService).sendEposta(argThat(list -> {
            EPostaDTO hataMaili = list.get(0);
            return hataMaili.getContentType().equals("text/html; charset=utf-8") &&
                   hataMaili.getApplicationName().equals("ODMDFIFSE");
        }));
    }
	
	@Test
    void getOdemeMektupDetayByProvizyon_BorclarBosIseBosListeDoner() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon)).thenReturn(new ArrayList<>());

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getOdemeMektupDetayByProvizyon_Adres50KarakterdenKisa() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = createProvizyonWithAddress("Kısa adres", "ANKARA", KararTipiEnum.TARIMSAL);

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        DocGrupVeri detay = result.get(0);
        assertEquals("Kısa adres", detay.getAlanVeri("IHRACATCIADRES1"));
        assertEquals("", detay.getAlanVeri("IHRACATCIADRES2"));
        assertEquals("", detay.getAlanVeri("IHRACATCIADRES3"));
    }

    @Test
    void getOdemeMektupDetayByProvizyon_Adres50Ile100KarakterArasi() throws Exception {
        // Arrange
        String longAddress = StringUtil.repeat("a", 75); // 75 karakter
        ProvizyonArsiv provizyon = createProvizyonWithAddress(longAddress, "ANKARA", KararTipiEnum.TARIMSAL);

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        DocGrupVeri detay = result.get(0);
        assertEquals(longAddress.substring(0,50), detay.getAlanVeri("IHRACATCIADRES1"));
        assertEquals(longAddress.substring(50), detay.getAlanVeri("IHRACATCIADRES2"));
        assertEquals("", detay.getAlanVeri("IHRACATCIADRES3"));
    }

    @Test
    void getOdemeMektupDetayByProvizyon_Adres100KarakterdenUzun() throws Exception {
        // Arrange
        String veryLongAddress = StringUtil.repeat("a", 150); // 150 karakter
        ProvizyonArsiv provizyon = createProvizyonWithAddress(veryLongAddress, "ANKARA", KararTipiEnum.TARIMSAL);

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        DocGrupVeri detay = result.get(0);
        assertEquals(veryLongAddress.substring(0,50), detay.getAlanVeri("IHRACATCIADRES1"));
        assertEquals(veryLongAddress.substring(50,100), detay.getAlanVeri("IHRACATCIADRES2"));
        assertEquals(veryLongAddress.substring(100), detay.getAlanVeri("IHRACATCIADRES3"));
    }

    @Test
    void getOdemeMektupDetayByProvizyon_AnkaraSubesiTarimDegilseIdareMerkezi() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = createProvizyonWithAddress("Adres", "ANKARA", KararTipiEnum.DIGER);

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        assertEquals("İdare Merkezi", result.get(0).getAlanVeri("TCMBSUBEADI"));
    }

    @Test
    void getOdemeMektupDetayByProvizyon_AnkaraSubesiTarimIseAnkara() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = createProvizyonWithAddress("Adres", "ANKARA", KararTipiEnum.TARIMSAL);

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        assertEquals("Ankara Şubesi", result.get(0).getAlanVeri("TCMBSUBEADI"));
    }

    @Test
    void getOdemeMektupDetayByProvizyon_TarihFormatlamaKontrolu() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = createProvizyonWithAddress("Adres", "IZMIR", KararTipiEnum.DIGER);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupDetayByProvizyon(provizyon);

        // Assert
        assertEquals(sdf.format(new Date()), result.get(0).getAlanVeri("TARIH"));
    }

   
    private ProvizyonArsiv createProvizyonWithAddress(String address, String subeId, KararTipiEnum kararTipi) {
        Karar karar = new Karar();
        karar.setSubeId(subeId);
        karar.setTip(kararTipi.getKod());
        karar.setKararNo("KARAR_001");
        karar.setAd("Test Kararı");

        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setAd("Test İhracatçı");
        ihracatci.setAdres(address);

        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.setKarar(karar);
        provizyon.setIhracatci(ihracatci);
        provizyon.setOdemeTarih(new Date());

        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(provizyon)).thenReturn(List.of(new BorcBilgiArsiv()));
        when(borcBilgiService.getProvizyonArsivToplamTutar(any(), anyBoolean())).thenReturn(new BigDecimal("1000.00"));

        return provizyon;
    }
	@Test
    void odemeMektubuGonder_ProvizyonListesiBosIseHataFirlatir() {
        // Arrange
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_001", LocalDate.now(), "VKN_001", "TCKN_001"
            )
        );
    }

    @Test
    void odemeMektubuGonder_NakitKararDegilseAtla() throws Exception {
        // Arrange
        Provizyon provizyon = new Provizyon();
        provizyon.getKarar().setNakitKarar(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_002", LocalDate.now(), "VKN_002", "TCKN_002"
        );

        // Assert
        verify(epostaGonderimService, never()).sendEposta(anyList(), anyString());
    }

    @Test
    void odemeMektubuGonder_BorcListesiBosIseAtla() throws Exception {
        // Arrange
        Provizyon provizyon = createValidProvizyon();
        when(borcBilgiService.getOdenmisBorclarByProvizyon(provizyon)).thenReturn(Collections.emptyList());
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_003", LocalDate.now(), "VKN_003", "TCKN_003"
        );

        // Assert
        verify(pikurIslemService, never()).pdfDocOlustur(any(), any(), any(), any());
    }

    @Test
    void odemeMektubuGonder_EmailYoksaAtla() throws Exception {
        // Arrange
        Provizyon provizyon = createValidProvizyon();
        provizyon.getIhracatci().setEmail(null);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_004", LocalDate.now(), "VKN_004", "TCKN_004"
        );

        // Assert
        verify(epostaGonderimService, never()).sendEposta(anyList(), anyString());
    }

    @Test
    void odemeMektubuGonder_GecerliProvizyonIleEmailGonder() throws Exception {
        // Arrange
        Provizyon provizyon = createValidProvizyon();
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005"
        );

        // Assert
        verify(epostaGonderimService).sendEposta(anyList(), eq("Ödeme Mektupları"));
    }

    @Test
    void odemeMektubuGonder_TumProvizyonlarGecersizIseHata() throws Exception {
        // Arrange
        Provizyon provizyon = createValidProvizyon();
        provizyon.getIhracatci().setEmail(null); // Email yok
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_006", LocalDate.now(), "VKN_006", "TCKN_006"
            )
        );
    }

    // Yardımcı Metod
    private Provizyon createValidProvizyon() {
        Karar karar = new Karar();
        karar.setNakitKarar(true);
        karar.setKararNo("KARAR_VALID");

        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setEmail("valid@tcmb.gov.tr");

        Provizyon provizyon = new Provizyon();
        provizyon.setKarar(karar);
        provizyon.setIhracatci(ihracatci);

        when(borcBilgiService.getOdenmisBorclarByProvizyon(provizyon)).thenReturn(List.of(new BorcBilgi()));
        return provizyon;
    }
	
	@Test
    void ihracatciOdemeMektuplariniCikar_BosProvizyonListesi() {
        // Arrange
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikar(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_001", LocalDate.now(), "VKN_001", "TCKN_001"
            )
        );
    }

    @Test
    void ihracatciOdemeMektuplariniCikar_NakitKararDegilseAtla() throws Exception {
        // Arrange
        Provizyon provizyon = new Provizyon();
        provizyon.getKarar().setNakitKarar(false);
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikar(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_002", LocalDate.now(), "VKN_002", "TCKN_002"
            )
        );
    }

    @Test
    void ihracatciOdemeMektuplariniCikar_BorcListesiBosIseAtla() throws Exception {
        // Arrange
        Provizyon provizyon = createProvizyon(true, "", List.of());
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikar(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_003", LocalDate.now(), "VKN_003", "TCKN_003"
            )
        );
    }

    @Test
    void ihracatciOdemeMektuplariniCikar_EmailVarIseAtla() throws Exception {
        // Arrange
        Provizyon provizyon = createProvizyon(true, "email@test.com", List.of(new BorcBilgi()));
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikar(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_004", LocalDate.now(), "VKN_004", "TCKN_004"
            )
        );
    }

    @Test
    void ihracatciOdemeMektuplariniCikar_GecerliProvizyon() throws Exception {
        // Arrange
        Provizyon provizyon = createProvizyon(true, "", List.of(new BorcBilgi()));
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));
        when(mektupService.getOdemeMektupDetayByProvizyon(any())).thenReturn(List.of(new DocGrupVeri()));

        // Act
        DocVeri result = mektupService.ihracatciOdemeMektuplariniCikar(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005"
        );

        // Assert
        assertTrue(result.getDocGrupSayi() > 0);
    }

    @Test
    void ihracatciOdemeMektuplariniCikar_TumProvizyonlarGecersiz() throws Exception {
        // Arrange
        Provizyon provizyon = createProvizyon(true, "email@test.com", Collections.emptyList());
        when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikar(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_006", LocalDate.now(), "VKN_006", "TCKN_006"
            )
        );
    }

    // Yardımcı Metod
    private Provizyon createProvizyon(boolean nakitKarar, String email, List<BorcBilgi> borcList) {
        Karar karar = new Karar();
        karar.setNakitKarar(nakitKarar);

        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setEmail(email);

        Provizyon provizyon = new Provizyon();
        provizyon.setKarar(karar);
        provizyon.setIhracatci(ihracatci);

        when(borcBilgiService.getOdenmisBorclarByProvizyon(provizyon)).thenReturn(borcList);
        return provizyon;
    }
	    @Test
    void sendIhracatciMektupMailRouter_OdemeMektubuMilatSonrasi() throws Exception {
        // Arrange
        LocalDate odemeTarihi = LocalDate.of(2025, 2, 1); // Milat tarihinden (20/01/2025) sonra
        when(kararIslemleriService.getKararByKararNoAndSube(any(), any())).thenReturn(new Karar());

        // Act
        mektupService.sendIhracatciMektupMailRouter(
            KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_001", odemeTarihi, "VKN_001", "TCKN_001", MektupTipEnum.ODEME_MEKTUPLARI
        );

        // Assert
        verify(mektupService).kepAdresiOlanIhracatcilaraOdemeMektuplariGonder(
            any(), anyInt(), anyInt(), anyString(), any(), any(), any()
        );
    }

    @Test
    void sendIhracatciMektupMailRouter_OdemeMektubuMilatOncesi() throws Exception {
        // Arrange
        LocalDate odemeTarihi = LocalDate.of(2024, 12, 31); // Milat tarihinden önce
        when(kararIslemleriService.getKararByKararNoAndSube(any(), any())).thenReturn(new Karar());

        // Act
        mektupService.sendIhracatciMektupMailRouter(
            KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_002", odemeTarihi, "VKN_002", "TCKN_002", MektupTipEnum.ODEME_MEKTUPLARI
        );

        // Assert
        verify(mektupService).kepAdresiOlanIhracatcilaraOdemeMektuplariGonderEski(
            any(), anyInt(), anyInt(), anyString(), any(), any(), any()
        );
    }

    @Test
    void sendIhracatciMektupMailRouter_DavetMektubu() throws Exception {
        // Arrange
        when(kararIslemleriService.getKararByKararNoAndSube(any(), any())).thenReturn(new Karar());

        // Act
        mektupService.sendIhracatciMektupMailRouter(
            KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_003", LocalDate.now(), "VKN_003", "TCKN_003", MektupTipEnum.IHRACATCI_DAVET_MEKTUPLARI
        );

        // Assert
        verify(mektupService).kepAdresiOlanIhracatcilaraDavetMektuplariGonder(
            any(), anyInt(), anyInt(), anyString(), any(), any(), any()
        );
    }

    @Test
    void sendIhracatciMektupMailRouter_HakedisDevirMektubu() throws Exception {
        // Act
        mektupService.sendIhracatciMektupMailRouter(
            KararTipiEnum.TARIMSAL, 1, 2023, null, LocalDate.now(), "VKN_004", "TCKN_004", MektupTipEnum.HAKEDIS_DEVIR_MEKTUPLARI
        );

        // Assert
        verify(mektupService).kepAdresiOlanIhracatcilaraHakedisDevirMektuplariGonder(any());
    }

    @Test
    void sendIhracatciMektupMailRouter_GecersizMektupTipi() {
        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.sendIhracatciMektupMailRouter(
                KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005", null
            )
        );
    }

    @Test
    void sendIhracatciMektupMailRouter_NakitKontrolException() {
        // Arrange
        when(kararIslemleriService.getKararByKararNoAndSube(any(), any())).thenReturn(null);

        // Act & Assert
        assertThrows(ValidationException.class,
            () -> mektupService.sendIhracatciMektupMailRouter(
                KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_006", LocalDate.now(), "VKN_006", "TCKN_006", MektupTipEnum.ODEME_MEKTUPLARI
            )
        );
    }

    @Test
    void sendIhracatciMektupMailRouter_ParametreKontrolException() {
        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.sendIhracatciMektupMailRouter(
                null, null, null, "KARAR_007", null, "VKN_007", "TCKN_007", MektupTipEnum.ODEME_MEKTUPLARI
            )
        );
    }
	
	    @Test
    void mektupYazdir_DavetMektubuIcinPdfOlusturur() throws Exception {
        // Arrange
        LocalDate odemeTarihi = LocalDate.now();
        when(kullaniciBilgileriService.getKullaniciSubeId()).thenReturn("ANKARA");
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        ExportedFile file = mektupService.mektupYazdir(
            KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_001", odemeTarihi, "VKN_001", "TCKN_001", MektupTipEnum.IHRACATCI_DAVET_MEKTUPLARI
        );

        // Assert
        assertNotNull(file);
        verify(kararIslemleriService).getKararByKararNoAndSube(anyString(), anyString());
        verify(pikurIslemService).xmlYukle(eq("print/IHRACATCIDAVETMEKTUP.xml"));
    }

    @Test
    void mektupYazdir_OdemeMektubuMilatSonrasi() throws Exception {
        // Arrange
        LocalDate odemeTarihi = LocalDate.of(2025, 2, 1); // Milat tarihi: 20/01/2025
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        ExportedFile file = mektupService.mektupYazdir(
            KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_002", odemeTarihi, "VKN_002", "TCKN_002", MektupTipEnum.ODEME_MEKTUPLARI
        );

        // Assert
        assertTrue(file.getFileName().contains("Ödeme Mektupları"));
        verify(mektupService).ihracatciOdemeMektuplariniCikar(any(), anyInt(), anyInt(), anyString(), any(), any(), any());
    }

    @Test
    void mektupYazdir_OdemeMektubuMilatOncesi() throws Exception {
        // Arrange
        LocalDate odemeTarihi = LocalDate.of(2024, 12, 31); // Milat tarihinden önce
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        ExportedFile file = mektupService.mektupYazdir(
            KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_003", odemeTarihi, "VKN_003", "TCKN_003", MektupTipEnum.ODEME_MEKTUPLARI
        );

        // Assert
        verify(mektupService).ihracatciOdemeMektuplariniCikarEski(any(), anyInt(), anyInt(), anyString(), any(), any(), any());
    }

    @Test
    void mektupYazdir_HakedisDevirMektubuOdemeTarihiNull() throws Exception {
        // Arrange
        MuhasebeBilgiDTO muhasebeBilgi = new MuhasebeBilgiDTO();
        muhasebeBilgi.setMuhasebeTarih(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        when(muhasebeClientService.loadMuhasebeBilgi(anyString())).thenReturn(muhasebeBilgi);
        when(pikurIslemService.xmlYukle(anyString())).thenReturn(new PikurDocument());
        when(pikurIslemService.pdfDocOlustur(any(), any(), any(), any())).thenReturn(new ByteArrayOutputStream());

        // Act
        ExportedFile file = mektupService.mektupYazdir(
            KararTipiEnum.TARIMSAL, 1, 2023, null, null, "VKN_004", "TCKN_004", MektupTipEnum.HAKEDIS_DEVIR_MEKTUPLARI
        );

        // Assert
        verify(muhasebeClientService).loadMuhasebeBilgi(eq("061"));
        assertNotNull(file);
    }

    @Test
    void mektupYazdir_GecersizMektupTipi() {
        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.mektupYazdir(
                KararTipiEnum.TARIMSAL, 1, 2023, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005", null
            )
        );
    }

    @Test
    void mektupYazdir_ParametreKontrolHatasi() {
        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.mektupYazdir(
                null, null, null, "KARAR_006", null, "VKN_006", "TCKN_006", MektupTipEnum.ODEME_MEKTUPLARI
            )
        );
    }
	
	    @Test
    void hakedisDevirMektubuCikar_HakedisYoksaException() {
        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.hakedisDevirMektubuCikar(new ServisTaslak(), Collections.emptyList())
        );
    }

    @Test
    void hakedisDevirMektubuCikar_AnkaraSubesiIcinTahhutSatirBos() throws Exception {
        // Arrange
        Hakedis hakedis = createHakedis("ANKARA", 75, false);
        when(hakedisIslemleriService.getHakedisById(any())).thenReturn(hakedis);

        // Act
        List<DocGrupVeri> result = mektupService.hakedisDevirMektubuCikar(
            new ServisTaslak(), List.of(hakedis, createDevirHakedis())
        );

        // Assert
        DocGrupVeri detay = result.get(0);
        assertEquals(" ", detay.getAlanVeri("TAAHHUTSATIR"));
    }

    @Test
    void hakedisDevirMektubuCikar_DigerSubelerIcinTahhutSatirDolu() throws Exception {
        // Arrange
        Hakedis hakedis = createHakedis("ISTANBUL", 120, true);
        when(hakedisIslemleriService.getHakedisById(any())).thenReturn(hakedis);

        // Act
        List<DocGrupVeri> result = mektupService.hakedisDevirMektubuCikar(
            new ServisTaslak(), List.of(hakedis, createDevirHakedis())
        );

        // Assert
        DocGrupVeri detay = result.get(0);
        assertEquals("İADELİ-TAAHHÜTLÜ", detay.getAlanVeri("TAAHHUTSATIR"));
    }

    @Test
    void hakedisDevirMektubuCikar_AdresParcalamaKontrolu() throws Exception {
        // Arrange
        String address = "A".repeat(150);
        Hakedis hakedis = createHakedis("IZMIR", address, 3);
        when(hakedisIslemleriService.getHakedisById(any())).thenReturn(hakedis);

        // Act
        List<DocGrupVeri> result = mektupService.hakedisDevirMektubuCikar(
            new ServisTaslak(), List.of(hakedis)
        );

        // Assert
        DocGrupVeri detay = result.get(0);
        assertEquals(address.substring(0,50), detay.getAlanVeri("IHRACATCIADRES1"));
        assertEquals(address.substring(50,100), detay.getAlanVeri("IHRACATCIADRES2"));
        assertEquals(address.substring(100), detay.getAlanVeri("IHRACATCIADRES3"));
    }

    @Test
    void hakedisDevirMektubuCikar_OgmServisiIcinSubeAdi() throws Exception {
        // Arrange
        ServisTaslak servis = new ServisTaslak();
        servis.setOgmServisiMi(true);
        Hakedis hakedis = createHakedis("ANKARA", 50, false);

        // Act
        List<DocGrupVeri> result = mektupService.hakedisDevirMektubuCikar(servis, List.of(hakedis));

        // Assert
        assertEquals("OPERASYON GENEL MÜDÜRLÜĞÜ", result.get(0).getAlanVeri("TCMBSUBEADI"));
    }

    @Test
    void hakedisDevirMektubuCikar_YeniSayfaEklemeKontrolu() throws Exception {
        // Arrange
        Hakedis hakedis = createHakedis("ISTANBUL", 80, true);
        when(hakedisIslemleriService.getHakedisById(any())).thenReturn(hakedis);

        // Act
        List<DocGrupVeri> result = mektupService.hakedisDevirMektubuCikar(
            new ServisTaslak(), List.of(hakedis, createDevirHakedis())
        );

        // Assert
        assertTrue(result.get(result.size()-1) instanceof YeniSayfaVeri);
    }

    // Yardımcı Metodlar
    private Hakedis createHakedis(String subeId, int addressLength, boolean isDevir) {
        Karar karar = new Karar();
        karar.setSubeId(subeId);
        karar.setKararNo("KARAR_" + subeId);

        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setAd("Test İhracatçı");
        ihracatci.setAdres("A".repeat(addressLength));
        ihracatci.setSubeId(subeId);

        Hakedis hakedis = new Hakedis();
        hakedis.setKarar(karar);
        hakedis.setIhracatci(ihracatci);
        hakedis.setDuzenlemeTarihi(new Date());
        if(isDevir) hakedis.setDevredenHakedisId(1L);

        return hakedis;
    }

    private Hakedis createDevirHakedis() {
        Hakedis devirHakedis = createHakedis("ANKARA", 60, false);
        devirHakedis.setTutarTl(new BigDecimal("5000.00"));
        return devirHakedis;
    }
	
	@Test
    void getOdemeMektupBorcBilgileri_BosEftListesi() throws Exception {
        // Arrange
        Provizyon provizyon = new Provizyon();
        provizyon.setId(1L);
        when(eftBilgisiYonetimRepository.getEftBilgiYonetimsByProvizyonId(any())).thenReturn(Collections.emptyList());

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, false);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getOdemeMektupBorcBilgileri_SadeceBorcYazdirVeEslesmeyenBorc() throws Exception {
        // Arrange
        EftBilgiYonetim eftBilgi = createEftBilgiYonetim("123", "01/01/2023");
        Provizyon provizyon = createProvizyonWithBorcBilgi(2L); // Farklı borcId

        when(eftBilgisiYonetimRepository.getEftBilgiYonetimsByProvizyonId(any())).thenReturn(List.of(eftBilgi));

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, true);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getOdemeMektupBorcBilgileri_KasTarihiNullIseAtla() throws Exception {
        // Arrange
        EftBilgiYonetim eftBilgi = createEftBilgiYonetim("456", null);
        Provizyon provizyon = createProvizyonWithBorcBilgi(456L);

        when(eftBilgisiYonetimRepository.getEftBilgiYonetimsByProvizyonId(any())).thenReturn(List.of(eftBilgi));

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, false);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void getOdemeMektupBorcBilgileri_GecerliVerilerleDocGrupOlusturur() throws Exception {
        // Arrange
        EftBilgiYonetim eftBilgi = createEftBilgiYonetim("789", "15/05/2023");
        Provizyon provizyon = createProvizyonWithBorcBilgi(789L);
        MusteriHesabaOdeme eftMesaj = createEftMesaj();

        when(eftBilgisiYonetimRepository.getEftBilgiYonetimsByProvizyonId(any())).thenReturn(List.of(eftBilgi));
        when(eftClientService.getGunlukKasMesajBySorguNoAndOdemeTarihi(any(), any())).thenReturn(eftMesaj);
        when(bankaSubeService.getBankaForBankaKodu(any())).thenReturn(new Banka("Test Bankası"));

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, false);

        // Assert
        DocGrupVeri doc = result.get(0);
        assertEquals("BORCBILGILERI", doc.getGrupAd());
        assertEquals("Ali Veli", doc.getAlanVeri("BORCALICISI"));
        assertEquals(new BigDecimal("1500.50"), doc.getAlanVeri("BORCTUTARI"));
        assertEquals("789-Test Bankası", doc.getAlanVeri("EFTBANKAKODUADI")); // 30 karakter kontrolü
    }

    @Test
    void getOdemeMektupBorcBilgileri_BankaAdi30KarakterUzunsaKisalt() throws Exception {
        // Arrange
        String uzunBankaAdi = "A".repeat(40);
        EftBilgiYonetim eftBilgi = createEftBilgiYonetim("999", "20/12/2023");
        Provizyon provizyon = createProvizyonWithBorcBilgi(999L);
        MusteriHesabaOdeme eftMesaj = createEftMesaj();

        when(eftBilgisiYonetimRepository.getEftBilgiYonetimsByProvizyonId(any())).thenReturn(List.of(eftBilgi));
        when(eftClientService.getGunlukKasMesajBySorguNoAndOdemeTarihi(any(), any())).thenReturn(eftMesaj);
        when(bankaSubeService.getBankaForBankaKodu(any())).thenReturn(new Banka(uzunBankaAdi));

        // Act
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, false);

        // Assert
        String bankaAdi = (String) result.get(0).getAlanVeri("EFTBANKAKODUADI");
        assertEquals(30, bankaAdi.length());
    }

    // Yardımcı Metodlar
    private EftBilgiYonetim createEftBilgiYonetim(String sorguNo, String kasTarih) {
        EftBilgiYonetim eftBilgi = new EftBilgiYonetim();
        eftBilgi.setBorcId(BigDecimal.valueOf(789));
        eftBilgi.setKasSorguNo(sorguNo);
        eftBilgi.setKasTarih(kasTarih);
        return eftBilgi;
    }

    private Provizyon createProvizyonWithBorcBilgi(Long borcId) {
        BorcBilgi borcBilgi = new BorcBilgi();
        borcBilgi.setId(borcId);

        Provizyon provizyon = new Provizyon();
        provizyon.setId(1L);
        provizyon.setBorcBilgiList(List.of(borcBilgi));
        return provizyon;
    }

    private MusteriHesabaOdeme createEftMesaj() {
        MusteriHesabaOdeme mesaj = new MusteriHesabaOdeme();
        mesaj.setAlAd("Ali Veli");
        mesaj.setTtr("1.500,50");
        mesaj.setAlKK("789");
        mesaj.setAlHesN("TR123456");
        mesaj.setTrh("15/05/2023");
        mesaj.setSN("EFT_123");
        mesaj.setAcklm("Test açıklama");
        return mesaj;
    }
	
	    @Test
    void odemeMektubuCikarEski_BosListe() {
        // Arrange
        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikarEski(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_001", LocalDate.now(), "VKN_001", "TCKN_001"
            )
        );
    }

    @Test
    void odemeMektubuCikarEski_NakitDegilseAtla() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.getKarar().setNakitKarar(false);
        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikarEski(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_002", LocalDate.now(), "VKN_002", "TCKN_002"
            )
        );
    }

    @Test
    void odemeMektubuCikarEski_EmailVarIseAtla() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = createProvizyonArsiv(true, "test@tcmb.gov.tr");
        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));

        // Act & Assert
        assertThrows(GecersizVeriException.class,
            () -> mektupService.ihracatciOdemeMektuplariniCikarEski(
                KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_003", LocalDate.now(), "VKN_003", "TCKN_003"
            )
        );
    }

    @Test
    void odemeMektubuCikarEski_GecerliVeri() throws Exception {
        // Arrange
        ProvizyonArsiv provizyon = createProvizyonArsiv(true, "");
        when(provizyonIslemleriService.listProvizyonArsiv(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(List.of(provizyon));
        when(borcBilgiService.getOdenmisBorclarByProvizyonArsiv(any())).thenReturn(List.of(new BorcBilgiArsiv()));
        when(mektupService.getOdemeMektupDetayByProvizyon(any())).thenReturn(List.of(new DocGrupVeri()));

        // Act
        DocVeri result = mektupService.ihracatciOdemeMektuplariniCikarEski(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_004", LocalDate.now(), "VKN_004", "TCKN_004"
        );

        // Assert
        assertTrue(result.getDocGrupSayi() > 0);
    }

    // Yardımcı Metod
    private ProvizyonArsiv createProvizyonArsiv(boolean nakitKarar, String email) {
        Karar karar = new Karar();
        karar.setNakitKarar(nakitKarar);
        Ihracatci ihracatci = new Ihracatci();
        ihracatci.setEmail(email);
        ProvizyonArsiv provizyon = new ProvizyonArsiv();
        provizyon.setKarar(karar);
        provizyon.setIhracatci(ihracatci);
        return provizyon;
    }
	
	@Test
void davetMektubuCikar_IdareMerkeziKullanicisiHata() {
    // Arrange
    when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(true);

    // Act & Assert
    assertThrows(ValidationException.class,
        () -> mektupService.ihracatciDavetMektuplariniCikar(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_005", LocalDate.now(), "VKN_005", "TCKN_005", "ANKARA"
        )
    );
}

@Test
void davetMektubuCikar_BosProvizyonListesi() {
    // Arrange
    when(kullaniciBilgileriService.idareMerkeziKullanicisiMi()).thenReturn(false);
    when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(GecersizVeriException.class,
        () -> mektupService.ihracatciDavetMektuplariniCikar(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_006", LocalDate.now(), "VKN_006", "TCKN_006", "ISTANBUL"
        )
    );
}

@Test
void davetMektubuCikar_MahsupKararDegilseAtla() throws Exception {
    // Arrange
    Provizyon provizyon = new Provizyon();
    provizyon.getKarar().setMahsupKarar(false);
    when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(provizyon));

    // Act & Assert
    assertThrows(GecersizVeriException.class,
        () -> mektupService.ihracatciDavetMektuplariniCikar(
            KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_007", LocalDate.now(), "VKN_007", "TCKN_007", "IZMIR"
        )
    );
}

@Test
void davetMektubuCikar_GecerliVeri() throws Exception {
    // Arrange
    Provizyon provizyon = createProvizyon(true);
    when(provizyonIslemleriService.listProvizyon(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(List.of(provizyon));
    when(mektupService.getDavetMektupDetayByProvizyon(any())).thenReturn(List.of(new DocGrupVeri()));

    // Act
    DocVeri result = mektupService.ihracatciDavetMektuplariniCikar(
        KararTipiEnum.TARIMSAL, 2023, 1, "KARAR_008", LocalDate.now(), "VKN_008", "TCKN_008", "ANKARA"
    );

    // Assert
    assertTrue(result.getDocGrupSayi() > 0);
}

// Yardımcı Metod
private Provizyon createProvizyon(boolean mahsupKarar) {
    Karar karar = new Karar();
    karar.setMahsupKarar(mahsupKarar);
    Provizyon provizyon = new Provizyon();
    provizyon.setKarar(karar);
    return provizyon;
}



    private Provizyon createProvizyonMock(String address, String kararNo, BigDecimal tutar) {
        Provizyon provizyon = mock(Provizyon.class);
        Ihracatci ihracatci = mock(Ihracatci.class);
        Karar karar = mock(Karar.class);
        
        when(provizyon.getIhracatci()).thenReturn(ihracatci);
        when(provizyon.getKarar()).thenReturn(karar);
        when(provizyon.getHakedisTutari()).thenReturn(tutar);
        
        when(ihracatci.getAd()).thenReturn("Test İhracatçı");
        when(ihracatci.getAdres()).thenReturn(address);
        
        when(karar.getKararNo()).thenReturn(kararNo);
        when(karar.getSubeId()).thenReturn(123L);
        
        return provizyon;
    }

    @Test
    void borclarVarIse_GerekliGruplarEklenir() throws Exception {
        // Arrange
        Provizyon provizyon = createProvizyonMock(StringUtils.repeat("a", 150), "KARAR123", new BigDecimal("1000.50"));
        when(kullaniciBilgileriService.getSubeKodu(anyLong())).thenReturn("TR1");
        
        List<DocGrupVeri> mockBorclar = Collections.singletonList(new DocGrupVeri());
        doReturn(mockBorclar).when(davetMektupService).getOdemeMektupBorcBilgileri(provizyon, true);

        // Act
        List<DocGrupVeri> result = davetMektupService.getDavetMektupDetayByProvizyon(provizyon);

        // Assert
        assertThat(result)
            .extracting(DocGrupVeri::getGrupAd)
            .containsExactly("DETAY", "BORCBILGILENDIRMEYAZISI", "EKBILGILENDIRMEYAZISI", null);
    }

    @Test
    void borclarYokIse_SadeceTemelGruplarEklenir() throws Exception {
        // Arrange
        Provizyon provizyon = createProvizyonMock("Kısa Adres", "KARAR456", BigDecimal.ZERO);
        when(kullaniciBilgileriService.getSubeKodu(anyLong())).thenReturn("TR2");
        doReturn(Collections.emptyList()).when(davetMektupService).getOdemeMektupBorcBilgileri(provizyon, true);

        // Act
        List<DocGrupVeri> result = davetMektupService.getDavetMektupDetayByProvizyon(provizyon);

        // Assert
        assertThat(result)
            .extracting(DocGrupVeri::getGrupAd)
            .containsExactly("DETAY", "EKBILGILENDIRMEYAZISI");
    }

    @Test
    void adresBolmeIslemi_CesitliSenaryolar() {
        // Arrange & Act
        DocGrupVeri result1 = testAddressSplit(50);
        DocGrupVeri result2 = testAddressSplit(100);
        DocGrupVeri result3 = testAddressSplit(150);

        // Assert
        assertThat(result1.getAlanVeri("IHRACATCIADRES2")).isEqualTo("");
        assertThat(result2.getAlanVeri("IHRACATCIADRES2")).isEqualTo("");
        assertThat(result3.getAlanVeri("IHRACATCIADRES2")).hasSize(50);
    }

    private DocGrupVeri testAddressSplit(int length) {
        Provizyon provizyon = createProvizyonMock(StringUtils.repeat("a", length), "TEST", BigDecimal.ONE);
        when(kullaniciBilgileriService.getSubeKodu(anyLong())).thenReturn("TR3");
        
        List<DocGrupVeri> result = davetMektupService.getDavetMektupDetayByProvizyon(provizyon);
        return findGrup(result, "DETAY");
    }

    private DocGrupVeri findGrup(List<DocGrupVeri> veriler, String grupAd) {
        return veriler.stream()
            .filter(g -> grupAd.equals(g.getGrupAd()))
            .findFirst()
            .orElseThrow();
    }
	
	@Test
    void testGetOdemeMektupBorcBilgileri_ShouldReturnEmptyList_WhenNoBorcFound() throws Exception {
        ProvizyonArsiv provizyon = mock(ProvizyonArsiv.class);
        when(provizyon.getId()).thenReturn(1L);
        when(eftBilgisiYonetimArsivRepository.getEftBilgiYonetimArsivsByProvizyonId(any(BigDecimal.class)))
                .thenReturn(Collections.emptyList());
        
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, false);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetOdemeMektupBorcBilgileri_ShouldReturnValidData() throws Exception {
        ProvizyonArsiv provizyon = mock(ProvizyonArsiv.class);
        when(provizyon.getId()).thenReturn(1L);
        
        EftBilgiYonetimArsiv eftBilgi = mock(EftBilgiYonetimArsiv.class);
        when(eftBilgi.getBorcId()).thenReturn(BigDecimal.valueOf(101));
        when(eftBilgi.getKasTarih()).thenReturn("01/01/2025");
        when(eftBilgi.getKasSorguNo()).thenReturn("123456");
        
        when(eftBilgisiYonetimArsivRepository.getEftBilgiYonetimArsivsByProvizyonId(any(BigDecimal.class)))
                .thenReturn(Collections.singletonList(eftBilgi));
        
        MusteriHesabaOdeme eftMesaj = mock(MusteriHesabaOdeme.class);
        when(eftMesaj.getAlAd()).thenReturn("Alıcı Adı");
        when(eftMesaj.getTtr()).thenReturn("1000,00");
        when(eftMesaj.getAlKK()).thenReturn("BANKA001");
        when(eftMesaj.getAlHesN()).thenReturn("TR123456789");
        when(eftMesaj.getTrh()).thenReturn("01/01/2025");
        when(eftMesaj.getSN()).thenReturn("987654");
        when(eftMesaj.getAcklm()).thenReturn("Açıklama");
        
        when(eftClientService.getGunlukKasMesajBySorguNoAndOdemeTarihi(anyString(), any(LocalDate.class)))
                .thenReturn(eftMesaj);
        
        List<DocGrupVeri> result = mektupService.getOdemeMektupBorcBilgileri(provizyon, false);
        
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Alıcı Adı", result.get(0).getAlanVeri("BORCALICISI"));
    }
	
	@Test
    void testGetProvizyonArsivToplamTutar_ShouldReturnZero_WhenNoEftBilgiFound() {
        ProvizyonArsiv provizyon = mock(ProvizyonArsiv.class);
        when(provizyon.getId()).thenReturn(1L);
        when(eftBilgisiYonetimArsivRepository.getEftBilgiYonetimArsivsByProvizyonId(any(BigDecimal.class)))
                .thenReturn(Collections.emptyList());
        
        BigDecimal result = mektupService.getProvizyonArsivToplamTutar(provizyon, false);
        
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testGetProvizyonArsivToplamTutar_ShouldReturnCorrectSum() {
        ProvizyonArsiv provizyon = mock(ProvizyonArsiv.class);
        when(provizyon.getId()).thenReturn(1L);
        
        EftBilgiYonetimArsiv eftBilgi1 = mock(EftBilgiYonetimArsiv.class);
        when(eftBilgi1.getBorcId()).thenReturn(BigDecimal.valueOf(101));
        when(eftBilgi1.getTutar()).thenReturn(BigDecimal.valueOf(500));
        
        EftBilgiYonetimArsiv eftBilgi2 = mock(EftBilgiYonetimArsiv.class);
        when(eftBilgi2.getBorcId()).thenReturn(BigDecimal.valueOf(102));
        when(eftBilgi2.getTutar()).thenReturn(BigDecimal.valueOf(700));
        
        when(eftBilgisiYonetimArsivRepository.getEftBilgiYonetimArsivsByProvizyonId(any(BigDecimal.class)))
                .thenReturn(Arrays.asList(eftBilgi1, eftBilgi2));
        
        BigDecimal result = mektupService.getProvizyonArsivToplamTutar(provizyon, false);
        
        assertEquals(BigDecimal.valueOf(1200), result);
    }
	
	
	 @Test
    void testGetHakedisDevirMektuplariniTopluAl_ShouldThrowException_WhenNoExporters() {
        LocalDate testDate = LocalDate.now();
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(testDate)).thenReturn(Collections.emptyList());
        
        Exception exception = assertThrows(GecersizVeriException.class, 
            () -> mektupService.getHakedisDevirMektuplariniTopluAl(testDate));
        
        assertEquals("İlgili tarihte deviri olan ihracatçı bulunamadı", exception.getMessage());
    }

    @Test
    void testGetHakedisDevirMektuplariniTopluAl_ShouldReturnValidData() throws Exception {
        LocalDate testDate = LocalDate.now();
        Ihracatci ihracatci = mock(Ihracatci.class);
        when(hakedisIslemleriService.getDeviriOlanIhracatcilar(testDate)).thenReturn(Collections.singletonList(ihracatci));
        when(kullaniciBilgileriService.getKullaniciSubeKodu()).thenReturn("IST");
        
        ServisTaslak servis = mock(ServisTaslak.class);
        when(muhasebeClientService.findServis(anyInt(), any())).thenReturn(servis);
        
        List<Hakedis> hakedisList = Collections.singletonList(mock(Hakedis.class));
        when(hakedisIslemleriService.getDevirdenOlusmusHakedisler(ihracatci, testDate)).thenReturn(hakedisList);
        
        List<DocGrupVeri> docGrupVeriList = Collections.singletonList(mock(DocGrupVeri.class));
        when(mektupService.hakedisDevirMektubuCikar(servis, hakedisList)).thenReturn(docGrupVeriList);
        
        DocVeri result = mektupService.getHakedisDevirMektuplariniTopluAl(testDate);
        
        assertNotNull(result);
        assertFalse(result.getDocGrupList().isEmpty());
    }
}
