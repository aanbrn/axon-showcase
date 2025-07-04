package showcase.query;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.val;
import org.springframework.data.domain.Pageable;
import showcase.query.PageRequest.Sort.Direction;

import java.util.List;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PageRequest {

    public static PageRequest withSort(@NonNull Sort sort) {
        return builder().sort(sort).build();
    }

    public static PageRequest from(@NonNull Pageable pageable) {
        val builder = builder();
        builder.pageNumber(pageable.getPageNumber());
        builder.pageSize(pageable.getPageSize());
        builder.sort(Sort.from(pageable.getSort()));
        return builder.build();
    }

    public static final int MIN_PAGE_NUMBER = 0;

    public static final int MAX_PAGE_NUMBER = 499;

    public static final int MIN_PAGE_SIZE = 1;

    public static final int MAX_PAGE_SIZE = 1000;

    public static final int DEFAULT_PAGE_SIZE = 20;

    @Min(MIN_PAGE_NUMBER)
    @Max(MAX_PAGE_NUMBER)
    int pageNumber;

    @Min(MIN_PAGE_SIZE)
    @Max(MAX_PAGE_SIZE)
    @Builder.Default
    int pageSize = DEFAULT_PAGE_SIZE;

    @NonNull
    @Builder.Default
    Sort sort = Sort.by(Direction.DESC, "startTime");

    public Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize, sort.toSort());
    }

    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class Sort {

        public static Sort by(@NonNull Direction direction, String... properties) {
            val builder = builder();
            for (val property : properties) {
                builder.order(Order.by(property, direction));
            }
            return builder.build();
        }

        public static Sort from(@NonNull org.springframework.data.domain.Sort sort) {
            val builder = builder();
            builder.orders(sort.stream().map(Order::from).toList());
            return builder.build();
        }

        @Singular(ignoreNullCollections = true)
        List<Order> orders;

        public org.springframework.data.domain.Sort toSort() {
            return org.springframework.data.domain.Sort.by(
                    orders.stream()
                          .map(Order::toOrder)
                          .toList());
        }

        public enum Direction {

            ASC,

            DESC
        }

        public enum NullHandling {

            NATIVE,

            NULLS_FIRST,

            NULLS_LAST
        }

        @Value
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        @Builder
        public static class Order {

            public static final Direction DEFAULT_DIRECTION = Direction.ASC;

            public static final NullHandling DEFAULT_NULL_HANDLING = NullHandling.NATIVE;

            public static Order by(@NonNull String property, @NonNull Direction direction) {
                return builder().property(property).direction(direction).build();
            }

            public static Order from(@NonNull org.springframework.data.domain.Sort.Order order) {
                val builder = builder();
                builder.direction(Direction.valueOf(order.getDirection().name()));
                builder.property(order.getProperty());
                builder.ignoreCase(order.isIgnoreCase());
                builder.nullHandling(NullHandling.valueOf(order.getNullHandling().name()));
                return builder.build();
            }

            @NonNull
            @Builder.Default
            Direction direction = DEFAULT_DIRECTION;

            @NonNull
            String property;

            boolean ignoreCase;

            @NonNull
            @Builder.Default
            NullHandling nullHandling = DEFAULT_NULL_HANDLING;

            public org.springframework.data.domain.Sort.Order toOrder() {
                return new org.springframework.data.domain.Sort.Order(
                        org.springframework.data.domain.Sort.Direction.valueOf(direction.name()),
                        property, ignoreCase,
                        org.springframework.data.domain.Sort.NullHandling.valueOf(nullHandling.name()));
            }
        }
    }
}
