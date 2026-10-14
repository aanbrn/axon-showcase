// SPDX-License-Identifier: MIT
package showcase.loadtests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Baseline drift tests")
class BaselineDriftTests {

    private static final String TARGET = "http://axon-showcase-api";

    @Test
    @DisplayName("A figure within the tolerance does not regress and the reference is written")
    void withinTolerance_writesTheReferenceWithoutRegression() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 12, 22, 33), 0.5, false);

        assertThat(drift.comparable()).isTrue();
        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.figures()).hasSize(3);
        assertThat(drift.figures().get(0).deltaMs()).isEqualTo(2);
    }

    @Test
    @DisplayName("A mean figure beyond the tolerance regresses, withholds the reference, and is named")
    void beyondTolerance_withholdsAndNamesTheFigure() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 16, 22, 33), 0.5, false);

        assertThat(drift.regressed()).isTrue();
        assertThat(drift.writeReference()).isFalse();
        assertThat(drift.regressedFigures())
                .extracting(BaselineDrift.Figure::label)
                .containsExactly("FetchShowcases.meanMs");
        assertThat(drift.report()).contains("REGRESSED").contains("withholding");
    }

    @Test
    @DisplayName("An improvement is reported as a negative delta and never a failure")
    void improvement_isReportedButNeverFails() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 8, 18, 28), 0.5, false);

        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.figures().get(0).deltaMs()).isEqualTo(-2);
    }

    @Test
    @DisplayName("A figure the reference does not record is not compared and does not regress")
    void missingFigure_isNotCompared() {
        val recorded = BaselineReference.builder()
                .target(TARGET)
                .operatingPoint(OPERATING_POINT)
                .requests(Map.of(
                        "FetchShowcases",
                        BaselineReference.Figures.builder().meanMs(10).p99Ms(15).build()))
                .build();
        val drift = BaselineDrift.compare(recorded, reference(TARGET, 12, 40, 20), 0.5, false);

        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.figures())
                .filteredOn(figure -> BaselineReference.P95.equals(figure.metric()))
                .singleElement()
                .satisfies(figure -> assertThat(figure.recordedMs()).isNull());
    }

    @Test
    @DisplayName("A reference recorded for another target has nothing to compare and writes the measurement")
    void anotherTarget_hasNothingToCompare() {
        val drift = BaselineDrift.compare(
                reference("http://another-target", 10, 20, 30), reference(TARGET, 12, 22, 33), 0.5, false);

        assertThat(drift.comparable()).isFalse();
        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.recordedTarget()).isEqualTo("http://another-target");
        assertThat(drift.report()).contains("http://another-target").contains(TARGET);
    }

    @Test
    @DisplayName("An intended refresh accepts a regression beyond the tolerance and writes the reference")
    void intendedRefresh_acceptsARegression() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 16, 22, 33), 0.5, true);

        assertThat(drift.regressed()).isTrue();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.report()).contains("regression accepted").doesNotContain("within the tolerance");
    }

    @Test
    @DisplayName("No recorded reference has nothing to compare and writes the measurement")
    void nothingRecorded_hasNothingToCompare() {
        val drift = BaselineDrift.compare(null, reference(TARGET, 12, 22, 33), 0.5, false);

        assertThat(drift.comparable()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.recordedTarget()).isNull();
        assertThat(drift.report()).contains("no reference recorded").contains(TARGET);
    }

    @Test
    @DisplayName("An operating point within the matching band still compares and can regress")
    void operatingPointWithinTheBand_stillCompares() {
        val drift = BaselineDrift.compare(
                reference(TARGET, 124, 10, 20, 30), reference(TARGET, 127, 16, 22, 33), 0.5, false);

        assertThat(drift.comparable()).isTrue();
        assertThat(drift.regressed()).isTrue();
        assertThat(drift.regressedFigures())
                .extracting(BaselineDrift.Figure::label)
                .containsExactly("FetchShowcases.meanMs");
    }

    @Test
    @DisplayName("An operating point beyond the matching band is not compared and writes the measurement")
    void operatingPointBeyondTheBand_isNotCompared() {
        val drift = BaselineDrift.compare(
                reference(TARGET, 124, 10, 20, 30), reference(TARGET, 300, 16, 22, 33), 0.5, false);

        assertThat(drift.comparable()).isFalse();
        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.report()).contains("124 units/s").contains("300").contains("nothing to compare");
    }

    @Test
    @DisplayName("A reference that records no operating point is not compared and writes the measurement")
    void noRecordedOperatingPoint_isNotCompared() {
        val recorded = BaselineReference.builder()
                .target(TARGET)
                .requests(Map.of(
                        "FetchShowcases",
                        BaselineReference.Figures.builder()
                                .meanMs(10)
                                .p95Ms(20)
                                .p99Ms(30)
                                .build()))
                .build();
        val drift = BaselineDrift.compare(recorded, reference(TARGET, 127, 16, 22, 33), 0.5, false);

        assertThat(drift.comparable()).isFalse();
        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.report()).contains("records no operating point").contains("nothing to compare");
    }

    @Test
    @DisplayName("A small recorded figure is held above the noise by its floor")
    void floor_holdsSmallFiguresAboveNoise() {
        val drift = BaselineDrift.compare(reference(TARGET, 2, 20, 30), reference(TARGET, 4, 22, 33), 0.5, false);

        assertThat(drift.regressed()).isFalse();
        assertThat(drift.figures().get(0).thresholdMs()).isEqualTo(5);
    }

    @Test
    @DisplayName("A measurement that records no figures withholds the reference without writing")
    void emptyMeasurement_withholdsTheReference() {
        val measured =
                BaselineReference.builder().target(TARGET).requests(Map.of()).build();
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), measured, 0.5, true);

        assertThat(drift.measuredEmpty()).isTrue();
        assertThat(drift.regressed()).isFalse();
        assertThat(drift.writeReference()).isFalse();
        assertThat(drift.report()).contains("no request figures were measured").doesNotContain("tolerance");
    }

    @Test
    @DisplayName("A p99-only excursion beyond the tolerance is unconfirmed, withholds the reference,"
            + " and names the confirmation")
    void p99OnlyExcursion_isUnconfirmedAndWithholdsTheReference() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 12, 22, 50), 0.5, false);

        assertThat(drift.regressed()).isFalse();
        assertThat(drift.unconfirmed()).isTrue();
        assertThat(drift.writeReference()).isFalse();
        assertThat(drift.unconfirmedFigures())
                .extracting(BaselineDrift.Figure::label)
                .containsExactly("FetchShowcases.p99Ms");
        assertThat(drift.regressedFigures()).isEmpty();
        assertThat(drift.report()).contains("99th-percentile excursion").contains("re-run at the same operating point");
    }

    @Test
    @DisplayName("A 95th-percentile figure beyond the tolerance regresses and withholds the reference")
    void p95Regression_regressesAndWithholdsTheReference() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 12, 34, 33), 0.5, false);

        assertThat(drift.regressed()).isTrue();
        assertThat(drift.unconfirmed()).isFalse();
        assertThat(drift.writeReference()).isFalse();
        assertThat(drift.regressedFigures())
                .extracting(BaselineDrift.Figure::label)
                .containsExactly("FetchShowcases.p95Ms");
    }

    @Test
    @DisplayName("A mean regression with a p99 excursion is a regression and reports both figures")
    void meanRegressionWithP99Excursion_isARegressionReportingBoth() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 16, 22, 50), 0.5, false);

        assertThat(drift.regressed()).isTrue();
        assertThat(drift.unconfirmed()).isFalse();
        assertThat(drift.writeReference()).isFalse();
        assertThat(drift.regressedFigures())
                .extracting(BaselineDrift.Figure::label)
                .containsExactly("FetchShowcases.meanMs");
        assertThat(drift.report())
                .contains("meanMs")
                .contains("REGRESSED")
                .contains("p99Ms")
                .contains("UNCONFIRMED")
                .contains("regressed beyond the tolerance")
                .contains("need a re-run at the same operating point");
    }

    @Test
    @DisplayName("An intended refresh accepts a p99-only excursion and writes the reference")
    void intendedRefresh_acceptsAP99OnlyExcursion() {
        val drift = BaselineDrift.compare(reference(TARGET, 10, 20, 30), reference(TARGET, 12, 22, 50), 0.5, true);

        assertThat(drift.regressed()).isFalse();
        assertThat(drift.unconfirmed()).isTrue();
        assertThat(drift.writeReference()).isTrue();
        assertThat(drift.report()).contains("with it accepted");
    }

    /**
     * The operating point the drift tests' references ran at, unless a case supplies another.
     */
    private static final int OPERATING_POINT = 127;

    /**
     * Builds a single-request reference at the default operating point for the drift tests.
     *
     * @param target the target measured
     * @param meanMs the mean response time in milliseconds
     * @param p95Ms the 95th-percentile response time in milliseconds
     * @param p99Ms the 99th-percentile response time in milliseconds
     * @return the reference
     */
    private static BaselineReference reference(
            String target, @Nullable Integer meanMs, @Nullable Integer p95Ms, @Nullable Integer p99Ms) {
        return reference(target, OPERATING_POINT, meanMs, p95Ms, p99Ms);
    }

    /**
     * Builds a single-request reference at the given operating point for the drift tests.
     *
     * @param target the target measured
     * @param operatingPoint the operating point the plateau ran at, in workload units per second
     * @param meanMs the mean response time in milliseconds
     * @param p95Ms the 95th-percentile response time in milliseconds
     * @param p99Ms the 99th-percentile response time in milliseconds
     * @return the reference
     */
    private static BaselineReference reference(
            String target,
            int operatingPoint,
            @Nullable Integer meanMs,
            @Nullable Integer p95Ms,
            @Nullable Integer p99Ms) {
        return BaselineReference.builder()
                .target(target)
                .operatingPoint(operatingPoint)
                .factor(5)
                .floorMeanMs(50)
                .floorP95Ms(100)
                .floorP99Ms(200)
                .requests(Map.of(
                        "FetchShowcases",
                        BaselineReference.Figures.builder()
                                .meanMs(meanMs)
                                .p95Ms(p95Ms)
                                .p99Ms(p99Ms)
                                .build()))
                .build();
    }
}
