package org.landm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.landm.entity.User;
import org.landm.repository.UserRepository;
import org.landm.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    public ChatbotServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String askQuestion(String userMessage, Long userId) {
        String userContext = buildUserContext(userId);
        String threadId = userId != null ? userId.toString() : "guest";

        try {
            String jsonBody = objectMapper.writeValueAsString(
                    Map.of("message", userMessage, "userId", threadId, "userContext", userContext)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServiceUrl + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("FastAPI status: {}, body: {}", response.statusCode(), response.body());
            Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
            Object reply = responseMap.get("reply");
            return reply != null ? reply.toString() : "Izvini, trenutno ne mogu da odgovorim.";
        } catch (Exception e) {
            log.error("Chatbot ML servis greška: {}", e.getMessage(), e);
            return "Izvini, trenutno ne mogu da odgovorim. Pokušaj ponovo ili piši na izdajemiznajmljujem.rs@gmail.com";
        }
    }

    private String buildUserContext(Long userId) {
        if (userId == null) {
            return "Korisnik je neulogovan gost — podstakni ga da se registruje/prijavi kad je to relevantno.";
        }
        return userRepository.findById(userId)
                .map(this::formatUserContext)
                .orElse("Korisnik je neulogovan gost.");
    }

    private String formatUserContext(User user) {
        String role = user.getRole() != null ? user.getRole().getName() : "USER";
        return """
                - Ime: %s
                - Uloga: %s
                - Kredit: %s RSD
                - Pozitivne recenzije: %d
                - Negativne recenzije: %d
                - Identifikovan (KYC): %s
                """.formatted(
                        user.getFirstname(),
                        role,
                        user.getCredit() != null ? user.getCredit().toPlainString() : "0",
                        user.getPositiveReviews(),
                        user.getNegativeReviews(),
                        user.isIdentified() ? "da" : "ne"
                );
    }
}
