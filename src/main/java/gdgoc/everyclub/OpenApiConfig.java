package gdgoc.everyclub;

import gdgoc.everyclub.docs.ApiErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
class OpenApiConfig {
    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String BAD_REQUEST_RESPONSE = "BadRequestResponse";
    private static final String UNAUTHORIZED_RESPONSE = "UnauthorizedResponse";
    private static final String FORBIDDEN_RESPONSE = "ForbiddenResponse";
    private static final String INTERNAL_SERVER_ERROR_RESPONSE = "InternalServerErrorResponse";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .info(new Info()
                        .title("EveryClub API"));
    }

    @Bean
    public OpenApiCustomizer commonResponseOpenApiCustomizer() {
        return openApi -> {
            Components components = openApi.getComponents();
            if (components == null) {
                components = new Components();
                openApi.setComponents(components);
            }

            ModelConverters.getInstance()
                    .read(ApiErrorResponse.class)
                    .forEach(components::addSchemas);

            components.addResponses(BAD_REQUEST_RESPONSE, errorResponse(
                    "잘못된 요청",
                    "validationFailure",
                    errorExample("ERROR", "태그는 최소 1개 이상 필요합니다.")
            ));
            components.addResponses(UNAUTHORIZED_RESPONSE, errorResponse(
                    "인증 필요",
                    "authenticationRequired",
                    errorExample("ERROR", "인증이 필요합니다.")
            ));
            components.addResponses(FORBIDDEN_RESPONSE, errorResponse(
                    "접근 권한 없음",
                    "accessDenied",
                    errorExample("ERROR", "접근 권한이 없습니다.")
            ));
            components.addResponses(INTERNAL_SERVER_ERROR_RESPONSE, errorResponse(
                    "서버 내부 오류",
                    "internalServerError",
                    errorExample("ERROR", "서버 내부 오류가 발생했습니다.")
            ));
        };
    }

    @Bean
    public OperationCustomizer commonResponseOperationCustomizer() {
        return (operation, handlerMethod) -> {
            ApiResponses responses = operation.getResponses();
            if (responses == null) {
                responses = new ApiResponses();
                operation.setResponses(responses);
            }

            addResponseIfAbsent(responses, "400", BAD_REQUEST_RESPONSE);
            addResponseIfAbsent(responses, "401", UNAUTHORIZED_RESPONSE);
            addResponseIfAbsent(responses, "403", FORBIDDEN_RESPONSE);
            addResponseIfAbsent(responses, "500", INTERNAL_SERVER_ERROR_RESPONSE);

            return operation;
        };
    }

    private void addResponseIfAbsent(ApiResponses responses, String statusCode, String componentName) {
        if (responses.containsKey(statusCode)) {
            return;
        }
        responses.addApiResponse(statusCode, new io.swagger.v3.oas.models.responses.ApiResponse()
                .$ref("#/components/responses/" + componentName));
    }

    private io.swagger.v3.oas.models.responses.ApiResponse errorResponse(
            String description,
            String exampleName,
            Example example
    ) {
        MediaType mediaType = new MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/ApiErrorResponse"))
                .addExamples(exampleName, example);

        return new io.swagger.v3.oas.models.responses.ApiResponse()
                .description(description)
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType));
    }

    private Example errorExample(String status, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("status", status);
        payload.put("message", message);
        payload.put("data", null);

        return new Example().value(payload);
    }
}
