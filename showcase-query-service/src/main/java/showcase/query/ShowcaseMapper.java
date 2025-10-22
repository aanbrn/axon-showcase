package showcase.query;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import showcase.projection.ShowcaseEntity;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
interface ShowcaseMapper {

    Showcase entityToDto(ShowcaseEntity entity);

    ShowcaseEntity dtoToEntity(Showcase dto);
}
