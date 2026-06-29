package com.example.movie.controller.admin;

import com.example.movie.dto.admin.DashboardStatisticsDTO;
import com.example.movie.service.AdminStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsControllerTest {

    @Mock
    private AdminStatisticsService adminStatisticsService;

    @InjectMocks
    private AdminStatisticsController adminStatisticsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
      mockMvc = MockMvcBuilders.standaloneSetup(adminStatisticsController).build();
    }

    @Test
    void shouldReturnStatisticsPayload() throws Exception {
        DashboardStatisticsDTO dto = new DashboardStatisticsDTO();
        dto.setRangeDays(30);
        dto.setGeneratedAt(LocalDateTime.of(2026, 6, 29, 12, 0));
        given(adminStatisticsService.getDashboardStatistics(eq(30))).willReturn(dto);

        mockMvc.perform(get("/api/admin/statistics").param("range", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.rangeDays").value(30));
    }
}
