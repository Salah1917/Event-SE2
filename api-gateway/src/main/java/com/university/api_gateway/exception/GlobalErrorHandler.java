package com.university.api_gateway.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Configuration
public class GlobalErrorHandler {

    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                           ErrorAttributeOptions options) {
                Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);
                errorAttributes.remove("trace");
                errorAttributes.remove("path");
                errorAttributes.remove("error");
                return errorAttributes;
            }
        };
    }

    @Bean
    @Order(-2)
    public RouterFunction<ServerResponse> errorRouterFunction(
            ServerCodecConfigurer serverCodecConfigurer) {
        return RouterFunctions.route(
                RequestPredicates.all(), this::handleError
        );
    }

    private Mono<ServerResponse> handleError(ServerRequest request) {
        Throwable error = getError(request);
        HttpStatus status = determineHttpStatus(error);

        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "status", status.value(),
                        "error", status.getReasonPhrase(),
                        "message", error != null ? error.getMessage() : "Unknown error"
                ));
    }

    private Throwable getError(ServerRequest request) {
        return (Throwable) request.attribute("error").orElse(null);
    }

    private HttpStatus determineHttpStatus(Throwable error) {
        if (error == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (error instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (error instanceof SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
