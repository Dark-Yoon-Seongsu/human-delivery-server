package goorm.humandelivery.customer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    @Test
    @DisplayName("고객 생성 시 모든 필드가 올바르게 설정된다")
    void createCustomer() {
        // given
        String loginId = "testUser";
        String password = "password123";
        String name = "홍길동";
        String phoneNumber = "010-1234-5678";

        // when
        Customer customer = Customer.builder()
                .loginId(loginId)
                .password(password)
                .name(name)
                .phoneNumber(phoneNumber)
                .build();

        // then
        assertThat(customer.getLoginId()).isEqualTo(loginId);
        assertThat(customer.getPassword()).isEqualTo(password);
        assertThat(customer.getName()).isEqualTo(name);
        assertThat(customer.getPhoneNumber()).isEqualTo(phoneNumber);
    }

    @Test
    @DisplayName("빌더 패턴으로 고객 객체를 생성할 수 있다")
    void createCustomerWithBuilder() {
        // given & when
        Customer customer = Customer.builder()
                .loginId("user123")
                .password("securePassword")
                .name("김철수")
                .phoneNumber("010-9876-5432")
                .build();

        // then
        assertThat(customer).isNotNull();
        assertThat(customer.getLoginId()).isEqualTo("user123");
        assertThat(customer.getName()).isEqualTo("김철수");
    }
} 