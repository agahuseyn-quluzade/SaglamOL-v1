package az.saglamol.policy.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.policy.dto.request.CreateInsuranceProductRequest;
import az.saglamol.policy.dto.request.UpdateInsuranceProductRequest;
import az.saglamol.policy.dto.response.InsuranceProductResponse;
import az.saglamol.policy.entity.InsuranceProductStatus;
import az.saglamol.policy.service.InsuranceProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/insurance-products")
public class InsuranceProductController {

    private final InsuranceProductService productService;

    public InsuranceProductController(InsuranceProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InsuranceProductResponse createProduct(@Valid @RequestBody CreateInsuranceProductRequest request) {
        return productService.createProduct(AuthContextHolder.getRequired(), request);
    }

    @PutMapping("/{productId}")
    public InsuranceProductResponse updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateInsuranceProductRequest request
    ) {
        return productService.updateProduct(AuthContextHolder.getRequired(), productId, request);
    }

    @PatchMapping("/{productId}/status")
    public InsuranceProductResponse changeStatus(
            @PathVariable UUID productId,
            @RequestParam InsuranceProductStatus status
    ) {
        return productService.changeStatus(AuthContextHolder.getRequired(), productId, status);
    }

    @GetMapping("/{productId}")
    public InsuranceProductResponse getProduct(@PathVariable UUID productId) {
        return productService.getProduct(productId, AuthContextHolder.getRequired());
    }

    @GetMapping
    public Page<InsuranceProductResponse> searchProducts(
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) InsuranceProductStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return productService.searchProducts(companyId, status, pageable, AuthContextHolder.getRequired());
    }
}
