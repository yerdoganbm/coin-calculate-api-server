
import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WordToPdfBatchGenerator {

    public static void main(String[] args) throws Exception {
        File inputFile = new File("template.docx"); // Şablon Word dosyası
        String fileNameWithoutExt = inputFile.getName().replaceFirst("[.][^.]+$", "");
        String basePath = inputFile.getParent() != null ? inputFile.getParent() : ".";

        // Word şablonunu belleğe al (tek sefer)
        byte[] templateBytes = Files.readAllBytes(inputFile.toPath());

        // 5000 örnek veri üret
        List<List<String>> dataList = new ArrayList<>();
        for (int i = 1; i <= 5000; i++) {
            List<String> veri = List.of(
                "Karar No: K-" + i,
                "Devreden Firma: Firma A, Firma B, Firma C",
                "Tutar: " + (1000 + i) + " TL",
                "ALICI: Ali Veli",
                "BANKA: Banka X",
                "HESAP: TR00 0000 0000 0000",
                "AÇIKLAMA: Ödeme açıklaması " + i
            );
            dataList.add(veri);
        }

        long start = System.currentTimeMillis();

        // Her veri için belge oluştur
        int index = 1;
        for (List<String> fields : dataList) {
            try (InputStream in = new ByteArrayInputStream(templateBytes)) {
                XWPFDocument doc = new XWPFDocument(in);

                // Tüm %s'leri sırayla doldur
                replaceAllPlaceholders(doc, fields);

                // Geçici .docx dosyasını yaz
                File filledDocx = new File(basePath, fileNameWithoutExt + "_filled_" + index + ".docx");
                try (FileOutputStream out = new FileOutputStream(filledDocx)) {
                    doc.write(out);
                }

                // PDF'e çevir
                File outputPdf = new File(basePath, fileNameWithoutExt + "_" + index + ".pdf");
                WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(filledDocx);
                try (FileOutputStream outPdf = new FileOutputStream(outputPdf)) {
                    Docx4J.toPDF(wordMLPackage, outPdf);
                }

                // Temizleme (isteğe bağlı)
                filledDocx.delete();
                index++;
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("Tamamlandı. Süre: " + (end - start) + " ms");
    }

    // %s placeholderlarını sırayla doldurur
    private static void replaceAllPlaceholders(XWPFDocument doc, List<String> values) {
        int valueIndex = 0;
        for (XWPFParagraph p : doc.getParagraphs()) {
            for (XWPFRun r : p.getRuns()) {
                String text = r.getText(0);
                if (text != null && text.contains("%s") && valueIndex < values.size()) {
                    String replaced = text.replaceFirst("%s", values.get(valueIndex++));
                    r.setText(replaced, 0);
                }
            }
        }
    }
}

       
       
       
       
       
       Aşağıda unvanı yer alan firmanın ilgili ihracatçı birliğine yaptığı başvurunun değerlendirilmesi sonucunda firmanıza tanınan hak edişe ilişkin belge hazırlanmış bulunmaktadır. Bu hak ediş belgesi, firmanız yetkili temsilcisine imza sirküleri ve T.C. Kimlik Kartının ibrazı kaydıyla teslim edilecektir. 
        
        İlgili Kararda belirtilen kurumlara olan borçlarınızın mahsuben ödenmesini teminen bu kurum ünitelerinden alınacak borç döküm formuyla birlikte Şubemize başvurmanız gerekmektedir.  Sosyal Güvenlik Kurumuna olan borçlar çevrimiçi (online) sorgulanacağı için borç döküm formunun ibrazına gerek bulunmamaktadır.

         Sosyal Güvenlik Kurumuna olan borçların ödenmesi sürecinde kullanılan bilişim altyapısı nedeniyle ödeme işlemi başvuru günü içerisinde tamamlanamamaktadır. Bu nedenle, söz konusu kuruma olan borçlara ilişkin mahsup talebi başvurularında bu durumun göz önünde bulundurulması gerekmektedir.

         Borç ödemesini müteakip borç tutarının hak ediş belgesi bakiyesinden düşülüp onaylanmasından sonra hak ediş belgesi tarafınıza teslim edilecektir. Hak ediş belgesinin bakiyesinin kalmaması halinde belge, Şubemizce alıkonacaktır. Aynı Karara ilişkin bir önceki hak ediş belgesi iade edilmeden yeni belge verilmeyecektir.

         Ayrıca, hak ediş belgesinin kaybolduğunun bildirilmesi halinde yenisi düzenlenecek olup kaybolan hak ediş belgesinin bulunması halinde Şubemize ibraz etmeniz gerekmektedir. Kaybolan hak ediş belgesi üzerinden herhangi bir hak talebinde bulunulamaz.

Bilginizi rica ederiz.


 
 
 
 
 
 
 
 
 
 
 DocVeri docVeri = new DocVeri();
                docVeri.addGrupVeriAll(veriler);

                PikurDocument pd = pikurIslemService.xmlYukle(ihracatciDevirMektubuPikurXMLPath);
                ByteArrayOutputStream baos = pikurIslemService.pdfDocOlustur(pd, docVeri, PageSize.A4, OrientationRequested.PORTRAIT);
                ExportedFile file = outputAsPDF(baos, this.handleExportFileName(odemeTarihi, MektupTipEnum.HAKEDIS_DEVIR_MEKTUPLARI));



<?xml version="1.0" encoding="ISO-8859-9"?>
<tns:pikur xmlns:tns="http://submuhm.tcmb.gov.tr/Pikur"
           charPerInch="15" linePerInch="8" sayfaSatirSayi="100">

    <gruplar>
        <grup ad="DETAY">
            <pikurAlan ad="IHRACATCIADI" satir="8" sutun="10">
                <dinamikAlan uzunluk="70" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="IHRACATCIADRES1" satir="9" sutun="10">
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="IHRACATCIADRES2" satir="10" sutun="10">
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="IHRACATCIADRES3" satir="11" sutun="10">
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>

            <pikurAlan ad="" satir="19" sutun="10">
                <statikAlan uzunluk="80" 
                    metin="Aşağıda unvanı yer alan firmanın ilgili ihracatçı birliğine yaptığı başvuru-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="20" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="nun değerlendirilmesi sonucunda firmanıza tanınan hak edişe ilişkin belge hazır-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="21" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="lanmış bulunmaktadır. Bu hak ediş belgesi, firmanız yetkili temsilcisine imza sirkü-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="22" sutun="5">
                <statikAlan uzunluk="59" 
                    metin="leri ve T.C. Kimlik Kartının ibrazı kaydıyla teslim edilecektir."/>
            </pikurAlan>

            <pikurAlan ad="" satir="24" sutun="10">
                <statikAlan uzunluk="80" 
                    metin="İlgili Kararda belirtilen kurumlara olan borçlarınızın mahsuben ödenmesini temin-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="25" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="en bu kurum ünitelerinden alınacak borç döküm formuyla birlikte Şubemize başvur-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="26" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="manız gerekmektedir. Sosyal Güvenlik Kurumuna olan borçlar çevrimiçi (online)"/>
            </pikurAlan>
            <pikurAlan ad="" satir="27" sutun="5">
                <statikAlan uzunluk="70" 
                    metin="sorgulanacağı için borç döküm formunun ibrazına gerek bulunmamaktadır."/>
            </pikurAlan>

            <pikurAlan ad="" satir="28" sutun="10">
                <statikAlan uzunluk="80" 
                    metin="Sosyal Güvenlik Kurumuna olan borçların ödenmesi sürecinde kullanılan bilişim"/>
            </pikurAlan>
            <pikurAlan ad="" satir="29" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="altyapısı nedeniyle ödeme işlemi başvuru günü içerisinde tamamlanamamaktadır."/>
            </pikurAlan>
            <pikurAlan ad="" satir="30" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="Bu nedenle, söz konusu kuruma olan borçlara ilişkin mahsup talebi başvurularında"/>
            </pikurAlan>
            <pikurAlan ad="" satir="31" sutun="5">
                <statikAlan uzunluk="50" 
                    metin="bu durumun göz önünde bulundurulması gerekmektedir."/>
            </pikurAlan>

            <pikurAlan ad="" satir="32" sutun="10">
                <statikAlan uzunluk="80" 
                    metin="Borç ödemesine müteakip borç tutarının hak ediş belgesi bakiyesinden düşülüp"/>
            </pikurAlan>
            <pikurAlan ad="" satir="33" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="onaylanmasından sonra hak ediş belgesi tarafınıza teslim edilecektir. Hak ediş"/>
            </pikurAlan>
            <pikurAlan ad="" satir="34" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="belgesinin bakiyesinin kalmaması halinde belge Şubemizce alıkonacaktır. Aynı Kara-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="35" sutun="5">
                <statikAlan uzunluk="90" 
                    metin="ra ilişkin bir önceki hak ediş belgesi iade edilmeden yeni belge verilmeyecektir."/>
            </pikurAlan>

            <pikurAlan ad="" satir="36" sutun="10">
                <statikAlan uzunluk="80" 
                    metin="Ayrıca, hak ediş belgesinin kaybolduğunun bildirilmesi halinde yenisi düzenlene-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="37" sutun="5">
                <statikAlan uzunluk="80" 
                    metin="cek olup kaybolan hak ediş belgesinin bulunması halinde Şubemize ibraz etmeniz"/>
            </pikurAlan>
            <pikurAlan ad="" satir="38" sutun="5">
                <statikAlan uzunluk="90" 
                    metin="gerekmektedir. Kaybolan hak ediş belgesi üzerinden herhangi bir hak talebinde"/>
            </pikurAlan>
            <pikurAlan ad="" satir="39" sutun="5">
                <statikAlan uzunluk="40" 
                    metin="bulunulamaz."/>
            </pikurAlan>

            <pikurAlan ad="" satir="41" sutun="10">
                <statikAlan uzunluk="30" 
                    metin="Bilginizi rica ederiz."/>
            </pikurAlan>

            <pikurAlan ad="" satir="43" sutun="46">
                <statikAlan uzunluk="33" 
                    metin="TÜRKİYE CUMHURİYET MERKEZ BANKASI"/>
            </pikurAlan>
            <pikurAlan ad="TCMBSUBEADI" satir="44" sutun="46">
                <dinamikAlan uzunluk="33" tip="string" align="center"/>
            </pikurAlan>

            <pikurAlan ad="" satir="46" sutun="5">
                <statikAlan uzunluk="50" 
                    metin="NOT : Bu yazı bilgi içindir."/>
            </pikurAlan>
            <pikurAlan ad="" satir="47" sutun="11">
                <statikAlan uzunluk="40" 
                    metin="Bankamızca tarafınıza bir ödeme yükümlülüğü ifade etmez."/>
            </pikurAlan>

            <pikurAlan ad="" satir="49" sutun="5">
                <statikAlan uzunluk="20" 
                    metin="DEVİR BİLGİLERİ"/>
            </pikurAlan>
        </grup>

        <grup ad="DEVIRBILGILERI">
            <pikurAlan ad="KARAR" satir="2" sutun="5">
                <statikAlan uzunluk="9" metin="Karar No:"/>
                <dinamikAlan uzunluk="25" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="DEVREDILENTUTAR" satir="2" sutun="40">
                <statikAlan uzunluk="12" metin="Tutar (TL): "/>
                <dinamikAlan uzunluk="24" tip="number" 
                              format="#,###,###,###,###,##0.00" align="left"/>
            </pikurAlan>
            <pikurAlan ad="DEVREDENIHRACATCI" satir="3" sutun="5">
                <statikAlan uzunluk="17" metin="Devreden Firma:"/>
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
        </grup>
    </gruplar>
</tns:pikur>
yunus






<?xml version="1.0" encoding="ISO-8859-9"?>
<tns:pikur xmlns:tns="http://submuhm.tcmb.gov.tr/Pikur"
           charPerInch="15" linePerInch="8" sayfaSatirSayi="100">

    <gruplar>
        <grup ad="DETAY">
            <!--<pikurAlan ad="" satir="1" sutun="64">
                <statikAlan uzunluk="16"
                    metin="İADELİ-TAAHHÜTLÜ" />
            </pikurAlan>
            <pikurAlan ad="TAAHHUTSATIR" satir="3" sutun="64">
                <dinamikAlan uzunluk="16" tip="string" />
            </pikurAlan>-->
            <pikurAlan ad="IHRACATCIADI" satir="8" sutun="10">
                <dinamikAlan uzunluk="70" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="IHRACATCIADRES1" satir="9" sutun="10">
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="IHRACATCIADRES2" satir="10" sutun="10">
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="IHRACATCIADRES3" satir="11" sutun="10">
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
            <!--<pikurAlan ad="TARIH" satir="17" sutun="64">
                <statikAlan uzunluk="6" metin="Tarih:"/>
                <dinamikAlan uzunluk="10" tip="string"/>
            </pikurAlan>-->
            <pikurAlan ad="" satir="19" sutun="10">
                <statikAlan uzunluk="80"
                            metin="Aşağıda unvanı yer alan firmanın ilgili ihracatçı birliğine yaptığı başvuru-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="20" sutun="5">
                <statikAlan uzunluk="80"
                            metin="nun değerlendirilmesi sonucunda firmanıza tanınan hak edişe ilişkin belge hazır-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="21" sutun="5">
                <statikAlan uzunluk="80"
                            metin="lanmış bulunmaktadır. Bu hak ediş belgesi, firmanız yetkili temsilcisine imza sirkü-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="22" sutun="5">
                <statikAlan uzunluk="59"
                            metin="leri ve T.C. Kimlik Kartının ibrazı kaydıyla teslim edilecektir."/>
            </pikurAlan>
            <pikurAlan ad="" satir="24" sutun="10">
                <statikAlan uzunluk="80"
                            metin="İlgili Kararda belirtilen kurumlara olan borçlarınızın mahsuben ödenmesini temin-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="25" sutun="5">
                <statikAlan uzunluk="80"
                            metin="en bu kurum ünitelerinden alınacak borç döküm formuyla birlikte Şubemize başvur-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="26" sutun="5">
                <statikAlan uzunluk="80"
                            metin="manız gerekmektedir.Sosyal Güvenlik Kurumuna olan olan borçlar çevrimiçi(online) "/>
            </pikurAlan>
            <pikurAlan ad="" satir="27" sutun="5">
                <statikAlan uzunluk="70"
                            metin="sorgulanacağı için borç döküm formunun ibrazına gerek bulunmamaktadır."/>
            </pikurAlan>
            <pikurAlan ad="" satir="28" sutun="10">
                <statikAlan uzunluk="90"
                            metin="Sosyal Güvenlik Kurumuna olan borçların ödenmesi sürecinde kullanılan bilişim "/>
            </pikurAlan>
            <pikurAlan ad="" satir="29" sutun="5">
                <statikAlan uzunluk="80"
                            metin="alt yapısı nedeniyle ödeme işlemi başvuru günü içerisinde tamamlanamamaktadır. "/>
            </pikurAlan>
            <pikurAlan ad="" satir="30" sutun="5">
                <statikAlan uzunluk="80"
                            metin="Bu nedenle, söz konusu kuruma olan borçlara ilişkin mahsup talebi başvurularında bu"/>
            </pikurAlan>
            <pikurAlan ad="" satir="31" sutun="5">
                <statikAlan uzunluk="50" metin="durumun göz önünde bulundurulması gerekmektedir."/>
            </pikurAlan>
            <pikurAlan ad="" satir="32" sutun="10">
                <statikAlan uzunluk="80"
                            metin="Borç ödemesine müteakip borç tutarının hak ediş belgesi bakiyesinden düşülüp"/>
            </pikurAlan>
            <pikurAlan ad="" satir="33" sutun="5">
                <statikAlan uzunluk="80"
                            metin="onaylanmasından sonra hak ediş belgesi tarafınıza teslim edilecektir. Hak ediş"/>
            </pikurAlan>
            <pikurAlan ad="" satir="34" sutun="5">
                <statikAlan uzunluk="80"
                            metin="belgesinin bakiyesinin kalmaması halinde belge Şubemizce alıkonacaktır.Aynı Kara-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="35" sutun="5">
                <statikAlan uzunluk="90"
                            metin="ra ilişkin bir önceki hak ediş belgesi iade edilmeden yeni belge verilmeyecektir."/>
            </pikurAlan>
            <pikurAlan ad="" satir="36" sutun="10">
                <statikAlan uzunluk="80"
                            metin="Ayrıca,hak ediş belgesinin kaybolduğunun bildirilmesi halinde yenisi düzenlene-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="37" sutun="5">
                <statikAlan uzunluk="80"
                            metin="cek olup kaybolan hak ediş belgesinin bulunması halinde Şubemize ibraz etmeniz gerek-"/>
            </pikurAlan>
            <pikurAlan ad="" satir="38" sutun="5">
                <statikAlan uzunluk="90"
                            metin="mektedir.Kaybolan hak ediş belgesi üzerinden herhangi bir hak talebinde bulunulamaz."/>
            </pikurAlan>
            <pikurAlan ad="" satir="39" sutun="10">
                <statikAlan uzunluk="22" metin="Bilginizi rica ederiz."/>
            </pikurAlan>
            <pikurAlan ad="" satir="42" sutun="46">
                <statikAlan uzunluk="33" metin="TÜRKİYE CUMHURİYET MERKEZ BANKASI"/>
            </pikurAlan>
            <pikurAlan ad="TCMBSUBEADI" satir="43" sutun="46">
                <dinamikAlan uzunluk="33" tip="string" align="center"/>
            </pikurAlan>
            <pikurAlan ad="" satir="45" sutun="5">
                <statikAlan uzunluk="50" metin="NOT : Bu yazı bilgi içindir."/>
            </pikurAlan>
            <pikurAlan ad="" satir="46" sutun="11">
                <statikAlan uzunluk="22" metin="Bankamızca tarafınıza"/>
            </pikurAlan>
            <pikurAlan ad="" satir="47" sutun="11">
                <statikAlan uzunluk="40" metin="bir ödeme yükümlülüğü ifade etmez."/>
            </pikurAlan>
            <pikurAlan ad="" satir="51" sutun="5">
                <statikAlan uzunluk="40" metin="DEVİR BİLGİLERİ"/>
            </pikurAlan>
        </grup>
        <grup ad="DEVIRBILGILERI">
            <pikurAlan ad="KARAR" satir="2" sutun="5">
                <statikAlan uzunluk="9" metin="Karar No:"/>
                <dinamikAlan uzunluk="25" tip="string"/>
            </pikurAlan>
            <pikurAlan ad="DEVREDILENTUTAR" satir="2" sutun="40">
                <statikAlan uzunluk="12" metin="Tutar (TL): "/>
                <dinamikAlan uzunluk="24" tip="number" format="#,###,###,###,###,##0.00"
                             align="left"/>
            </pikurAlan>
            <pikurAlan ad="DEVREDENIHRACATCI" satir="3" sutun="5">
                <statikAlan uzunluk="17" metin="Devreden Firma:"/>
                <dinamikAlan uzunluk="50" tip="string"/>
            </pikurAlan>
        </grup>
    </gruplar>
</tns:pikur>
