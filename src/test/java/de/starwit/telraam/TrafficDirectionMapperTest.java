package de.starwit.telraam;

import de.starwit.telraam.config.SegmentMappingProperties;
import de.starwit.telraam.config.SegmentMappingProperties.SegmentMapping;
import de.starwit.telraam.dto.dave.DetectionDTO;
import de.starwit.telraam.dto.telraam.TrafficRecord;
import de.starwit.telraam.mapper.TrafficDirectionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TrafficDirectionMapperTest {

    private static final String   SEGMENT_ID   = "9000001463";
    private static final UUID   ZAEHLUNG_ID  = UUID.randomUUID();
    private static final int    DIR_A_TO_B   = 1; // North
    private static final int    DIR_B_TO_A   = 2; // South

    private TrafficDirectionMapper mapper;

    @BeforeEach
    void setUp() {
        SegmentMapping mapping = new SegmentMapping();
        mapping.setSegmentId(SEGMENT_ID);
        mapping.setZaehlungId(ZAEHLUNG_ID);
        mapping.setDirectionAtoB(DIR_A_TO_B);
        mapping.setDirectionBtoA(DIR_B_TO_A);

        SegmentMappingProperties props = new SegmentMappingProperties();
        props.setSegmentMapping(List.of(mapping));

        mapper = new TrafficDirectionMapper(props);
    }

    private TrafficRecord record(int direction,
                                  double carLft, double carRgt,
                                  double bikeLft, double bikeRgt,
                                  double pedLft,  double pedRgt,
                                  double heavyLft,double heavyRgt,
                                  double motoLft, double motoRgt) {
        return new TrafficRecord(
                OffsetDateTime.parse("2024-06-01T08:00:00Z"),
                SEGMENT_ID, direction,
                pedLft + pedRgt,   pedLft,   pedRgt,
                bikeLft + bikeRgt, bikeLft,  bikeRgt,
                motoLft + motoRgt, motoLft,  motoRgt,
                carLft  + carRgt,  carLft,   carRgt,
                heavyLft+ heavyRgt,heavyLft, heavyRgt,
                0.9, null
        );
    }

    @Test
    void trueDirection_lftIsAtoB() {
        TrafficRecord rec = record(1, 10, 5, 3, 2, 1, 4, 0, 0, 0, 0);
        List<DetectionDTO> dtos = mapper.map(rec);

        assertThat(dtos).hasSize(2);

        DetectionDTO aToB = dtos.get(0);
        assertThat(aToB.getVon()).isEqualTo(DIR_A_TO_B);
        assertThat(aToB.getNach()).isEqualTo(DIR_B_TO_A);
        assertThat(aToB.getPkw()).isEqualTo(10);     // carLft
        assertThat(aToB.getFahrradfahrer()).isEqualTo(3); // bikeLft
        assertThat(aToB.getFussgaenger()).isEqualTo(1);   // pedLft

        DetectionDTO bToA = dtos.get(1);
        assertThat(bToA.getVon()).isEqualTo(DIR_B_TO_A);
        assertThat(bToA.getNach()).isEqualTo(DIR_A_TO_B);
        assertThat(bToA.getPkw()).isEqualTo(5);      // carRgt
    }

    @Test
    void falseDirection_rgtIsAtoB() {
        TrafficRecord rec = record(0, 10, 5, 3, 2, 1, 4, 0, 0, 0, 0);
        List<DetectionDTO> dtos = mapper.map(rec);

        assertThat(dtos).hasSize(2);
        // direction=0: rgt becomes A→B
        assertThat(dtos.get(0).getPkw()).isEqualTo(5);  // carRgt
        assertThat(dtos.get(1).getPkw()).isEqualTo(10); // carLft
    }

    @Test
    void unmappedSegment_returnsEmptyList() {
        TrafficRecord rec = record(1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        // change segment ID to one that isn't configured
        TrafficRecord unknown = new TrafficRecord(
                rec.date(), "9999999", rec.direction(),
                rec.pedestrian(), rec.pedestrianLft(), rec.pedestrianRgt(),
                rec.bike(), rec.bikeLft(), rec.bikeRgt(),
                rec.motorbike(), rec.motorbikeLft(), rec.motorbikeRgt(),
                rec.car(), rec.carLft(), rec.carRgt(),
                rec.heavy(), rec.heavyLft(), rec.heavyRgt(),
                rec.uptime(), rec.v85()
        );
        assertThat(mapper.map(unknown)).isEmpty();
    }

    @Test
    void zaehlungIdIsSetCorrectly() {
        TrafficRecord rec = record(1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(mapper.map(rec))
                .allMatch(dto -> ZAEHLUNG_ID.equals(dto.getZaehlungId()));
    }

    @Test
    void intervalIsExactly15Minutes() {
        TrafficRecord rec = record(1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        DetectionDTO dto = mapper.map(rec).get(0);
        long minutes = (dto.getEndeUhrzeit().getEpochSecond() -
                        dto.getStartUhrzeit().getEpochSecond()) / 60;
        assertThat(minutes).isEqualTo(15);
    }

    @Test
    void nullCounts_defaultToZero() {
        TrafficRecord rec = new TrafficRecord(
                OffsetDateTime.parse("2024-06-01T08:00:00Z"),
                SEGMENT_ID, 1,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null
        );
        DetectionDTO dto = mapper.map(rec).get(0);
        assertThat(dto.getPkw()).isZero();
        assertThat(dto.getFahrradfahrer()).isZero();
        assertThat(dto.getFussgaenger()).isZero();
    }
}
