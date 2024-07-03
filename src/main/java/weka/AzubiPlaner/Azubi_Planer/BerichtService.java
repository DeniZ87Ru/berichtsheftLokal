package weka.AzubiPlaner.Azubi_Planer;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
public class BerichtService {

    private final String berichteVerzeichnis = System.getProperty("user.home") + "/Dokumente_Azubi_Planer";

    public List<Bericht> berichteLaden() {
        List<Bericht> berichte = new ArrayList<>();
        File verzeichnis = new File(berichteVerzeichnis);
        File[] dateien = verzeichnis.listFiles();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

        if (dateien != null) {
            for (File datei : dateien) {
                if (datei.isFile() && datei.getName().endsWith(".txt")) {
                    String dateiName = datei.getName().replace(".txt", ""); // Entferne ".txt" aus dem Dateinamen
                    LocalDateTime startDateTime = LocalDateTime.parse(dateiName, dateTimeFormatter);
                    LocalDateTime endDateTime = LocalDateTime.parse(dateiName, dateTimeFormatter);
                    String[] parts = dateiName.split("_");
                    LocalDate start = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalTime startTime = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH-mm"));
                    LocalTime endTime = LocalTime.parse(getEndzeitByDatumZeit(start.toString(), startTime.toString()));
                    Long idLong = getIdByDatum(parts[0], parts[1]);
                    long id = 0;
                    if (idLong != null) {
                        id = idLong;
                    }
                    // Lies den Inhalt der Datei und verwende ihn als Titel
                    String status = getStatusByDatum(start.toString(), startTime.toString());
                    // Erstelle einen Bericht mit Titel und füge ihn zur Liste hinzu
                    Bericht bericht = new Bericht(id, status, "", "", startDateTime.toLocalDate(), endDateTime.toLocalDate(),
                            startDateTime.toLocalTime(), endTime);
                    bericht.setStatus(status);
                    bericht.setId(id);
                    berichte.add(bericht);
                }
            }
        }
        return berichte;
    }

    public List<Bericht> berichtLaden(String datum_start) {
        List<Bericht> berichte = new ArrayList<>();
        File verzeichnis = new File(berichteVerzeichnis);
        File[] dateien = verzeichnis.listFiles();

        if (dateien != null) {
            for (File datei : dateien) {
                if (datei.isFile() && datei.getName().endsWith(".txt")) {
                    String dateiName = datei.getName().replace(".txt", ""); // Entferne ".txt" aus dem Dateinamen
                    String[] parts = dateiName.split("_");
                    // Überprüfe, ob das Datum im Dateinamen dem gewünschten Datum entspricht
                    if (parts[0].equals(datum_start)) {
                        LocalDate start = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        LocalTime startTime = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH-mm"));
                        Long idLong = getIdByDatum(parts[0], parts[1]);
                        long id = 0;
                        if (idLong != null) {
                            id = idLong;
                        }

                        String status = getStatusByDatum(start.toString(), startTime.toString());
                        String inhalt = getInhaltByDatum(datum_start, String.valueOf(startTime));
                        String notiz = getNotizByDatum(datum_start, String.valueOf(startTime));
                        LocalDate datum_ende = LocalDate.parse(getDatum_endeByStartDatum(datum_start, String.valueOf(startTime)));
                        LocalTime zeit_ende = LocalTime.parse(getEndzeitByDatumZeit(datum_start, String.valueOf(startTime)));
                        // Erstelle einen Bericht mit Titel und füge ihn zur Liste hinzu
                        Bericht bericht = new Bericht(id, status, inhalt, notiz, start, datum_ende, startTime, zeit_ende);
                        bericht.setStatus(status);
                        bericht.setId(id);
                        bericht.setInhalt(inhalt);
                        bericht.setNotiz(notiz);
                        berichte.add(bericht);
                    }
                }
            }
        }
        return berichte;
    }

