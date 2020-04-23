package com.house.care.gatewayserver.config.swagger;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration
@EnableSwagger2WebMvc
@Import(SpringDataRestConfiguration.class)
public class SwaggerConfig {

    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";

    @Bean
    public Docket api() {

        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo())
                .useDefaultResponseMessages(false);
        // .securitySchemes(Lists.newArrayList(apiKey()))
        // .securityContexts(Lists.newArrayList(securityContext()));
        // .globalResponseMessage(RequestMethod.GET, Lists.newArrayList(new
        // ResponseMessageBuilder().code(500)
        // .message("500 message")
        // .responseModel(new ModelRef("Error"))
        // .build(),
        // new ResponseMessageBuilder().code(403)
        // .message("Forbidden!!!!!")
        // .build()));
    }

    private ApiInfo apiInfo() {

        ApiInfo apiInfo = new ApiInfo("My REST API", "Some custom description of API.", "API TOS", "Terms of service",
                new Contact("John Doe", "www.example.com", "myeaddress@company.com"), "License of API", "API license URL", Collections.emptyList());
        return apiInfo;
    }

    // private ApiKey apiKey() {
    //
    // return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
    // }
    //
    // private SecurityContext securityContext() {
    //
    // return SecurityContext.builder()
    // .securityReferences(defaultAuth())
    // .forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
    // .build();
    // }

    // private List<SecurityReference> defaultAuth() {
    //
    // AuthorizationScope authorizationScope = new AuthorizationScope("global",
    // "accessEverything");
    // AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    // authorizationScopes[0] = authorizationScope;
    // return Lists.newArrayList(
    // new SecurityReference("JWT", authorizationScopes));
    // }

    // private List<AuthorizationType> getAuthorizationTypes() {
    //
    // List<AuthorizationType> authorizationTypes = new ArrayList<>();
    // List<AuthorizationScope> scopes = new ArrayList<>();
    // scopes.add(new AuthorizationScope("my-resource.read", "Read access on the
    // API"));
    //
    // List<GrantType> grantTypes = new ArrayList<>();
    // ImplicitGrant implicitGrant = new ImplicitGrant(new
    // LoginEndpoint(swaggerOAuthUrl), "access_code");
    // grantTypes.add(implicitGrant);
    //
    // AuthorizationType oauth = new OAuthBuilder()
    // .scopes(scopes)
    // .grantTypes(grantTypes)
    // .build();
    // authorizationTypes.add(oauth);
    // return authorizationTypes;
    // }
}