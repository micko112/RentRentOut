package org.landm.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.landm.dto.user.UserDto;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Role;
import org.landm.entity.User;
import org.landm.mapper.UserMapper;
import org.landm.security.JwtUtil;
import org.landm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper om;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private UserMapper userMapper;

    private RequestPostProcessor authAs(String userId) {
        return request -> {
            request.setUserPrincipal(new UsernamePasswordAuthenticationToken(
                    userId, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
            return request;
        };
    }
    @Test
    void getMe_AuthUser_returnsUserDto() throws Exception {
        UserDto dto = userMapper.toDto(createUser("test@test.com"));


        when(userService.getMe(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/user/me").with(authAs("5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(dto.getEmail()));

        verify(userService).getMe(5L);
    }
    @Test
    void getMe_anonymousUser_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("test123");
        user.setFirstname("test");
        user.setLastname("testic");
        user.setCurrency(Currency.RSD);
        Role role = new Role();
        role.setName("USER_" + UUID.randomUUID());
        user.setRole(role);
        return user;

    }
}
