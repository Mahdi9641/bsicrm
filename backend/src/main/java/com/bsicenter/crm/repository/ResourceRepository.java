package com.bsicenter.crm.repository;

import com.bsicenter.crm.entity.SalonResourcesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<SalonResourcesEntity, Long> {
  List<SalonResourcesEntity> findByBranchIdOrderByName(long branchId);

  Optional<SalonResourcesEntity> findByIdAndBranchId(long id, long branchId);
}
