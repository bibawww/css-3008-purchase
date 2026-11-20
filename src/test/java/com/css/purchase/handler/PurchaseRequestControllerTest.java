package com.css.purchase.handler;

import com.css.purchase.config.RequestService;
import com.css.purchase.domain.DuplicateRequest;
import com.css.purchase.domain.PurchaseRequest;
import com.css.purchase.domain.RequestNotFound;
import com.css.purchase.domain.RequestStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseRequestController.class)
@ContextConfiguration(classes = {PurchaseRequestController.class, ApiExceptionHandler.class})
class PurchaseRequestControllerTest {

    private static final String BASE = "/api/v1/purchase-requests";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    RequestService service;

    private static String body(String businessKey, String title) {
        return "{\"businessKey\":\"" + businessKey + "\",\"title\":\"" + title + "\"}";
    }

    private static PurchaseRequest row(UUID id, String key, String title) {
        return new PurchaseRequest(id, key, RequestStatus.DRAFT, title);
    }

    @Test
    void validBodyReturns201WithLocation() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create("PR-1", "Laptops")).thenReturn(row(id, "PR-1", "Laptops"));

        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("PR-1", "Laptops")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.businessKey").value("PR-1"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.title").value("Laptops"));
    }

    @Test
    void title120CharsReturns201() throws Exception {
        String title = "a".repeat(120);
        when(service.create("PR-1", title)).thenReturn(row(UUID.randomUUID(), "PR-1", title));

        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("PR-1", title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(title));
    }

    @Test
    void blankTitleReturns400AndServiceIsNotCalled() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("PR-1", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value(containsString("title")));

        verifyNoInteractions(service);
    }

    @Test
    void spacesOnlyTitleReturns400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("PR-1", "   ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("title must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void blankBusinessKeyReturns400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("", "Laptops")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("business key must not be blank"));

        verifyNoInteractions(service);
    }

    @Test
    void title121CharsReturns400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("PR-1", "a".repeat(121))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size must be between 0 and 120"));

        verifyNoInteractions(service);
    }

    @Test
    void bodyThatIsNotJsonReturns400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("body must be JSON"));
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.create(any(), any())).thenThrow(DuplicateRequest.class);

        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body("PR-1", "Laptops")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));
    }

    @Test
    void missingRowReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(RequestNotFound.class);

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void getByUuidReturns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenReturn(row(id, "PR-1", "Laptops"));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void businessKeyInPathReturns400() throws Exception {
        mvc.perform(get(BASE + "/PR-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").value("id must be a UUID"));
    }

    @Test
    void correlationIdIsEchoedOn400() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "abc-123")
                        .content(body("PR-1", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").value("abc-123"));
    }

    @Test
    void missingCorrelationIdGetsNewOne() throws Exception {
        mvc.perform(get(BASE + "/PR-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.correlationId").isNotEmpty());
    }
}
