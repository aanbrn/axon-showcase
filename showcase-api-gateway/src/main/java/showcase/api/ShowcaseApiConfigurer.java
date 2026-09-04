// SPDX-License-Identifier: MIT
package showcase.api;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux configuration for the showcase API gateway, allowing the configured web UI origins to call the gateway
 * cross-origin.
 */
@Component
class ShowcaseApiConfigurer implements WebFluxConfigurer {
    /**
     * The gateway configuration properties.
     */
    private final ShowcaseApiProperties apiProperties;

    /**
     * Creates the configurer with the gateway properties.
     *
     * @param apiProperties the gateway configuration properties
     */
    public ShowcaseApiConfigurer(ShowcaseApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(apiProperties.getCors().getAllowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
