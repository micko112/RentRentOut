package org.landm.service.impl;

import org.landm.dto.user.UserDto;
import org.landm.dto.user.ChangeUserPasswordDto;
import org.landm.dto.user.LoginUserRequestDto;
import org.landm.dto.user.RegisterUserRequestDto;
import org.landm.dto.user.UpdateUserDto;
import org.landm.entity.Category;
import org.landm.entity.EmailVerificationToken;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.exception.WrongCredentialsException;
//import org.landm.exception.UserNotFoundException;
//import org.landm.exception.WrongCredentialsException;
import org.landm.mapper.UserMapper;
import org.landm.repository.RoleRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.EmailVerificationMailService;
import org.landm.service.UserService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    private final EmailVerificationMailService emailVerificationService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder, UserMapper userMapper,
                           JwtUtil jwtUtil, RoleRepository roleRepository, 
                           EmailVerificationMailService emailVerificationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
		this.roleRepository = roleRepository;
		this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    @Override
    public UserDto register(RegisterUserRequestDto req) {

		Role role = roleRepository.findByName(req.getRole());
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

        if (user != null) {
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
//    	long userId = jwtUtil.extractUserId(authHeader.substring(7));
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
	public UserDto get(long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("User not found!"));
		return userMapper.toDto(user);
	}
    
    @Override
    public UpdateUserDto getMe(long userId) {
    	User userToReturn = userRepository.findById(userId)
    			.orElseThrow(() -> new UserNotFoundException("Error with updating user data!"));
    	return userMapper.toEditDto(userToReturn);
    }
	
    @Transactional
	@Override
	public UpdateUserDto update(UpdateUserDto editUserDto, long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException("Error with updating user data!"));
    	if(editUserDto.getFirstname() != null) user.setFirstname(editUserDto.getFirstname());
    	if(editUserDto.getLastname() != null) user.setLastname(editUserDto.getLastname());
    	if(editUserDto.getEmail() != null) user.setEmail(editUserDto.getEmail());
    	user = userRepository.save(user);
    	return userMapper.toEditDto(user);
	}

    @Transactional
	@Override
	public String updatePassword(ChangeUserPasswordDto data, long userId) {
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
	public String deleteMe(long myId) {
    	try {
    		userRepository.deleteById(myId);
    		return "Successfully deleted Your account!";
    	} catch(EmptyResultDataAccessException ex) {
    		throw new UserNotFoundException("User not found!");
    	}
	}  
    
}
