package showcase.command;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Manages uniqueness of showcase titles using a database row-level lock.
 *
 * <p>Titles are stored in lowercase to enforce case-insensitive uniqueness.
 */
@Component
@RequiredArgsConstructor
final class ShowcaseTitleReservation {
    /**
     * Thrown when a title is already reserved by another showcase.
     */
    static class DuplicateTitleException extends RuntimeException {

        /**
         * Creates the exception with the given cause.
         *
         * @param cause the underlying duplicate key cause
         */
        DuplicateTitleException(Throwable cause) {
            super("Given title is reserved already", cause);
        }
    }

    /** The JDBC client used to access the title reservation table. */
    private final JdbcClient jdbcClient;

    /**
     * Reserves a title for a new showcase.
     *
     * @param title the title to reserve (stored in lowercase)
     * @throws DuplicateTitleException if the title is already reserved
     */
    void save(String title) throws DuplicateTitleException {
        try {
            jdbcClient.sql("INSERT INTO showcase_title_reservation (title) VALUES (lower(:title))")
                      .param("title", title)
                      .update();
        } catch (DuplicateKeyException e) {
            throw new DuplicateTitleException(e);
        }
    }

    /**
     * Releases a previously reserved title so it can be reused.
     *
     * @param title the title to release (matched case-insensitively)
     */
    void delete(String title) {
        jdbcClient.sql("DELETE FROM showcase_title_reservation WHERE title = lower(:title)")
                  .param("title", title)
                  .update();
    }
}
