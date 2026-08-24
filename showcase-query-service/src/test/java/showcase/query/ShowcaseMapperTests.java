// SPDX-License-Identifier: MIT
package showcase.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import showcase.projection.ShowcaseEntity;

@DisplayName("Showcase mapper unit tests")
class ShowcaseMapperTests {

    private final ShowcaseMapper mapper = new ShowcaseMapperImpl();

    @Test
    @DisplayName("Mapping an entity to a DTO preserves all fields")
    void entityToDto_preservesAllFields() {
        val entity = ShowcaseEntity.builder()
                .showcaseId("33gkCN0UNn3Kzr3x7iuDaVT6sZi")
                .title("My Showcase")
                .startTime(Instant.parse("2026-08-01T10:00:00Z"))
                .duration(Duration.ofMinutes(5))
                .status(showcase.projection.ShowcaseStatus.STARTED)
                .scheduledAt(Instant.parse("2026-08-01T09:00:00Z"))
                .startedAt(Instant.parse("2026-08-01T10:00:00Z"))
                .finishedAt(null)
                .build();

        val dto = mapper.entityToDto(entity);
        assertThat(dto).isNotNull();
        val dtoStatus = dto.status();
        assertThat(dtoStatus).isNotNull();
        val entityStatus = entity.status();
        assertThat(entityStatus).isNotNull();

        assertThat(dto.showcaseId()).isEqualTo(entity.showcaseId());
        assertThat(dto.title()).isEqualTo(entity.title());
        assertThat(dto.startTime()).isEqualTo(entity.startTime());
        assertThat(dto.duration()).isEqualTo(entity.duration());
        assertThat(dtoStatus.name()).isEqualTo(entityStatus.name());
        assertThat(dto.scheduledAt()).isEqualTo(entity.scheduledAt());
        assertThat(dto.startedAt()).isEqualTo(entity.startedAt());
        assertThat(dto.finishedAt()).isEqualTo(entity.finishedAt());
    }

    @Test
    @DisplayName("Mapping a DTO to an entity preserves all fields")
    void dtoToEntity_preservesAllFields() {
        val dto = Showcase.builder()
                .showcaseId("33gkCN0UNn3Kzr3x7iuDaVT6sZi")
                .title("My Showcase")
                .startTime(Instant.parse("2026-08-01T10:00:00Z"))
                .duration(Duration.ofMinutes(5))
                .status(ShowcaseStatus.FINISHED)
                .scheduledAt(Instant.parse("2026-08-01T09:00:00Z"))
                .startedAt(Instant.parse("2026-08-01T10:00:00Z"))
                .finishedAt(Instant.parse("2026-08-01T10:05:00Z"))
                .build();

        val entity = mapper.dtoToEntity(dto);
        assertThat(entity).isNotNull();
        val entityStatus = entity.status();
        assertThat(entityStatus).isNotNull();
        val dtoStatus = dto.status();
        assertThat(dtoStatus).isNotNull();

        assertThat(entity.showcaseId()).isEqualTo(dto.showcaseId());
        assertThat(entity.title()).isEqualTo(dto.title());
        assertThat(entity.startTime()).isEqualTo(dto.startTime());
        assertThat(entity.duration()).isEqualTo(dto.duration());
        assertThat(entityStatus.name()).isEqualTo(dtoStatus.name());
        assertThat(entity.scheduledAt()).isEqualTo(dto.scheduledAt());
        assertThat(entity.startedAt()).isEqualTo(dto.startedAt());
        assertThat(entity.finishedAt()).isEqualTo(dto.finishedAt());
    }

    @Test
    @DisplayName("Mapping a null value returns null")
    void entityToDto_null_returnsNull() {
        assertThat(mapper.entityToDto(null)).isNull();
        assertThat(mapper.dtoToEntity(null)).isNull();
    }
}
