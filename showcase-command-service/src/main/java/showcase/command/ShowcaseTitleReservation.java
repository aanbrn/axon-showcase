package showcase.command;

import lombok.NonNull;
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

    void save(@NonNull String title) throws DuplicateTitleException {
        try {
            jdbcClient.sql("INSERT INTO showcase_title_reservation (title) VALUES (:title)")
                      .param("title", title)
                      .update();
        } catch (DuplicateKeyException e) {
            throw new DuplicateTitleException(e);
        }
    }

    void delete(@NonNull String title) {
        jdbcClient.sql("DELETE FROM showcase_title_reservation WHERE title = :title")
                  .param("title", title)
                  .update();
    }
}
