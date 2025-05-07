package com.perftest.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to initialize JMeter environment and handle compatibility issues
 * with JavaScript JSR223 components.
 */
public class JMeterInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMeterInitializer.class);
    private static boolean initialized = false;

    /**
     * Initialize the JMeter environment with necessary system properties
     * to avoid compatibility issues with Java 21.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        try {
            LOGGER.info("Initializing JMeter environment");
            
            // Set system properties to configure JMeter environment
            // No longer need Groovy-specific properties as we're using JavaScript
            System.setProperty("apple.awt.UIElement", "true");
            System.setProperty("java.awt.headless", "true");
            System.setProperty("jmeter.home", "./");
            
            // Check available script engines
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine jsEngine = manager.getEngineByName("javascript");
            ScriptEngine groovyEngine = manager.getEngineByName("groovy");
            
            LOGGER.info("Available script engines:");
            manager.getEngineFactories().forEach(factory -> 
                LOGGER.info(" - {} ({})", factory.getEngineName(), factory.getNames()));
            
            if (jsEngine != null) {
                LOGGER.info("JavaScript engine found: {}", jsEngine.getFactory().getEngineName());
            } else {
                LOGGER.warn("JavaScript engine not found! This may cause issues with JSR223 components.");
            }
            
            if (groovyEngine != null) {
                LOGGER.info("Groovy engine found: {}", groovyEngine.getFactory().getEngineName());
                
                // If Groovy is available, prefer it for JSR223 as it handles both JS and Groovy syntax
                System.setProperty("jsr223.javascript.language", "groovy");
                System.setProperty("javax.script.language", "groovy");
                System.setProperty("jmeter.jsr223.init.file.javascript", "");
                System.setProperty("jmeter.jsr223.language", "groovy");
                
                // Additional properties to ensure JSR223 components use Groovy
                System.setProperty("jmeter.jsr223.use.language", "groovy");
                System.setProperty("jmeter.jsr223.engine.javascript", "groovy");
                
                // Critical property to force JSR223 to use Groovy 
                System.setProperty("javax.script.engine.javascript", "groovy");
            } else if (jsEngine != null) {
                // Fall back to JavaScript/Nashorn if Groovy is not available
                LOGGER.info("Groovy engine not found, falling back to JavaScript");
                
                // Force JavaScript as the default script language for JSR223 components
                System.setProperty("jsr223.javascript.language", "javascript");
                System.setProperty("javax.script.language", "javascript");
                System.setProperty("jmeter.jsr223.init.file.javascript", "");
                System.setProperty("jmeter.jsr223.language", "javascript");
                
                // Additional properties to ensure JSR223 components use JavaScript
                System.setProperty("jmeter.jsr223.use.language", "javascript");
                System.setProperty("jmeter.jsr223.engine.javascript", "javascript");
                
                // Critical property to force JSR223 to use JavaScript 
                System.setProperty("javax.script.engine.javascript", "nashorn");
            } else {
                LOGGER.error("No suitable script engines found! Tests requiring JSR223 components will fail.");
            }
            
            // Explicitly disable Groovy
            System.setProperty("groovy.script.casting.cache", "0");
            System.setProperty("groovy.source.encoding", "NO-GROOVY");
            // Tell JMeter not to look for Groovy
            System.setProperty("groovy.skip", "true");
            
            // Disable bean introspection for problematic JMeter classes
            System.setProperty("org.apache.jmeter.util.BeanShellBeanInfoImpl.skip", "true");
            System.setProperty("org.apache.jmeter.extractor.JSR223PostProcessorBeanInfo.skip", "true");
            System.setProperty("org.apache.jmeter.util.JSR223BeanInfoSupport.skip", "true");
            
            LOGGER.info("JMeter environment initialized successfully");
            initialized = true;
        } catch (Exception e) {
            LOGGER.error("Error initializing JMeter environment: {}", e.getMessage(), e);
        }
    }
}