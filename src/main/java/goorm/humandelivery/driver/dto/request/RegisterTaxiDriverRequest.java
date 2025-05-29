package goorm.humandelivery.driver.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterTaxiDriverRequest {

    @Email
    @NotBlank
    private String loginId;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @NotBlank
    private String licenseCode;

    @NotBlank
    private String phoneNumber;

    @Valid
    private RegisterTaxiRequest taxi;


}
