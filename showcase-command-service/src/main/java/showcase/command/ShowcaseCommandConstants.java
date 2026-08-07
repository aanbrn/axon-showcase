package showcase.command;

import lombok.experimental.UtilityClass;

/**
 * Holds the cache names used by the showcase command service.
 */
@UtilityClass
class ShowcaseCommandConstants {
    /**
     * The name of the aggregate cache for showcases.
     */
    static final String SHOWCASE_CACHE_NAME = "showcase-cache";

    /**
     * The name of the cache for saga instances.
     */
    static final String SAGA_CACHE_NAME = "saga-cache";

    /**
     * The name of the cache for saga associations.
     */
    static final String SAGA_ASSOCIATIONS_CACHE_NAME = "saga-associations-cache";
}
