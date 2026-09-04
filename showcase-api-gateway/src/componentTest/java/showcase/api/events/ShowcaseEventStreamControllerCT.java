// SPDX-License-Identifier: MIT
package showcase.api.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import java.time.Instant;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

@WebFluxTest(ShowcaseEventStreamController.class)
@DisplayName("Showcase event stream controller component tests")
class ShowcaseEventStreamControllerCT {

    @Configuration
    @ComponentScan(
            useDefaultFilters = false,
            includeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = ShowcaseEventStreamController.class))
    static class TestConfiguration {

        @Bean
        SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
            return http.csrf(CsrfSpec::disable)
                    .authorizeExchange(authorize -> authorize.anyExchange().permitAll())
                    .build();
        }

        @Bean
        Flux<ShowcaseEventDto> showcaseEventStream() {
            return Flux.just(ShowcaseEventDto.builder()
                    .type("SCHEDULED")
                    .showcaseId("1")
                    .timestamp(Instant.parse("2026-09-02T10:00:00Z"))
                    .build());
        }
    }

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /events streams showcase events as Server-Sent Events")
    void getEvents_streamsShowcaseEventsAsServerSentEvents() {
        val body = webTestClient
                .get()
                .uri("/events")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentTypeCompatibleWith(TEXT_EVENT_STREAM)
                .returnResult(String.class)
                .getResponseBody()
                .blockFirst();

        assertThat(body).contains("\"type\":\"SCHEDULED\"");
        assertThat(body).contains("\"showcaseId\":\"1\"");
        assertThat(body).contains("\"timestamp\":\"2026-09-02T10:00:00Z\"");
    }
}
