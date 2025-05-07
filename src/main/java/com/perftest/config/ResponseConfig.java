package com.perftest.config;

/**
 * Configuration class for expected responses and their validation schemas.
 */
public class ResponseConfig {
    private String status;
    private String schema;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    @Override
    public String toString() {
        return "ResponseConfig{" +
                "status='" + status + '\'' +
                ", schema='" + schema + '\'' +
                '}';
    }
}
