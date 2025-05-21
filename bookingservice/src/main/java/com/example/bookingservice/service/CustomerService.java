package com.example.bookingservice.service;

import com.example.bookingservice.entity.Customer;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.UserRequest;
import com.example.bookingservice.response.CustomerDetails;
import com.example.bookingservice.response.CustomerResponse;
import com.example.bookingservice.util.KeycloakProvider;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final KeycloakProvider keycloakProvider;

    public CustomerResponse registerCustomer(UserRequest userRequest) {
        UsersResource usersResource = keycloakProvider.getUsersResource();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(userRequest.email()); // Consider using username if distinct from email
        userRepresentation.setEmail(userRequest.email());
        userRepresentation.setFirstName(userRequest.firstName());
        userRepresentation.setLastName(userRequest.lastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        if (userRequest.password() == null || userRequest.password().isEmpty()) {
            log.error("Password is required for new user registration.");
            throw new IllegalArgumentException("Password is required for new user registration.");
        }
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(userRequest.password());
        userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));

        Response response = null;
        String userId = null;
        try {
            response = usersResource.create(userRepresentation);
            int status = response.getStatus();

            if (status == 201) { // Created
                String path = response.getLocation().getPath();
                userId = path.substring(path.lastIndexOf('/') + 1);
                log.info("User created successfully in Keycloak with ID: {}", userId);

                // User is created with password, no need for resetPassword here.

                Customer customer = Customer.builder()
                        .id(UUID.fromString(userId))
                        .username(userRequest.email()) // Align with what's set in Keycloak
                        .email(userRequest.email())
                        .fullName(userRequest.firstName() + " " + userRequest.lastName())
                        .phoneNumber(userRequest.phoneNumber())
                        .birthday(userRequest.birthday())
                        .country(userRequest.country())
                        // .createdAt(LocalDate.now()) // Consider if Customer entity has this field
                        .build();

                customerRepository.save(customer);
                log.info("Customer saved to local DB with ID: {}", userId);

                RealmResource realmResource = keycloakProvider.getRealmResource();
                List<RoleRepresentation> rolesToAssign;

                if (userRequest.roles() == null || userRequest.roles().isEmpty()) {
                    RoleRepresentation defaultRole = realmResource.roles().get("user").toRepresentation();
                    if (defaultRole == null) {
                        log.warn("Default role 'user' not found in Keycloak realm.");
                        rolesToAssign = Collections.emptyList();
                    } else {
                        rolesToAssign = List.of(defaultRole);
                    }
                } else {
                    rolesToAssign = realmResource.roles()
                            .list()
                            .stream()
                            .filter(role -> userRequest.roles()
                                    .stream()
                                    .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                            .toList();
                }

                if (!rolesToAssign.isEmpty()) {
                    realmResource.users().get(userId).roles().realmLevel().add(rolesToAssign);
                    log.info("Assigned roles to user {}: {}", userId, rolesToAssign.stream().map(RoleRepresentation::getName).toList());
                } else {
                    log.info("No roles to assign or roles not found for user {}", userId);
                }


                return new CustomerResponse(customer);

            } else if (status == 409) { // Conflict
                String responseBody = response.hasEntity() ? response.readEntity(String.class) : "No entity";
                log.error("User already exists in Keycloak. Status: {}, Body: {}", status, responseBody);
                throw new RuntimeException("User already exists in Keycloak: " + responseBody);
            } else { // Other errors
                String responseBody = response.hasEntity() ? response.readEntity(String.class) : "No entity";
                log.error("Error creating user in Keycloak. Status: {}, Body: {}", status, responseBody);
                throw new RuntimeException("Error creating user in Keycloak: " + responseBody);
            }
        } catch (Exception e) {
            log.error("Exception during user registration process", e);
            // Consider if you need to attempt to delete the Keycloak user if DB save fails
            // or implement a more robust transactional approach (e.g., Saga pattern)
            // For now, rethrow:
            throw new RuntimeException("Error during user registration: " + e.getMessage(), e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public CustomerResponse getById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return new CustomerResponse(customer);
    }

    public CustomerDetails geyCustomerDetails(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!customer.getId().equals(customerId)) {
            throw new RuntimeException("Access denied");
        }

        return new CustomerDetails(customer);
    }
}
