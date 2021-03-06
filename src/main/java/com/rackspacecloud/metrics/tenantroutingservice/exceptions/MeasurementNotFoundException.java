package com.rackspacecloud.metrics.tenantroutingservice.exceptions;

/**
 * This exception is thrown when route information failed to delete for given tenantId.
 */
public class MeasurementNotFoundException extends RuntimeException {
    public MeasurementNotFoundException(String tenantId, Throwable e) {
        super("Couldn't delete route information for tenantId: [" + tenantId + "]", e);
    }
}
