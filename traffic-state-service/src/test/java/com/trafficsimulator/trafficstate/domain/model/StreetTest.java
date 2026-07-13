package com.trafficsimulator.trafficstate.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreetTest {

    private static Street streetWithCapacity(int capacity) {
        return new Street("s1", "Main Ave", "A", "B", capacity);
    }

    @Test
    void newStreetStartsWithZeroVolumeAndIsFree() {
        Street street = streetWithCapacity(100);

        assertEquals(0, street.currentVolume());
        assertEquals(0.0, street.congestionRatio());
        assertEquals(CongestionLevel.FREE, street.congestionLevel());
        assertFalse(street.isJammed());
    }

    @Test
    void computesCongestionRatioAndLevel() {
        Street street = new Street("s1", "Main Ave", "A", "B", 100, 90);

        assertEquals(0.9, street.congestionRatio());
        assertEquals(CongestionLevel.JAMMED, street.congestionLevel());
        assertTrue(street.isJammed());
    }

    @Test
    void injectFlowAccumulatesVolume() {
        Street street = streetWithCapacity(100);

        street.injectFlow(40);
        assertEquals(CongestionLevel.FREE, street.congestionLevel());

        street.injectFlow(45);
        assertEquals(85, street.currentVolume());
        assertEquals(CongestionLevel.JAMMED, street.congestionLevel());
    }

    @Test
    void volumeMayExceedCapacityWhenOversaturated() {
        Street street = new Street("s1", "Main Ave", "A", "B", 100, 150);

        assertEquals(1.5, street.congestionRatio());
        assertEquals(CongestionLevel.JAMMED, street.congestionLevel());
    }

    @Test
    void releaseFlowNeverDropsBelowZero() {
        Street street = new Street("s1", "Main Ave", "A", "B", 100, 30);

        street.releaseFlow(50);

        assertEquals(0, street.currentVolume());
        assertEquals(CongestionLevel.FREE, street.congestionLevel());
    }

    @Test
    void updateVolumeReplacesAbsoluteValue() {
        Street street = streetWithCapacity(100);

        street.updateVolume(60);

        assertEquals(60, street.currentVolume());
        assertEquals(CongestionLevel.HEAVY, street.congestionLevel());
    }

    @Test
    void rejectsNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> streetWithCapacity(0));
        assertThrows(IllegalArgumentException.class, () -> streetWithCapacity(-10));
    }

    @Test
    void rejectsNegativeInitialVolume() {
        assertThrows(IllegalArgumentException.class,
                () -> new Street("s1", "Main Ave", "A", "B", 100, -1));
    }

    @Test
    void rejectsBlankIdentifiers() {
        assertThrows(IllegalArgumentException.class,
                () -> new Street(" ", "Main Ave", "A", "B", 100));
        assertThrows(IllegalArgumentException.class,
                () -> new Street("s1", "", "A", "B", 100));
        assertThrows(IllegalArgumentException.class,
                () -> new Street("s1", "Main Ave", null, "B", 100));
    }

    @Test
    void rejectsNegativeFlowMutations() {
        Street street = streetWithCapacity(100);

        assertThrows(IllegalArgumentException.class, () -> street.injectFlow(-1));
        assertThrows(IllegalArgumentException.class, () -> street.releaseFlow(-1));
        assertThrows(IllegalArgumentException.class, () -> street.updateVolume(-1));
    }

    @Test
    void identityIsDefinedById() {
        Street a = new Street("s1", "Main Ave", "A", "B", 100, 10);
        Street sameId = new Street("s1", "Other Name", "X", "Y", 200, 50);
        Street differentId = new Street("s2", "Main Ave", "A", "B", 100, 10);

        assertEquals(a, sameId);
        assertEquals(a.hashCode(), sameId.hashCode());
        assertNotEquals(a, differentId);
    }
}
