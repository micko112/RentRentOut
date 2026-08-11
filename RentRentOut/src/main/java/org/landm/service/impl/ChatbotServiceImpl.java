package org.landm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.landm.entity.User;
import org.landm.repository.UserRepository;
import org.landm.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotServiceImpl.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ExecutorService streamExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "chatbot-sse");
        t.setDaemon(true);
        return t;
    });

    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${ai.service.url}")
    private String aiServiceUrl;

    @Value("${ai.service.api-key:}")
    private String aiServiceApiKey;

    public ChatbotServiceImpl(UserRepository userRepository, MessageSource messageSource) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private HttpRequest.Builder baseRequest(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(aiServiceUrl + path))
                .header("Content-Type", "application/json");
        if (aiServiceApiKey != null && !aiServiceApiKey.isBlank()) {
            b.header("X-Internal-API-Key", aiServiceApiKey);
        }
        return b;
    }

    @Override
    public String askQuestion(String userMessage, Long userId) {
        String userContext = buildUserContext(userId);
        String threadId = userId != null ? userId.toString() : "guest";

        try {
            String jsonBody = objectMapper.writeValueAsString(
                    Map.of("message", userMessage, "userId", threadId, "userContext", userContext)
            );
            HttpRequest request = baseRequest("/api/chat")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                return msg("chatbot.fallback.short");
            }
            Map<?, ?> responseMap = objectMapper.readValue(response.body(), Map.class);
            Object reply = responseMap.get("reply");
            return reply != null ? reply.toString() : msg("chatbot.fallback.short");
        } catch (Exception e) {
            log.error("Chatbot ML servis greška: {}", e.getMessage(), e);
            return msg("chatbot.fallback.long");
        }
    }

    @Override
    public SseEmitter streamQuestion(String userMessage, Long userId) {
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(2).toMillis());
        String userContext = buildUserContext(userId);
        String threadId = userId != null ? userId.toString() : "guest";

        streamExecutor.execute(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(
                        Map.of("message", userMessage, "userId", threadId, "userContext", userContext)
                );
                HttpRequest request = baseRequest("/api/chat/stream")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<java.io.InputStream> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofInputStream()
                );

                if (response.statusCode() != 200) {
                    emitter.send(SseEmitter.event().name("error").data("ML servis: " + response.statusCode()));
                    emitter.complete();
                    return;
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                    String line;
                    String currentEvent = "message";
                    while ((line = reader.readLine()) != null) {
                        if (line.isEmpty()) {
                            currentEvent = "message";
                            continue;
                        }
                        if (line.startsWith("event:")) {
                            currentEvent = line.substring(6).trim();
                        } else if (line.startsWith("data:")) {
                            String payload = line.substring(5).stripLeading();
                            emitter.send(SseEmitter.event().name(currentEvent).data(payload));
                            if ("done".equals(currentEvent)) {
                                break;
                            }
                        }
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                log.error("Chatbot stream greška: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(msg("chatbot.fallback.long")));
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
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
