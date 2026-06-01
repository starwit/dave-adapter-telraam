package de.starwit.telraam.dto.dave;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/**
 * Mirrors {@code de.muenchen.dave.domain.dtos.external.DetectionDTO} from the
 * Starwit DAVe backend (sprint branch).
 *
 * <p>
 * Field semantics:
 * </p>
 * <ul>
 * <li>{@code zaehlungId} – UUID of the DAVe Zählung this detection belongs to.
 * Must be pre-configured / looked up; a Telraam segment_id is mapped to
 * a DAVe Zählung UUID via the {@code telraam.segment-mapping} config.</li>
 * <li>{@code startUhrzeit} / {@code endeUhrzeit} – 15-minute interval in
 * UTC.</li>
 * <li>{@code von} / {@code nach} – DAVe direction integers matching the compass
 * model shown in the README (1 = North, 2 = South, 3 = East, 4 = West,
 * 5–8 = further legs for complex intersections). A single Telraam record
 * produces two {@code DetectionDTO}s per vehicle class: one A→B, one B→A.</li>
 * </ul>
 *
 * All vehicle-count fields are nullable (sensor may not report every class).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetectionDTO {

    /** DAVe Zählung UUID – maps from Telraam segment_id via configuration. */
    private UUID zaehlungId;

    /** Start of the 15-minute interval (UTC). */
    private Instant startUhrzeit;

    /** End of the 15-minute interval (UTC). */
    private Instant endeUhrzeit;

    /** Cars / Pkw. */
    private Integer pkw;

    /** Trucks / Lkw (Telraam "heavy" category). */
    private Integer lkw;

    /** Articulated lorries / Lastzüge – not distinguished by Telraam; left null. */
    private Integer lastzuege;

    /** Buses – not distinguished by Telraam; left null. */
    private Integer busse;

    /** Motorcycles / Krafträder (Telraam "motorbike" category). */
    private Integer kraftraeder;

    /** Cyclists / Fahrradfahrer. */
    private Integer fahrradfahrer;

    /** Pedestrians / Fußgänger. */
    private Integer fussgaenger;

    /**
     * DAVe "from" direction integer (compass leg the traffic originates from).
     * See the direction diagram in the project README.
     */
    private Integer von;

    /**
     * DAVe "to" direction integer (compass leg the traffic heads towards).
     */
    private Integer nach;


    public UUID getZaehlungId() {
        return zaehlungId;
    }

    public void setZaehlungId(UUID zaehlungId) {
        this.zaehlungId = zaehlungId;
    }

    public Instant getStartUhrzeit() {
        return startUhrzeit;
    }

    public void setStartUhrzeit(Instant startUhrzeit) {
        this.startUhrzeit = startUhrzeit;
    }

    public Instant getEndeUhrzeit() {
        return endeUhrzeit;
    }

    public void setEndeUhrzeit(Instant endeUhrzeit) {
        this.endeUhrzeit = endeUhrzeit;
    }

    public Integer getPkw() {
        return pkw;
    }

    public void setPkw(Integer pkw) {
        this.pkw = pkw;
    }

    public Integer getLkw() {
        return lkw;
    }

    public void setLkw(Integer lkw) {
        this.lkw = lkw;
    }

    public Integer getLastzuege() {
        return lastzuege;
    }

    public void setLastzuege(Integer lastzuege) {
        this.lastzuege = lastzuege;
    }

    public Integer getBusse() {
        return busse;
    }

    public void setBusse(Integer busse) {
        this.busse = busse;
    }

    public Integer getKraftraeder() {
        return kraftraeder;
    }

    public void setKraftraeder(Integer kraftraeder) {
        this.kraftraeder = kraftraeder;
    }

    public Integer getFahrradfahrer() {
        return fahrradfahrer;
    }

    public void setFahrradfahrer(Integer fahrradfahrer) {
        this.fahrradfahrer = fahrradfahrer;
    }

    public Integer getFussgaenger() {
        return fussgaenger;
    }

    public void setFussgaenger(Integer fussgaenger) {
        this.fussgaenger = fussgaenger;
    }

    public Integer getVon() {
        return von;
    }

    public void setVon(Integer von) {
        this.von = von;
    }

    public Integer getNach() {
        return nach;
    }

    public void setNach(Integer nach) {
        this.nach = nach;
    }

    @Override
    public String toString() {
        return "DetectionDTO{zaehlungId=" + zaehlungId +
                ", start=" + startUhrzeit +
                ", ende=" + endeUhrzeit +
                ", von=" + von + ", nach=" + nach +
                ", pkw=" + pkw + ", lkw=" + lkw +
                ", rad=" + fahrradfahrer + ", fuss=" + fussgaenger + '}';
    }
}
