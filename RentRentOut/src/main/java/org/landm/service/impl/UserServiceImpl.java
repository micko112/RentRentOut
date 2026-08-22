package org.landm.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.persistence.OptimisticLockException;
import org.springframework.transaction.annotation.Transactional;
import org.landm.dto.ad.AdPreviewDto;
import org.landm.dto.requestDto.DepositRequestDto;
import org.landm.dto.review.ReviewDto;
import org.landm.dto.user.*;
import org.landm.entity.EmailVerificationToken;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.exception.WrongCredentialsException;
import org.landm.mapper.UserMapper;
import org.landm.entity.Location;
import org.landm.repository.LocationRepository;
import org.landm.repository.RoleRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.AdService;
import org.landm.service.EmailVerificationService;
import org.landm.service.ReviewService;
import org.landm.service.UserService;
import org.landm.util.HtmlSanitizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final EmailVerificationService emailVerificationService;
	private final AdService adService;
	private final ReviewService reviewService;
	private final LocationRepository locationRepository;
	private final MessageSource messageSource;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${google.client-id}")
	private String googleClientId;

	@Value("${facebook.app-id}")
	private String facebookAppId;

	@Value("${facebook.app-secret}")
	private String facebookAppSecret;

	@Value("${apple.client-id}")
	private String appleClientId;

	public UserServiceImpl(UserRepository userRepository,
						   PasswordEncoder passwordEncoder, UserMapper userMapper,
						   JwtUtil jwtUtil, RoleRepository roleRepository,
						   EmailVerificationService emailVerificationService, AdService adService, ReviewService reviewService,
						   LocationRepository locationRepository, MessageSource messageSource) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
		this.roleRepository = roleRepository;
		this.emailVerificationService = emailVerificationService;
		this.adService = adService;
		this.reviewService = reviewService;
		this.locationRepository = locationRepository;
		this.messageSource = messageSource;
	}

	private String msg(String key, Object... args) {
		return messageSource.getMessage(key, args, org.springframework.context.i18n.LocaleContextHolder.getLocale());
	}

    @Transactional
    @Override
    public UserDto register(RegisterUserRequestDto req) {

		Role role = roleRepository.findByName("ROLE_USER");
		if (role == null) {
			throw new RuntimeException("Role not found in database: ROLE_USER");
		}
            if (userRepository.existsByEmail(req.getEmail())) {
                throw new WrongCredentialsException("Email already exists!");
            } else {
                User userToSave = new User(
                        req.getEmail(),
                        passwordEncoder.encode(req.getPassword()),
                        req.getFirstname(),
                        req.getLastname(),
						role
                        );
				userToSave.setIdentified(false);
				userToSave.setPositiveReviews(0);
				userToSave.setNegativeReviews(0);
				userToSave.setCurrency(Currency.RSD);
                User savedUser = userRepository.save(userToSave);

                EmailVerificationToken verificationToken =
                		emailVerificationService.createAndSaveToken(savedUser);

                emailVerificationService.sendVerificationEmail(savedUser.getEmail(),
                		savedUser.getFirstname(), verificationToken.getToken());

                return userMapper.toDto(savedUser);
            }
    }
    
    @Override
    public User login(LoginUserRequestDto req) {

        User user = userRepository.findByEmail(req.getEmail());

        if (user == null) {
            throw new WrongCredentialsException("Wrong email or password!");
        }
        if (!user.isEnabled()) {
            throw new WrongCredentialsException(msg("error.user.email_not_verified"));
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new WrongCredentialsException("Wrong email or password!");
        }
        return user;
    }

	@Override
	public UserProfileDto getUserProfile(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found!"));
		return userMapper.toUserProfileDto(user);
	}

    @Override
    public UserDto getMe(Long userId) {
    	User userToReturn = userRepository.findById(userId)
    			.orElseThrow(() -> new UserNotFoundException("User not found"));
    	return userMapper.toDto(userToReturn);
    }

	@Override
	public PublicProfileDto getUser(Pageable pageable, Long userId){
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		UserProfileDto userProfile = userMapper.toUserProfileDto(user);
		Page<AdPreviewDto> adsPage = adService.findAllActiveByUser(pageable, userId);
		Page<ReviewDto> reviewsPage = reviewService.getAllForUser(pageable, userId);

		return new PublicProfileDto(userProfile, adsPage, reviewsPage);
	}

	@Override
	public String getRealPhoneNumber(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        return user.getPhoneNumber();
	}

	@Override
    @Retryable(
    		retryFor = OptimisticLockException.class,
    		maxAttempts = 3,
    		backoff = @Backoff(delay = 100)
    		)
    @Transactional
	public UserDto depositMoney(Long userId, DepositRequestDto req) {
		
    	BigDecimal amount = req.getAmount();
    	
    	User user = userRepository.findByIdForCheck(userId)
    			.orElseThrow(() -> new UserNotFoundException("User not found"));
    	
    	BigDecimal userMoney = user.getCredit();
    	user.setCredit(userMoney.add(amount));
    	
		return userMapper.toDto(
				userRepository.save(user));
	}

	@Transactional
	@Override
	public UpdateUserDto update(UpdateUserDto editUserDto, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
    	if (editUserDto.getFirstname() != null && !editUserDto.getFirstname().isBlank()) user.setFirstname(editUserDto.getFirstname());
    	if (editUserDto.getLastname() != null && !editUserDto.getLastname().isBlank()) user.setLastname(editUserDto.getLastname());
    	if(editUserDto.getEmail() != null && !editUserDto.getEmail().equals(user.getEmail())){
			if (userRepository.existsByEmail(editUserDto.getEmail())) {
				throw new IllegalArgumentException(msg("error.user.email_taken"));
			}
			user.setEmail(editUserDto.getEmail());
			user.setEnabled(false);
			EmailVerificationToken verificationToken = emailVerificationService.createAndSaveToken(user);
			emailVerificationService.sendVerificationEmail(user.getEmail(), user.getFirstname(), verificationToken.getToken());
		}
		if (editUserDto.getDescription() != null) user.setDescription(HtmlSanitizer.sanitize(editUserDto.getDescription()));
		if (editUserDto.getPhoneNumber() != null) {
			user.setPhoneNumber(editUserDto.getPhoneNumber().isBlank() ? null : editUserDto.getPhoneNumber());
		}
		if (editUserDto.getAddress() != null) {
			user.setAddress(editUserDto.getAddress().isBlank() ? null : editUserDto.getAddress().trim());
		}
		if (editUserDto.getAvatarUrl() != null) user.setAvatarUrl(editUserDto.getAvatarUrl());
		if (editUserDto.getCurrency() != null) {
			try {
				user.setCurrency(Currency.valueOf(editUserDto.getCurrency()));
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException(msg("error.user.unknown_currency", editUserDto.getCurrency()));
			}
		}
		if (editUserDto.getLocationId() != null) {
			if (editUserDto.getLocationId() == 0) {
				user.setLocation(null);
			} else {
				locationRepository.findById(editUserDto.getLocationId()).ifPresent(user::setLocation);
			}
		}
		try {
    		user = userRepository.save(user);
		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			throw new IllegalArgumentException(msg("error.user.email_taken"));
		}
    	return userMapper.toEditDto(user);
	}

    @Transactional
	@Override
	public String updatePassword(ChangeUserPasswordDto data, Long userId) {
    	User user = userRepository.findById(userId)
    			.orElseThrow(() -> new UserNotFoundException("User not found"));

    	boolean hasPassword = user.getPassword() != null && !user.getPassword().isBlank();

    	if (!hasPassword) {
    		// Nalog je napravljen socijalnom prijavom i jos nema lozinku, pa nema
    		// sta da se poredi. Bezbedno je jer je korisnik vec autentikovan
    		// sesijom - ovo NIJE put za zaobilazenje stare lozinke, sto stiti
    		// uslov iznad: cim lozinka postoji, trazi se i potvrda.
    		user.setPassword(passwordEncoder.encode(data.getNewPassword()));
    		userRepository.save(user);
    		return "Successfully set password!";
    	}

    	if (!passwordEncoder.matches(data.getOldPassword(), user.getPassword())) {
    		throw new WrongCredentialsException("Error while changing password!");
    	}

    	user.setPassword(passwordEncoder.encode(data.getNewPassword()));
    	userRepository.save(user);
    	return "Successfully changed password!";
	}

    @Transactional
	@Override
	public String deleteMe(Long myId) {
		User user = userRepository.findById(myId).orElseThrow(() -> new UserNotFoundException("User not found!"));
		user.setEnabled(false);
		user.setEmail("deleted_" + myId + "@landm.org");
		user.setFirstname("Obrisani");
		user.setLastname("Korisnik");
		user.setPhoneNumber(null);
		userRepository.save(user);

		return "Successfully deleted Your account!";
	}  
    
    @Recover
    public UserDto recover(OptimisticLockException e) {
    	throw new IllegalStateException(msg("error.common.optimistic_lock"));
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public User facebookLogin(String accessToken) {
        // Validate token against our app via debug_token
        URI debugUri = UriComponentsBuilder
                .fromHttpUrl("https://graph.facebook.com/debug_token")
                .queryParam("input_token", accessToken)
                .queryParam("access_token", facebookAppId + "|" + facebookAppSecret)
                .build().toUri();

        Map<String, Object> debugResponse;
        try {
            debugResponse = restTemplate.getForObject(debugUri, Map.class);
        } catch (Exception e) {
            throw new WrongCredentialsException(msg("error.auth.facebook_validation"));
        }

        if (debugResponse == null) throw new WrongCredentialsException(msg("error.auth.facebook_invalid_token"));

        Map<String, Object> data = (Map<String, Object>) debugResponse.get("data");
        if (data == null || !Boolean.TRUE.equals(data.get("is_valid"))) {
            throw new WrongCredentialsException(msg("error.auth.facebook_token_not_valid"));
        }
        if (!facebookAppId.equals(String.valueOf(data.get("app_id")))) {
            throw new WrongCredentialsException(msg("error.auth.facebook_wrong_app"));
        }

        String facebookUserId = (String) data.get("user_id");

        // Fetch user profile data
        URI meUri = UriComponentsBuilder
                .fromHttpUrl("https://graph.facebook.com/me")
                .queryParam("fields", "id,email,first_name,last_name")
                .queryParam("access_token", accessToken)
                .build().toUri();

        Map<String, String> fbUser;
        try {
            fbUser = restTemplate.getForObject(meUri, Map.class);
        } catch (Exception e) {
            throw new WrongCredentialsException(msg("error.auth.facebook_fetch_user"));
        }

        if (fbUser == null) throw new WrongCredentialsException(msg("error.auth.facebook_no_user"));

        String email = fbUser.get("email");
        String firstName = fbUser.getOrDefault("first_name", "Facebook");
        String lastName = fbUser.getOrDefault("last_name", "Korisnik");

        // Find or create user
        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email);
        }
        if (user == null) {
            user = userRepository.findByFacebookId(facebookUserId);
        }

        if (user == null) {
            String userEmail = email != null ? email : (facebookUserId + "@facebook.placeholder");
            Role role = roleRepository.findByName("ROLE_USER");
            user = new User(userEmail, "", firstName, lastName, role);
            user.setEnabled(true);
            user.setIdentified(false);
            user.setPositiveReviews(0);
            user.setNegativeReviews(0);
            user.setCurrency(Currency.RSD);
            user.setFacebookId(facebookUserId);
            user = userRepository.save(user);
        } else if (!user.isEnabled()) {
            throw new WrongCredentialsException(msg("error.auth.account_disabled"));
        } else if (user.getFacebookId() == null) {
            user.setFacebookId(facebookUserId);
            user = userRepository.save(user);
        }

        return user;
    }

    @Override
    @Transactional
    public User appleLogin(String identityToken) {
        try {
            JWKSet jwkSet = JWKSet.load(new URL("https://appleid.apple.com/auth/keys"));

            SignedJWT signedJWT = SignedJWT.parse(identityToken);

            String kid = signedJWT.getHeader().getKeyID();
            com.nimbusds.jose.jwk.JWK jwk = jwkSet.getKeyByKeyId(kid);
            if (jwk == null) throw new WrongCredentialsException(msg("error.auth.apple_key_not_found"));

            JWSVerifier verifier = new RSASSAVerifier(((RSAKey) jwk).toRSAPublicKey());
            if (!signedJWT.verify(verifier)) {
                throw new WrongCredentialsException(msg("error.auth.apple_signature_invalid"));
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
                throw new WrongCredentialsException(msg("error.auth.apple_unknown_issuer"));
            }
            if (!claims.getAudience().contains(appleClientId)) {
                throw new WrongCredentialsException(msg("error.auth.apple_wrong_app"));
            }
            if (claims.getExpirationTime().before(new Date())) {
                throw new WrongCredentialsException(msg("error.auth.apple_token_expired"));
            }

            String appleUserId = claims.getSubject();
            String email = claims.getStringClaim("email");

            // Find or create user
            User user = userRepository.findByAppleId(appleUserId);
            if (user == null && email != null) {
                user = userRepository.findByEmail(email);
            }

            if (user == null) {
                String userEmail = email != null ? email : (appleUserId + "@apple.placeholder");
                Role role = roleRepository.findByName("ROLE_USER");
                user = new User(userEmail, "", "Apple", "Korisnik", role);
                user.setEnabled(true);
                user.setIdentified(false);
                user.setPositiveReviews(0);
                user.setNegativeReviews(0);
                user.setCurrency(Currency.RSD);
                user.setAppleId(appleUserId);
                user = userRepository.save(user);
            } else if (!user.isEnabled()) {
                throw new WrongCredentialsException(msg("error.auth.account_disabled"));
            } else if (user.getAppleId() == null) {
                user.setAppleId(appleUserId);
                user = userRepository.save(user);
            }

            return user;
        } catch (WrongCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(msg("error.auth.apple_login_error", e.getMessage()), e);
        }
    }

    @Override
    @Transactional
    public User googleLogin(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(List.of(googleClientId))
                    .build();

            GoogleIdToken googleToken = verifier.verify(idToken);
            if (googleToken == null) {
                throw new WrongCredentialsException(msg("error.auth.google_invalid_token"));
            }

            GoogleIdToken.Payload payload = googleToken.getPayload();
            if (!payload.getEmailVerified()) {
                throw new WrongCredentialsException(msg("error.auth.google_email_not_verified"));
            }

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            if (firstName == null) firstName = email.split("@")[0];
            if (lastName == null) lastName = "";

            User user = userRepository.findByEmail(email);
            if (user == null) {
                Role role = roleRepository.findByName("ROLE_USER");
                user = new User(email, "", firstName, lastName, role);
                user.setEnabled(true);
                user.setIdentified(false);
                user.setPositiveReviews(0);
                user.setNegativeReviews(0);
                user.setCurrency(Currency.RSD);
                user = userRepository.save(user);
            } else if (!user.isEnabled()) {
                throw new WrongCredentialsException(msg("error.auth.account_disabled"));
            }

            return user;
        } catch (WrongCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(msg("error.auth.google_login_error", e.getMessage()), e);
        }
    }

}
