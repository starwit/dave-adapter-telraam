package de.starwit.telraam.mapper;

import de.starwit.telraam.config.SegmentMappingProperties;
import de.starwit.telraam.config.SegmentMappingProperties.SegmentMapping;
import de.starwit.telraam.dto.dave.DetectionDTO;
import de.starwit.telraam.dto.telraam.TrafficRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Converts a Telraam {@link TrafficRecord} into a pair of {@link DetectionDTO}s
 * (one per direction: A→B and B→A), using the exact field names expected by the
 * Starwit DAVe {@code DetectorController}.
 *
 * <h2>Telraam direction convention</h2>
 * 
 * <pre>
 *  direction = 1 (True side):   lft → A→B,  rgt → B→A
 *  direction = 0 (False side):  rgt → A→B,  lft → B→A
 * </pre>
 *
 * <h2>DAVe direction convention</h2>
 * Each segment is configured with two integers ({@code directionAtoB},
 * {@code directionBtoA}) that correspond to DAVe's compass-leg numbering
 * (1=N, 2=S, 3=E, 4=W). These come from {@link SegmentMappingProperties}.
 *
 * <p>
 * A single Telraam record produces <em>two</em> {@link DetectionDTO}s so
 * that DAVe can store inbound and outbound flows independently.
 * </p>
 */
@Component
public class TrafficDirectionMapper {

    private static final Logger log = LoggerFactory.getLogger(TrafficDirectionMapper.class);

    private final SegmentMappingProperties mappingProperties;

    public TrafficDirectionMapper(SegmentMappingProperties mappingProperties) {
        this.mappingProperties = mappingProperties;
    }

    /**
     * Maps one Telraam record to a list of {@link DetectionDTO}s ready for DAVe.
     *
     * <p>
     * Returns an empty list if no segment mapping is configured for the
     * record's segment_id (with a warning log).
     * </p>
     *
     * @param record raw Telraam traffic record
     * @return 0, 1 or 2 {@link DetectionDTO}s (one per non-zero direction)
     */
    public List<DetectionDTO> map(TrafficRecord record) {
        Optional<SegmentMapping> mappingOpt = mappingProperties.findBySegmentId(record.segmentId());

        if (mappingOpt.isEmpty()) {
            log.warn("No DAVe mapping configured for Telraam segment {}  – skipping record",
                    record.segmentId());
            return List.of();
        }

        SegmentMapping mapping = mappingOpt.get();
        boolean trueDirection = record.direction() == null || record.direction() == 1;

        // Resolve lft/rgt counts into A→B and B→A buckets
        int carAtoB = round(trueDirection ? record.carLft() : record.carRgt());
        int carBtoA = round(trueDirection ? record.carRgt() : record.carLft());
        int bikeAtoB = round(trueDirection ? record.bikeLft() : record.bikeRgt());
        int bikeBtoA = round(trueDirection ? record.bikeRgt() : record.bikeLft());
        int motoAtoB = round(trueDirection ? record.motorbikeLft() : record.motorbikeRgt());
        int motoBtoA = round(trueDirection ? record.motorbikeRgt() : record.motorbikeLft());
        int pedAtoB = round(trueDirection ? record.pedestrianLft() : record.pedestrianRgt());
        int pedBtoA = round(trueDirection ? record.pedestrianRgt() : record.pedestrianLft());
        int heavyAtoB = round(trueDirection ? record.heavyLft() : record.heavyRgt());
        int heavyBtoA = round(trueDirection ? record.heavyRgt() : record.heavyLft());

        var start = record.date().toInstant();
        var end = start.plusSeconds(15 * 60);

        List<DetectionDTO> results = new ArrayList<>(2);

        // A→B direction
        DetectionDTO aToB = new DetectionDTO();
        aToB.setZaehlungId(mapping.getZaehlungId());
        aToB.setStartUhrzeit(start);
        aToB.setEndeUhrzeit(end);
        aToB.setVon(mapping.getDirectionAtoB());
        aToB.setNach(mapping.getDirectionBtoA());
        aToB.setPkw(carAtoB);
        aToB.setLkw(heavyAtoB); // Telraam "heavy" → DAVe Lkw
        aToB.setKraftraeder(motoAtoB);
        aToB.setFahrradfahrer(bikeAtoB);
        aToB.setFussgaenger(pedAtoB);
        // lastzuege / busse: Telraam does not differentiate → leave null
        results.add(aToB);

        // B→A direction
        DetectionDTO bToA = new DetectionDTO();
        bToA.setZaehlungId(mapping.getZaehlungId());
        bToA.setStartUhrzeit(start);
        bToA.setEndeUhrzeit(end);
        bToA.setVon(mapping.getDirectionBtoA());
        bToA.setNach(mapping.getDirectionAtoB());
        bToA.setPkw(carBtoA);
        bToA.setLkw(heavyBtoA);
        bToA.setKraftraeder(motoBtoA);
        bToA.setFahrradfahrer(bikeBtoA);
        bToA.setFussgaenger(pedBtoA);
        results.add(bToA);

        return results;
    }

    private int round(Double value) {
        return value != null ? (int) Math.round(value) : 0;
    }
}
