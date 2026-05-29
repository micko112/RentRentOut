package org.landm.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.landm.dto.user.UserDto;
import org.landm.entity.User;
import org.landm.exception.UserNotFoundException;
import org.landm.mapper.UserMapper;
import org.landm.repository.CategoryRepository;
import org.landm.repository.LocationRepository;
import org.landm.repository.RoleRepository;
import org.landm.repository.UserRepository;
import org.landm.security.JwtUtil;
import org.landm.service.impl.UserServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailVerificationService emailVerificationService;
    @Mock
    private AdService adService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getMe_returnUserDto(){
        User user = new User();
        user.setId(5L);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userMapper.toDto(user));

        UserDto dto = userService.getMe(5L);
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto).isNotNull();
    }
    @Test
    void getMe_Fail(){
        when(userRepository.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getMe(5L)).isInstanceOf(UserNotFoundException.class);
    }
}
