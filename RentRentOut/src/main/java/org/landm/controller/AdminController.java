package org.landm.controller;


import org.landm.service.AdService;
import org.landm.service.AdminService;
import org.landm.service.RentalContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
    private final AdminService adminService;


    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PatchMapping("/ads/{adId}/suspend")
    public ResponseEntity<String> deleteAd(@PathVariable Long adId){
        String response = adminService.suspendAd(adId);
        return ResponseEntity.ok(response);
    }
}
