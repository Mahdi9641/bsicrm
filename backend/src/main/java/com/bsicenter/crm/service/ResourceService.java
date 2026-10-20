package com.bsicenter.crm.service;

import com.bsicenter.crm.dto.ResourceRequests.*;
import com.bsicenter.crm.entity.SalonResourcesEntity;
import com.bsicenter.crm.exception.ApiError;
import com.bsicenter.crm.repository.ResourceRepository;
import com.bsicenter.crm.security.Access;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResourceService {
  private final ResourceRepository repository;
  private final Access access;

  public ResourceService(ResourceRepository repository, Access access) {
    this.repository = repository;
    this.access = access;
  }

  @Transactional(readOnly = true)
  public List<View> list() {
    access.roles("ADMIN", "MANAGER", "RECEPTION", "STYLIST");
    return repository.findByBranchIdOrderByName(access.branch()).stream().map(this::view).toList();
  }

  private View view(SalonResourcesEntity entity) {
    return new View(entity.getId(), entity.getName(), entity.getKind(), entity.getActive());
  }

  @Transactional
  public View save(Long id, Write request) {
    access.roles("ADMIN", "MANAGER");
    var entity =
        id == null
            ? new SalonResourcesEntity()
            : repository.findByIdAndBranchId(id, access.branch()).orElseThrow(ApiError::missing);
    entity.setBranchId(access.branch());
    entity.setName(request.name().trim());
    entity.setKind(request.kind());
    entity.setActive(request.active());
    repository.saveAndFlush(entity);
    access.audit(id == null ? "CREATE" : "UPDATE", "RESOURCE", entity.getId(), null);
    return view(entity);
  }
}
