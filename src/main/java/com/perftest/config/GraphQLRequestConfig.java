package com.perftest.config;

import java.util.Map;

/**
 * Configuration class for GraphQL requests.
 * <p>
 * This class extends the base RequestConfig to provide additional properties specific to GraphQL requests,
 * such as the operation name, query document, and variables. It supports both standard HTTP features
 * inherited from RequestConfig (headers, endpoint, etc.) and GraphQL-specific configurations.
 * <p>
 * Example YAML configuration:
 * <pre>
 * graphQLRequests:
 *   - name: Query Repository Info
 *     endpoint: ${baseUrl}
 *     statusCode: 200
 *     headers: headers/github_graphql_headers.json
 *     query: |
 *       query GetRepoInfo($owner: String!, $name: String!) {
 *         repository(owner: $owner, name: $name) {
 *           name
 *           description
 *         }
 *       }
 *     operationName: GetRepoInfo
 *     graphQLVariables:
 *       owner: ${repo_owner}
 *       name: ${repo_name}
 * </pre>
 */
public class GraphQLRequestConfig extends RequestConfig {
    private String operationName;
    private String query;
    private String queryFile;
    private Map<String, Object> variables;
    private boolean ignoreVariables;
    
    /**
     * Gets the GraphQL operation name.
     * 
     * @return The operation name
     */
    public String getOperationName() {
        return operationName;
    }
    
    /**
     * Sets the GraphQL operation name.
     * 
     * @param operationName The operation name to set
     */
    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }
    
    /**
     * Gets the GraphQL query.
     * 
     * @return The query
     */
    public String getQuery() {
        return query;
    }
    
    /**
     * Sets the GraphQL query. This can be inline or a reference to a file.
     * 
     * @param query The query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }
    
    /**
     * Gets the GraphQL variables map.
     * 
     * @return The variables map
     */
    public Map<String, Object> getGraphQLVariables() {
        return variables;
    }
    
    /**
     * Sets the GraphQL variables map.
     * 
     * @param variables The variables map to set
     */
    public void setGraphQLVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    /**
     * Gets whether to ignore variables in the request.
     * 
     * @return true if variables should be ignored, false otherwise
     */
    public boolean isIgnoreVariables() {
        return ignoreVariables;
    }
    
    /**
     * Sets whether to ignore variables in the request.
     * 
     * @param ignoreVariables true if variables should be ignored, false otherwise
     */
    public void setIgnoreVariables(boolean ignoreVariables) {
        this.ignoreVariables = ignoreVariables;
    }
    
    /**
     * Gets the path to the GraphQL query file.
     * 
     * @return The query file path
     */
    public String getQueryFile() {
        return queryFile;
    }
    
    /**
     * Sets the path to the GraphQL query file.
     * This is an alternative to using the inline query.
     * 
     * @param queryFile The query file path to set
     */
    public void setQueryFile(String queryFile) {
        this.queryFile = queryFile;
    }
}
