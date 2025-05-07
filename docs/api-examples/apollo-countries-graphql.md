# Apollo GraphQL Countries API Example

This example demonstrates how to use the Performance Automation Framework to test GraphQL APIs using the Apollo Countries GraphQL API.

## About the API

The Apollo Countries GraphQL API is a public, read-only GraphQL API that provides information about countries, continents, and languages. It is available at:

[https://studio.apollographql.com/public/countries/variant/current/explorer](https://studio.apollographql.com/public/countries/variant/current/explorer)

The service endpoint is:
```
https://countries.trevorblades.com/graphql
```

This API allows querying country information, filtering by various attributes, and exploring relationships between countries, continents, and languages.

## API Schema

The API provides the following main types:

- **Country**: Information about countries (name, code, capital, currency, etc.)
- **Continent**: Information about continents (name, code, countries)
- **Language**: Information about languages (name, code, native name)

## Configuration Example

The framework uses YAML configuration files for testing with file-based GraphQL queries. Here's an example YAML configuration file (`graphql_test_config.yaml`):

```yaml
execution:
  threads: 1
  iterations: 1
  successThreshold: 95.0
  variables:
    baseUrl: https://countries.trevorblades.com

scenarios:
  - name: "GraphQL Countries API Test"
    graphQLRequests:
      - name: "Get Country by Code"
        endpoint: "${baseUrl}/graphql"
        queryFile: "src/test/resources/queries/get_country.graphql"
        graphQLVariables:
          code: "US"
        statusCode: 200
        
      - name: "Get Countries by Continent"
        endpoint: "${baseUrl}/graphql"
        queryFile: "src/test/resources/queries/get_countries_by_continent.graphql"
        graphQLVariables:
          code: "EU"
        statusCode: 200
        
      - name: "Get All Continents"
        endpoint: "${baseUrl}/graphql"
        queryFile: "src/test/resources/queries/get_all_continents.graphql"
        statusCode: 200
```

## Sample GraphQL Queries

### Query a Specific Country

```graphql
query GetCountry($code: ID!) {
  country(code: $code) {
    name
    capital
    currency
    languages {
      name
    }
  }
}

# Variables
{
  "code": "US"
}
```

### Sample Response

```json
{
  "data": {
    "country": {
      "name": "United States",
      "capital": "Washington D.C.",
      "currency": "USD,USN,USS",
      "languages": [
        {
          "name": "English"
        }
      ]
    }
  }
}
```

### Query Countries by Continent

```graphql
query GetCountriesByContinent($code: ID!) {
  continent(code: $code) {
    name
    countries {
      name
      capital
      currency
    }
  }
}

# Variables
{
  "code": "EU"
}
```

### Query All Continents

```graphql
{
  continents {
    name
    code
  }
}
```

## Java Implementation

The following Java code demonstrates how to run a GraphQL test with Java 21 compatibility:

```java
package com.perftest;

import com.perftest.core.TestExecutor;
import com.perftest.logging.TestLogger;
import com.perftest.utils.JMeterInitializer;
import com.perftest.utils.ScriptEngineChecker;

public class GraphQLCountriesExample {
    public static void main(String[] args) {
        // Initialize the logger
        TestLogger.initialize();
        
        // Initialize JMeter environment for GraphQL testing
        JMeterInitializer.initialize();
        
        // GraphQL response validation requires a script engine for JSON path validation
        boolean jsEngineAvailable = JMeterInitializer.isJavaScriptEngineAvailable();
        boolean groovyEngineAvailable = JMeterInitializer.isGroovyEngineAvailable();
        
        if (!jsEngineAvailable && !groovyEngineAvailable) {
            System.err.println("Warning: No script engines available for GraphQL response validation!");
            ScriptEngineChecker.checkAvailableEngines();
            
            // For GraphQL, JSON Path validation is often critical
            System.err.println("GraphQL response validation requires a script engine. Continuing anyway...");
        } else {
            System.out.println("Using " + 
                (jsEngineAvailable ? "JavaScript (Nashorn)" : "Groovy") + 
                " engine for GraphQL response validation");
        }
        
        // Create a test executor
        TestExecutor executor = new TestExecutor();
        
        // Execute the test with the GraphQL configuration
        String configFile = "src/test/resources/configs/graphql_test_config.yaml";
        boolean success = executor.execute(configFile);
        
        System.out.println("GraphQL Test Execution: " + (success ? "PASSED" : "FAILED"));
    }
}
```

This implementation highlights several important considerations for GraphQL testing with Java 21:

1. GraphQL response validation relies heavily on JSON Path assertions
2. The script engine selection is deterministic - will use Nashorn if available, otherwise fall back to Groovy
3. The code provides more detailed diagnostic information about which engine is being used
4. Unlike SOAP, GraphQL testing can often proceed even without script engines, but with reduced validation capabilities

## Response Validation

For GraphQL responses, the framework provides multiple ways to validate the response:

### Status Code Validation

```yaml
statusCode: 200
```

### JSON Path Validation

```yaml
responses:
  JsonPath: "$.data.country.name=United States"
```

### Response Content Validation

```yaml
responses:
  Contains: "United States"
```

## Complex Queries with Fragments

GraphQL fragments can be used for more complex queries:

```graphql
query GetCountriesWithFragments($code1: ID!, $code2: ID!) {
  country1: country(code: $code1) {
    ...CountryDetails
  }
  country2: country(code: $code2) {
    ...CountryDetails
  }
}

fragment CountryDetails on Country {
  name
  capital
  currency
  languages {
    name
    native
  }
}

# Variables
{
  "code1": "US",
  "code2": "CA"
}
```

## Error Handling

The framework handles GraphQL errors by validating both the HTTP status code and the presence of errors in the GraphQL response:

```yaml
- name: "Invalid Country Request"
  endpoint: "${baseUrl}/graphql"
  queryFile: "src/test/resources/queries/get_country_error.graphql"
  graphQLVariables:
    code: "INVALID"
  statusCode: 200
  responses:
    Contains: "errors"
```

## Introspection Queries

GraphQL introspection can be used to query the schema:

```graphql
{
  __schema {
    types {
      name
      kind
      fields {
        name
        type {
          name
          kind
        }
      }
    }
  }
}
```

## Conclusion

The Apollo Countries GraphQL API provides a practical, publicly available endpoint for testing GraphQL functionality. The Performance Automation Framework simplifies the process of testing GraphQL APIs by providing a configuration-driven approach to query execution and response validation.