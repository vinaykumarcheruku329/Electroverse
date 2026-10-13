package com.electroverse.service;

import com.electroverse.dto.request.FeatureRequests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleServiceTest {

    private FeatureRequests.Lifecycle request(int repair, int upgrade, int support, int warranty, int energy) {
        return new FeatureRequests.Lifecycle(1L, repair, upgrade, support, warranty, energy,
                null, null, null, true);
    }

    @Test
    void computesWeightedOverallScore() {
        double overall = LifecycleService.computeOverall(request(80, 60, 100, 80, 40));
        assertThat(overall).isEqualTo(72.0);
    }

    @Test
    void perfectScoresProduceHundred() {
        assertThat(LifecycleService.computeOverall(request(100, 100, 100, 100, 100))).isEqualTo(100.0);
    }

    @Test
    void zeroScoresProduceZero() {
        assertThat(LifecycleService.computeOverall(request(0, 0, 0, 0, 0))).isEqualTo(0.0);
    }
}
