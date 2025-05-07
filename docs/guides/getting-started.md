# Getting Started

This guide will help you get started with the Performance Automation Framework. It covers installation, configuration, and running your first test.

## Prerequisites

Before using the framework, ensure you have the following installed:

- Java Development Kit (JDK) 21 or higher (compatible with our Java 21 fixes)
- Maven 3.9 or higher
- Git (for cloning the repository)

## Installation

### Step 1: Clone the Repository

```bash
git clone https://github.com/TestAutomationMultiverse/PerformanceAutomationFramework.git
cd PerformanceAutomationFramework
```

### Step 2: Build the Project

```bash
mvn clean install
```

This will compile the code, run tests, and install the framework to your local Maven repository.

### Step 3: Verify Installation

To verify the installation was successful, run the sample tests:

```bash
mvn test
```

If successful, you should see output indicating that the tests passed with 0% error rate.

## Maven Configuration

This framework uses several dependencies that are managed through Maven. The main dependencies include:

```xml
<!-- JMeter Core & Components -->
<dependency>
    <groupId>org.apache.jmeter</groupId>
    <artifactId>ApacheJMeter_core</artifactId>
    <version>5.5</version>
</dependency>
<dependency>
    <groupId>org.apache.jmeter</groupId>
    <artifactId>ApacheJMeter_http</artifactId>
    <version>5.5</version>
</dependency>
<dependency>
    <groupId>org.apache.jmeter</groupId>
    <artifactId>ApacheJMeter_components</artifactId>
    <version>5.5</version>
</dependency>

<!-- JMeter DSL Libraries -->
<dependency>
    <groupId>us.abstracta.jmeter</groupId>
    <artifactId>jmeter-java-dsl</artifactId>
    <version>1.29.1</version>
</dependency>
<dependency>
    <groupId>us.abstracta.jmeter</groupId>
    <artifactId>jmeter-java-dsl-http</artifactId>
    <version>1.29.1</version>
</dependency>

<!-- Script Engine Support for Java 21 -->
<dependency>
    <groupId>org.openjdk.nashorn</groupId>
    <artifactId>nashorn-core</artifactId>
    <version>15.4</version>
</dependency>
<dependency>
    <groupId>org.apache.groovy</groupId>
    <artifactId>groovy-all</artifactId>
    <version>4.0.14</version>
    <type>pom</type>
</dependency>

<!-- Data Processing & Parsing -->
<dependency>
    <groupId>org.yaml</groupId>
    <artifactId>snakeyaml</artifactId>
    <version>2.0</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.14.2</version>
</dependency>

<!-- Logging -->
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-api</artifactId>
    <version>2.17.2</version>
</dependency>
<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-core</artifactId>
    <version>2.17.2</version>
</dependency>
```

## Basic Test Configuration

The framework uses YAML files for test configuration. Here's a simple example:

```yaml
execution:
  threads: 1
  iterations: 1
  successThreshold: 95.0
  variables:
    baseUrl: https://jsonplaceholder.typicode.com

scenarios:
  - name: "Simple HTTP Test"
    requests:
      - name: "Get Posts"
        endpoint: "${baseUrl}/posts"
        method: GET
        statusCode: 200
```

This configuration defines a simple test that:
- Uses 1 thread
- Runs 1 iteration
- Has a success threshold of 95%
- Defines a `baseUrl` variable
- Includes one scenario with one request

## Running Tests

### Via Java Code

Create a Java class to run a test:

```java
package com.perftest;

import com.perftest.core.TestExecutor;
import com.perftest.logging.TestLogger;

public class SimpleTest {
    public static void main(String[] args) {
        // Initialize the logger
        TestLogger.initialize();
        
        // Create a test executor
        TestExecutor executor = new TestExecutor();
        
        // Execute the test
        String configFile = "src/test/resources/configs/http_test_config.yaml";
        boolean success = executor.execute(configFile);
        
        System.out.println("Test execution: " + (success ? "PASSED" : "FAILED"));
    }
}
```

### Via Maven

Run tests using Maven:

```bash
mvn test -Dtest=com.perftest.GraphQLTest
mvn test -Dtest=com.perftest.SoapTest
mvn test -Dtest=com.perftest.YamlConfigTest
```

## Supported Test Types

The framework supports three main API protocols:

1. **HTTP/REST API** - Test RESTful services with JSON responses
2. **SOAP API** - Test SOAP web services with XML payloads
3. **GraphQL API** - Test GraphQL queries and mutations

Each protocol has specific configuration options in the YAML file format.

## Next Steps

After setting up and running your first test, you can:

1. Check out the [Protocol Support](protocols.md) documentation for details on specific API protocols
2. Explore the [Components](components.md) documentation for details on framework internals
3. Learn about [Test Design](test-design.md) techniques including response validation
4. Review the API Examples for real-world examples using public APIs:
   - [REST API (JSONPlaceholder)](../api-examples/jsonplaceholder.md)
   - [SOAP API (Beeceptor)](../api-examples/soap-service-beeceptor.md)
   - [GraphQL API (Apollo Countries)](../api-examples/apollo-countries-graphql.md)