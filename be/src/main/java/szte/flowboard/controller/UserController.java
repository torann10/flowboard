package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import szte.flowboard.dto.response.UserResponse;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.service.UserService;
import szte.flowboard.service.UserSyncService;

import java.util.List;

/**
 * REST controller for managing users.
 * Provides endpoints for retrieving user information and syncing users from Keycloak.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserSyncService userSyncService;

    /**
     * Retrieves the current authenticated user's information.
     * First attempts to sync the user from Keycloak, then returns the user data.
     *
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the user response with HTTP status 200 if found, 404 if not found, or 500 on error
     */
    @Operation(operationId = "getCurrentUser", summary = "Get current user", description = "Retrieves the current authenticated user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        try {
            // First try to sync user from Keycloak
            UserEntity userEntity = userSyncService.syncUserFromKeycloak(authentication);
            
            if (userEntity != null) {
                UserResponse response = new UserResponse(
                        userEntity.getId(),
                        userEntity.getKeycloakId(),
                        userEntity.getFirstName(),
                        userEntity.getLastName(),
                        userEntity.getEmailAddress()
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all users in the system.
     *
     * @return ResponseEntity containing a list of user responses with HTTP status 200
     */
    @Operation(operationId = "getAllUsers", summary = "Get all users", description = "Retrieves all users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    })
    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(users);
    }
}
