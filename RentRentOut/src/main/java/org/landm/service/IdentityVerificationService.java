package org.landm.service;

import org.landm.dto.verification.AdminVerificationDetailsDto;
import org.landm.dto.verification.AdminVerificationDto;
import org.landm.dto.verification.VerificationStatusDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IdentityVerificationService {

    VerificationStatusDto submit(Long userId, MultipartFile docFront, MultipartFile docBack, MultipartFile selfie);

    VerificationStatusDto getMyStatus(Long userId);

    Page<AdminVerificationDto> listForAdmin(String statusFilter, Pageable pageable);

    AdminVerificationDetailsDto getDetailsForAdmin(Long verificationId);

    void approve(Long verificationId, Long adminUserId);

    void reject(Long verificationId, Long adminUserId, String reason);
}
