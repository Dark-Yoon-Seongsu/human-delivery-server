package goorm.humandelivery.customer.application;

import goorm.humandelivery.customer.application.dto.FavoriteDestinationDto;
import goorm.humandelivery.customer.application.port.in.FavoriteDestinationUseCase;
import goorm.humandelivery.customer.domain.Customer;
import goorm.humandelivery.customer.domain.FavoriteDestination;
import goorm.humandelivery.customer.exception.CustomerNotFoundException;
import goorm.humandelivery.customer.infrastructure.persistence.JpaCustomerRepository;
import goorm.humandelivery.customer.infrastructure.persistence.FavoriteDestinationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FavoriteDestinationIntegrationTest {

    @Autowired
    private FavoriteDestinationUseCase favoriteDestinationUseCase;

    @Autowired
    private JpaCustomerRepository customerRepository;

    @Autowired
    private FavoriteDestinationRepository favoriteDestinationRepository;

    private Customer customer;
    private FavoriteDestinationDto.CreateRequest createRequest;
    private FavoriteDestinationDto.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // 고객 생성
        customer = Customer.builder()
                .loginId("testUser")
                .password("password")
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .build();
        customer = ((org.springframework.data.repository.CrudRepository<Customer, Long>) customerRepository).save(customer);

        // 요청 객체 생성
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

    @AfterEach
    void tearDown() {
        favoriteDestinationRepository.deleteAllInBatch();
        customerRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("즐겨찾는 목적지를 생성하고 조회한다")
    void createAndGetFavoriteDestination() {
        // given
        FavoriteDestinationDto.Response created = favoriteDestinationUseCase.createFavoriteDestination(
                customer.getId(),
                createRequest
        );

        // when
        List<FavoriteDestinationDto.Response> destinations = favoriteDestinationUseCase.getFavoriteDestinations(customer.getId());

        // then
        assertThat(destinations).hasSize(1);
        assertThat(destinations.get(0).getName()).isEqualTo("집");
        assertThat(destinations.get(0).getAddress()).isEqualTo("서울시 강남구");
        assertThat(destinations.get(0).getLatitude()).isEqualTo(37.123456);
        assertThat(destinations.get(0).getLongitude()).isEqualTo(127.123456);
    }

    @Test
    @DisplayName("즐겨찾는 목적지를 수정한다")
    void updateFavoriteDestination() {
        // given
        FavoriteDestinationDto.Response created = favoriteDestinationUseCase.createFavoriteDestination(
                customer.getId(),
                createRequest
        );

        // when
        FavoriteDestinationDto.Response updated = favoriteDestinationUseCase.updateFavoriteDestination(
                customer.getId(),
                created.getId(),
                updateRequest
        );

        // then
        assertThat(updated.getName()).isEqualTo("회사");
        assertThat(updated.getAddress()).isEqualTo("서울시 서초구");
        assertThat(updated.getLatitude()).isEqualTo(37.234567);
        assertThat(updated.getLongitude()).isEqualTo(127.234567);
    }

    @Test
    @DisplayName("즐겨찾는 목적지를 삭제한다")
    void deleteFavoriteDestination() {
        // given
        FavoriteDestinationDto.Response created = favoriteDestinationUseCase.createFavoriteDestination(
                customer.getId(),
                createRequest
        );

        // when
        favoriteDestinationUseCase.deleteFavoriteDestination(customer.getId(), created.getId());

        // then
        List<FavoriteDestinationDto.Response> destinations = favoriteDestinationUseCase.getFavoriteDestinations(customer.getId());
        assertThat(destinations).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 고객 ID로 즐겨찾는 목적지를 생성하면 예외가 발생한다")
    void createFavoriteDestinationWithInvalidCustomerId() {
        // when & then
        assertThatThrownBy(() -> favoriteDestinationUseCase.createFavoriteDestination(999L, createRequest))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("여러 개의 즐겨찾는 목적지를 생성하고 조회한다")
    void createAndGetMultipleFavoriteDestinations() {
        // given
        FavoriteDestinationDto.CreateRequest secondRequest = new FavoriteDestinationDto.CreateRequest(
                "회사",
                "서울시 서초구",
                37.234567,
                127.234567
        );

        favoriteDestinationUseCase.createFavoriteDestination(customer.getId(), createRequest);
        favoriteDestinationUseCase.createFavoriteDestination(customer.getId(), secondRequest);

        // when
        List<FavoriteDestinationDto.Response> destinations = favoriteDestinationUseCase.getFavoriteDestinations(customer.getId());

        // then
        assertThat(destinations).hasSize(2);
        assertThat(destinations).extracting("name")
                .containsExactlyInAnyOrder("집", "회사");
    }
} 