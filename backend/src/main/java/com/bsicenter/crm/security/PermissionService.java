package com.bsicenter.crm.security;

import com.bsicenter.crm.repository.Db;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
  private final Db db;
  public PermissionService(Db db){this.db=db;}
  public record Snapshot(Set<String> permissions, List<String> menus, String roleName, long roleId, long version) {}
  public Snapshot resolve(Map<String,Object> user){
    long id=Db.number(user,"id");
    String base=Objects.toString(user.get("role"),"");
    var rows=db.list("select r.* from access_roles r join app_users u on u.access_role_id=r.id where u.id=? and r.active and r.base_role=u.role and (r.salon_id is null or r.salon_id=u.salon_id)",id);
    if(rows.isEmpty())return new Snapshot(Set.of("security.read","security.write"),List.of("security"),"نقش غیرفعال",0,0);
    var role=rows.get(0);long roleId=Db.number(role,"id");
    Set<String> permissions=new HashSet<>();
    for(var row:db.list("select permission from access_role_permissions where role_id=?",roleId)) permissions.add(row.get("permission").toString());
    for(var row:db.list("select permission,allowed from user_permission_overrides where user_id=?",id)){
      String code=row.get("permission").toString();
      if(Boolean.TRUE.equals(row.get("allowed")))permissions.add(code);else permissions.remove(code);
    }
    permissions.retainAll(PermissionCatalog.ceiling(base));
    if(base.equals("ADMIN"))permissions=new HashSet<>(PermissionCatalog.defaults("ADMIN"));
    var effective=PermissionCatalog.normalize(permissions);
    return new Snapshot(effective,PermissionCatalog.menus(effective),role.get("name").toString(),roleId,Db.number(role,"version"));
  }
}
