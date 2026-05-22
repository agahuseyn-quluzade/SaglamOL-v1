package az.saglamol.userprofile.validation;

import az.saglamol.userprofile.dto.request.UpsertPatientProfileRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ProfileValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsInvalidEmailPhoneAndFutureBirthDate() {
        var request = new UpsertPatientProfileRequest(
                "Aga",
                "Huseyn",
                LocalDate.now().plusDays(1),
                null,
                "abc",
                "not-email",
                null,
                null,
                null,
                null,
                null
        );

        assertFalse(validator.validate(request).isEmpty());
    }
}
