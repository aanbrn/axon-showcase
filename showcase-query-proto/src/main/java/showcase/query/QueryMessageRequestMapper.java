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

@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public final class QueryMessageRequestMapper {

    private final Serializer messageSerializer;

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
