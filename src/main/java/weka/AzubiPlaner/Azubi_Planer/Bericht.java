package weka.AzubiPlaner.Azubi_Planer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Bericht {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;
    private String inhalt;
    private String notiz;
    private LocalDate datum_start;
    private LocalDate datum_ende;
    private LocalTime zeit_start;
    private LocalTime zeit_ende;

    public Bericht(Long id, String status, String inhalt, String notiz, LocalDate datum_start, LocalDate datum_ende, LocalTime zeit_start, LocalTime zeit_ende) {
        this.id = id;
        this.status = status;
        this.inhalt = inhalt;
        this.notiz = notiz;
        this.datum_start = datum_start;
        this.datum_ende = datum_ende;
        this.zeit_start = zeit_start;
        this.zeit_ende = zeit_ende;
    }

    public Bericht() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = this.status;
    }

    public String getInhalt() {
        return inhalt;
    }

    public void setInhalt(String inhalt) {
        this.inhalt = inhalt;
    }

    public String getNotiz() {
        return notiz;
    }

    public void setNotiz(String notiz) {
        this.notiz = notiz;
    }

    public LocalDate getDatum_start() {
        return datum_start;
    }

    public void setDatum_start(LocalDate datum_start) {
        this.datum_start = datum_start;
    }

    public LocalDate getDatum_ende() {
        return datum_ende;
    }

    public void setDatum_ende(LocalDate datum_ende) {
        this.datum_ende = datum_ende;
    }

    public LocalTime getZeit_start() {
        return zeit_start;
    }

    public void setZeit_start(LocalTime zeit_start) {
        this.zeit_start = zeit_start;
    }

    public LocalTime getZeit_ende() {
        return zeit_ende;
    }

    public void setZeit_ende(LocalTime zeit_ende) {
        this.zeit_ende = zeit_ende;
    }
}
