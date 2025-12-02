package com.celalabaci.service.impl;

import com.celalabaci.dto.subscriptionplan.SubscriptionPlanDto;
import com.celalabaci.dto.subscriptionplan.SubscriptionPlanCreateUpdateDto;
import com.celalabaci.entity.SubscriptionPlan;
import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.MessageType;
import com.celalabaci.mapper.SubscriptionPlanMapper;
import com.celalabaci.repository.SubscriptionPlanRepository;
import com.celalabaci.service.ISubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionPlanServiceImpl implements ISubscriptionPlanService {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public List<SubscriptionPlanDto> getAllPlans() {
        return subscriptionPlanRepository.findAll().stream()
                .map(subscriptionPlanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanDto getPlanById(Long id) {
        SubscriptionPlan plan = findPlanById(id);
        return subscriptionPlanMapper.toDto(plan);
    }

    @Override
    public SubscriptionPlanDto createPlan(SubscriptionPlanCreateUpdateDto dto) {
        if (subscriptionPlanRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "A subscription plan with the name '" + dto.getName() + "' already exists.");
        }
        SubscriptionPlan plan = subscriptionPlanMapper.toEntity(dto);
        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toDto(savedPlan);
    }

    @Override
    public SubscriptionPlanDto updatePlan(Long id, SubscriptionPlanCreateUpdateDto dto) {
        SubscriptionPlan existingPlan = findPlanById(id);

        // Eğer plan adı değiştiriliyorsa ve yeni ad zaten başka bir plana aitse, hataya izin ver.
        if (!existingPlan.getName().equalsIgnoreCase(dto.getName()) && subscriptionPlanRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new BaseException(MessageType.GENERAL_EXCEPTION, "A subscription plan with the name '" + dto.getName() + "' already exists.");
        }

        subscriptionPlanMapper.updateEntityFromDto(dto, existingPlan);
        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(existingPlan);
        return subscriptionPlanMapper.toDto(updatedPlan);
    }

    @Override
    public void deletePlan(Long id) {
        if (!subscriptionPlanRepository.existsById(id)) {
            throw new BaseException(MessageType.NO_RECORD_EXIST, "Subscription Plan with id " + id + " not found.");
        }
        subscriptionPlanRepository.deleteById(id);
    }

    private SubscriptionPlan findPlanById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new BaseException(MessageType.NO_RECORD_EXIST, "Subscription Plan with id " + id + " not found."));
    }
}
