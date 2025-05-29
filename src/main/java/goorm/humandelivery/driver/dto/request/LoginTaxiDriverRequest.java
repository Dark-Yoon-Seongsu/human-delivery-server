package goorm.humandelivery.driver.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginTaxiDriverRequest {

    @Email
    @NotBlank
    private String loginId;

    @NotBlank
    private String password;
}
