package com.celalabaci.controller;

import com.celalabaci.common.ApiResponse;
import com.celalabaci.dto.subscriptionplan.SubscriptionPlanDto;
import com.celalabaci.dto.subscriptionplan.SubscriptionPlanCreateUpdateDto;
import com.celalabaci.service.ISubscriptionPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
public class SubscriptionPlanController {

    @Autowired
    private ISubscriptionPlanService subscriptionPlanService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionPlanDto>>> getAllPlans() {
        List<SubscriptionPlanDto> plans = subscriptionPlanService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> getPlanById(@PathVariable Long id) {
        SubscriptionPlanDto plan = subscriptionPlanService.getPlanById(id);
        return ResponseEntity.ok(ApiResponse.success(plan));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> createPlan(@Valid @RequestBody SubscriptionPlanCreateUpdateDto dto) {
        SubscriptionPlanDto createdPlan = subscriptionPlanService.createPlan(dto);
        return new ResponseEntity<>(ApiResponse.success("Subscription plan created successfully.", createdPlan), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanDto>> updatePlan(@PathVariable Long id, @Valid @RequestBody SubscriptionPlanCreateUpdateDto dto) {
        SubscriptionPlanDto updatedPlan = subscriptionPlanService.updatePlan(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan updated successfully.", updatedPlan));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePlan(@PathVariable Long id) {
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok(ApiResponse.success("Subscription plan deleted successfully.", null));
    }
}
