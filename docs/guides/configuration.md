# Configuration Guide

This comprehensive guide covers all aspects of configuring the Performance Automation Framework using YAML configuration files, including execution parameters, scenarios, requests, and variables.

## Configuration Structure

The framework uses YAML configuration files with a hierarchical structure:

```yaml
# Test identification
name: Sample API Test
description: Tests basic API functionality

# Global variables
variables:
  baseUrl: https://jsonplaceholder.typicode.com
  apiVersion: v1

# Execution parameters
execution:
  threads: 10
  iterations: 5
  rampUpPeriod: 2
  successThreshold: 95.0
  variables:
    timestamp: ${__time()}

# Test scenarios
scenarios:
  - name: User API Tests
    variables:
      endpoint: /users
    requests:
      - name: Get Users
        endpoint: ${baseUrl}${endpoint}
        method: GET
        statusCode: 200
```

## Execution Configuration

The `execution` section defines global parameters for test execution:

```yaml
execution:
  threads: 10                # Number of concurrent threads (users)
  iterations: 5              # Number of iterations per thread
  rampUpPeriod: 30           # Ramp-up period in seconds
  duration: 300              # Test duration in seconds (alternative to iterations)
  successThreshold: 95.0     # Success threshold percentage
  logLevel: INFO             # Log level (INFO, DEBUG, WARN, ERROR)
  outputDirectory: "results" # Output directory for results
  
  variables:                 # Execution-level variables
    baseUrl: "https://api.example.com"
    apiKey: "abc123"
```

### Key Execution Parameters

| Parameter | Type | Description | Default | Required |
|-----------|------|-------------|---------|----------|
| `threads` | Integer | Number of concurrent threads (users) | 1 | No |
| `iterations` | Integer | Number of iterations per thread | 1 | No |
| `rampUpPeriod` | Integer | Ramp-up period in seconds | 0 | No |
| `duration` | Integer | Test duration in seconds | null | No |
| `successThreshold` | Double | Success threshold percentage | 100.0 | No |
| `logLevel` | String | Log level (INFO, DEBUG, WARN, ERROR) | INFO | No |
| `outputDirectory` | String | Output directory for results | "results" | No |
| `variables` | Map | Execution-level variables | {} | No |

**Notes:**
- Either `iterations` or `duration` should be specified, but not both
- If both are specified, `duration` takes precedence

### Advanced Execution Parameters

```yaml
execution:
  threadCount: 10
  iterations: -1             # Run indefinitely (until duration is reached)
  durationSeconds: 300       # Run for 5 minutes (300 seconds)
  rampUpPeriod: 5            # Ramp up over 5 seconds
  holdSeconds: 60            # Maintain full thread count for 60 seconds
  thinkTimeMs: 500           # 500ms delay between requests
```

## Scenario Configuration

The `scenarios` section defines one or more test scenarios:

```yaml
scenarios:
  - name: "User API Test"
    weight: 2                # Relative weight for scenario selection
    iterations: 3            # Override global iterations for this scenario
    
    variables:               # Scenario-specific variables
      userId: 123
    
    requests:
      # Request definitions
```

### Scenario Parameters

| Parameter | Type | Description | Default | Required |
|-----------|------|-------------|---------|----------|
| `name` | String | Name of the scenario | - | Yes |
| `weight` | Integer | Relative weight for scenario selection | 1 | No |
| `iterations` | Integer | Override global iterations for this scenario | null | No |
| `variables` | Map | Scenario-specific variables | {} | No |
| `requests` | List | List of HTTP requests | [] | Yes |
| `soapRequests` | List | List of SOAP requests | [] | No |
| `graphQLRequests` | List | List of GraphQL requests | [] | No |

## Request Configuration

The framework supports three types of requests: HTTP/REST, SOAP, and GraphQL.

### HTTP/REST Requests

```yaml
requests:
  - name: "Get User"
    endpoint: "${baseUrl}/users/${userId}"
    method: GET
    headers: headers/user_headers.json  # Path to headers file
    params: params/user_params.json     # Path to query parameters file
    statusCode: 200
    responseTimeThreshold: 1000
    
    variables:                          # Request-specific variables
      includeDetails: true
    
    responses:                          # Response validation
      JsonPath: "$.name=John Doe"
      Contains: "active"
      MaxSize: 10240
```

#### HTTP Request Parameters

| Parameter | Type | Description | Default | Required |
|-----------|------|-------------|---------|----------|
| `name` | String | Name of the request | - | Yes |
| `endpoint` | String | URL endpoint to call | - | Yes |
| `method` | String | HTTP method (GET, POST, PUT, DELETE, etc.) | GET | No |
| `headers` | String or Map | Path to headers file or inline headers | null | No |
| `params` | String or Map | Path to query parameters file or inline parameters | null | No |
| `body` | String | Path to body file or inline body content | null | No |
| `files` | Map | Files to upload (for multipart requests) | null | No |
| `statusCode` | Integer | Expected status code | null | No |
| `responseTimeThreshold` | Integer | Maximum acceptable response time (ms) | null | No |
| `variables` | Map | Request-specific variables | {} | No |
| `responses` | Map | Response validation rules | {} | No |

