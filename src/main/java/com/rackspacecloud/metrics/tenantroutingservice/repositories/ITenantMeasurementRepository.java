package com.rackspacecloud.metrics.tenantroutingservice.repositories;

import com.rackspacecloud.metrics.tenantroutingservice.domain.TenantMeasurements;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITenantMeasurementRepository extends CrudRepository<TenantMeasurements, String> {
}
