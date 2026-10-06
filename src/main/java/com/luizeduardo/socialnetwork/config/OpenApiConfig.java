package com.luizeduardo.socialnetwork.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacao OpenAPI exposta em /swagger-ui.html (interface) e /v3/api-docs (JSON).
 * Todas as rotas exigem JWT por padrao; as de autenticacao removem o requisito com @SecurityRequirements.
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    // O chat em tempo real usa STOMP, que o OpenAPI nao descreve; fica documentado aqui
    private static final String DESCRIPTION = """
            API da rede social: autenticacao, perfis, seguidores, posts com midia, feed e chat.

            **Autenticacao:** faca login em `POST /api/auth/login`, copie o `token` e clique em **Authorize**.

            **Chat em tempo real (WebSocket/STOMP):**
            - Conecte em `ws://<host>/ws` enviando `Authorization: Bearer <token>` no frame `CONNECT`
            - Assine `/user/queue/messages` (novas mensagens), `/user/queue/read` (confirmacoes de leitura) e `/user/queue/errors`
            - Envie `{"content": "..."}` para `/app/chats/{conversationId}/send`
            - Marque como lida enviando para `/app/chats/{conversationId}/read`
            """;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rede Social API")
                        .version("v1")
                        .description(DESCRIPTION))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
