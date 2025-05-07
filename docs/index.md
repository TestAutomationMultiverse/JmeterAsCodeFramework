# Performance Automation Framework

A robust Java-based performance testing framework built on top of JMeter DSL, providing advanced testing capabilities for HTTP, SOAP, and GraphQL APIs.

## Key Features

- **Multi-protocol support**: Test HTTP, SOAP, and GraphQL APIs with a unified framework
- **Advanced Assertion Framework**: Validate responses using status codes, JSON Path, XPath, and more
- **Dynamic variable resolution**: Define variables at global, scenario, and request levels
- **Comprehensive reporting**: Generate HTML reports with charts and detailed statistics
- **Java 21 compatibility**: Fully compatible with the latest Java versions
- **Template-based testing**: Use templates for request bodies with variable substitution
- **Robust error handling**: Detailed error tracking and logging for easier debugging
- **DevContainer support**: Consistent development environment using VS Code DevContainers

## Latest Updates

The framework has been updated to include:

- **Full Java 21 Compatibility**: Fixed script engine issues with dynamic engine selection
- **Enhanced Script Engines**: Added Nashorn 15.4 and Groovy 4.0.15 with JSR-223 support
- **Improved Error Logging**: Variables are now properly resolved in logs for easier debugging
- **Comprehensive Documentation**: Complete guides for development and configuration
- **DevContainer Support**: Added Docker-based development environment for consistency

## Quick Start Guide

### Prerequisites

- Java 21 or higher
- Maven 3.9 or higher
- Required dependencies:
  - Nashorn Script Engine (added automatically via Maven)
  - Groovy Script Engine (added automatically via Maven)

### Installation

```bash
git clone https://github.com/TestAutomationMultiverse/PerformanceAutomationFramework.git
cd PerformanceAutomationFramework
mvn clean install
```

### Sample Test Configuration

```yaml
execution:
  threads: 2
  iterations: 5
  successThreshold: 95.0
  variables:
    baseUrl: https://jsonplaceholder.typicode.com

scenarios:
  - name: "HTTP API Test"
    requests:
      - name: "Get Users"
        endpoint: "${baseUrl}/users"
        method: GET
        statusCode: 200
```

### Running Tests

```java
TestExecutor executor = new TestExecutor();
boolean success = executor.execute("src/test/resources/configs/http_test_config.yaml");
```

## Documentation Guides

- [Getting Started](guides/getting-started.md): Complete installation and setup guide
- [DevContainer Setup](guides/devcontainer-setup.md): Development environment setup using DevContainers
- [Framework Components](guides/components.md): Overview of key framework components
- [Protocol Support](guides/protocols.md): Details on supported API protocols

## Technical Documentation

- [Architecture](guides/architecture.md): Framework architecture and design principles
- [Configuration Guide](guides/configuration.md): Comprehensive guide to YAML configuration
- [Component Details](guides/components.md): In-depth information about framework components
- [Protocol Details](guides/protocols.md): Detailed protocol-specific documentation
- [Test Design Guide](guides/test-design.md): Guidelines for designing effective tests

### API Reference

- [Configuration API](api/config-api.md): API for working with configuration objects
- [Core API](api/core-api.md): Core framework APIs for test execution
- [Utility API](api/utility-api.md): Utility APIs for common operations

### Usage Examples

- [Basic Example](examples/basic-example.md): Simple examples to get started
- [Multiple Scenarios](examples/multiple-scenarios.md): Working with multiple test scenarios
- [Variable Usage](examples/variable-usage.md): Examples of variable substitution

### Reporting

- [HTML Reports](reporting/html-reports.md): Generating and interpreting HTML test reports

### API Examples

- [REST API](api-examples/jsonplaceholder.md): Testing RESTful APIs with JSONPlaceholder
- [SOAP API](api-examples/soap-service-beeceptor.md): Testing SOAP web services
- [GraphQL API](api-examples/apollo-countries-graphql.md): Testing GraphQL endpoints

## License

This project is licensed under the MIT License - see the LICENSE file for details.