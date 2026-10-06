// SPDX-License-Identifier: MIT
package showcase.query;

import com.google.protobuf.ByteString;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ClassUtils;
import org.axonframework.messaging.GenericMessage;
import org.axonframework.messaging.MetaData;
import org.axonframework.queryhandling.GenericStreamingQueryMessage;
import org.axonframework.queryhandling.QueryResponseMessage;
import org.axonframework.queryhandling.StreamingQueryMessage;
import org.axonframework.serialization.SerializedMetaData;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.SimpleSerializedObject;
import org.axonframework.serialization.SimpleSerializedType;

/**
 * Maps between Axon streaming query messages/responses and their Protobuf {@link QueryRequest}/{@link QueryResponse}
 * representations.
 */
@RequiredArgsConstructor
@SuppressWarnings("ClassCanBeRecord")
public final class QueryMessageMapper {
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
        val requestBuilder = QueryRequest.newBuilder()
                .setQueryName(message.getQueryName())
                .setQueryIdentifier(message.getIdentifier())
                .setPayloadType(payload.getType().getName())
                .setSerializedPayload(ByteString.copyFrom(payload.getData()))
                .setSerializedMetaData(ByteString.copyFrom(metaData.getData()))
                .setResponseType(
                        message.getResponseType().getExpectedResponseType().getName());
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
        val payloadType = new SimpleSerializedType(
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

    /**
     * Converts the given query response message into a {@link QueryResponse}.
     *
     * @param message the response message to convert
     * @return the serialized query response
     */
    public QueryResponse messageToResponse(QueryResponseMessage<?> message) {
        val payload = message.serializePayload(messageSerializer, byte[].class);
        val metaData = message.serializeMetaData(messageSerializer, byte[].class);
        val responseBuilder = QueryResponse.newBuilder()
                .setPayloadType(payload.getType().getName())
                .setSerializedPayload(ByteString.copyFrom(payload.getData()))
                .setSerializedMetaData(ByteString.copyFrom(metaData.getData()));
        if (payload.getType().getRevision() != null) {
            responseBuilder.setPayloadRevision(payload.getType().getRevision());
        }
        return responseBuilder.build();
    }

    /**
     * Deserializes the payload of the given {@link QueryResponse} into the expected type.
     *
     * @param response    the response to deserialize
     * @param payloadType the expected payload type
     * @param <T>         the payload type
     * @return the deserialized payload
     */
    public <T> T payloadFromResponse(QueryResponse response, Class<T> payloadType) {
        val type = new SimpleSerializedType(
                response.getPayloadType(),
                Optional.of(response)
                        .filter(QueryResponse::hasPayloadRevision)
                        .map(QueryResponse::getPayloadRevision)
                        .orElse(null));
        val serialized =
                new SimpleSerializedObject<>(response.getSerializedPayload().toByteArray(), byte[].class, type);
        return payloadType.cast(messageSerializer.deserialize(serialized));
    }
}
