package de.starwit.telraam.config;

import de.starwit.telraam.config.AdapterProperties.DaveProperties;
import de.starwit.telraam.config.AdapterProperties.TelraamProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Provides pre-configured {@link WebClient} beans.
 *
 * <ul>
 *   <li>{@code telraamWebClient} – uses a static {@code X-Api-Key} header.</li>
 *   <li>{@code daveWebClient} – uses OAuth2 client-credentials flow (Keycloak)
 *       so the adapter can call DAVe's {@code FACHADMIN}-protected endpoints.
 *       Configure the registration under the id {@code dave} in
 *       {@code spring.security.oauth2.client.registration.dave.*}.</li>
 * </ul>
 */
@Configuration
@Profile({"dev","test"})
public class WebClientConfigNoSecurity {

    @Bean("telraamWebClient")
    public WebClient telraamWebClient(TelraamProperties props) {
        return WebClient.builder()
                .baseUrl(props.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Api-Key", props.getApiKey())
                .build();
    }

    @Bean("daveWebClient")
    public WebClient daveWebClient(DaveProperties props) {
        return WebClient.builder()
                .baseUrl(props.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
