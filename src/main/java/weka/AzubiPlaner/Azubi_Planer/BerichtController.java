package weka.AzubiPlaner.Azubi_Planer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class BerichtController {

    @Autowired
    private BerichtService berichtService;

    @GetMapping("/")
    public String startSeite(Model model) {
        List<Bericht> berichte = berichtService.berichteLaden();
        model.addAttribute("berichte", berichte);
        return "start_Seite";
    }

    @GetMapping("/erstellen")
    public String bearbeiten(Model model, @RequestParam(name = "datum_start") String datum_start, @RequestParam(name = "zeit_start") String zeit_start) {
        if (datum_start != null) {
            String berichte = berichtService.getBerichtByStartDatum(datum_start, zeit_start);
            String status = berichtService.getStatusByDatum(datum_start, zeit_start);
            String inhalt = berichtService.getInhaltByDatum(datum_start, zeit_start);
            String notiz = berichtService.getNotizByDatum(datum_start, zeit_start);
            String datum_ende = berichtService.getDatum_endeByStartDatum(datum_start, zeit_start);
            Long id = berichtService.getIdByDatum(datum_start, zeit_start);
            String zeit_ende = berichtService.getEndzeitByDatumZeit(datum_start, zeit_start);
            model.addAttribute("id", id);
            model.addAttribute("zeit_start", zeit_start);
            model.addAttribute("endTime", zeit_ende);
            model.addAttribute("datum_start", datum_start);
            model.addAttribute("datum_ende", datum_ende);
            model.addAttribute("status", status);
            model.addAttribute("inhalt", inhalt);
            model.addAttribute("notiz", notiz);
            model.addAttribute("berichte", berichte);
            return "erstellen";
        }
        return "redirect:/";
    }

    @PostMapping("/erstellen/speichern")
    public String erstellenSpeichern(Long id, @RequestParam(name = "datum_start") String datum_start, @RequestParam String datum_ende,
                                      @RequestParam String status, @RequestParam String inhalt, @RequestParam String notiz,
                                      @RequestParam(name = "zeit_start") String zeit_start, @RequestParam String zeit_ende) {
        berichtService.speichern(id, datum_start, datum_ende, status, inhalt, notiz, zeit_start, zeit_ende);
        return "redirect:/";
    }

    @GetMapping("/bearbeiten/neu")
    public String bearbeitenNeu(Model model, @RequestParam(name = "datum_start") String datum_start, @RequestParam(name = "zeit_start") String zeit_start) {
        if (datum_start != null) {
            List<Bericht> berichte = berichtService.berichtLaden(datum_start);
            String status = berichtService.getStatusByDatum(datum_start, zeit_start);
            model.addAttribute("berichte", berichte);
            model.addAttribute("status", status);
            model.addAttribute("allStatusOptions", Arrays.asList("Betrieb", "Mobil", "Schule/Schulung", "Urlaub", "Krank"));
            return "bearbeiten";
        }
        return "redirect:/";
    }

    @PostMapping("/bearbeiten/speichern")
    public String bearbeitenSpeichern(Long id, @RequestParam(name = "datum_start") String datum_start, @RequestParam String datum_ende,
                                      @RequestParam String status, @RequestParam String inhalt, @RequestParam String notiz,
                                      @RequestParam(name = "zeit_start") String zeit_start, @RequestParam String zeit_ende) {
        berichtService.speichern(id, datum_start, datum_ende, status, inhalt, notiz, zeit_start, zeit_ende);
        return "redirect:/";
    }

    @PostMapping("/bearbeiten/pdf")
    public String pdfSpeichern(@RequestParam(name = "datum_start") String datum_start) {
        berichtService.createPdf(datum_start);
        return "redirect:/";
    }

    @GetMapping("/bearbeiten/loeschen")
    public String bearbeitenLoeschen(@RequestParam(name = "datum_start") String datum_start, @RequestParam(name = "zeit_start") String zeit_start) {
        berichtService.loeschen(datum_start, zeit_start);
        return "redirect:/";
    }

    @GetMapping("/start_Seite/wochenbericht")
    public String wochenbericht(@RequestParam(name = "datum_start") String datum_start) {
        berichtService.berichtLaden(datum_start);
        return "redirect:/";
    }

    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getEvents() {
        List<Bericht> berichte = berichtService.berichteLaden();
        List<Map<String, Object>> events = new ArrayList<>();
        for (Bericht bericht : berichte) {
            LocalDateTime startDateTime = bericht.getDatum_start().atTime(bericht.getZeit_start());
            LocalDateTime endDateTime = bericht.getDatum_ende().atTime(bericht.getZeit_ende());

            Map<String, Object> event = new HashMap<>();
            event.put("start", startDateTime);
            event.put("end", endDateTime);
            event.put("title", bericht.getStatus());
            event.put("color", getColorByStatus(bericht.getStatus()));
            events.add(event);
        }
        return events;
    }

    // Hilfsmethode, um die Farbe basierend auf dem Status zu bestimmen
    private String getColorByStatus(String status) {
        switch (status) {
            case "Betrieb":
                return "#FF8C00";
            case "Mobil":
                return "#228B22";
            case "Schule/Schulung":
                return "#9400D3";
            case "Urlaub":
                return "#7FFFD4";
            case "Krank":
                return "#FF0000";
            default:
                return "#FF00FF";
        }
    }

    @GetMapping("/getInformationForDate")
    @ResponseBody
    public List<Map<String, String>> getInformationForDate(@RequestParam(name = "datum_start") String datum_start) {
        // Hier rufen Sie die Daten für das ausgewählte Datum ab
        List<Bericht> berichte = berichtService.berichtLaden(datum_start);
        List<Map<String, String>> allDateInformation = new ArrayList<>();

        for (Bericht bericht : berichte) {
            Map<String, String> dateInformation = new HashMap<>();
            if (bericht != null) {
                // Erstellen Sie ein JSON-Objekt mit den Daten
                dateInformation.put("id", bericht.getId().toString());
                dateInformation.put("datum_start", bericht.getDatum_start().toString());
                dateInformation.put("datum_ende", bericht.getDatum_ende().toString());
                dateInformation.put("status", bericht.getStatus());
                dateInformation.put("inhalt", bericht.getInhalt());
                dateInformation.put("notiz", bericht.getNotiz());
                dateInformation.put("zeit_start", bericht.getZeit_start().toString());
                dateInformation.put("zeit_ende", bericht.getZeit_ende().toString());
            } else {
                // Erstellen Sie ein leeres JSON-Objekt
                dateInformation.put("id", "");
                dateInformation.put("start_datum", "");
                dateInformation.put("end_datum", "");
                dateInformation.put("status", "");
                dateInformation.put("inhalt", "");
                dateInformation.put("notiz", "");
                dateInformation.put("zeit_start", "");
                dateInformation.put("zeit_ende", "");
            }
            allDateInformation.add(dateInformation);
        }
        return allDateInformation;
    }

    @GetMapping("/sucheBerichtById")
    @ResponseBody
    public List<Map<String, String>> sucheBerichteById(@RequestParam(name = "id") long id) {
        // Hier rufen Sie die Daten für das ausgewählte Datum ab
        List<Bericht> suchBericht = berichtService.sucheBerichteNachId(id);
        List<Map<String, String>> allSearchDateInformation = new ArrayList<>();

        for (Bericht bericht : suchBericht) {
            Map<String, String> dateInformation = new HashMap<>();
            if (bericht != null) {
                // Erstellen Sie ein JSON-Objekt mit den Daten
                dateInformation.put("id", bericht.getId().toString());
                dateInformation.put("datum_start", bericht.getDatum_start().toString());
                dateInformation.put("datum_ende", bericht.getDatum_ende().toString());
                dateInformation.put("status", bericht.getStatus());
                dateInformation.put("inhalt", bericht.getInhalt());
                dateInformation.put("notiz", bericht.getNotiz());
                dateInformation.put("zeit_start", bericht.getZeit_start().toString());
                dateInformation.put("zeit_ende", bericht.getZeit_ende().toString());
            } else {
                // Erstellen Sie ein leeres JSON-Objekt
                dateInformation.put("id", "");
                dateInformation.put("start_datum", "");
                dateInformation.put("end_datum", "");
                dateInformation.put("status", "");
                dateInformation.put("inhalt", "");
                dateInformation.put("notiz", "");
                dateInformation.put("zeit_start", "");
                dateInformation.put("zeit_ende", "");
            }
            allSearchDateInformation.add(dateInformation);
        }
        return allSearchDateInformation;
    }

    @GetMapping("/sucheBerichtByDatum")
    @ResponseBody
    public List<Map<String, String>> sucheBerichteByDatum(@RequestParam(name = "datum_start") String datum_start) {
        // Hier rufen Sie die Daten für das ausgewählte Datum ab
        List<Bericht> suchBericht = berichtService.sucheBerichteNachDatum(datum_start);
        List<Map<String, String>> allSearchDateInformation = new ArrayList<>();

        for (Bericht bericht : suchBericht) {
            Map<String, String> dateInformation = new HashMap<>();
            if (bericht != null) {
                // Erstellen Sie ein JSON-Objekt mit den Daten
                dateInformation.put("id", bericht.getId().toString());
                dateInformation.put("datum_start", bericht.getDatum_start().toString());
                dateInformation.put("datum_ende", bericht.getDatum_ende().toString());
                dateInformation.put("status", bericht.getStatus());
                dateInformation.put("inhalt", bericht.getInhalt());
                dateInformation.put("notiz", bericht.getNotiz());
                dateInformation.put("zeit_start", bericht.getZeit_start().toString());
                dateInformation.put("zeit_ende", bericht.getZeit_ende().toString());
            } else {
                // Erstellen Sie ein leeres JSON-Objekt
                dateInformation.put("id", "");
                dateInformation.put("start_datum", "");
                dateInformation.put("end_datum", "");
                dateInformation.put("status", "");
                dateInformation.put("inhalt", "");
                dateInformation.put("notiz", "");
                dateInformation.put("zeit_start", "");
                dateInformation.put("zeit_ende", "");
            }
            allSearchDateInformation.add(dateInformation);
        }
        return allSearchDateInformation;
    }
}
