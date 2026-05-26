package az.saglamol.payment.controller;

import az.saglamol.common.security.AuthContextHolder;
import az.saglamol.payment.dto.request.CreateInvoiceRequest;
import az.saglamol.payment.dto.response.InvoiceResponse;
import az.saglamol.payment.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return invoiceService.createInvoice(AuthContextHolder.getRequired(), request);
    }

    @GetMapping("/{id}")
    public InvoiceResponse getInvoice(@PathVariable UUID id) {
        return invoiceService.getInvoice(id, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-company")
    public Page<InvoiceResponse> getByCompany(@RequestParam UUID companyId, @PageableDefault(size = 20) Pageable pageable) {
        return invoiceService.getByCompany(companyId, pageable, AuthContextHolder.getRequired());
    }

    @GetMapping("/by-hospital")
    public Page<InvoiceResponse> getByHospital(@RequestParam UUID hospitalId, @PageableDefault(size = 20) Pageable pageable) {
        return invoiceService.getByHospital(hospitalId, pageable, AuthContextHolder.getRequired());
    }

    @PostMapping("/{id}/issue")
    public InvoiceResponse issueInvoice(@PathVariable UUID id) {
        return invoiceService.issueInvoice(id, AuthContextHolder.getRequired());
    }

    @PostMapping("/{id}/mark-paid")
    public InvoiceResponse markPaid(@PathVariable UUID id) {
        return invoiceService.markPaid(id, AuthContextHolder.getRequired());
    }

    @PostMapping("/{id}/cancel")
    public InvoiceResponse cancelInvoice(@PathVariable UUID id) {
        return invoiceService.cancelInvoice(id, AuthContextHolder.getRequired());
    }
}