### SOAP Requests

```yaml
soapRequests:
  - name: "Get Weather"
    endpoint: "${baseUrl}/WeatherService"
    soapAction: "GetWeather"
    soapEnvelope: soap/get_weather.xml  # Path to SOAP envelope file
    statusCode: 200
    
    xpath:                             # XPath validation
      - expression: "//m:Temperature"
        expected: "72"
        namespaces:
          m: "http://www.example.org/weather"
```

#### SOAP Request Parameters

| Parameter | Type | Description | Default | Required |
|-----------|------|-------------|---------|----------|
| `name` | String | Name of the request | - | Yes |
| `endpoint` | String | SOAP service endpoint URL | - | Yes |
| `soapAction` | String | SOAPAction header value | - | Yes |
| `soapEnvelope` | String | Path to SOAP envelope file or inline XML | - | Yes |
| `headers` | String or Map | Additional HTTP headers | null | No |
| `statusCode` | Integer | Expected status code | null | No |
| `responseTimeThreshold` | Integer | Maximum acceptable response time (ms) | null | No |
| `variables` | Map | Request-specific variables | {} | No |
| `xpath` | List | XPath validation rules | [] | No |

### GraphQL Requests

```yaml
graphQLRequests:
  - name: "Get User"
    endpoint: "${baseUrl}/graphql"
    query: graphql/get_user.graphql  # Path to GraphQL query file
    operationName: "GetUser"
    graphQLVariables:                # GraphQL variables
      id: "${userId}"
    statusCode: 200
    
    responses:                       # Response validation
      JsonPath: "$.data.user.name=John Doe"
```

#### GraphQL Request Parameters

| Parameter | Type | Description | Default | Required |
|-----------|------|-------------|---------|----------|
| `name` | String | Name of the request | - | Yes |
| `endpoint` | String | GraphQL endpoint URL | - | Yes |
| `query` | String | Path to GraphQL query file or inline query | - | Yes |
| `operationName` | String | Name of the operation to execute | null | No |
| `graphQLVariables` | Map | Variables for the GraphQL operation | {} | No |
| `headers` | String or Map | Additional HTTP headers | null | No |
| `statusCode` | Integer | Expected status code | null | No |
| `responseTimeThreshold` | Integer | Maximum acceptable response time (ms) | null | No |
| `variables` | Map | Request-specific variables | {} | No |
| `responses` | Map | Response validation rules | {} | No |

## Variables System

The framework provides a comprehensive variable system that allows dynamic values at different levels:

### Variable Levels

Variables can be defined at four different levels, with each level having a different scope and precedence:

1. **Global Variables** - Top level, available to all scenarios and requests
2. **Execution Variables** - Defined in execution section, available to all scenarios and requests
3. **Scenario Variables** - Defined in a specific scenario, available only to that scenario's requests
4. **Request Variables** - Defined in a specific request, available only to that request

### Variable Precedence

Variables with the same name defined at different levels follow a precedence order:

1. Request Variables (Highest Priority)
2. Scenario Variables
3. Execution Variables 
4. Global Variables (Lowest Priority)

This means that if the same variable is defined at multiple levels, the value from the most specific level will be used.

### Variable Usage

Variables can be used in various parts of the configuration using the `${variable}` syntax:

```yaml
requests:
  - name: Get User
    endpoint: ${baseUrl}/users/${userId}
    method: GET
    headers: |
      {
        "Authorization": "Bearer ${token}"
      }
```

### Built-in Functions

The framework supports several built-in functions for generating dynamic values:

- `${__time()}` - Current time in milliseconds
- `${__timeShift(format,amount,unit)}` - Shifted time value
- `${__random(min,max)}` - Random number between min and max
- `${__randomString(length)}` - Random string of specified length
- `${__randomUUID()}` - Random UUID
- `${__dataFile(file)}` - Read from a data file
- `${__CSV(file,column)}` - Read from a CSV file

## Response Validation

The framework supports multiple validation types:

### Status Code Validation

```yaml
statusCode: 200
```

### Response Time Validation

```yaml
responseTimeThreshold: 1000  # milliseconds
```

### JSON Path Validation

```yaml
responses:
  JsonPath: "$.name=John Doe"
```

Multiple JSON Path validations:

```yaml
responses:
  JsonPath:
    - "$.name=John Doe"
    - "$.id=123"
    - "$.active=true"
```

### Response Contains Validation

```yaml
responses:
  Contains: "success"
```

Multiple content validations:

```yaml
responses:
  Contains:
    - "success"
    - "completed"
```

### Response Size Validation

```yaml
responses:
  MaxSize: 10240  # bytes
```

### XPath Validation (for SOAP/XML)

```yaml
xpath:
  - expression: "//m:Temperature"
    expected: "72"
    namespaces:
      m: "http://www.example.org/weather"
      soap: "http://schemas.xmlsoap.org/soap/envelope/"
```

## Resource Files

The framework uses external resource files for headers, bodies, parameters, and other assets. These files should be organized in a specific structure:

