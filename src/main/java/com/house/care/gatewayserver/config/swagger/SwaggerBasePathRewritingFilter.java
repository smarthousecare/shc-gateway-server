package com.house.care.gatewayserver.config.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.context.RequestContext;

/**
 * Zuul filter to rewrite micro-services Swagger URL Base Path.
 */
public class SwaggerBasePathRewritingFilter extends SendResponseFilter {

    public static final String DEFAULT_URL = "/v3/api-docs";

    private final Logger log = LoggerFactory.getLogger(SwaggerBasePathRewritingFilter.class);

    private final ObjectMapper mapper = new ObjectMapper();

    public SwaggerBasePathRewritingFilter(ZuulProperties zuulProperties) {

        super(zuulProperties);
    }

    @Override
    public String filterType() {

        return "post";
    }

    @Override
    public int filterOrder() {

        return 100;
    }

    /**
     * Filter requests to micro-services Swagger docs.
     */
    @Override
    public boolean shouldFilter() {

        return RequestContext.getCurrentContext().getRequest().getRequestURI().endsWith(DEFAULT_URL);
    }

    @Override
    public Object run() {

        RequestContext context = RequestContext.getCurrentContext();

        if (!context.getResponseGZipped()) {
            context.getResponse().setCharacterEncoding("UTF-8");
        }

        String rewrittenResponse = rewriteBasePath(context);
        context.setResponseBody(rewrittenResponse);
        return null;
    }

    @SuppressWarnings("unchecked")
    private String rewriteBasePath(RequestContext context) {

        InputStream responseDataStream = context.getResponseDataStream();
        String requestUri = RequestContext.getCurrentContext().getRequest().getRequestURI();
        try {
            if (context.getResponseGZipped()) {
                responseDataStream = new GZIPInputStream(context.getResponseDataStream());
            }
            String response = IOUtils.toString(responseDataStream, StandardCharsets.UTF_8);
            if (response != null) {
                LinkedHashMap<String, Object> map = this.mapper.readValue(response, LinkedHashMap.class);

                String basePath = requestUri.replace(DEFAULT_URL, "");
                ArrayList<LinkedHashMap<String, String>> servers = new ArrayList<>();
                LinkedHashMap<String, String> url = new LinkedHashMap<>();
                String protocol = RequestContext.getCurrentContext().getRequest().getProtocol();
                protocol = protocol.toLowerCase().contains("https") ? "https://" : "http://";

                url.put("url", protocol.concat(RequestContext.getCurrentContext().getRequest().getHeader("host").concat(basePath)));
                servers.add(url);
                LinkedHashMap<String, String> desription = new LinkedHashMap<>();
                desription.put("desciption", "Generated Server");
                servers.add(desription);
                map.put("servers", servers);

                log.debug("Swagger-docs: rewritten Base URL with correct micro-service route: {}", basePath);
                return mapper.writeValueAsString(map);
            }
        }
        catch (IOException e) {
            log.error("Swagger-docs filter error", e);
        }
        return null;
    }
}
