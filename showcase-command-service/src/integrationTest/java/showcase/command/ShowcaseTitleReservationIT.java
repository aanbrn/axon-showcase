package showcase.command;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import showcase.command.ShowcaseTitleReservation.DuplicateTitleException;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static showcase.command.RandomCommandTestUtils.aShowcaseTitle;
import static showcase.command.RandomCommandTestUtils.aTooLongShowcaseTitle;

@JdbcTest
@DirtiesContext
@Testcontainers
class ShowcaseTitleReservationIT {

    @Configuration
    @ComponentScan(excludeFilters = @Filter(type = ASSIGNABLE_TYPE, classes = ShowcaseCommandApplication.class))
    static class TestConfig {
    }

    @Container
    @ServiceConnection
    @SuppressWarnings({ "resource", "unused" })
    private static final PostgreSQLContainer<?> dbEvents =
            new PostgreSQLContainer<>("postgres:" + System.getProperty("postgres.image.version"))
                    .waitingFor(Wait.forListeningPort());

    @Autowired
    private ShowcaseTitleReservation showcaseTitleReservation;

    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void save_uniqueTitle_savesSuccessfully() {
        val title = aShowcaseTitle();

        showcaseTitleReservation.save(title);

        assertThat(jdbcClient.sql("SELECT title FROM showcase_title_reservation")
                             .query(String.class)
                             .single())
                .isEqualTo(title.toLowerCase(Locale.ROOT));
    }

    @Test
    void save_alreadyUsedTitle_throwsDuplicateKeyException() {
        val title = aShowcaseTitle();

        jdbcClient.sql("INSERT INTO showcase_title_reservation (title) VALUES (:title)")
                  .param("title", title.toLowerCase(Locale.ROOT))
                  .update();

        assertThatThrownBy(() -> showcaseTitleReservation.save(title))
                .isExactlyInstanceOf(DuplicateTitleException.class)
                .hasMessageContaining("Given title is reserved already")
                .hasCauseInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void save_tooLongTitle_throwsDataIntegrityViolationException() {
        assertThatExceptionOfType(DataIntegrityViolationException.class).isThrownBy(
                () -> showcaseTitleReservation.save(aTooLongShowcaseTitle()));
    }

    @Test
    void deleteByShowcaseId_existingReservation_deletesSuccessfully() {
        val title = aShowcaseTitle();

        assertThat(jdbcClient.sql("INSERT INTO showcase_title_reservation (title) VALUES (:title)")
                             .param("title", title.toLowerCase(Locale.ROOT))
                             .update())
                .isOne();

        showcaseTitleReservation.delete(title);

        assertThat(jdbcClient.sql("SELECT COUNT(*) FROM showcase_title_reservation")
                             .query(Long.TYPE)
                             .single())
                .isZero();
    }
}
