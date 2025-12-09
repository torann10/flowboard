package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication-related operations.
 * Provides endpoints for retrieving current user authentication information.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * Retrieves the current user's authentication status and details.
     *
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing a map with authentication status, name, and authorities with HTTP status 200
     */
    @Operation(operationId = "getCurrentUserAuth", summary = "Get current user authentication info", description = "Retrieves the current user's authentication status and details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication info retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class)))
    })
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "name", authentication.getName(),
                    "authorities", authentication.getAuthorities()
            ));
        } else {
            return ResponseEntity.ok(Map.of("authenticated", false));
        }
    }
}


