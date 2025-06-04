package goorm.humandelivery.driver.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterTaxiDriverRequest {

    @Email
    @NotBlank(message = "아이디를 입력해 주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해 주세요.")
    private String password;

    @NotBlank(message = "이름을 입력해 주세요.")
    private String name;

    @NotBlank(message = "운전면허번호를 입력해 주세요.")
    private String licenseCode;

    @NotBlank(message = "전화번호를 입력해 주세요.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식은 010-XXXX-XXXX 이어야 합니다.")
    private String phoneNumber;

    @Valid
    private RegisterTaxiRequest taxi;

}
