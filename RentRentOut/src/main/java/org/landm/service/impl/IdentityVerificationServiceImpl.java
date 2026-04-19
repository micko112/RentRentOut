package org.landm.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.landm.dto.verification.AdminVerificationDetailsDto;
import org.landm.dto.verification.AdminVerificationDto;
import org.landm.dto.verification.VerificationStatusDto;
import org.landm.entity.Enums.VerificationStatus;
import org.landm.entity.IdentityVerification;
import org.landm.entity.User;
import org.landm.repository.IdentityVerificationRepository;
import org.landm.repository.UserRepository;
import org.landm.service.HtmlEmailService;
import org.landm.service.IdentityVerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
public class IdentityVerificationServiceImpl implements IdentityVerificationService {

    private static final Logger log = LoggerFactory.getLogger(IdentityVerificationServiceImpl.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String CLOUDINARY_FOLDER = "rent-rent-out/verifications";

    private final Cloudinary cloudinary;
    private final IdentityVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final HtmlEmailService emailService;

    @Value("${app.frontend.base-url:http://localhost:4200}")
    private String frontendBaseUrl;

    public IdentityVerificationServiceImpl(Cloudinary cloudinary,
                                           IdentityVerificationRepository verificationRepository,
                                           UserRepository userRepository,
                                           HtmlEmailService emailService) {
        this.cloudinary = cloudinary;
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ─────────────────────────────────────────────────────────────
    //  USER ACTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public VerificationStatusDto submit(Long userId,
                                        MultipartFile docFront,
                                        MultipartFile docBack,
                                        MultipartFile selfie) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Korisnik nije pronađen."));

        if (user.isIdentified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vaš nalog je već verifikovan.");
        }

        validateImage(docFront, "Prednja strana dokumenta");
        validateImage(selfie, "Selfi fotografija");
        // docBack je opciono
        if (docBack != null && !docBack.isEmpty()) {
            validateImage(docBack, "Zadnja strana dokumenta");
        }

        IdentityVerification existing = verificationRepository.findByUserId(userId).orElse(null);
        if (existing != null && existing.getStatus() == VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Već imate zahtev koji čeka pregled.");
        }

        // Upload slika kao authenticated (ne javno dostupno)
        String frontPublicId = uploadPrivate(docFront, userId + "-front");
        String backPublicId  = (docBack != null && !docBack.isEmpty())
            ? uploadPrivate(docBack, userId + "-back") : null;
        String selfiePublicId = uploadPrivate(selfie, userId + "-selfie");

        IdentityVerification verification;
        if (existing != null) {
            // Resubmit nakon REJECTED
            deleteFromCloudinary(existing.getDocFrontPublicId());
            deleteFromCloudinary(existing.getDocBackPublicId());
            deleteFromCloudinary(existing.getSelfiePublicId());

            existing.setDocFrontPublicId(frontPublicId);
            existing.setDocBackPublicId(backPublicId);
            existing.setSelfiePublicId(selfiePublicId);
            existing.setStatus(VerificationStatus.PENDING);
            existing.setSubmittedAt(LocalDateTime.now());
            existing.setReviewedAt(null);
            existing.setReviewedBy(null);
            existing.setRejectionReason(null);
            verification = existing;
        } else {
            verification = new IdentityVerification(user, frontPublicId, backPublicId, selfiePublicId);
        }

        verificationRepository.save(verification);
        return toStatusDto(verification);
    }

    @Override
    @Transactional(readOnly = true)
    public VerificationStatusDto getMyStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (user.isIdentified()) {
            return new VerificationStatusDto("APPROVED", null, null, null);
        }

