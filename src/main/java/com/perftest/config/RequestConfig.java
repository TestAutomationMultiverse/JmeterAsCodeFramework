package com.perftest.config;

import java.util.Map;

/**
 * Configuration class for HTTP requests within a scenario.
 */
public class RequestConfig {
    private String name;
    private String protocol;
    private String endpoint;
    private String method;
    private String body;
    private String headers;
    private String params;
    private Integer statusCode;
    private Integer responseTimeThreshold;
    private Map<String, Object> variables;
    private Map<String, Object> responses;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }
    
    public Integer getResponseTimeThreshold() {
        return responseTimeThreshold;
    }

    public void setResponseTimeThreshold(Integer responseTimeThreshold) {
        this.responseTimeThreshold = responseTimeThreshold;
    }
    
    public Map<String, Object> getResponses() {
        return responses;
    }

    public void setResponses(Map<String, Object> responses) {
        this.responses = responses;
    }

    @Override
    public String toString() {
        return "RequestConfig{" +
                "name='" + name + '\'' +
                ", protocol='" + protocol + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", body='" + body + '\'' +
                ", headers='" + headers + '\'' +
                ", params='" + params + '\'' +
                ", statusCode=" + statusCode +
                ", responseTimeThreshold=" + responseTimeThreshold +
                ", variables=" + variables +
                ", responses=" + responses +
                '}';
    }
}