```
src/test/resources/
├── body/       # Request bodies (JSON, XML, etc.)
├── headers/    # HTTP headers in JSON format
├── params/     # URL parameters
├── schemas/    # JSON schemas for validation
├── configs/    # YAML configuration files
└── data/       # Test data files (CSV, etc.)
```

### Headers Files

Headers files are JSON files containing key-value pairs of HTTP headers:

```json
{
  "Content-Type": "application/json",
  "Accept": "application/json",
  "Authorization": "Bearer ${token}"
}
```

### Body Files

Body files contain the request body and can be in any format (JSON, XML, plain text, etc.):

```json
{
  "name": "${userName}",
  "email": "${userEmail}",
  "age": ${userAge}
}
```

### Parameters Files

Parameters files contain URL parameters in JSON format:

```json
{
  "page": 1,
  "size": 10,
  "sort": "desc",
  "filter": "${filterValue}"
}
```

## Complete Configuration Example

Here's a comprehensive example showing all major configuration features:

```yaml
name: Complete API Test Suite
description: Demonstrates all configuration options for the framework

# Global variables
variables:
  baseUrl: https://api.example.com
  apiVersion: v1
  timeout: 30000
  contentType: application/json

# Execution configuration
execution:
  threads: 10
  iterations: 5
  rampUpPeriod: 5
  successThreshold: 95.0
  outputDirectory: "test-results"
  
  # Execution variables
  variables:
    timestamp: ${__time()}
    sessionId: "session-${__randomString(8)}"

# Test scenarios
scenarios:
  - name: HTTP API Tests
    # Scenario variables
    variables:
      endpoint: /users
      defaultLimit: 10
    
    # HTTP/REST requests
    requests:
      - name: Get Users
        endpoint: ${baseUrl}/${apiVersion}${endpoint}
        method: GET
        headers: |
          {
            "Accept": "application/json",
            "Cache-Control": "no-cache"
          }
        params: |
          {
            "limit": ${defaultLimit},
            "page": 1
          }
        statusCode: 200
        responseTimeThreshold: 1000
        
        responses:
          JsonPath: 
            - "$.length()=${defaultLimit}"
            - "$..[0].id=1"
          Contains: "user"
          MaxSize: 10240
      
      - name: Create User
        endpoint: ${baseUrl}/${apiVersion}${endpoint}
        method: POST
        headers: |
          {
            "Content-Type": "${contentType}",
            "Accept": "application/json"
          }
        body: |
          {
            "name": "Test User",
            "email": "test@example.com",
            "createdAt": "${timestamp}"
          }
        statusCode: 201
        
        # Request variables
        variables:
          requestId: "req-${__randomString(6)}"
  
  - name: SOAP API Tests
    # Scenario variables
    variables:
      service: /weather
    
    # SOAP requests
    soapRequests:
      - name: Get Weather
        endpoint: ${baseUrl}${service}
        soapAction: "GetWeather"
        soapEnvelope: |
          <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://www.example.org/weather">
            <soapenv:Header/>
            <soapenv:Body>
              <web:GetWeatherRequest>
                <web:City>New York</web:City>
              </web:GetWeatherRequest>
            </soapenv:Body>
          </soapenv:Envelope>
        statusCode: 200
        
        xpath:
          - expression: "//m:Temperature"
            expected: "72"
            namespaces:
              m: "http://www.example.org/weather"
  
  - name: GraphQL API Tests
    # Scenario variables
    variables:
      graphqlEndpoint: /graphql
    
    # GraphQL requests
    graphQLRequests:
      - name: Get Country
        endpoint: ${baseUrl}${graphqlEndpoint}
        query: |
          query GetCountry($code: ID!) {
            country(code: $code) {
              name
              capital
              currency
            }
          }
        operationName: "GetCountry"
        graphQLVariables:
          code: "US"
        statusCode: 200
        
        responses:
          JsonPath: 
            - "$.data.country.name=United States"
            - "$.data.country.capital=Washington D.C."
```

## Best Practices

### Configuration Organization

For large test suites, it's recommended to organize configuration files as follows:

```
configs/
├── http_tests/
│   ├── user_api_test.yaml
│   └── product_api_test.yaml
├── soap_tests/
│   ├── weather_service_test.yaml
│   └── flight_service_test.yaml
├── graphql_tests/
│   └── countries_api_test.yaml
└── common/
    ├── headers/
    │   ├── auth_headers.json
    │   └── content_type_headers.json
    ├── bodies/
    │   ├── create_user.json
    │   └── update_product.json
    └── env/
        ├── dev.yaml
        ├── test.yaml
        └── prod.yaml
```

### Test Config Best Practices

1. Use descriptive names for tests, scenarios, and requests
2. Extract common variables to the appropriate level
3. Use resource files for headers, bodies, and parameters that are reused
4. Organize your YAML configuration files logically by API or feature
5. Include appropriate validations for all requests
6. Define success criteria (status codes, response content) for all requests
7. Set appropriate thread counts and iterations for your use case
8. Use variables for values that might change between environments