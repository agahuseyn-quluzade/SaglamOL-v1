package az.saglamol.notification.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateRendererTest {

    @Test
    void rendersPlaceholders() {
        String rendered = new TemplateRenderer().render("Claim {{ claimNumber }} approved for {{amount}}",
                Map.of("claimNumber", "CLM-1", "amount", "120.00"));

        assertEquals("Claim CLM-1 approved for 120.00", rendered);
    }

    @Test
    void missingPlaceholderRendersEmpty() {
        String rendered = new TemplateRenderer().render("Hello {{name}}", Map.of());

        assertEquals("Hello ", rendered);
    }
}
