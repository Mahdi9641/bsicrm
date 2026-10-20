package com.bsicenter.crm.controller;

import com.bsicenter.crm.dto.BookingRequests;
import com.bsicenter.crm.service.CustomerPortalService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portal")
public class CustomerPortalController {
  private final CustomerPortalService service;
  public CustomerPortalController(CustomerPortalService service) { this.service = service; }
  @GetMapping("/me") public Object me() { return service.me(); }
  @GetMapping("/appointments") public Object appointments() { return service.appointments(); }
  @PostMapping("/appointments") public Object book(@Valid @RequestBody BookingRequests.Create body) { return service.book(body); }
  @PostMapping("/appointments/{id}/cancel") public Object cancel(@PathVariable long id, @Valid @RequestBody(required=false) Cancel body) { return service.cancel(id, body == null ? "لغو توسط مشتری" : body.reason()); }
  @GetMapping("/catalog") public Object catalog() { return service.catalog(); }
  @PostMapping("/availability") public Object availability(@Valid @RequestBody BookingRequests.Availability body){return service.availability(body);}
  @PutMapping("/appointments/{id}/schedule") public Object reschedule(@PathVariable long id,@Valid @RequestBody BookingRequests.Reschedule body){return service.reschedule(id,body);}
  @GetMapping("/history") public Object history(){return service.history();}
  @PutMapping("/me") public Object profile(@Valid @RequestBody Profile b){return service.profile(b.firstName(),b.lastName(),b.city(),b.address(),b.contactChannel(),b.marketingConsent());}
  public record Profile(@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=100) String firstName,@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=100) String lastName,@jakarta.validation.constraints.Size(max=100) String city,@jakarta.validation.constraints.Size(max=500) String address,@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.Pattern(regexp="SMS|PHONE|WHATSAPP") String contactChannel,boolean marketingConsent){}
  public record Cancel(@jakarta.validation.constraints.Size(max=500) String reason) {}
}
