package showcase.query;

import com.google.protobuf.ByteString;
import lombok.val;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.GenericQueryMessage;
import org.axonframework.serialization.Revision;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static showcase.test.RandomTestUtils.anAlphabeticString;

class QueryMessageRequestMapperCT {

    private record Payload(String value) {
    }

    @Revision("1")
    private record PayloadWithRevision(String value) {
    }

    private static class Response {
    }

    static List<Arguments> payloadAndMetaData() {
        return List.of(
                argumentSet("Payload without Revision",
                            new Payload(anAlphabeticString(12)),
                            MetaData.with(anAlphabeticString(12), anAlphabeticString(12))),
                argumentSet("Payload with Revision",
                            new PayloadWithRevision(anAlphabeticString(12)),
                            MetaData.with(anAlphabeticString(12), anAlphabeticString(12))));
    }

    private final Serializer messageSerializer = JacksonSerializer.defaultSerializer();

    @ParameterizedTest
    @MethodSource("payloadAndMetaData")
    void messageToRequest(Object payload, MetaData metaData) {
        val message = new GenericQueryMessage<>(payload, ResponseTypes.instanceOf(Response.class))
                              .withMetaData(metaData);

        val serializedPayload = messageSerializer.serialize(message.getPayload(), byte[].class);
        val serializedMetaData = messageSerializer.serialize(message.getMetaData(), byte[].class);

        val queryRequest = new QueryMessageRequestMapper(messageSerializer).messageToRequest(message);
        assertThat(queryRequest).isNotNull();
        assertThat(queryRequest.getQueryName()).isEqualTo(message.getQueryName());
        assertThat(queryRequest.getQueryIdentifier()).isEqualTo(message.getIdentifier());
        assertThat(queryRequest.getPayloadType()).isEqualTo(serializedPayload.getType().getName());
        if (serializedPayload.getType().getRevision() != null) {
            assertThat(queryRequest.hasPayloadRevision()).isTrue();
            assertThat(queryRequest.getPayloadRevision()).isEqualTo(serializedPayload.getType().getRevision());
        } else {
            assertThat(queryRequest.hasPayloadRevision()).isFalse();
        }
        assertThat(queryRequest.getSerializedPayload()).isEqualTo(ByteString.copyFrom(serializedPayload.getData()));
        assertThat(queryRequest.getSerializedMetaData()).isEqualTo(ByteString.copyFrom(serializedMetaData.getData()));
        assertThat(queryRequest.getExpectedResponseType()).isEqualTo(Response.class.getName());
    }

    @ParameterizedTest
    @MethodSource("payloadAndMetaData")
    void requestToMessage(Object payload, MetaData metaData) throws Exception {
        val serializedPayload = messageSerializer.serialize(payload, byte[].class);
        val serializedMetaData = messageSerializer.serialize(metaData, byte[].class);

        val queryRequestBuilder =
                QueryRequest
                        .newBuilder()
                        .setQueryName(Payload.class.getName())
                        .setQueryIdentifier(randomUUID().toString())
                        .setPayloadType(serializedPayload.getType().getName())
                        .setSerializedPayload(ByteString.copyFrom(serializedPayload.getData()))
                        .setSerializedMetaData(ByteString.copyFrom(serializedMetaData.getData()))
                        .setExpectedResponseType(Response.class.getName());
        if (serializedPayload.getType().getRevision() != null) {
            queryRequestBuilder.setPayloadRevision(serializedPayload.getType().getRevision());
        }
        val queryRequest = queryRequestBuilder.build();

        val queryMessage = new QueryMessageRequestMapper(messageSerializer).requestToMessage(queryRequest);
        assertThat(queryMessage).isNotNull();
        assertThat(queryMessage.getQueryName()).isEqualTo(queryRequest.getQueryName());
        assertThat(queryMessage.getIdentifier()).isEqualTo(queryRequest.getQueryIdentifier());
        assertThat(queryMessage.getPayloadType().getName()).isEqualTo(queryRequest.getPayloadType());
        assertThat(queryMessage.getPayload()).isEqualTo(payload);
        assertThat(queryMessage.getMetaData()).isEqualTo(metaData);
        assertThat(queryMessage.getResponseType()).isEqualTo(ResponseTypes.instanceOf(Response.class));
    }
}
