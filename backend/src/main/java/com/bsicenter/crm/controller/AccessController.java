package com.bsicenter.crm.controller;
import com.bsicenter.crm.security.Access;
import com.bsicenter.crm.service.AccessManagementService;
import com.bsicenter.crm.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/access")
public class AccessController {
  private final AccessManagementService service;private final AuthService auth;
  public AccessController(AccessManagementService service,AuthService auth){this.service=service;this.auth=auth;}
  @GetMapping("/me") public Object me(){return auth.me();}
  @GetMapping("/catalog") public Object catalog(){return service.catalog();}
  @GetMapping("/roles") public Object roles(){return service.roles();}
  @PostMapping("/roles") public Object createRole(@Valid @RequestBody AccessManagementService.RoleWrite body){return service.saveRole(null,body);}
  @PutMapping("/roles/{id}") public Object updateRole(@PathVariable long id,@Valid @RequestBody AccessManagementService.RoleWrite body){return service.saveRole(id,body);}
  @GetMapping("/options") public Object options(@RequestParam(required=false) Long branchId){return service.options(branchId);}
  @GetMapping("/users") public Object users(){return service.users();}
  @GetMapping("/users/{id}") public Object user(@PathVariable long id){return service.user(id);}
  @PostMapping("/users") public Object createUser(@Valid @RequestBody AccessManagementService.UserWrite body){return service.saveUser(null,body);}
  @PutMapping("/users/{id}") public Object updateUser(@PathVariable long id,@Valid @RequestBody AccessManagementService.UserWrite body){return service.saveUser(id,body);}
  @PostMapping("/users/{id}/password") public Object password(@PathVariable long id,@Valid @RequestBody AccessManagementService.Password body){return service.reset(id,body);}
  @PostMapping("/users/{id}/revoke") public Object revoke(@PathVariable long id){return service.revoke(id);}
}
