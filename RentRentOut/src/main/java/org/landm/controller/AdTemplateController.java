package org.landm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.landm.dto.AdTemplateDto;
import org.landm.entity.AdTemplate;
import org.landm.entity.User;
import org.landm.repository.AdTemplateRepository;
import org.landm.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@PreAuthorize("isAuthenticated()")
public class AdTemplateController {

    private static final int MAX_TEMPLATES_PER_USER = 50;
    private static final int MAX_NAME_LENGTH = 80;

    private final AdTemplateRepository repository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AdTemplateController(AdTemplateRepository repository,
                                UserRepository userRepository,
                                ObjectMapper objectMapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<AdTemplateDto> list(@AuthenticationPrincipal Long userId) {
        return repository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal Long userId) {
        String name = sanitizeName(body.get("name"));
        if (name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Naziv šablona je obavezan."));
        }

        Object dataRaw = body.get("data");
        if (!(dataRaw instanceof Map<?, ?>)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Podaci šablona su obavezni."));
        }

        if (repository.countByUserId(userId) >= MAX_TEMPLATES_PER_USER) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Dostigli ste maksimalan broj šablona (" + MAX_TEMPLATES_PER_USER + ")."));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Korisnik nije pronađen."));

        AdTemplate template = new AdTemplate(user, name, serialize(dataRaw));
        return ResponseEntity.ok(toDto(repository.save(template)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal Long userId) {
        AdTemplate template = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Šablon nije pronađen."));

        if (!template.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Nemate pristup ovom šablonu."));
        }

        if (body.containsKey("name")) {
            String name = sanitizeName(body.get("name"));
            if (name.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Naziv ne sme biti prazan."));
            }
            template.setName(name);
        }
        if (body.containsKey("data")) {
            Object dataRaw = body.get("data");
            if (!(dataRaw instanceof Map<?, ?>)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Podaci moraju biti objekat."));
            }
            template.setData(serialize(dataRaw));
        }

        return ResponseEntity.ok(toDto(repository.save(template)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal Long userId) {
        AdTemplate template = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Šablon nije pronađen."));

        if (!template.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Nemate pristup ovom šablonu."));
        }

        repository.delete(template);
        return ResponseEntity.noContent().build();
    }

    private AdTemplateDto toDto(AdTemplate t) {
        return new AdTemplateDto(t.getId(), t.getName(), deserialize(t.getData()),
                t.getCreatedAt(), t.getUpdatedAt());
    }

    private String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Neispravni podaci šablona.", e);
        }
    }

    private Map<String, Object> deserialize(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return Map.of();
        }
    }

    private String sanitizeName(Object raw) {
        if (raw == null) return "";
        String s = raw.toString().trim();
        if (s.length() > MAX_NAME_LENGTH) s = s.substring(0, MAX_NAME_LENGTH);
        return s;
    }
}
