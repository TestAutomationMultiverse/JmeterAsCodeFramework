# JMeter DSL Framework Resources

This directory contains organized resources for the JMeter DSL test framework.

## Resource Organization

The resources are now organized into the following structure:

- `/body` - Request body JSON files (e.g., `create_user_body.json`)
- `/headers` - Request headers JSON files (e.g., `create_user_headers.json`, `default_headers.json`)
- `/params` - Request parameters template files (e.g., `create_user_params.template`)
- `/schemas` - JSON Schema files for response validation (e.g., `create_user.schema.json`, `genericError.schema.json`)
- `/configs` - Configuration YAML files (e.g., `http_api_test.yaml`)
- `/data` - Test data files like CSV (e.g., `sample_HTTP_API_Test.csv`)

## Compatibility

The framework maintains backward compatibility with the flat structure. Both approaches will work:

```yaml
# Original approach with flat structure
body: create_user_body.json

# New approach with subfolder structure
body: body/create_user_body.json
```

The ResourceLoader class handles path resolution for both formats, searching in both locations.

## Transition Strategy

A utility class `ResourceReorganizer` is included to help with the transition. It's called during test setup to ensure resources are available in both the original flat structure and the new organized structure.

## Usage in Tests

For new tests, it's recommended to use the subfolder pattern for better organization:

```yaml
body: body/my_request_body.json
headers: headers/my_request_headers.json
params: params/my_request_params.template
responses:
  'Passed': schemas/my_response.schema.json
  'Failed': schemas/genericError.schema.json
```