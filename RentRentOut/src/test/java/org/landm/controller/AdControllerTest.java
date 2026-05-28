package org.landm.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.landm.dto.ad.AdDto;
import org.landm.dto.ad.CreateAdRequestDto;
import org.landm.entity.Enums.Currency;
import org.landm.entity.Enums.PriceInterval;
import org.landm.security.JwtFilter;
import org.landm.security.JwtUtil;
import org.landm.service.AdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private AdService adService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private JwtFilter jwtFilter;

    // ovo je claude dodao
    private RequestPostProcessor authAs(String userId) {
        return request -> {
            request.setUserPrincipal(new UsernamePasswordAuthenticationToken(
                    userId, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
            return request;
        };
    }

    @Test
    void TestGetAdById() throws Exception {
        AdDto ad = new AdDto();
        ad.setId(1L);
        ad.setTitle("Bušilica");

        when(adService.getAdById(1L)).thenReturn(ad);

        mockMvc.perform(get("/api/ads/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bušilica"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void TestCreateAd_ValidRequest() throws Exception {
        CreateAdRequestDto req = new CreateAdRequestDto();
        req.setTitle("Test");
        req.setDescription("Opis");
        req.setPrice(BigDecimal.valueOf(100));
        req.setCurrency(Currency.RSD);
        req.setPriceInterval(PriceInterval.PER_DAY);
        req.setCategoryId(1L);
        req.setLocationId(1L);
        req.setTotalQuantity(1);
        req.setImages(List.of("https://example.com/img.jpg"));
        req.setDeposit(BigDecimal.valueOf(200));

        AdDto ad = new AdDto();
        ad.setId(5L);
        when(adService.create(any(), eq(10L))).thenReturn(ad);

        mockMvc.perform(post("/api/ads")
                        .with(authAs("10"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void createAd_BadRequest() throws Exception {
        mockMvc.perform(post("/api/ads")
                        .with(authAs("10"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteAd() throws Exception {
        when(adService.deleteAd(5L, 10L)).thenReturn("Successfully deleted your Ad!");

        mockMvc.perform(delete("/api/ads/5").with(authAs("10")))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully deleted your Ad!"));

        verify(adService).deleteAd(5L, 10L);
    }
}
