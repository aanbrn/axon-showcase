package showcase.query;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import showcase.projection.ShowcaseEntity;

@Mapper
interface ShowcaseMapper {

    Showcase entityToDto(ShowcaseEntity entity);

    @Mapping(target = "version", ignore = true)
    ShowcaseEntity dtoToEntity(Showcase showcase);
}
