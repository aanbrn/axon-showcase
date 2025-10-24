package showcase.query;

import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import showcase.projection.ShowcaseEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
@AnnotateWith(NullUnmarked.class)
interface ShowcaseMapper {

    @Nullable Showcase entityToDto(@Nullable ShowcaseEntity entity);

    @Nullable ShowcaseEntity dtoToEntity(@Nullable Showcase dto);
}
