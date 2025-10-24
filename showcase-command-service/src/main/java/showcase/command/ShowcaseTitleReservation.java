package showcase.command;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
final class ShowcaseTitleReservation {

    static class DuplicateTitleException extends RuntimeException {

        DuplicateTitleException(Throwable cause) {
            super("Given title is reserved already", cause);
        }
    }

    private final JdbcClient jdbcClient;

    void save(String title) throws DuplicateTitleException {
        try {
            jdbcClient.sql("INSERT INTO showcase_title_reservation (title) VALUES (lower(:title))")
                      .param("title", title)
                      .update();
        } catch (DuplicateKeyException e) {
            throw new DuplicateTitleException(e);
        }
    }

    void delete(String title) {
        jdbcClient.sql("DELETE FROM showcase_title_reservation WHERE title = lower(:title)")
                  .param("title", title)
                  .update();
    }
}
