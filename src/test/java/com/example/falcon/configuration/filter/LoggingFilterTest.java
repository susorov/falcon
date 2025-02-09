package com.example.falcon.configuration.filter;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest
class LoggingFilterTest {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldPropagateCorrelationIdHeaderFromRequestToResponse() throws Exception {
        var CORRELATION_ID = "c0c9741b-2988-4711-aceb-12560b88567d";

        mockMvc.perform(post("/").header(CORRELATION_ID_HEADER, CORRELATION_ID))
                .andExpect(header().string(CORRELATION_ID_HEADER, CORRELATION_ID));
    }

    @Test
    void shouldThrowExceptionForIncorrectCorrelationId() throws Exception {
        var CORRELATION_ID = "c0c9741b";

        assertThatIllegalArgumentException().isThrownBy(() -> {
            mockMvc.perform(post("/").header(CORRELATION_ID_HEADER, CORRELATION_ID))
                    .andExpect(header().string(CORRELATION_ID_HEADER, CORRELATION_ID));
        });
    }

    @Test
    void shouldLogCorrelationId() throws Exception {
        var CORRELATION_ID = "c0c9741b-2988-4711-aceb-12560b88567d";

        try (MockedStatic<MDC> mockedMDC = Mockito.mockStatic(MDC.class)) {
            mockMvc.perform(post("/").header(CORRELATION_ID_HEADER, CORRELATION_ID))
                    .andExpect(header().string(CORRELATION_ID_HEADER, CORRELATION_ID));

            mockedMDC.verify(() -> MDC.put(CORRELATION_ID_HEADER, CORRELATION_ID));
            mockedMDC.verify(() -> MDC.remove(CORRELATION_ID_HEADER));
        }
    }
}
