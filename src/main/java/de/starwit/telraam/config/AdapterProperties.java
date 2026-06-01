package de.starwit.telraam.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Strongly-typed configuration bound from {@code application.properties} /
 * {@code application.yml} under the {@code telraam} and {@code dave} prefixes.
 */
@Component
@Validated
public class AdapterProperties {

    @ConfigurationProperties(prefix = "telraam")
    @Component
    @Validated
    public static class TelraamProperties {

        private String apiUrl = "https://telraam-api.net";

        @NotBlank(message = "telraam.api-key must not be blank")
        private String apiKey;

        /** Bounding box for auto-discovery (minLon,minLat,maxLon,maxLat). */
        @NotNull
        private BoundingBox boundingBox = new BoundingBox();

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public BoundingBox getBoundingBox() {
            return boundingBox;
        }

        public void setBoundingBox(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
        }

        public static class BoundingBox {
            private double minLon = 0.0;
            private double minLat = 0.0;
            private double maxLon = 0.0;
            private double maxLat = 0.0;

            public double getMinLon() {
                return minLon;
            }

            public void setMinLon(double minLon) {
                this.minLon = minLon;
            }

            public double getMinLat() {
                return minLat;
            }

            public void setMinLat(double minLat) {
                this.minLat = minLat;
            }

            public double getMaxLon() {
                return maxLon;
            }

            public void setMaxLon(double maxLon) {
                this.maxLon = maxLon;
            }

            public double getMaxLat() {
                return maxLat;
            }

            public void setMaxLat(double maxLat) {
                this.maxLat = maxLat;
            }
        }
    }

    @ConfigurationProperties(prefix = "dave")
    @Component
    @Validated
    public static class DaveProperties {

        /** Base URL of the DAVe REST API. */
        @NotBlank(message = "dave.api-url must not be blank")
        private String apiUrl;

        /** Optional bearer token for DAVe authentication. */
        private String apiKey;

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
