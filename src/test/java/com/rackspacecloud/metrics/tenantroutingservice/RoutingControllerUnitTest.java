package com.rackspacecloud.metrics.tenantroutingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rackspacecloud.metrics.tenantroutingservice.controllers.GlobalExceptionHandler;
import com.rackspacecloud.metrics.tenantroutingservice.controllers.RoutingController;
import com.rackspacecloud.metrics.tenantroutingservice.domain.RetentionPolicyEnum;
import com.rackspacecloud.metrics.tenantroutingservice.domain.TenantRoutes;
import com.rackspacecloud.metrics.tenantroutingservice.exceptions.RouteConflictException;
import com.rackspacecloud.metrics.tenantroutingservice.exceptions.RouteDeleteException;
import com.rackspacecloud.metrics.tenantroutingservice.exceptions.RouteNotFoundException;
import com.rackspacecloud.metrics.tenantroutingservice.exceptions.RouteWriteException;
import com.rackspacecloud.metrics.tenantroutingservice.model.IngestionRoutingInformationInput;
import com.rackspacecloud.metrics.tenantroutingservice.services.RoutingServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@WebAppConfiguration
@WebMvcTest(value = RoutingController.class)
public class RoutingControllerUnitTest {
    private MockMvc mockMvc;

    @MockBean
    private RoutingServiceImpl routingServiceImpl;

    @InjectMocks
    RoutingController controller;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    public void test_setTenantRoutingInformation_validInput_returnsIngestionRoutingInformationOutput(){
        TenantRoutes tenantRoutingInformation = getTenantRoutes();

        when(routingServiceImpl.setIngestionRoutingInformation(
                anyString(), any(IngestionRoutingInformationInput.class))
        ).thenReturn(tenantRoutingInformation);

        TenantRoutes out = controller.setTenantRoutingInformation("test",
                new IngestionRoutingInformationInput());

        Assert.assertEquals("http://test-path:8086", out.getRoutes().get("full").getPath());
    }

    private TenantRoutes getTenantRoutes() {
        IngestionRoutingInformationInput input = new IngestionRoutingInformationInput();
        input.setDatabaseName("test_tenantId");
        input.setPath("http://test-path:8086");
        List<RetentionPolicyEnum> list = new ArrayList<>();
        list.add(RetentionPolicyEnum.FULL);
        list.add(RetentionPolicyEnum.FIVE_MINUTES);
        list.add(RetentionPolicyEnum.TWENTY_MINUTES);
        list.add(RetentionPolicyEnum.ONE_HOUR);
        list.add(RetentionPolicyEnum.FOUR_HOURS);
        list.add(RetentionPolicyEnum.ONE_DAY);

        return new TenantRoutes("test_tenantId", input, list);
    }

    @Test
    public void test_getTenantRoutingInformation_validInput_returnsTenantRoutes(){
        TenantRoutes output = getTenantRoutes();

        when(routingServiceImpl.getIngestionRoutingInformation(anyString())).thenReturn(output);

        TenantRoutes out = controller.getTenantRoutingInformation(anyString());

        Assert.assertEquals("http://test-path:8086", out.getRoutes().get("full").getPath());
    }

    @Test
    public void test_deleteTenantRoutingInformation_validInput_DeletesRoutingInformation(){
        doNothing().when(routingServiceImpl).removeIngestionRoutingInformation(anyString());

        TenantRoutes output = null;

        controller.removeTenantRoutingInformation(anyString());

        Assert.assertNull(output);
    }

    @Test(expected = RouteNotFoundException.class)
    public void test_getTenantRoutingInformation_nonExistingTenant_throwsRouteNotFoundException(){
        doThrow(RouteNotFoundException.class).when(routingServiceImpl).getIngestionRoutingInformation(anyString());
        TenantRoutes out = controller.getTenantRoutingInformation(anyString());
    }

    @Test
    public void test_GlobalExceptionHandler_getMethod_nonExistingTenant_throwsRouteNotFoundException() throws Exception {
        doThrow(RouteNotFoundException.class).when(routingServiceImpl).getIngestionRoutingInformation(anyString());

        this.mockMvc.perform(get("/dummy").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().string("{\"message\":null,\"rootCause\":null}"));
    }

    @Test
    public void test_GlobalExceptionHandler_postMethod_existingTenant_throwsRouteConflictException() throws Exception {
        doThrow(RouteConflictException.class).when(routingServiceImpl)
                .setIngestionRoutingInformation(anyString(), any());

        IngestionRoutingInformationInput input = new IngestionRoutingInformationInput();
        input.setDatabaseName("test_database");
        input.setPath("http://test-path:8086");

        ObjectMapper mapper = new ObjectMapper();

        this.mockMvc.perform(post("/dummy")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isConflict())
                .andExpect(content().string("{\"message\":null,\"rootCause\":null}"));
    }

    @Test
    public void test_GlobalExceptionHandler_postMethod_newTenant_throwsRouteWriteException() throws Exception {
        doThrow(RouteWriteException.class).when(routingServiceImpl)
                .setIngestionRoutingInformation(anyString(), any());

        IngestionRoutingInformationInput input = new IngestionRoutingInformationInput();
        input.setDatabaseName("test_database");
        input.setPath("http://test-path:8086");

        ObjectMapper mapper = new ObjectMapper();

        this.mockMvc.perform(post("/dummy")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"message\":null,\"rootCause\":null}"));
    }

    @Test
    public void test_GlobalExceptionHandler_deleteMethod_nonExistingTenant_throwsRouteDeleteException() throws Exception {
        doThrow(RouteDeleteException.class).when(routingServiceImpl).removeIngestionRoutingInformation(anyString());

        this.mockMvc.perform(delete("/dummy").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"message\":null,\"rootCause\":null}"));
    }

    @Test
    public void test_GlobalExceptionHandler_getMethod_invalidArgument_throwsMethodArgumentNotValidException() throws Exception {
        IngestionRoutingInformationInput input = new IngestionRoutingInformationInput();
        input.setDatabaseName("test_database");
        input.setPath("invalidPath");

        ObjectMapper mapper = new ObjectMapper();

        this.mockMvc.perform(post("/dummy")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void test_GlobalExceptionHandler_deleteMethod_existingTenant_throwsException() throws Exception {
        doThrow(RuntimeException.class).when(routingServiceImpl).removeIngestionRoutingInformation(anyString());

        this.mockMvc.perform(delete("/dummy").accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("{\"message\":null,\"rootCause\":null}"));
    }
}
