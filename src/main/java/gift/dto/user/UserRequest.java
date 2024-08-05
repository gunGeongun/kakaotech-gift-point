package gift.dto.user;

import gift.common.enums.LoginType;
import gift.model.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UserRequest {
    public record Create(
            @Email
            @NotBlank
            String email,
            @NotBlank
            String password,
            @NotBlank
            String name

    ) {
        public User toEntity() {
            return new User(this.email, this.password, this.name, LoginType.DEFAULT);
        }
    }

    public record Check(
            String email,
            String password
    ) {

    }
}
