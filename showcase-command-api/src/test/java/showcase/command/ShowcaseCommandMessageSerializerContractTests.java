// SPDX-License-Identifier: MIT
package showcase.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static showcase.command.RandomCommandTestUtils.aFinishShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aRemoveShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aScheduleShowcaseCommand;
import static showcase.command.RandomCommandTestUtils.aShowcaseCommandErrorDetails;
import static showcase.command.RandomCommandTestUtils.aStartShowcaseCommand;

import java.util.List;
import lombok.val;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Showcase command message serializer contract tests")
class ShowcaseCommandMessageSerializerContractTests {

    private final Serializer serializer = JacksonSerializer.defaultSerializer();

    static List<Arguments> commands() {
        return List.of(
                argumentSet("Schedule", aScheduleShowcaseCommand()),
                argumentSet("Start", aStartShowcaseCommand()),
                argumentSet("Finish", aFinishShowcaseCommand()),
                argumentSet("Remove", aRemoveShowcaseCommand()));
    }

    @ParameterizedTest
    @MethodSource("commands")
    @DisplayName("A command survives a Jackson serialization round-trip")
    void command_roundTripsThroughJacksonSerializer(ShowcaseCommand command) {
        val serialized = serializer.serialize(command, byte[].class);
        val deserialized = serializer.<byte[], ShowcaseCommand>deserialize(serialized);

        assertThat(deserialized).isEqualTo(command);
    }

    @Test
    @DisplayName("Error details survive a Jackson serialization round-trip")
    void errorDetails_roundTripsThroughJacksonSerializer() {
        val errorDetails = aShowcaseCommandErrorDetails();

        val serialized = serializer.serialize(errorDetails, byte[].class);
        val deserialized = serializer.<byte[], ShowcaseCommandErrorDetails>deserialize(serialized);

        assertThat(deserialized).isEqualTo(errorDetails);
    }
}
