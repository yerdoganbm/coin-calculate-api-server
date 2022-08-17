package tr.com.sgveteris.coin.config;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.*;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.zalando.logbook.*;
import org.zalando.logbook.httpclient.LogbookHttpRequestInterceptor;
import org.zalando.logbook.httpclient.LogbookHttpResponseInterceptor;
import org.zalando.logbook.servlet.LogbookFilter;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.Filter;
import java.util.*;

import static javax.servlet.DispatcherType.*;
import static org.zalando.logbook.Conditions.exclude;
import static org.zalando.logbook.Conditions.requestTo;

@Configuration
@EnableSwagger2
@EnableCircuitBreaker
@EnableCaching
@EnableTransactionManagement
@RequiredArgsConstructor
@EnableAsync
public class CoinCalculatorApiConfiguration {

    private final CoinCalculatorApiServerConfigurationProperties configurationProperties;

    @Bean
    public LocaleResolver localeResolver() {
        var resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(new Locale("tr", "TR"));
        resolver.setSupportedLocales(List.of(new Locale("tr", "TR"), Locale.US));
        return resolver;
    }

    @Bean
    @ConditionalOnProperty(prefix = "api.coin.oauth", name = "enabled", havingValue = "false")
    public RestTemplate restTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                .requestFactory(() -> {
                    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
                    factory.setHttpClient(HttpClientBuilder.create()
                            .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook()))
                            .addInterceptorFirst(new LogbookHttpResponseInterceptor()).build());
                    factory.setReadTimeout(120000);
                    factory.setConnectTimeout(120000);

                    return factory;
                });

        return restTemplateBuilder.build();
    }

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .formatter(new JsonHttpLogFormatter())
                .headerFilter(HeaderFilters.defaultValue())
                .writer(new DefaultHttpLogWriter(LoggerFactory.getLogger("logbook"), DefaultHttpLogWriter.Level.INFO))
                .condition(exclude(requestTo("/swagger**"), requestTo("/v3/api-docs"),
                        requestTo("/jobs**"),
                        requestTo("/webjars**"), requestTo("/csrf")))
                .rawRequestFilter(RawRequestFilters.defaultValue())
                .rawResponseFilter(RawResponseFilters.defaultValue())
                .requestFilter(RequestFilter.none())
                .responseFilter(ResponseFilter.none())
                .queryFilter(QueryFilters.defaultValue())
                .build();
    }

    @Bean
    public FilterRegistrationBean logbookFilter() {
        final Filter filter = new LogbookFilter(logbook());
        @SuppressWarnings("unchecked") // as of Spring Boot 2.x
        final FilterRegistrationBean registration = new FilterRegistrationBean(filter);
        registration.setName("logbookFilter");
        registration.setDispatcherTypes(REQUEST, ASYNC, ERROR);
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }

    @Bean
    public Docket coinApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("tr.com.sgveteris"))
//                .paths(regex("/.*"))
                .build()
                //.securitySchemes(Collections.singletonList(securityScheme()))
                //.securityContexts(Collections.singletonList(securityContext()))
                .apiInfo(new ApiInfo("Coin Calculator API",
                        "Coin Calculator API",
                        "1.0",
                        null,
                        new Contact("SgVeteris", "", ""),
                        null,
                        null,
                        Collections.emptyList()));
    }

    @Bean
    public SecurityConfiguration securityConfiguration() {
        return SecurityConfigurationBuilder.builder()
                .useBasicAuthenticationWithAccessCodeGrant(true)
                .clientSecret("secret")
                .clientId("client")
                .scopeSeparator(" ")
                .appName("coin-api")
                .build();
    }


    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(
            WebEndpointsSupplier webEndpointsSupplier,
            ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
            EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
            WebEndpointProperties webEndpointProperties, Environment environment) {
        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = shouldRegisterLinksMapping(webEndpointProperties, environment, basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
                corsProperties.toCorsConfiguration(), new EndpointLinksResolver(allEndpoints, basePath),
                shouldRegisterLinksMapping);
    }
    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
                                               String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
                || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }

    @Bean(name = "blockChainRestTemplate")
    public RestTemplate blockChainRestTemplate() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                .requestFactory(() -> {
                    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
                    HttpClientBuilder clientBuilder = HttpClientBuilder.create()
                            .addInterceptorFirst(new LogbookHttpRequestInterceptor(logbook()))
                            .disableContentCompression()
                            .disableCookieManagement()
                            .disableAutomaticRetries()
                            .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                            .addInterceptorFirst(new LogbookHttpResponseInterceptor());

                    if (configurationProperties.getHttpProxy().isProxyHttpEnabled() || configurationProperties.getHttpProxy().isProxyHttpsEnabled()) {

                        CredentialsProvider credentialsPovider = new BasicCredentialsProvider();
                        credentialsPovider.setCredentials(new AuthScope(configurationProperties.getHttpProxy().getProxyHost(), configurationProperties.getHttpProxy().getProxyPort()), new
                                UsernamePasswordCredentials(configurationProperties.getHttpProxy().getProxyUsername(), configurationProperties.getHttpProxy().getProxyPasso()));
                        clientBuilder.setDefaultCredentialsProvider(credentialsPovider);

                        clientBuilder.setProxy(new HttpHost(configurationProperties.getHttpProxy().getProxyHost(), configurationProperties.getHttpProxy().getProxyPort()));
                    }


                    factory.setHttpClient(clientBuilder.build());
                    factory.setReadTimeout(120000);
                    factory.setConnectTimeout(120000);

                    return factory;
                });

        return restTemplateBuilder.build();
    }







}