        return verificationRepository.findByUserId(userId)
            .map(this::toStatusDto)
            .orElse(new VerificationStatusDto("NONE", null, null, null));
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN ACTIONS
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<AdminVerificationDto> listForAdmin(String statusFilter, Pageable pageable) {
        Page<IdentityVerification> page;
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            try {
                VerificationStatus s = VerificationStatus.valueOf(statusFilter.toUpperCase());
                page = verificationRepository.findAllByStatusOrderBySubmittedAtAsc(s, pageable);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nepoznat status.");
            }
        } else {
            page = verificationRepository.findAllByOrderBySubmittedAtDesc(pageable);
        }
        return page.map(this::toAdminDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminVerificationDetailsDto getDetailsForAdmin(Long verificationId) {
        IdentityVerification v = verificationRepository.findById(verificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtev nije pronađen."));

        AdminVerificationDetailsDto dto = new AdminVerificationDetailsDto();
        dto.setId(v.getId());
        dto.setUserId(v.getUser().getId());
        dto.setUserFullName(v.getUser().getFirstname() + " " + v.getUser().getLastname());
        dto.setUserEmail(v.getUser().getEmail());
        dto.setStatus(v.getStatus().name());
        dto.setSubmittedAt(v.getSubmittedAt());
        dto.setDocFrontUrl(signedUrl(v.getDocFrontPublicId()));
        dto.setDocBackUrl(signedUrl(v.getDocBackPublicId()));
        dto.setSelfieUrl(signedUrl(v.getSelfiePublicId()));
        return dto;
    }

    @Override
    @Transactional
    public void approve(Long verificationId, Long adminUserId) {
        IdentityVerification v = verificationRepository.findById(verificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtev nije pronađen."));

        if (v.getStatus() != VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zahtev je već obrađen.");
        }

        User admin = userRepository.findById(adminUserId).orElse(null);

        v.setStatus(VerificationStatus.APPROVED);
        v.setReviewedAt(LocalDateTime.now());
        v.setReviewedBy(admin);

        User user = v.getUser();
        user.setIdentified(true);
        userRepository.save(user);

        // Brišemo slike — ne čuvamo osetljive dokumente
        deleteFromCloudinary(v.getDocFrontPublicId());
        deleteFromCloudinary(v.getDocBackPublicId());
        deleteFromCloudinary(v.getSelfiePublicId());
        v.setDocFrontPublicId(null);
        v.setDocBackPublicId(null);
        v.setSelfiePublicId(null);

        verificationRepository.save(v);

        // Email
        try {
            emailService.sendVerificationApprovedEmail(user.getEmail(), user.getFirstname(),
                frontendBaseUrl + "/my-profile");
        } catch (Exception e) {
            log.warn("Slanje email-a za odobrenu verifikaciju nije uspelo: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void reject(Long verificationId, Long adminUserId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Razlog odbijanja je obavezan.");
        }
        if (reason.length() > 300) reason = reason.substring(0, 300);

        IdentityVerification v = verificationRepository.findById(verificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zahtev nije pronađen."));

        if (v.getStatus() != VerificationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zahtev je već obrađen.");
        }

        User admin = userRepository.findById(adminUserId).orElse(null);

        v.setStatus(VerificationStatus.REJECTED);
        v.setReviewedAt(LocalDateTime.now());
        v.setReviewedBy(admin);
        v.setRejectionReason(reason);

        User user = v.getUser();

        // Brišemo slike — GDPR, ne čuvamo osetljive dokumente
        deleteFromCloudinary(v.getDocFrontPublicId());
        deleteFromCloudinary(v.getDocBackPublicId());
        deleteFromCloudinary(v.getSelfiePublicId());
        v.setDocFrontPublicId(null);
        v.setDocBackPublicId(null);
        v.setSelfiePublicId(null);

        verificationRepository.save(v);

        try {
            emailService.sendVerificationRejectedEmail(user.getEmail(), user.getFirstname(),
                reason, frontendBaseUrl + "/verify");
        } catch (Exception e) {
            log.warn("Slanje email-a za odbijenu verifikaciju nije uspelo: {}", e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────

    private void validateImage(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " je obavezno polje.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, fieldName + " je prevelika (max 10MB).");
        }
        String ct = file.getContentType();
        if (ct == null || !ALLOWED_CONTENT_TYPES.contains(ct.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + ": samo JPG, PNG ili WEBP.");
        }
    }

    private String uploadPrivate(MultipartFile file, String identifier) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", CLOUDINARY_FOLDER,
                "resource_type", "image",
                "type", "authenticated",
                "public_id", identifier + "-" + System.currentTimeMillis(),
                "overwrite", true
            ));
            return (String) result.get("public_id");
        } catch (IOException e) {
            log.error("Greška pri upload-u verifikacione slike", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Neuspelo otpremanje slike.");
        }
    }

    /**
     * Generiše signed URL za sliku tipa `authenticated`.
     * Bez potpisa slika nije dostupna — jedini način pristupa je ovaj endpoint (samo admin).
     */
    private String signedUrl(String publicId) {
        if (publicId == null) return null;
        try {
            return cloudinary.url()
                .resourceType("image")
                .type("authenticated")
                .signed(true)
                .secure(true)
                .generate(publicId);
        } catch (Exception e) {
            log.warn("Ne mogu da generišem signed URL za {}: {}", publicId, e.getMessage());
            return null;
        }
    }

    private void deleteFromCloudinary(String publicId) {
        if (publicId == null) return;
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "type", "authenticated",
                "resource_type", "image",
                "invalidate", true
            ));
        } catch (Exception e) {
            log.warn("Ne mogu da obrišem Cloudinary sliku {}: {}", publicId, e.getMessage());
        }
    }

    private VerificationStatusDto toStatusDto(IdentityVerification v) {
        return new VerificationStatusDto(
            v.getStatus().name(),
            v.getSubmittedAt(),
            v.getReviewedAt(),
            v.getRejectionReason()
        );
    }

    private AdminVerificationDto toAdminDto(IdentityVerification v) {
        return new AdminVerificationDto(
            v.getId(),
            v.getUser().getId(),
            v.getUser().getFirstname() + " " + v.getUser().getLastname(),
            v.getUser().getEmail(),
            v.getStatus().name(),
            v.getSubmittedAt(),
            v.getReviewedAt(),
            v.getRejectionReason()
        );
    }
}
