package showcase.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import showcase.identifier.KSUID;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryException;
import showcase.query.ShowcaseStatus;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

/**
 * REST API for managing showcases.
 *
 * <p>Operations are split between command and query sides. Commands are handled asynchronously — if the operation
 * times out, a {@code 202} is returned with the idempotency key in the response header so the client can retry
 * safely. Queries are served from read-side projections backed by OpenSearch.
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Showcase REST API",
                version = "0.1.0",
                description = "The REST API to manage showcases."
        )
)
@Tag(name = "Showcase Operations")
@SuppressWarnings("unused")
interface ShowcaseApi {

    /**
     * The HTTP header name used for idempotency keys on write operations.
     */
    String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    /**
     * Schedules a new showcase.
     *
     * <p>If the request is accepted for asynchronous processing, a {@code 202} is returned with the idempotency key
     * in the {@code Idempotency-Key} header. The client should retry the same request using that key to obtain the
     * final result.
     *
     * @param idempotencyKey optional unique key ensuring idempotency; generated automatically if omitted
     * @param request        the schedule parameters
     * @return {@code 201} with the scheduled showcase ID and a {@code Location} header, or {@code 202} if still
     * processing, or {@code 409} if the title is already in use
     */
    @Operation(
            description = "Schedules a showcase with the given parameters.",
            method = "POST",
            parameters = @Parameter(
                    name = IDEMPOTENCY_KEY_HEADER,
                    description = """
                            A unique key that ensures the operation's idempotency. When provided, repeated requests
                            with the same key are treated as the same operation. The server may return one in responses
                            (e.g., on timeout), so the client can safely retry later.""",
                    in = ParameterIn.HEADER
            ),
            requestBody = @RequestBody(
                    description = "Parameters for a showcase to schedule.",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE),
                    required = true
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "The showcase has been scheduled successfully.",
                            headers = @Header(
                                    name = "Location",
                                    description = "The path by which the showcase details are accessible.",
                                    examples = @ExampleObject("/showcases/33gkCN0UNn3Kzr3x7iuDaVT6sZi")
                            ),
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ScheduleShowcaseResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "202",
                            description = """
                                    The request has been accepted for asynchronous processing and is still being
                                    handled. Retry the same request using the provided idempotency key to obtain
                                    the final result once the operation completes.""",
                            headers = @Header(
                                    name = IDEMPOTENCY_KEY_HEADER,
                                    description = "The idempotency key to use for retrying the same operation.",
                                    examples = @ExampleObject("33gkCN0UNn3Kzr3x7iuDaVT6sZi")
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The given title is in use already.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Conflict",
                                              "status": 409,
                                              "detail": "Given title is in use already"
                                            }"""
                                    )
                            )
                    )
            }
    )
    Mono<ResponseEntity<ScheduleShowcaseResponse>> schedule(
            @KSUID String idempotencyKey,
            @Valid ScheduleShowcaseRequest request);

    /**
     * Starts a scheduled showcase explicitly before its automatic start time.
     *
     * @param showcaseId the ID of the showcase to start
     * @return {@code 200} on success, {@code 202} if still processing, {@code 400} for an invalid ID, {@code 404} if
     * not found, or {@code 409} if already finished
     */
    @Operation(
            description = "Starts the scheduled showcase explicitly before it's started automatically at the " +
                                  "scheduled date-time.",
            method = "PUT",
            parameters = @Parameter(
                    name = "showcaseId",
                    description = "The ID of the showcase to start.",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi"
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The requested showcase has been started successfully."
                    ),
                    @ApiResponse(
                            responseCode = "202",
                            description = """
                                    The request has been accepted for asynchronous processing and is still being
                                    handled. Retry the same request to obtain the final result once the operation
                                    completes."""
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The given showcase ID is not valid.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "Validation failure"
                                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The requested showcase does not exist.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Not Found",
                                              "status": 404,
                                              "detail": "No showcase with id"
                                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The requested showcase is finished already.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Conflict",
                                              "status": 409,
                                              "detail": "Showcase is finished already"
                                            }"""
                                    )
                            )
                    )
            }
    )
    Mono<ResponseEntity<Void>> start(@KSUID String showcaseId);

    /**
     * Finishes a started showcase explicitly before its automatic end time.
     *
     * @param showcaseId the ID of the showcase to finish
     * @return {@code 200} on success, {@code 202} if still processing, {@code 400} for an invalid ID, {@code 404} if
     * not found, or {@code 409} if not started yet
     */
    @Operation(
            description = "Finishes the started showcase explicitly before it's finished automatically at the end of " +
                                  "the duration.",
            method = "PUT",
            parameters = @Parameter(
                    name = "showcaseId",
                    description = "The ID of the showcase to finish.",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi"
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The requested showcase has been finished successfully."
                    ),
                    @ApiResponse(
                            responseCode = "202",
                            description = """
                                    The request has been accepted for asynchronous processing and is still being
                                    handled. Retry the same request to obtain the final result once the operation
                                    completes."""
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The given showcase ID is not valid.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "Validation failure"
                                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The requested showcase does not exist.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Not Found",
                                              "status": 404,
                                              "detail": "No showcase with id"
                                            }"""
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "The requested showcase is not started yet.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Conflict",
                                              "status": 409,
                                              "detail": "Showcase must be started first"
                                            }"""
                                    )
                            )
                    )
            }
    )
    Mono<ResponseEntity<Void>> finish(@KSUID String showcaseId);

    /**
     * Removes a showcase, finishing it first if it has already been started.
     *
     * @param showcaseId the ID of the showcase to remove
     * @return {@code 200} on success, or {@code 202} if still processing
     */
    @Operation(
            description = "Removes the given showcase finishing it when it's already started.",
            method = "DELETE",
            parameters = @Parameter(
                    name = "showcaseId",
                    description = "The ID of the showcase to remove.",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi"
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The requested showcase has been removed successfully."
                    ),
                    @ApiResponse(
                            responseCode = "202",
                            description = """
                                    The request has been accepted for asynchronous processing and is still being
                                    handled. Retry the same request to obtain the final result once the operation
                                    completes."""
                    ),
            }
    )
    Mono<ResponseEntity<Void>> remove(@KSUID String showcaseId);

    /**
     * Fetches the existing showcases, sorted by ID in reverse chronological order, optionally filtered by title and
     * status.
     *
     * @param title    filter by full-text title match
     * @param statuses filter by one or more statuses
     * @param afterId  fetch only showcases with an ID after this one (for pagination)
     * @param size     number of showcases to return (between 1 and 1000, default 20)
     * @return the matching showcase list
     */
    @Operation(
            description = "Fetches the existing showcases sorting them by IDs in reverse order and optionally " +
                                  "filtering by the given status(es).",
            method = "GET",
            parameters = {
                    @Parameter(
                            name = "title",
                            description = "The title to filter by with full-text matching.",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "status",
                            description = "The status(es) to filter by.",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "afterId",
                            description = "The ID of a showcase after which to fetch.",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "size",
                            description = "The number of showcases to fetch.",
                            in = ParameterIn.QUERY
                    )
            },
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "The requested amount of showcases.",
                    content = @Content(
                            mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Showcase.class)
                    )
            )
    )
    Flux<Showcase> fetchList(
            String title,
            List<ShowcaseStatus> statuses,
            @KSUID String afterId,
            @Min(FetchShowcaseListQuery.MIN_SIZE) @Max(FetchShowcaseListQuery.MAX_SIZE) int size);

    /**
     * Fetches a single showcase by its ID.
     *
     * @param showcaseId the ID of the showcase
     * @return the showcase if found
     * @throws ShowcaseQueryException with {@code NOT_FOUND} if the showcase does not exist
     */
    @Operation(
            description = "Fetches the showcase given by ID.",
            method = "GET",
            parameters = @Parameter(
                    name = "showcaseId",
                    description = "The ID of the showcase to fetch.",
                    in = ParameterIn.PATH,
                    required = true,
                    example = "33gkCN0UNn3Kzr3x7iuDaVT6sZi"
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "The requested showcase exists.",
                            content = @Content(
                                    mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = Showcase.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The requested showcase does not exist.",
                            content = @Content(
                                    mediaType = APPLICATION_PROBLEM_JSON_VALUE,
                                    examples = @ExampleObject("""
                                            {
                                              "type": "about:blank",
                                              "title": "Not Found",
                                              "status": 404,
                                              "detail": "No showcase with id"
                                            }"""
                                    )
                            )
                    )
            }
    )
    Mono<Showcase> fetchById(@KSUID String showcaseId);
}
