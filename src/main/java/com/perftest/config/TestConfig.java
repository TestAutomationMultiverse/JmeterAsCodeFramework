package com.perftest.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.perftest.logging.TestLogger;

/**
 * Main configuration class representing the entire test YAML structure.
 */
public class TestConfig {
    private String protocol;
    private String data;
    private Map<String, Object> variables;
    private ExecutionConfig execution;
    private List<ScenarioConfig> scenarios;

    /**
     * Loads a TestConfig from a YAML string content.
     *
     * @param yamlContent The YAML content as a string
     * @return The TestConfig object
     * @throws IOException If the YAML content cannot be parsed
     */
    public static TestConfig fromYaml(String yamlContent) throws IOException {
        // Parse YAML using SnakeYAML
        Yaml yaml = new Yaml(new Constructor(TestConfig.class));
        TestConfig config = yaml.load(yamlContent);

        TestLogger.logInfo("Loaded test configuration from YAML content");
        return config;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public ExecutionConfig getExecution() {
        return execution;
    }

    public void setExecution(ExecutionConfig execution) {
        this.execution = execution;
    }

    public List<ScenarioConfig> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<ScenarioConfig> scenarios) {
        this.scenarios = scenarios;
    }

    @Override
    public String toString() {
        return "TestConfig{" +
                "protocol='" + protocol + '\'' +
                ", data='" + data + '\'' +
                ", variables=" + variables +
                ", execution=" + execution +
                ", scenarios=" + scenarios +
                '}';
    }
}
