package org.landm.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.landm.dto.user.UserDto;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private RequestPostProcessor authAs(String userId) {
        return request -> {
            request.setUserPrincipal(new UsernamePasswordAuthenticationToken(
                    userId, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
            return request;
        };
    }
    @Test
    void getMe_AuthUser_returnsUserDto() throws Exception {
        UserDto dto = new UserDto();
        dto.setId(5L);
        dto.setEmail("test@test.com");
        dto.setFirstname("test");

        when(userService.getMe(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/user/me").with(authAs("5")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(dto.getEmail()));

        verify(userService).getMe(5L);
    }

}
