package org.landm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.security.JwtUtil;
import org.landm.service.EmailVerificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class EmailVerificationController {

	private final EmailVerificationService emailVerificationService;
	private final JwtUtil jwtUtil;
	private final UserMapper userMapper;

	@Value("${app.cookie.secure:false}")
	private boolean cookieSecure;

	@Value("${jwt.access-expiration:900000}")
	private long accessExpiration;

	@Value("${jwt.refresh-expiration:604800000}")
	private long refreshExpiration;

	public EmailVerificationController(EmailVerificationService emailVerificationService,
									   JwtUtil jwtUtil, UserMapper userMapper) {
		this.emailVerificationService = emailVerificationService;
		this.jwtUtil = jwtUtil;
		this.userMapper = userMapper;
	}

	@GetMapping("/validate-email")
	public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam("token") String token,
															 HttpServletRequest request,
															 HttpServletResponse response) {
		User user = emailVerificationService.verifyEmail(token);

		String accessToken  = jwtUtil.generateAccessToken(user);
		String refreshToken = jwtUtil.generateRefreshToken(user);
		response.addHeader("Set-Cookie", buildCookie("access_token",  accessToken,  accessExpiration  / 1000).toString());
		response.addHeader("Set-Cookie", buildCookie("refresh_token", refreshToken, refreshExpiration / 1000).toString());

		Map<String, Object> res = new HashMap<>();
		res.put("user", userMapper.toDto(user));
		res.put("wsToken", jwtUtil.generateToken(user));

		String platform = request.getHeader("X-Client-Platform");
		if ("mobile".equalsIgnoreCase(platform)) {
			res.put("accessToken", accessToken);
			res.put("refreshToken", refreshToken);
		}

		return ResponseEntity.ok(res);
	}

	private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
		return ResponseCookie.from(name, value)
				.httpOnly(true)
				.secure(cookieSecure)
				.path("/")
				.maxAge(maxAgeSeconds)
				.sameSite("Lax")
				.build();
	}
}