    public String getBerichtByStartDatum(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" + zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                StringBuilder berichtInhalt = new StringBuilder();
                while (scanner.hasNextLine()) {
                    berichtInhalt.append(scanner.nextLine()).append("\n");
                }
                scanner.close();

                Bericht bericht = new Bericht();
                bericht.setDatum_start(LocalDate.parse(datum_start, DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                return bericht.toString(); // Gibt den String zurück, der den Bericht enthält.

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public Long getIdByDatum(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" +  zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                if (scanner.hasNextLine()) {
                    Long id = Long.parseLong(scanner.nextLine());
                    scanner.close();
                    return id;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getDatum_endeByStartDatum(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" +  zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                String datum_ende = null;
                int lineCount = 0;
                while (scanner.hasNextLine() && lineCount < 3) {
                    datum_ende = scanner.nextLine();
                    lineCount++;
                }
                scanner.close();
                if (lineCount == 3) {
                    // Entfernen Sie "Bis: " vom Anfang des Strings
                    datum_ende = datum_ende.replace("Bis: ", "");
                    // Parsen Sie das datum_ende in ein LocalDate-Objekt
                    LocalDate ende = LocalDate.parse(datum_ende, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    return ende.toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getStatusByDatum(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" +  zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                String status = null;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Status: ")) {
                        status = line.substring(8);
                        break;
                    }
                }
                scanner.close();
                if (status != null) {
                    return status;
                } else {
                    return "Status nicht gefunden.";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getInhaltByDatum(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" +  zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                StringBuilder inhalt = new StringBuilder();
                boolean startReading = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.equals("##ANFANG DES INHALTS##")) {
                        startReading = true;
                        continue;
                    }
                    if (line.equals("##ENDE DES INHALTS##")) {
                        break;
                    }
                    if (startReading) {
                        inhalt.append(line).append("\n");
                    }
                }
                scanner.close();
                return inhalt.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getNotizByDatum(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" +  zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                StringBuilder inhalt = new StringBuilder();
                boolean startReading = false;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.equals("##ANFANG DER NOTIZEN##")) {
                        startReading = true;
                        continue;
                    }
                    if (line.equals("##ENDE DER NOTIZEN##")) {
                        break;
                    }
                    if (startReading) {
                        inhalt.append(line).append("\n");
                    }
                }
                scanner.close();
                return inhalt.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getEndzeitByDatumZeit(String datum_start, String zeit_start) {
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + datum_start + "_" +  zeit_start.replace(":", "-") + ".txt";
        File datei = new File(dateiName);
        if (datei.exists()) {
            try {
                Scanner scanner = new Scanner(datei);
                String endTime = null;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Ende: ")) {
                        endTime = line.substring(6);
                        break;
                    }
                }
                scanner.close();
                if (endTime != null) {
                    return endTime;
                } else {
                    return "Endzeit nicht gefunden.";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public void speichern(long currentId, String datum_start, String datum_ende, String status,
                          String inhalt, String notiz, String zeit_start, String zeit_ende) {
        // Erstelle Formatter für das Datum und die Zeit
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        // Parse das Start- und Enddatum sowie die Start- und Endzeit
        LocalDate startDatum = LocalDate.parse(datum_start, formatter);
        LocalDate endDatum = LocalDate.parse(datum_ende, formatter);
        LocalTime startTime = LocalTime.parse(zeit_start, timeFormatter);
        LocalTime endTime = LocalTime.parse(zeit_ende, timeFormatter);
        // Durchlaufe jeden Tag vom Startdatum bis zum Enddatum
        for (LocalDate date = startDatum; !date.isAfter(endDatum); date = date.plusDays(1)) {
            // Erstelle ein LocalDateTime-Objekt für den Startzeitpunkt
            LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
            // Erstelle den Dateinamen
            String dateiName = System.getProperty("user.home") + File.separator +
                    "Dokumente_Azubi_Planer" + File.separator + startDateTime.format(dateTimeFormatter) + ".txt";
            try {
                // Erstelle einen FileWriter
                FileWriter writer = new FileWriter(dateiName);

                // Schreibe die Daten in die Datei
                writer.write(currentId + "\n" + "Von: " + datum_start + "\n" + "Bis: "
                        + datum_ende + "\n" + "\n" + "Status: " + status + "\n" + "\n" +
                        "##ANFANG DES INHALTS##" + "\n" + inhalt + "\n" +
                        "##ENDE DES INHALTS##" + "\n" + "##ANFANG DER NOTIZEN##"
                        + "\n" + notiz + "\n" + "##ENDE DER NOTIZEN##" + "\n" +
                        "Start: " + startTime + "\n" + "Ende: " + endTime);
                // Schließe den FileWriter
                writer.close();
            } catch (IOException e) {
                // Drucke den Stack Trace bei einer IOException
                e.printStackTrace();
            }
        }
    }


    public void loeschen(String datum_start, String zeit_start) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDate startDatum = LocalDate.parse(datum_start, formatter);
        LocalTime startTime = LocalTime.parse(zeit_start, timeFormatter);
        LocalDateTime startDateTime = LocalDateTime.of(startDatum, startTime);
        String dateiName = System.getProperty("user.home") + File.separator + "Dokumente_Azubi_Planer" + File.separator + startDateTime.format(dateTimeFormatter) + ".txt";
        File file = new File(dateiName);
        if(file.delete()) {
            System.out.println("Datei wurde gelöscht");
        } else System.out.println("Datei konnte nicht gelöscht werden");
    }

    public void createPdf(String datum_start) {
        try {
            List<Bericht> berichte = berichtLaden(datum_start);
            Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            int counter = 1;
            for (Bericht bericht : berichte) {
                String berichteVerzeichnis = System.getProperty("user.home") + "/Dokumente_Azubi_Planer/PDF_Berichte";
                String dateiName = berichteVerzeichnis + "/" + datum_start + ".pdf";

                // Überprüfen, ob die Datei bereits existiert
                while (new File(dateiName).exists()) {
                    counter++; // Erhöhe den Zähler, um eine neue fortlaufende Nummer zu generieren
                    dateiName = berichteVerzeichnis + "/" + datum_start + "_" + counter + ".pdf";
                }

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(dateiName));
                document.open();

                PdfPTable table = new PdfPTable(2); // 2 Spalte
                PdfPCell cell;

                Phrase phrase1 = new Phrase();
                phrase1.add(new Chunk("Bericht Nummer: ", font));
                phrase1.add(new Chunk(bericht.getId().toString()));
                document.add(phrase1);

                document.add(new Paragraph("\n"));

                Phrase phrase2 = new Phrase();
                phrase2.add(new Chunk("\nTag: ", font));
                phrase2.add(new Chunk(bericht.getDatum_start().toString()));
                cell = new PdfPCell(phrase2);
                table.addCell(cell);

                Phrase phrase3 = new Phrase();
                phrase3.add(new Chunk("\nBis: ", font));
                phrase3.add(new Chunk(bericht.getDatum_ende().toString()));
                cell = new PdfPCell(phrase3);
                table.addCell(cell);

                Phrase phrase4 = new Phrase();
                phrase4.add(new Chunk("\nBeginn: ", font));
                phrase4.add(new Chunk(bericht.getZeit_start().toString()));
                cell = new PdfPCell(phrase4);
                table.addCell(cell);

                Phrase phrase5 = new Phrase();
                phrase5.add(new Chunk("\nEndet: ", font));
                phrase5.add(new Chunk(bericht.getZeit_ende().toString()));
                cell = new PdfPCell(phrase5);
                table.addCell(cell);

                // Füge die Tabelle zum Dokument hinzu
                document.add(table);

                document.add(new Paragraph("\n"));

                Phrase phrase6 = new Phrase();
                phrase6.add(new Chunk("Status: ", font));
                phrase6.add(new Chunk(bericht.getStatus()));
                document.add(phrase6);

                document.add(new Paragraph("\n"));
                document.add(new Paragraph("\n"));

                document.add(new Paragraph("Bericht:", font));
                document.add(new Paragraph("\n"));
                String inhalt = bericht.getInhalt();
                int linesToAdd = 10 - inhalt.split("\n").length;
                inhalt += String.join("", Collections.nCopies(linesToAdd, "\n"));
                document.add(new Phrase(inhalt));

                document.add(new Paragraph("\n"));

                document.add(new Paragraph("Notizen:", font));
                document.add(new Paragraph("\n"));
                String notiz = bericht.getNotiz();
                linesToAdd = 10 - notiz.split("\n").length;
                notiz += String.join("", Collections.nCopies(linesToAdd, "\n"));
                document.add(new Phrase(notiz));

                PdfPTable table2 = new PdfPTable(2); // 2 Spalte
                PdfPCell cell2;

                Phrase phrase7 = new Phrase();
                phrase7.add(new Chunk("\nAuszubildender:", font));
                cell2 = new PdfPCell(phrase7);
                table2.addCell(cell2);

                Phrase phrase8 = new Phrase();
                phrase8.add(new Chunk("\ngesetz.Vertreter:", font));
                cell2 = new PdfPCell(phrase8);
                table2.addCell(cell2);


                Phrase phrase9 = new Phrase();
                phrase9.add(new Chunk("\nDatum:", font));
                cell2 = new PdfPCell(phrase9);
                table2.addCell(cell2);

                Phrase phrase10 = new Phrase();
                phrase10.add(new Chunk("\nAusbilder:", font));
                cell2 = new PdfPCell(phrase10);
                table2.addCell(cell2);

                document.add(table2);
                document.close();
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Bericht> sucheBerichteNachDatum(String datum_start) {
        List<Bericht> berichte = new ArrayList<>();
        File verzeichnis = new File(berichteVerzeichnis);
        File[] dateien = verzeichnis.listFiles();

        if (dateien != null) {
            for (File datei : dateien) {
                if (datei.isFile() && datei.getName().endsWith(".txt")) {
                    String dateiName = datei.getName().replace(".txt", ""); // Entferne ".txt" aus dem Dateinamen
                    String[] parts = dateiName.split("_");
                    // Überprüfe, ob das Datum im Dateinamen dem gewünschten Datum entspricht
                    if (parts[0].equals(datum_start)) {
                        LocalDate start = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        LocalTime startTime = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH-mm"));
                        Long idLong = getIdByDatum(parts[0], parts[1]);
                        long id = 0;
                        if (idLong != null) {
                            id = idLong;
                        }

                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(datei));
                            String zeile;
                            while ((zeile = reader.readLine()) != null) {
                                if (zeile.contains(datum_start)) {
                                    String status = getStatusByDatum(start.toString(), startTime.toString());
                                    String inhalt = getInhaltByDatum(datum_start, String.valueOf(startTime));
                                    String notiz = getNotizByDatum(datum_start, String.valueOf(startTime));
                                    LocalDate datum_ende = LocalDate.parse(getDatum_endeByStartDatum(datum_start, String.valueOf(startTime)));
                                    LocalTime zeit_ende = LocalTime.parse(getEndzeitByDatumZeit(datum_start, String.valueOf(startTime)));

                                    Bericht bericht = new Bericht(id, status, inhalt, notiz, start, datum_ende, startTime, zeit_ende);
                                    bericht.setStatus(status);
                                    bericht.setId(id);
                                    bericht.setInhalt(inhalt);
                                    bericht.setNotiz(notiz);
                                    berichte.add(bericht);
                                    break;
                                }
                            }
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return berichte;
    }

    public List<Bericht> sucheBerichteNachId(long id) {
        List<Bericht> berichte = new ArrayList<>();
        File verzeichnis = new File(berichteVerzeichnis);
        File[] dateien = verzeichnis.listFiles();

        if (dateien != null) {
            for (File datei : dateien) {
                if (datei.isFile() && datei.getName().endsWith(".txt")) {
                    String dateiName = datei.getName().replace(".txt", ""); // Entferne ".txt" aus dem Dateinamen
                    String[] parts = dateiName.split("_");
                    LocalDate start = LocalDate.parse(parts[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    LocalTime startTime = LocalTime.parse(parts[1], DateTimeFormatter.ofPattern("HH-mm"));
                    Long idLong = getIdByDatum(parts[0], parts[1]);
                    if (idLong != null && idLong == id) {
                        try {
                            BufferedReader reader = new BufferedReader(new FileReader(datei));
                            String zeile;
                            while ((zeile = reader.readLine()) != null) {
                                if (zeile.contains(Long.toString(id))) {
                                    String status = getStatusByDatum(start.toString(), startTime.toString());
                                    String inhalt = getInhaltByDatum(parts[0], String.valueOf(startTime));
                                    String notiz = getNotizByDatum(parts[0], String.valueOf(startTime));
                                    LocalDate datum_ende = LocalDate.parse(getDatum_endeByStartDatum(parts[0], String.valueOf(startTime)));
                                    LocalTime zeit_ende = LocalTime.parse(getEndzeitByDatumZeit(parts[0], String.valueOf(startTime)));

                                    Bericht bericht = new Bericht(id, status, inhalt, notiz, start, datum_ende, startTime, zeit_ende);
                                    bericht.setStatus(status);
                                    bericht.setId(id);
                                    bericht.setInhalt(inhalt);
                                    bericht.setNotiz(notiz);
                                    berichte.add(bericht);
                                    break;
                                }
                            }
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return berichte;
    }
}
