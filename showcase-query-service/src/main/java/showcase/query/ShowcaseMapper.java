package showcase.query;

import org.mapstruct.Mapper;
import showcase.projection.ShowcaseEntity;

@Mapper
interface ShowcaseMapper {

    Showcase entityToDto(ShowcaseEntity entity);

    ShowcaseEntity dtoToEntity(Showcase showcase);
}
