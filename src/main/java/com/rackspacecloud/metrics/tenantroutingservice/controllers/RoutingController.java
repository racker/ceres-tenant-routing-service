package com.rackspacecloud.metrics.tenantroutingservice.controllers;

import com.rackspacecloud.metrics.tenantroutingservice.domain.TenantRoutes;
import com.rackspacecloud.metrics.tenantroutingservice.services.RoutingService;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Collection;

@RestController
@RequestMapping("")
@Slf4j
public class RoutingController {

    @Autowired
    private RoutingService routingService;

    /**
     * Will return the routes for the given tenantId and measurement
     * @param tenantId
     * @param measurement
     * @return
     */
    @RequestMapping(
            value = "/{tenantId}/{measurement}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Timed(value = "tenant.routing", extraTags = {"operation","get"})
    public TenantRoutes getTenantRoutingInformation(
            @NotNull @PathVariable final String tenantId,
            @NotNull @PathVariable final String measurement,
            @RequestParam(value = "readOnly", required = false, defaultValue = "false") boolean readOnly) throws Exception {
        log.debug("getTenantRoutingInformation: get routing request received for tenantId [{}]" +
                " and measurement [{}]", tenantId, measurement);
        return routingService.getIngestionRoutingInformation(tenantId, measurement, readOnly);
    }

    /**
     * Get all measurements for the given tenantId
     * @param tenantId
     */
    @RequestMapping(
            value = "/{tenantId}/measurements",
            method = RequestMethod.GET
    )
    @Timed(value = "tenant.routing", extraTags = {"operation","get"})
    public Collection<String> getMeasurements(@NotNull @PathVariable final String tenantId){
        log.info("getMeasurements: get measurements request received for tenantId [{}]", tenantId);
        return routingService.getMeasurements(tenantId);
    }
}
