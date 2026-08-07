package showcase.query;

import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.axonframework.messaging.GenericMessage;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.queryhandling.StreamingQueryMessage;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.SimpleSerializedType;

import java.util.Optional;

/**
 * Maps between Axon streaming query messages and their Protobuf {@link QueryRequest} representations.
 */
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public final class QueryMessageRequestMapper {
    /**
     * The serializer used to serialize and deserialize payloads and metadata.
     */
    private final Serializer messageSerializer;

    /**
     * Converts the given streaming query message into a {@link QueryRequest}.
     *
     * @param message the message to convert
     * @return the serialized query request
     */
    public QueryRequest messageToRequest(StreamingQueryMessage<?, ?> message) {
        val payload = message.serializePayload(messageSerializer, byte[].class);
        val metaData = message.serializeMetaData(messageSerializer, byte[].class);
        val requestBuilder =
                QueryRequest
                        .newBuilder()
                        .setQueryName(message.getQueryName())
                        .setQueryIdentifier(message.getIdentifier())
                        .setPayloadType(payload.getType().getName())
                        .setSerializedPayload(ByteString.copyFrom(payload.getData()))
                        .setSerializedMetaData(ByteString.copyFrom(metaData.getData()))
                        .setResponseType(message.getResponseType().getExpectedResponseType().getName());
        if (payload.getType().getRevision() != null) {
            requestBuilder.setPayloadRevision(payload.getType().getRevision());
        }
        return requestBuilder.build();
    }

    /**
     * Converts the given {@link QueryRequest} into a streaming query message.
     *
     * @param request the request to convert
     * @return the deserialized streaming query message
     * @throws ClassNotFoundException if the response type class cannot be resolved
     */
    public StreamingQueryMessage<?, ?> requestToMessage(QueryRequest request) throws ClassNotFoundException {
        val payloadType =
                new SimpleSerializedType(
                        request.getPayloadType(),
                        Optional.of(request)
                                .filter(QueryRequest::hasPayloadRevision)
                                .map(QueryRequest::getPayloadRevision)
                                .orElse(null));
        val payload = messageSerializer.deserialize(
                new SimpleSerializedObject<>(request.getSerializedPayload().toByteArray(), byte[].class, payloadType));
        val metaData = messageSerializer.<byte[], MetaData>deserialize(
                new SerializedMetaData<>(request.getSerializedMetaData().toByteArray(), byte[].class));
        val responseType = ClassUtils.getClass(request.getResponseType());
        return new GenericStreamingQueryMessage<>(
                new GenericMessage<>(request.getQueryIdentifier(), payload, metaData),
                request.getQueryName(),
                responseType);
    }
}
