package com.ganeshutsav.backend.controller;

import com.ganeshutsav.backend.dto.DeveloperDashboardDTO;
import com.ganeshutsav.backend.service.DeveloperDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/developer/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DEVELOPER')")
public class DeveloperDashboardController {

    private final DeveloperDashboardService developerDashboardService;

    @GetMapping("/overview")
    public DeveloperDashboardDTO getOverview() {
        return developerDashboardService.getGlobalOverview();
    }
}
