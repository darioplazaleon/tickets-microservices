package com.example.bookingservice.service;

import com.example.bookingservice.client.KeycloakAdminClient;
import com.example.bookingservice.entity.Customer;
import com.example.bookingservice.repository.CustomerRepository;
import com.example.bookingservice.request.UserRequest;
import com.example.bookingservice.response.CustomerResponse;
import jakarta.persistence.EntityExistsException;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

  private final KeycloakAdminClient keycloakAdminClient;
  private final CustomerRepository customerRepository;

  public CustomerResponse registerCustomer(UserRequest userRequest) {
    if (customerRepository.findByEmail(userRequest.email()).isPresent()) {
      throw new EntityExistsException("Email already exists");
    }

    if (customerRepository.findByUsername(userRequest.username()).isPresent()) {
      throw new EntityExistsException("Username already exists");
    }

    UUID keycloakId = keycloakAdminClient.createUserAngGetId(userRequest);

    Customer customer =
        Customer.builder()
                .id(keycloakId)
            .username(userRequest.username())
            .email(userRequest.email())
            .fullName(userRequest.fullName())
            .phoneNumber(userRequest.phoneNumber())
            .createdAt(LocalDate.now())
            .birthday(userRequest.birthday())
            .country(userRequest.country())
            .build();

    customerRepository.save(customer);

    return new CustomerResponse(customer);
  }

  public CustomerResponse getById(UUID customerId) {
    Customer customer = customerRepository.findById(customerId)
        .orElseThrow(() -> new RuntimeException("Customer not found"));
    return new CustomerResponse(customer);
  }


}
