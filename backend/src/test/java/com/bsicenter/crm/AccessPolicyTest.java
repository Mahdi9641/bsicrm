package com.bsicenter.crm;

import com.bsicenter.crm.security.*;
import com.bsicenter.crm.repository.Db;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AccessPolicyTest {
  @Test void everyApiControllerMethodHasAnExplicitBackendPolicy() throws Exception {
    var scanner=new ClassPathScanningCandidateComponentProvider(false);
    scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
    int checked=0;
    for(var candidate:scanner.findCandidateComponents("com.bsicenter.crm.controller")){
      var type=Class.forName(candidate.getBeanClassName());
      var root=AnnotatedElementUtils.findMergedAnnotation(type,RequestMapping.class);
      if(root==null)continue;
      String prefix=root.value().length==0?"":root.value()[0];
      if(!prefix.startsWith("/api"))continue;
      for(var method:type.getDeclaredMethods()){
        var route=AnnotatedElementUtils.findMergedAnnotation(method,RequestMapping.class);if(route==null)continue;
        String path=prefix+(route.value().length==0?"":route.value()[0]);
        assertTrue(route.method().length>0,"An explicit HTTP method is required: "+path);
        for(var verb:route.method())assertNotNull(EndpointPermissions.required(verb.name(),path),verb+" "+path);
        checked++;
      }
    }
    assertTrue(checked>100);
  }
  @Test void unknownPathsAndMethodsAreDeniedAndPublicEndpointsAreExact(){
    assertNull(EndpointPermissions.required("GET","/api/new-sensitive-endpoint"));
    assertNull(EndpointPermissions.required("DELETE","/api/auth/salons"));
    assertEquals("security.read",EndpointPermissions.required("GET","/api/auth/sessions"));
    assertEquals("users.write",EndpointPermissions.required("PUT","/api/access/users/25"));
    assertEquals("visits.execute",EndpointPermissions.required("POST","/api/visit-services/5/status"));
    assertEquals("PUBLIC",EndpointPermissions.required("POST","/api/auth/login"));
  }
  @Test void customerAndStylistCeilingsCannotBecomeTenantAdministrators(){
    for(String role:List.of("CUSTOMER","STYLIST")){
      var permissions=PermissionCatalog.ceiling(role);
      assertFalse(permissions.contains("users.read"));assertFalse(permissions.contains("roles.write"));
      assertFalse(permissions.contains("finance.read"));assertFalse(permissions.contains("visits.write"));
    }
    assertTrue(PermissionCatalog.ceiling("STYLIST").contains("visits.execute"));
    assertTrue(PermissionCatalog.ceiling("CUSTOMER").contains("portal.appointments.write"));
  }
  @Test void deniedReadRemovesItsWritesAndCannotRemoveAccountSecurity(){
    var permissions=PermissionCatalog.normalize(Set.of("customers.write","visits.execute"));
    assertFalse(permissions.contains("customers.write"));assertFalse(permissions.contains("visits.execute"));
    assertTrue(permissions.containsAll(Set.of("security.read","security.write")));
  }
  @Test void managerCeilingNeverContainsPlatformAdministration(){
    assertFalse(PermissionCatalog.ceiling("MANAGER").contains("salons.write"));
    assertFalse(PermissionCatalog.ceiling("MANAGER").contains("settings.write"));
    assertFalse(PermissionCatalog.ceiling("RECEPTION").contains("roles.write"));
  }
  @Test void userDenyOverridesRoleAndPermissionsAreReloadedOnEachResolution(){
    Db db=mock(Db.class);var service=new PermissionService(db);
    when(db.list(contains("select r.*"),any())).thenReturn(List.of(Map.of("id",2L,"name","نقش آزمایشی","version",0L)));
    when(db.list(contains("access_role_permissions"),any())).thenReturn(List.of(Map.of("permission","customers.read"),Map.of("permission","customers.write")));
    when(db.list(contains("user_permission_overrides"),any())).thenReturn(List.of(Map.of("permission","customers.read","allowed",false)));
    var user=Map.<String,Object>of("id",9L,"role","RECEPTION");
    assertFalse(service.resolve(user).permissions().contains("customers.write"));
    when(db.list(contains("user_permission_overrides"),any())).thenReturn(List.of());
    assertTrue(service.resolve(user).permissions().contains("customers.write"));
    assertTrue(service.resolve(user).menus().contains("customers"));
  }
}
