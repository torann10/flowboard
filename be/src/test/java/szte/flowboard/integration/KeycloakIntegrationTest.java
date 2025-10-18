package szte.flowboard.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "keycloak.auth-server-url=http://localhost:9090",
    "keycloak.realm=flowboard",
    "keycloak.credentials.secret=test-secret"
})
class KeycloakIntegrationTest {

    @Test
    void contextLoads() {
        // This test ensures the application context loads with Keycloak configuration
    }
}

