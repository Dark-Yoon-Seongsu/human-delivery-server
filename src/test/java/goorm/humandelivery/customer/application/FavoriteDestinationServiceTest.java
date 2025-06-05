package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.dto.FavoriteDestinationDto;
import goorm.humandelivery.customer.application.port.out.FavoriteDestinationPort;
import goorm.humandelivery.customer.application.port.out.LoadCustomerPort;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.domain.FavoriteDestination;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteDestinationServiceTest {

    @InjectMocks
    private FavoriteDestinationService favoriteDestinationService;

    @Mock
    private FavoriteDestinationPort favoriteDestinationPort;

    @Mock
    private LoadCustomerPort loadCustomerPort;

    private Customer customer;
    private FavoriteDestination favoriteDestination;
    private FavoriteDestinationDto.CreateRequest createRequest;
    private FavoriteDestinationDto.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .loginId("testUser")
                .password("password")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        try {
            java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(customer, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        favoriteDestination = FavoriteDestination.create(
                customer,
                "집",
                "서울시 강남구",
                37.123456,
                127.123456
        );

        createRequest = new FavoriteDestinationDto.CreateRequest(
                "집",
                "서울시 강남구",
                37.123456,
                127.123456
        );

        updateRequest = new FavoriteDestinationDto.UpdateRequest(
                "회사",
                "서울시 서초구",
                37.234567,
                127.234567
        );
    }

    @Nested
    @DisplayName("즐겨찾는 목적지 생성")
    class CreateFavoriteDestination {

        @Test
        @DisplayName("성공적으로 즐겨찾는 목적지를 생성한다")
        void createFavoriteDestinationSuccess() {
            // given
            when(loadCustomerPort.findById(1L)).thenReturn(Optional.of(customer));
            when(favoriteDestinationPort.save(any(FavoriteDestination.class))).thenReturn(favoriteDestination);

            // when
            FavoriteDestinationDto.Response response = favoriteDestinationService.createFavoriteDestination(1L, createRequest);

            // then
            assertThat(response.getName()).isEqualTo("집");
            assertThat(response.getAddress()).isEqualTo("서울시 강남구");
            assertThat(response.getLatitude()).isEqualTo(37.123456);
            assertThat(response.getLongitude()).isEqualTo(127.123456);

            verify(loadCustomerPort).findById(1L);
            verify(favoriteDestinationPort).save(any(FavoriteDestination.class));
        }

        @Test
        @DisplayName("존재하지 않는 고객 ID로 생성 시 예외가 발생한다")
        void createFavoriteDestinationWithInvalidCustomerId() {
            // given
            when(loadCustomerPort.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> favoriteDestinationService.createFavoriteDestination(999L, createRequest))
                    .isInstanceOf(CustomerNotFoundException.class);

            verify(loadCustomerPort).findById(999L);
            verify(favoriteDestinationPort, never()).save(any(FavoriteDestination.class));
        }
    }

    @Nested
    @DisplayName("즐겨찾는 목적지 목록 조회")
    class GetFavoriteDestinations {

        @Test
        @DisplayName("성공적으로 즐겨찾는 목적지 목록을 조회한다")
        void getFavoriteDestinationsSuccess() {
            // given
            List<FavoriteDestination> destinations = Arrays.asList(
                    favoriteDestination,
                    FavoriteDestination.create(customer, "회사", "서울시 서초구", 37.234567, 127.234567)
            );
            when(favoriteDestinationPort.findByCustomerId(1L)).thenReturn(destinations);

            // when
            List<FavoriteDestinationDto.Response> responses = favoriteDestinationService.getFavoriteDestinations(1L);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getName()).isEqualTo("집");
            assertThat(responses.get(1).getName()).isEqualTo("회사");

            verify(favoriteDestinationPort).findByCustomerId(1L);
        }
    }

    @Nested
    @DisplayName("즐겨찾는 목적지 수정")
    class UpdateFavoriteDestination {

        @Test
        @DisplayName("성공적으로 즐겨찾는 목적지를 수정한다")
        void updateFavoriteDestinationSuccess() {
            // given
            when(favoriteDestinationPort.findById(1L)).thenReturn(Optional.of(favoriteDestination));

            // when
            FavoriteDestinationDto.Response response = favoriteDestinationService.updateFavoriteDestination(1L, 1L, updateRequest);

            // then
            assertThat(response.getName()).isEqualTo("회사");
            assertThat(response.getAddress()).isEqualTo("서울시 서초구");
            assertThat(response.getLatitude()).isEqualTo(37.234567);
            assertThat(response.getLongitude()).isEqualTo(127.234567);

            verify(favoriteDestinationPort).findById(1L);
        }

        @Test
        @DisplayName("다른 고객의 즐겨찾는 목적지를 수정하려고 할 때 예외가 발생한다")
        void updateFavoriteDestinationWithInvalidCustomer() {
            // given
            Customer otherCustomer = Customer.builder()
                    .loginId("otherUser")
                    .password("password")
                    .name("Other User")
                    .phoneNumber("010-8765-4321")
                    .build();
            try {
                java.lang.reflect.Field idField = Customer.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(otherCustomer, 2L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            FavoriteDestination otherDestination = FavoriteDestination.create(
                    otherCustomer,
                    "집",
                    "서울시 강남구",
                    37.123456,
                    127.123456
            );
            when(favoriteDestinationPort.findById(1L)).thenReturn(Optional.of(otherDestination));

            // when & then
            assertThatThrownBy(() -> favoriteDestinationService.updateFavoriteDestination(1L, 1L, updateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Not authorized to update this destination");

            verify(favoriteDestinationPort).findById(1L);
        }
    }

    @Nested
    @DisplayName("즐겨찾는 목적지 삭제")
    class DeleteFavoriteDestination {

        @Test
        @DisplayName("성공적으로 즐겨찾는 목적지를 삭제한다")
        void deleteFavoriteDestinationSuccess() {
            // given
            doNothing().when(favoriteDestinationPort).deleteByIdAndCustomerId(1L, 1L);

            // when
            favoriteDestinationService.deleteFavoriteDestination(1L, 1L);

            // then
            verify(favoriteDestinationPort).deleteByIdAndCustomerId(1L, 1L);
        }
    }
} 