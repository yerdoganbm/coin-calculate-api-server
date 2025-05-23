import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.Conversion;
import org.docx4j.fonts.IdentityPlusMapper;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class FileProcessingUtil {

    /**
     * Birden fazla DTO'yu alır, hepsini işleyip tek bir birleşik PDF üretir.
     */
    public static ByteArrayOutputStream generateValidatedMektupPdf(List<FileProcessDTO> dtoList) throws Exception {
        long start = System.currentTimeMillis();
        logger.info("generateValidatedMektupPdf(List) başladı");

        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream mergedOutputStream = new ByteArrayOutputStream();

        for (FileProcessDTO dto : dtoList) {
            ByteArrayOutputStream singlePdf = generateSingleMektupPdf(dto);
            merger.addSource(new ByteArrayInputStream(singlePdf.toByteArray()));
        }

        merger.setDestinationStream(mergedOutputStream);
        merger.mergeDocuments(null);

        logger.info("generateValidatedMektupPdf(List) bitti. Süre(ms): " + (System.currentTimeMillis() - start));
        return mergedOutputStream;
    }

    /**
     * Tek bir DTO'yu işler, bir küçük PDF üretir.
     */
    private static ByteArrayOutputStream generateSingleMektupPdf(FileProcessDTO dto) throws Exception {
        InputStream in = new ByteArrayInputStream(dto.getInputFileBytes());
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(in);

        // Font yüklemesi
        InputStream fontInput = FileProcessingUtil.class.getClassLoader().getResourceAsStream("print/DejaVuSans.ttf");
        if (fontInput == null) {
            throw new FileNotFoundException("DejaVuSans.ttf bulunamadı!");
        }
        File tempFile = File.createTempFile("dejavusans_", ".ttf");
        Files.copy(fontInput, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        PhysicalFonts.addPhysicalFont(tempFile.toURI().toURL());
        wordMLPackage.setFontMapper(new IdentityPlusMapper());
        tempFile.deleteOnExit();

        // MainDocument işlemleri
        MainDocumentPart mainPart = wordMLPackage.getMainDocumentPart();
        List<Object> content = mainPart.getContent();

        for (int i = 0; i < content.size(); i++) {
            Object o = content.get(i);
            if (o instanceof P) {
                P para = (P) o;
                String text = getTextFromParagraph(para);

                if (MektupTipEnum.HAKEDIS_DEVIR_MEKTUPLARI.equals(dto.getMektupTip())) {
                    if ("%%DVR_DATA%%".equals(text)) {
                        PPr existingPPr = para.getPPr();
                        content.remove(i);

                        P paragraph = buildParagraphWithLineBreaks(dto.getObjectList(), existingPPr);
                        content.add(i, paragraph);
                        break;
                    }
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfConversion conversion = new Conversion(wordMLPackage);
        conversion.output(baos, new org.docx4j.convert.out.pdf.PdfSettings());
        baos.flush();

        return baos;
    }

    /**
     * Paragrafı, satır atlamalı olarak oluşturur.
     */
    private static P buildParagraphWithLineBreaks(List<String[]> objectList, PPr existingPPr) {
        P paragraph = new P();
        if (existingPPr != null) {
            paragraph.setPPr(XmlUtils.deepCopy(existingPPr));
        }

        for (String[] objectLine : objectList) {
            if (objectLine.length >= 2) {
                String kararNo = objectLine[0];
                String tutarNo = objectLine[1];

                R run = new R();
                Text text = new Text();
                text.setValue(simplifyTurkishCharacters("Karar No: " + kararNo + " Tutar (TL): " + tutarNo));
                run.getContent().add(text);
                paragraph.getContent().add(run);

                R brRun = new R();
                brRun.getContent().add(new Br());
                paragraph.getContent().add(brRun);

                if (objectLine.length >= 3) {
                    String firma = objectLine[2];

                    R run2 = new R();
                    Text text2 = new Text();
                    text2.setValue(simplifyTurkishCharacters("Devreden Firma: " + firma));
                    run2.getContent().add(text2);
                    paragraph.getContent().add(run2);
                }
            }
        }

        return paragraph;
    }

    /**
     * Text içindeki Türkçe karakterleri sadeleştirir.
     */
    public static String simplifyTurkishCharacters(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace('İ', 'I')
                .replace('I', 'I')
                .replace('ı', 'i')
                .replace('Ğ', 'G')
                .replace('ğ', 'g')
                .replace('Ü', 'U')
                .replace('ü', 'u')
                .replace('Ş', 'S')
                .replace('ş', 's')
                .replace('Ö', 'O')
                .replace('ö', 'o')
                .replace('Ç', 'C')
                .replace('ç', 'c');
    }

    /**
     * Paragraftaki Text içerikleri birleştirir.
     */
    private static String getTextFromParagraph(P paragraph) {
        StringBuilder text = new StringBuilder();
        List<Object> runs = paragraph.getContent();
        for (Object obj : runs) {
            if (obj instanceof R) {
                R run = (R) obj;
                for (Object content : run.getContent()) {
                    if (content instanceof Text) {
                        Text t = (Text) content;
                        text.append(t.getValue());
                    }
                }
            }
        }
        return text.toString();
    }
}






public static String simplifyTurkishCharacters(String input) {
    if (input == null) {
        return null;
    }

    return input
            .replace('İ', 'I')
            .replace('I', 'I') // zaten büyük I olmalı
            .replace('ı', 'i')
            .replace('Ğ', 'G')
            .replace('ğ', 'g')
            .replace('Ü', 'U')
            .replace('ü', 'u')
            .replace('Ş', 'S')
            .replace('ş', 's')
            .replace('Ö', 'O')
            .replace('ö', 'o')
            .replace('Ç', 'C')
            .replace('ç', 'c');
}



import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public static String normalizeTurkishCharacters(String input) {
    if (input == null) {
        return null;
    }

    // 1. Unicode normalize et
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFC);

    // 2. Bütün bozuk karakterler için düzeltme tablosu
    Map<String, String> turkishChars = new HashMap<>();
    turkishChars.put("Ã‡", "Ç");
    turkishChars.put("Ã§", "ç");
    turkishChars.put("Äž", "Ğ");
    turkishChars.put("ÄŸ", "ğ");
    turkishChars.put("Ä°", "İ");
    turkishChars.put("Ä±", "ı");
    turkishChars.put("Ã–", "Ö");
    turkishChars.put("Ã¶", "ö");
    turkishChars.put("Ãœ", "Ü");
    turkishChars.put("Ã¼", "ü");
    turkishChars.put("Åž", "Ş");
    turkishChars.put("ÅŸ", "ş");
    turkishChars.put("#C", "Ç");
    turkishChars.put("#c", "ç");
    turkishChars.put("#G", "Ğ");
    turkishChars.put("#g", "ğ");
    turkishChars.put("#S", "Ş");
    turkishChars.put("#s", "ş");
    turkishChars.put("#I", "İ");
    turkishChars.put("#i", "ı");
    turkishChars.put("#O", "Ö");
    turkishChars.put("#o", "ö");
    turkishChars.put("#U", "Ü");
    turkishChars.put("#u", "ü");
    turkishChars.put("#", ""); // Boş kare gibi görülen karakterleri tamamen temizle

    for (Map.Entry<String, String> entry : turkishChars.entrySet()) {
        normalized = normalized.replace(entry.getKey(), entry.getValue());
    }

    return normalized;
}





import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public static String normalizeTurkishCharacters(String input) {
    if (input == null) {
        return null;
    }

    // 1. Unicode normalize et
    String normalized = Normalizer.normalize(input, Normalizer.Form.NFC);

    // 2. Türkçe karakterleri manuel koru
    Map<String, String> turkishChars = new HashMap<>();
    turkishChars.put("Ã‡", "Ç");
    turkishChars.put("Ã§", "ç");
    turkishChars.put("ÄŸ", "ğ");
    turkishChars.put("Äž", "Ğ");
    turkishChars.put("Ã–", "Ö");
    turkishChars.put("Ã¶", "ö");
    turkishChars.put("Ãœ", "Ü");
    turkishChars.put("Ã¼", "ü");
    turkishChars.put("ÅŸ", "ş");
    turkishChars.put("Åž", "Ş");
    turkishChars.put("Ä°", "İ");
    turkishChars.put("Ä±", "ı");

    for (Map.Entry<String, String> entry : turkishChars.entrySet()) {
        normalized = normalized.replace(entry.getKey(), entry.getValue());
    }

    return normalized;
}









for (String[] devir : devirList) {
    // 1. satır: Karar No + Tutar
    P p1 = createParagraph(String.format("Karar No: %s    Tutar (TL): %s", devir[0], devir[1]));
    // 2. satır: Devreden Firma
    P p2 = createParagraph("Devreden Firma: " + devir[2]);
    // Boşluk için boş paragraf
    P pEmpty = createParagraph("");

    paragraphs.add(p1);
    paragraphs.add(p2);
    paragraphs.add(pEmpty);
}

// Şablonda %%DEVIR_BILGILERI%% olan paragrafı bul ve yerine bu satırları ekle
List<Object> content = mainPart.getContent();
for (int i = 0; i < content.size(); i++) {
    Object o = content.get(i);
    if (o instanceof P) {
        P para = (P) o;
        String text = getTextFromParagraph(para);
        if ("%%DEVIR_BILGILERI%%".equals(text)) {
            content.remove(i);
            content.addAll(i, paragraphs);
            break;
        }
    }
}










private static P createParagraph(String text) {
    P p = new P();
    Text t = new Text();
    t.setValue(text);
    R r = new R();
    r.getContent().add(t);
    p.getContent().add(r);
    return p;
}

private static String getTextFromParagraph(P p) {
    StringBuilder sb = new StringBuilder();
    List<Object> runs = p.getContent();
    for (Object obj : runs) {
        if (obj instanceof R) {
            R run = (R) obj;
            for (Object content : run.getContent()) {
                if (content instanceof Text) {
                    sb.append(((Text) content).getValue());
                }
            }
        }
    }
    return sb.toString().trim();
}

       import org.apache.poi.xwpf.usermodel.*;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class WordToPdfBatchGenerator {

    public static void main(String[] args) {
        try {
            File inputFile = new File("template.docx");
            String fileNameWithoutExt = inputFile.getName().replaceFirst("[.][^.]+$", "");
            String basePath = "word/print";

            // Alt klasörü oluştur
            File outputDir = new File(basePath);
            if (!outputDir.exists()) outputDir.mkdirs();

            // Word şablonunu belleğe al
            byte[] templateBytes = Files.readAllBytes(inputFile.toPath());

            // Örnek veri üret
            List<List<String>> dataList = new ArrayList<>();
            for (int i = 1; i <= 5; i++) { // test için 5 kayıt, 5000 yapabilirsin
                List<String> fields = List.of(
                        "Karar No: K-" + i,
                        "Devreden Firma: Firma A, Firma B, Firma C",
                        "Tutar: " + (1000 + i) + " TL",
                        "ALICI: Ali Veli",
                        "BANKA: Banka X",
                        "HESAP: TR00 0000 0000 0000",
                        "AÇIKLAMA: Ödeme açıklaması " + i
                );
                dataList.add(fields);
            }

            long start = System.currentTimeMillis();

            // Her veri için döngü
            int index = 1;
            for (List<String> fields : dataList) {
                try (InputStream in = new ByteArrayInputStream(templateBytes)) {
                    XWPFDocument doc = new XWPFDocument(in);

                    // %s alanlarını doldur
                    replaceAllPlaceholders(doc, fields);

                    // Doldurulmuş .docx'i yaz
                    File filledDocx = new File(outputDir, fileNameWithoutExt + "_filled_" + index + ".docx");
                    try (FileOutputStream out = new FileOutputStream(filledDocx)) {
                        doc.write(out);
                    }

                    // PDF'e çevir
                    File outputPdf = new File(outputDir, fileNameWithoutExt + "_" + index + ".pdf");
                    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(filledDocx);
                    try (FileOutputStream outPdf = new FileOutputStream(outputPdf)) {
                        Docx4J.toPDF(wordMLPackage, outPdf);
                    }

                    filledDocx.delete(); // isteğe bağlı
                    index++;
                }
            }

            long end = System.currentTimeMillis();
            System.out.println("Tamamlandı. Süre: " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Hata: " + e.getMessage());
        }
    }

    // %s alanlarını sırayla doldurur
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
