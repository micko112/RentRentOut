package org.landm.service.impl;

import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
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
import org.landm.repository.RoleRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.AdService;
import org.landm.service.EmailVerificationService;
import org.landm.service.ReviewService;
import org.landm.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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

	public UserServiceImpl(UserRepository userRepository,
						   PasswordEncoder passwordEncoder, UserMapper userMapper,
						   JwtUtil jwtUtil, RoleRepository roleRepository,
						   EmailVerificationService emailVerificationService, AdService adService, ReviewService reviewService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
		this.roleRepository = roleRepository;
		this.emailVerificationService = emailVerificationService;
		this.adService = adService;
		this.reviewService = reviewService;
	}

    @Transactional
    @Override
    public UserDto register(RegisterUserRequestDto req) {

		String roleName = req.getRole();
		if (roleName == null || roleName.isBlank()) {
			roleName = "ROLE_USER";
		}
		Role role = roleRepository.findByName(roleName);
		if (role == null) {
			throw new RuntimeException("Role not found in database: " + roleName);
		}
            if (userRepository.existsByEmail(req.getEmail())) {
//                throw new RuntimeException("Email already exists!");
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
                		verificationToken.getToken());
                
                return userMapper.toDto(savedUser);
                //return new UserDto(req.getFirstname(), req.getLastname(),
                  //      req.getEmail(), BigDecimal.ZERO);
            }
    }
    
    @Override
    public User login(LoginUserRequestDto req) {

        User user = userRepository.findByEmail(req.getEmail());

        if (user != null && user.isEnabled()) {
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
//                throw new RuntimeException("Wrong email or password!");
                throw new WrongCredentialsException("Wrong email or password!");
            }
//            Map<String, Object> respMap = new HashMap<>();
//            List<String> roles = user.getStringRoles();
//            respMap.put("token", jwtUtil.generateToken(user.getId(), roles));
//            respMap.put("user", userMapper.toDto(user));
//            return respMap;
            
            return user;
            
        } else {
//            throw new RuntimeException("User not found!");
            throw new UserNotFoundException("User not found!");
        }
    }
    
//    public UserDto update(UserDto newInfo, String authHeader){
//    	Long userId = jwtUtil.extractUserId(authHeader.substring(7));
//    	Optional<User> userToUpdateOpt = userRepository.findById(userId);
//    	if(userToUpdateOpt.isPresent()) {
//    		User userToUpdate = userToUpdateOpt.get();
//    		userToUpdate.setFirstname(newInfo.getFirstName());
//    		userToUpdate.setLastname(newInfo.getLastname());
//    		userToUpdate = userRepository.save(userToUpdate);
//    		return userMapper.toDto(userToUpdate);
//    	}else {
////            throw new RuntimeException("Error with updating user data!");
//    		throw new UserNotFoundException("Error with updating user data!");
//    	}
//    }
    
	@Override
	public UserProfileDto getUserProfile(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found!"));
		return userMapper.toUserProfileDto(user);
	}

    @Override
    public UserDto getMe(Long userId) {
    	User userToReturn = userRepository.findById(userId)
    			.orElseThrow(() -> new UserNotFoundException("Error with updating user data!"));
    	return userMapper.toDto(userToReturn);
    }

	@Override
	public PublicProfileDto getUser(Pageable pageable, Long userId){
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("Error with updating user data!"));
		UserProfileDto userProfile = userMapper.toUserProfileDto(user);
		Page<AdPreviewDto> adsPage = adService.findAllByUser(pageable, userId);
		Page<ReviewDto> reviewsPage = reviewService.getAllForUser(pageable, userId);

		return new PublicProfileDto(userProfile, adsPage, reviewsPage);
	}

	@Override
	public String getRealPhoneNumber(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Error with updating user data!"));
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
    			.orElseThrow(() -> new RuntimeException("User not found!"));
    	
    	if(true) {
    		BigDecimal userMoney = user.getCredit();
    		user.setCredit(userMoney.add(amount));
    	}
    	
		return userMapper.toDto(
				userRepository.save(user));
	}

	@Transactional
	@Override
	public UpdateUserDto update(UpdateUserDto editUserDto, Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("Error with updating user data!"));
    	if(editUserDto.getFirstname() != null) user.setFirstname(editUserDto.getFirstname());
    	if(editUserDto.getLastname() != null) user.setLastname(editUserDto.getLastname());
    	if(editUserDto.getEmail() != null && !editUserDto.getEmail().equals(user.getEmail())){
			if (userRepository.existsByEmail(editUserDto.getEmail())) {
				throw new RuntimeException("Taj email je već zauzet!"); // Ili neki tvoj Custom exception
			}
			user.setEmail(editUserDto.getEmail());
			// 2. OBAVEZNO MU UKINI PRISTUP DOK NE POTVRDI NOVI EMAIL!
			user.setEnabled(false);

			// 3. Pošalji novi token na novi mejl
			EmailVerificationToken verificationToken = emailVerificationService.createAndSaveToken(user);
			emailVerificationService.sendVerificationEmail(user.getEmail(), verificationToken.getToken());
		}
    	user = userRepository.save(user);
    	return userMapper.toEditDto(user);
	}

    @Transactional
	@Override
	public String updatePassword(ChangeUserPasswordDto data, Long userId) {
    	User user = userRepository.findById(userId)
    			.orElseThrow(() -> new UserNotFoundException("Error while changing password!"));
    	if(passwordEncoder.matches(data.getOldPassword(), user.getPassword())) {
    		user.setPassword(passwordEncoder.encode(data.getNewPassword()));
    		return "Successfully changed password!";
    	}else {
    		throw new WrongCredentialsException("Error while changing password!");
    	}
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
    	throw new RuntimeException("Your request could not be processed due to concurrent update. Please try again.");
    }
    
}
