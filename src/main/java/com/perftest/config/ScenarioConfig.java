package com.perftest.config;

import java.util.List;
import java.util.Map;

/**
 * Configuration class for test scenarios.
 */
public class ScenarioConfig {
    private String name;
    private String description;
    private Map<String, Object> variables;
    private List<RequestConfig> requests;
    private List<GraphQLRequestConfig> graphQLRequests;
    private List<SoapRequestConfig> soapRequests;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public List<RequestConfig> getRequests() {
        return requests;
    }

    public void setRequests(List<RequestConfig> requests) {
        this.requests = requests;
    }
    
    public List<GraphQLRequestConfig> getGraphQLRequests() {
        return graphQLRequests;
    }

    public void setGraphQLRequests(List<GraphQLRequestConfig> graphQLRequests) {
        this.graphQLRequests = graphQLRequests;
    }
    
    public List<SoapRequestConfig> getSoapRequests() {
        return soapRequests;
    }

    public void setSoapRequests(List<SoapRequestConfig> soapRequests) {
        this.soapRequests = soapRequests;
    }

    @Override
    public String toString() {
        return "ScenarioConfig{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", variables=" + variables +
                ", requests=" + requests +
                ", graphQLRequests=" + graphQLRequests +
                ", soapRequests=" + soapRequests +
                '}';
    }
}
