package com.trafficsimulator.trafficstate.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CongestionLevelTest {

    @ParameterizedTest
    @CsvSource({
            "0.00, FREE",
            "0.49, FREE",
            "0.50, HEAVY",   // lower boundary is HEAVY
            "0.65, HEAVY",
            "0.80, HEAVY",   // upper boundary is still HEAVY
            "0.81, JAMMED",
            "1.00, JAMMED",
            "1.50, JAMMED"   // oversaturated
    })
    void classifiesRatioAtThresholdBoundaries(double ratio, CongestionLevel expected) {
        assertEquals(expected, CongestionLevel.fromRatio(ratio));
    }

    @Test
    void rejectsNegativeRatio() {
        assertThrows(IllegalArgumentException.class, () -> CongestionLevel.fromRatio(-0.1));
    }

    @Test
    void rejectsNonFiniteRatio() {
        assertThrows(IllegalArgumentException.class, () -> CongestionLevel.fromRatio(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> CongestionLevel.fromRatio(Double.POSITIVE_INFINITY));
    }

    @Test
    void exposesUiColor() {
        assertEquals("Green", CongestionLevel.FREE.color());
        assertEquals("Yellow", CongestionLevel.HEAVY.color());
        assertEquals("Red", CongestionLevel.JAMMED.color());
    }
}
