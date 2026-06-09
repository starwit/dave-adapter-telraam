package de.starwit.telraam.config;

import de.starwit.telraam.config.AdapterProperties.DaveProperties;
import de.starwit.telraam.config.AdapterProperties.TelraamProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ClientCredentialsReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
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
@Profile("auth")
public class WebClientConfig {

    @Bean("telraamWebClient")
    public WebClient telraamWebClient(TelraamProperties props) {
        return WebClient.builder()
                .baseUrl(props.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Api-Key", props.getApiKey())
                .build();
    }

    @Bean("daveWebClient")
    public WebClient daveWebClient(
            DaveProperties props,
            ReactiveClientRegistrationRepository clientRegistrations,
            ReactiveOAuth2AuthorizedClientService authorizedClientService) {

        // Build a reactive client-credentials manager bound to our "dave" registration
        var clientProvider = new ClientCredentialsReactiveOAuth2AuthorizedClientProvider();
        var authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                        clientRegistrations, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(clientProvider);

        // Filter that automatically fetches/refreshes the access token
        var oauth2Filter = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                authorizedClientManager);
        oauth2Filter.setDefaultClientRegistrationId("dave");

        return WebClient.builder()
                .baseUrl(props.getApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(oauth2Filter)
                .build();
    }
}
