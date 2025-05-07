# SOAP Service Beeceptor Example

This example demonstrates how to use the Performance Automation Framework to test SOAP services using the Beeceptor mock SOAP service.

## About the API

The Beeceptor mock SOAP service is a free, public mock service for testing SOAP implementations. It is available at:

[https://app.beeceptor.com/mock-server/soap-service-free](https://app.beeceptor.com/mock-server/soap-service-free)

This service provides a simple SOAP endpoint that accepts various SOAP envelopes and returns predefined responses, making it ideal for testing SOAP implementations without needing to set up a real SOAP server.

## Available Operations

The mock SOAP service supports the following operations:

1. **GetWeather** - Get weather information for a city
2. **GetCountryInfo** - Get country information by name or code
3. **GetFlightStatus** - Get flight status information

## Configuration Example

Instead of inline requests, the framework uses a YAML configuration file to define the SOAP test. Here's an example of a SOAP test configuration file (`soap_test_config.yaml`):

```yaml
execution:
  threads: 1
  iterations: 1
  successThreshold: 95.0
  variables:
    baseUrl: https://soap-service-free.mock.beeceptor.com

scenarios:
  - name: "SOAP API Test"
    soapRequests:
      - name: "Get Weather Information"
        endpoint: "${baseUrl}/WeatherService"
        soapAction: "GetWeather"
        soapEnvelopeFile: "src/test/resources/body/weather_request.xml"
        statusCode: 200
        xpath:
          - expression: "//m:Temperature"
            expected: "72"
            namespaces:
              m: "http://www.example.org/weather"
      
      - name: "Get Country Info"
        endpoint: "${baseUrl}/CountryInfoService"
        soapAction: "GetCountryByName"
        soapEnvelopeFile: "src/test/resources/body/country_request.xml"
        statusCode: 200
        xpath:
          - expression: "//m:Population"
            expected: "331002651"
            namespaces:
              m: "http://www.example.org/country"
```

## Sample SOAP Request

Here's a sample SOAP request to the GetCountryInfo operation:

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://www.example.org/country">
  <soapenv:Header/>
  <soapenv:Body>
    <web:GetCountryByNameRequest>
      <web:CountryName>United States</web:CountryName>
    </web:GetCountryByNameRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

## Sample SOAP Response

Here's an example response from the GetCountryInfo operation:

```xml
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Body>
    <ns2:GetCountryByNameResponse xmlns:ns2="http://www.example.org/country">
      <ns2:Country>
        <ns2:Name>United States</ns2:Name>
        <ns2:Capital>Washington, D.C.</ns2:Capital>
        <ns2:Population>331002651</ns2:Population>
        <ns2:Currency>USD</ns2:Currency>
      </ns2:Country>
    </ns2:GetCountryByNameResponse>
  </soap:Body>
</soap:Envelope>
```

## Java Implementation

The following Java code demonstrates how to run a SOAP test with Java 21 compatibility:

```java
package com.perftest;

import com.perftest.core.TestExecutor;
import com.perftest.logging.TestLogger;
import com.perftest.utils.JMeterInitializer;
import com.perftest.utils.ScriptEngineChecker;

public class SoapServiceExample {
    public static void main(String[] args) {
        // Initialize the logger
        TestLogger.initialize();
        
        // Initialize JMeter environment and detect available script engines
        JMeterInitializer.initialize();
        
        // Log script engine status for debugging
        System.out.println("JavaScript Engine available: " + 
            JMeterInitializer.isJavaScriptEngineAvailable());
        System.out.println("Groovy Engine available: " + 
            JMeterInitializer.isGroovyEngineAvailable());
        
        // For SOAP APIs, XPath validation requires script engine support
        if (!JMeterInitializer.isJavaScriptEngineAvailable() && !JMeterInitializer.isGroovyEngineAvailable()) {
            System.err.println("Warning: No script engines available for XPath validation!");
            ScriptEngineChecker.checkAvailableEngines();
            return;
        }
        
        // Create a test executor
        TestExecutor executor = new TestExecutor();
        
        // Execute the test with the SOAP configuration
        String configFile = "src/test/resources/configs/soap_test_config.yaml";
        boolean success = executor.execute(configFile);
        
        System.out.println("SOAP Test Execution: " + (success ? "PASSED" : "FAILED"));
    }
}
```

This implementation includes specific considerations for SOAP testing:

1. XPath validation for SOAP responses requires script engine support
2. The code explicitly checks for script engine availability before running tests
3. Both JavaScript (Nashorn) and Groovy engines can be used for XPath validation
4. The script diagnostics are more detailed for SOAP testing due to XML processing requirements

## XPath Response Validation

The framework provides robust XPath validation for SOAP responses:

```yaml
xpath:
  - expression: "//m:Population"
    expected: "331002651"
    namespaces:
      m: "http://www.example.org/country"
```

This validation ensures that the response XML contains the expected values in the specified XML paths, using namespaces as needed.

## Adding SOAP Headers

For SOAP services that require specific headers, you can include them in the `soapEnvelope`:

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="http://www.example.org/weather">
  <soapenv:Header>
    <web:Security>
      <web:Username>testuser</web:Username>
      <web:Password>testpass</web:Password>
    </web:Security>
  </soapenv:Header>
  <soapenv:Body>
    <!-- SOAP body content -->
  </soapenv:Body>
</soapenv:Envelope>
```

## Handling SOAP Faults

The framework can validate SOAP fault responses by checking the status code and the content of the SOAP fault:

```yaml
- name: "Invalid Weather Request"
  endpoint: "${baseUrl}/WeatherService"
  soapAction: "GetWeather"
  soapEnvelopeFile: "src/test/resources/body/invalid_weather_request.xml"
  statusCode: 500
  xpath:
    - expression: "//faultstring"
      expected: "City not found"
```

## Conclusion

The Beeceptor mock SOAP service provides a convenient way to test SOAP API implementations without setting up a real SOAP server. Combined with the Performance Automation Framework, it allows for comprehensive testing of SOAP service interactions including request handling, response validation, and error scenarios.