package goorm.humandelivery.customer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.humandelivery.customer.application.dto.FavoriteDestinationDto;
import goorm.humandelivery.customer.application.port.in.FavoriteDestinationUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FavoriteDestinationController.class)
class FavoriteDestinationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FavoriteDestinationUseCase favoriteDestinationUseCase;

    @Test
    @WithMockUser
    @DisplayName("즐겨찾는 목적지를 성공적으로 생성한다")
    void createFavoriteDestinationSuccess() throws Exception {
        // given
        FavoriteDestinationDto.CreateRequest request = new FavoriteDestinationDto.CreateRequest(
                "집",
                "서울시 강남구",
                37.123456,
                127.123456
        );

        FavoriteDestinationDto.Response response = new FavoriteDestinationDto.Response(
                1L,
                "집",
                "서울시 강남구",
                37.123456,
                127.123456
        );

        when(favoriteDestinationUseCase.createFavoriteDestination(eq(1L), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/customers/1/favorite-destinations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("집"))
                .andExpect(jsonPath("$.address").value("서울시 강남구"))
                .andExpect(jsonPath("$.latitude").value(37.123456))
                .andExpect(jsonPath("$.longitude").value(127.123456));

        verify(favoriteDestinationUseCase).createFavoriteDestination(eq(1L), any());
    }

    @Test
    @WithMockUser
    @DisplayName("즐겨찾는 목적지 목록을 성공적으로 조회한다")
    void getFavoriteDestinationsSuccess() throws Exception {
        // given
        List<FavoriteDestinationDto.Response> responses = Arrays.asList(
                new FavoriteDestinationDto.Response(1L, "집", "서울시 강남구", 37.123456, 127.123456),
                new FavoriteDestinationDto.Response(2L, "회사", "서울시 서초구", 37.234567, 127.234567)
        );

        when(favoriteDestinationUseCase.getFavoriteDestinations(1L))
                .thenReturn(responses);

        // when & then
        mockMvc.perform(get("/api/v1/customers/1/favorite-destinations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("집"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("회사"));

        verify(favoriteDestinationUseCase).getFavoriteDestinations(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("즐겨찾는 목적지를 성공적으로 수정한다")
    void updateFavoriteDestinationSuccess() throws Exception {
        // given
        FavoriteDestinationDto.UpdateRequest request = new FavoriteDestinationDto.UpdateRequest(
                "회사",
                "서울시 서초구",
                37.234567,
                127.234567
        );

        FavoriteDestinationDto.Response response = new FavoriteDestinationDto.Response(
                1L,
                "회사",
                "서울시 서초구",
                37.234567,
                127.234567
        );

        when(favoriteDestinationUseCase.updateFavoriteDestination(eq(1L), eq(1L), any()))
                .thenReturn(response);

        // when & then
        mockMvc.perform(put("/api/v1/customers/1/favorite-destinations/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("회사"))
                .andExpect(jsonPath("$.address").value("서울시 서초구"))
                .andExpect(jsonPath("$.latitude").value(37.234567))
                .andExpect(jsonPath("$.longitude").value(127.234567));

        verify(favoriteDestinationUseCase).updateFavoriteDestination(eq(1L), eq(1L), any());
    }

    @Test
    @WithMockUser
    @DisplayName("즐겨찾는 목적지를 성공적으로 삭제한다")
    void deleteFavoriteDestinationSuccess() throws Exception {
        // given
        doNothing().when(favoriteDestinationUseCase).deleteFavoriteDestination(1L, 1L);

        // when & then
        mockMvc.perform(delete("/api/v1/customers/1/favorite-destinations/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(favoriteDestinationUseCase).deleteFavoriteDestination(1L, 1L);
    }
} 