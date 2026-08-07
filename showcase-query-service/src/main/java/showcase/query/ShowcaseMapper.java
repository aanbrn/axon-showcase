package showcase.query;

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import showcase.projection.ShowcaseEntity;

/**
 * Maps between {@link ShowcaseEntity} persistence objects and {@link Showcase} DTOs.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@AnnotateWith(NullUnmarked.class)
interface ShowcaseMapper {
    /**
     * Converts an entity to a DTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO, or {@code null} if the entity is {@code null}
     */
    @Nullable Showcase entityToDto(@Nullable ShowcaseEntity entity);

    /**
     * Converts a DTO to an entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity, or {@code null} if the DTO is {@code null}
     */
    @Nullable ShowcaseEntity dtoToEntity(@Nullable Showcase dto);
}
