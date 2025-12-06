package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.subscription.AdminSubscriptionUpdateDto;
import com.celalabaci.dto.subscription.SubscriptionCreateDto;
import com.celalabaci.dto.subscription.SubscriptionDto;
import com.celalabaci.entity.User;
import com.celalabaci.service.ISubscriptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    @Autowired
    private ISubscriptionService subscriptionService;

    // --- USER Endpoints ---

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getMySubscriptions(@AuthenticationPrincipal User currentUser) {
        List<SubscriptionDto> subscriptions = subscriptionService.getMySubscriptions(currentUser);
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> createMySubscription(@Valid @RequestBody SubscriptionCreateDto dto, @AuthenticationPrincipal User currentUser) {
        SubscriptionDto createdSubscription = subscriptionService.createMySubscription(dto, currentUser);
        return new ResponseEntity<>(ApiResponse.success("Subscription started successfully.", createdSubscription), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> cancelMySubscription(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        SubscriptionDto cancelledSubscription = subscriptionService.cancelMySubscription(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully.", cancelledSubscription));
    }

    // --- ADMIN Endpoints ---

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionDto>>> getAllSubscriptions() {
        List<SubscriptionDto> subscriptions = subscriptionService.getAllSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> getSubscriptionById(@PathVariable Long id) {
        SubscriptionDto subscription = subscriptionService.getSubscriptionByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success(subscription));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionDto>> updateSubscriptionByAdmin(@PathVariable Long id, @Valid @RequestBody AdminSubscriptionUpdateDto dto) {
        SubscriptionDto updatedSubscription = subscriptionService.updateSubscriptionByAdmin(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Subscription updated successfully by admin.", updatedSubscription));
    }
}
