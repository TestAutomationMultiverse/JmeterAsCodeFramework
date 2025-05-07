# JSONPlaceholder API Example

This example demonstrates how to use the Performance Automation Framework to test REST APIs using the JSONPlaceholder service.

## About the API

[JSONPlaceholder](https://jsonplaceholder.typicode.com/) is a free online REST API that provides fake data for testing and prototyping. It offers endpoints for posts, comments, albums, photos, todos, and users.

## Available Endpoints

JSONPlaceholder provides the following endpoints:

- `/posts` - 100 posts
- `/comments` - 500 comments
- `/albums` - 100 albums
- `/photos` - 5000 photos
- `/todos` - 200 todos
- `/users` - 10 users

Each resource supports standard HTTP methods (GET, POST, PUT, PATCH, DELETE).

## Configuration Example

The framework uses YAML configuration files with separate files for request bodies and headers. Here's an example configuration file (`jsonplaceholder_test.yaml`):

```yaml
execution:
  threads: 2
  iterations: 5
  successThreshold: 95.0
  variables:
    baseUrl: https://jsonplaceholder.typicode.com

scenarios:
  - name: "JSONPlaceholder API Test"
    requests:
      - name: "Get All Posts"
        endpoint: "${baseUrl}/posts"
        method: GET
        statusCode: 200
        responseTimeThreshold: 1000
        
      - name: "Get Single Post"
        endpoint: "${baseUrl}/posts/1"
        method: GET
        statusCode: 200
        responses:
          JsonPath: "$.title=sunt aut facere repellat provident occaecati excepturi optio reprehenderit"
          
      - name: "Create Post"
        endpoint: "${baseUrl}/posts"
        method: POST
        headersFile: "src/test/resources/headers/json_content_type.json"
        bodyFile: "src/test/resources/body/create_post.json"
        statusCode: 201
        
      - name: "Update Post"
        endpoint: "${baseUrl}/posts/1"
        method: PUT
        headersFile: "src/test/resources/headers/json_content_type.json"
        bodyFile: "src/test/resources/body/update_post.json"
        statusCode: 200
        
      - name: "Patch Post"
        endpoint: "${baseUrl}/posts/1"
        method: PATCH
        headersFile: "src/test/resources/headers/json_content_type.json"
        bodyFile: "src/test/resources/body/patch_post.json"
        statusCode: 200
        
      - name: "Delete Post"
        endpoint: "${baseUrl}/posts/1"
        method: DELETE
        statusCode: 200
        
      - name: "Get Comments for Post"
        endpoint: "${baseUrl}/posts/1/comments"
        method: GET
        statusCode: 200
        
      - name: "Get Users"
        endpoint: "${baseUrl}/users"
        method: GET
        statusCode: 200
        
      - name: "Get User by ID"
        endpoint: "${baseUrl}/users/1"
        method: GET
        statusCode: 200
        responses:
          JsonPath: "$.name=Leanne Graham"
```

## Sample Responses

### Get Posts Response

```json
[
  {
    "userId": 1,
    "id": 1,
    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
    "body": "quia et suscipit suscipit recusandae consequuntur expedita et cum reprehenderit molestiae ut ut quas totam nostrum rerum est autem sunt rem eveniet architecto"
  },
  {
    "userId": 1,
    "id": 2,
    "title": "qui est esse",
    "body": "est rerum tempore vitae sequi sint nihil reprehenderit dolor beatae ea dolores neque fugiat blanditiis voluptate porro vel nihil molestiae ut reiciendis qui aperiam non debitis possimus qui neque nisi nulla"
  }
]
```

### Get Single Post Response

```json
{
  "userId": 1,
  "id": 1,
  "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
  "body": "quia et suscipit suscipit recusandae consequuntur expedita et cum reprehenderit molestiae ut ut quas totam nostrum rerum est autem sunt rem eveniet architecto"
}
```

### Create Post Response

```json
{
  "title": "foo",
  "body": "bar",
  "userId": 1,
  "id": 101
}
```

## Java Implementation

The following Java code demonstrates how to run a test against the JSONPlaceholder API with Java 21 compatibility:

```java
package com.perftest;

import com.perftest.core.TestExecutor;
import com.perftest.logging.TestLogger;
import com.perftest.utils.JMeterInitializer;
import com.perftest.utils.ScriptEngineChecker;

public class JsonPlaceholderExample {
    public static void main(String[] args) {
        // Initialize the logger
        TestLogger.initialize();
        
        // Initialize JMeter environment and script engines
        JMeterInitializer.initialize();
        
        // Optionally verify script engines availability
        if (!JMeterInitializer.isJavaScriptEngineAvailable() && !JMeterInitializer.isGroovyEngineAvailable()) {
            System.err.println("Warning: No script engines available. Run ScriptEngineChecker for details.");
            ScriptEngineChecker.checkAvailableEngines();
            return;
        }
        
        // Create a test executor
        TestExecutor executor = new TestExecutor();
        
        // Execute the test with the JSONPlaceholder configuration
        String configFile = "src/test/resources/configs/jsonplaceholder_test.yaml";
        boolean success = executor.execute(configFile);
        
        System.out.println("JSONPlaceholder Test Execution: " + (success ? "PASSED" : "FAILED"));
    }
}
```

This implementation includes the following Java 21 compatibility features:

1. JMeter initialization with `JMeterInitializer.initialize()` to set up the environment
2. Script engine availability check before executing tests
3. Option to run `ScriptEngineChecker` for diagnostics if no engines are available
4. Support for dynamic script engine selection based on availability

## Nested Resources

JSONPlaceholder supports nested routes for related resources:

```yaml
requests:
  - name: "Get Posts by User"
    endpoint: "${baseUrl}/users/1/posts"
    method: GET
    statusCode: 200
    
  - name: "Get Albums by User"
    endpoint: "${baseUrl}/users/1/albums"
    method: GET
    statusCode: 200
    
  - name: "Get Photos by Album"
    endpoint: "${baseUrl}/albums/1/photos"
    method: GET
    statusCode: 200
```

## Testing with Query Parameters

You can add query parameters to filter results using parameter files:

```yaml
requests:
  - name: "Filter Posts by User ID"
    endpoint: "${baseUrl}/posts"
    method: GET
    paramsFile: "src/test/resources/params/user_filter.json"
    statusCode: 200
    
  - name: "Filter Comments by Post ID"
    endpoint: "${baseUrl}/comments"
    method: GET
    paramsFile: "src/test/resources/params/post_comments_filter.json"
    statusCode: 200
```

## Testing Todos Resource

The todos resource can be used to test task management functionality:

```yaml
requests:
  - name: "Get All Todos"
    endpoint: "${baseUrl}/todos"
    method: GET
    statusCode: 200
    
  - name: "Get Completed Todos"
    endpoint: "${baseUrl}/todos"
    method: GET
    paramsFile: "src/test/resources/params/completed_filter.json"
    statusCode: 200
    responses:
      JsonPath: "$[0].completed=true"
    
  - name: "Create Todo"
    endpoint: "${baseUrl}/todos"
    method: POST
    headersFile: "src/test/resources/headers/json_content_type.json"
    bodyFile: "src/test/resources/body/create_todo.json"
    statusCode: 201
```

## Testing User Authentication (Simulated)

JSONPlaceholder doesn't actually handle authentication, but you can simulate it:

```yaml
requests:
  - name: "Login (Simulated)"
    endpoint: "${baseUrl}/users"
    method: POST
    headersFile: "src/test/resources/headers/json_content_type.json"
    bodyFile: "src/test/resources/body/login.json"
    statusCode: 201
    variables:
      token: "simulated-jwt-token"
    
  - name: "Access Protected Resource (Simulated)"
    endpoint: "${baseUrl}/users/1"
    method: GET
    headersFile: "src/test/resources/headers/auth_header.json"
    statusCode: 200
```

## Performance Testing

JSONPlaceholder is useful for performance testing simulations:

```yaml
execution:
  threads: 50
  rampUp: 30
  duration: 300
  successThreshold: 95.0
  variables:
    baseUrl: https://jsonplaceholder.typicode.com

scenarios:
  - name: "Load Test - Posts API"
    requests:
      - name: "Get All Posts"
        endpoint: "${baseUrl}/posts"
        method: GET
        statusCode: 200
        responseTimeThreshold: 1000
        
      - name: "Get Single Post"
        endpoint: "${baseUrl}/posts/1"
        method: GET
        statusCode: 200
        responseTimeThreshold: 500
```

## Handling Rate Limiting

JSONPlaceholder may rate limit requests if too many are sent:

```yaml
execution:
  threads: 5
  iterations: 10
  rampUp: 30
  successThreshold: 90.0  # Lower threshold to account for potential rate limiting
  variables:
    baseUrl: https://jsonplaceholder.typicode.com

scenarios:
  - name: "Rate Limit Test"
    requests:
      - name: "Rapid Requests"
        endpoint: "${baseUrl}/posts"
        method: GET
        statusCode: 200
        variables:
          sleep: 500  # Add delay between requests (milliseconds)
```

## Conclusion

JSONPlaceholder provides a comprehensive set of REST API endpoints for testing HTTP requests across various resources. It's ideal for integration testing, performance testing, and developing API client libraries without the need for a real backend API. Its simplicity and stability make it perfect for demonstrating the features of the Performance Automation Framework.