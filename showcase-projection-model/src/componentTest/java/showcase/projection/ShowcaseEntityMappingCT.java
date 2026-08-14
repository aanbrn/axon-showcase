package showcase.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.index.MappingBuilder;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Showcase entity OpenSearch mapping component tests")
class ShowcaseEntityMappingCT {

    private final SimpleElasticsearchMappingContext mappingContext = new SimpleElasticsearchMappingContext();
    private final MappingElasticsearchConverter converter = new MappingElasticsearchConverter(mappingContext);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("The showcase entity maps to the showcases index with the showcaseId sort setting")
    void indexAndSortSetting() throws Exception {
        converter.afterPropertiesSet();
        val entity = mappingContext.getRequiredPersistentEntity(ShowcaseEntity.class);

        assertThat(entity.getIndexCoordinates().getIndexName()).isEqualTo("showcases");

        val settings = objectMapper.readTree(entity.getDefaultSettings().toJson());
        assertThat(settings.at("/index/sort/field/0").asText()).isEqualTo("showcaseId");
        assertThat(settings.at("/index/sort/order/0").asText()).isEqualTo("desc");
    }

    @Test
    @DisplayName("The showcase entity fields are mapped with the expected OpenSearch types and date formats")
    void fieldMapping() throws Exception {
        converter.afterPropertiesSet();
        val mapping = objectMapper.readTree(new MappingBuilder(converter).buildPropertyMapping(ShowcaseEntity.class));

        assertThat(mapping.at("/properties/showcaseId/type").asText()).isEqualTo("keyword");
        assertThat(mapping.at("/properties/title/type").asText()).isEqualTo("text");
        assertThat(mapping.at("/properties/startTime/type").asText()).isEqualTo("date_nanos");
        assertThat(mapping.at("/properties/startTime/format").asText()).isEqualTo("strict_date_optional_time_nanos");
        assertThat(mapping.at("/properties/status/type").asText()).isEqualTo("keyword");
        assertThat(mapping.at("/properties/scheduledAt/type").asText()).isEqualTo("date_nanos");
        assertThat(mapping.at("/properties/scheduledAt/format").asText()).isEqualTo("strict_date_optional_time_nanos");
        assertThat(mapping.at("/properties/startedAt/type").asText()).isEqualTo("date_nanos");
        assertThat(mapping.at("/properties/startedAt/format").asText()).isEqualTo("strict_date_optional_time_nanos");
        assertThat(mapping.at("/properties/finishedAt/type").asText()).isEqualTo("date_nanos");
        assertThat(mapping.at("/properties/finishedAt/format").asText()).isEqualTo("strict_date_optional_time_nanos");
    }

    @Test
    @DisplayName("The showcase entity mapping has no unexpected fields")
    void noUnexpectedFields() throws Exception {
        converter.afterPropertiesSet();
        val properties = objectMapper.readTree(new MappingBuilder(converter).buildPropertyMapping(ShowcaseEntity.class))
                                     .path("properties");

        assertThat(properties.size()).isEqualTo(8);
        assertThat(properties.has("_class")).isTrue();
        assertThat(properties.has("duration")).isFalse();
    }
}
