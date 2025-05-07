package com.perftest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.perftest.utils.JMeterInitializer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * A simple test class to verify that the script engines are properly integrated.
 * Checks both Nashorn (JavaScript) and Groovy engines.
 */
public class NashornEngineTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NashornEngineTest.class);

    public static void main(String[] args) {
        // Initialize JMeter environment (sets JavaScript engine properties)
        JMeterInitializer.initialize();
        
        // Create a script engine manager
        ScriptEngineManager manager = new ScriptEngineManager();
        
        // Check for JavaScript engine (Nashorn)
        checkEngine(manager, "javascript", "var message = 'Hello from Nashorn!'; message;");
        
        // Check for Groovy engine 
        checkEngine(manager, "groovy", "def message = 'Hello from Groovy!'; return message;");
        
        System.out.println("All script engine tests completed successfully.");
    }
    
    private static void checkEngine(ScriptEngineManager manager, String engineName, String testScript) {
        System.out.println("\nChecking for " + engineName + " engine:");
        
        // Try to get the engine
        ScriptEngine engine = manager.getEngineByName(engineName);
        
        if (engine == null) {
            LOGGER.error("{} engine not found! The engine dependency may not be properly configured.", engineName);
            System.out.println("ERROR: " + engineName + " engine not found!");
            System.out.println("Available engines:");
            manager.getEngineFactories().forEach(factory -> 
                System.out.println(" - " + factory.getEngineName() + " (" + factory.getNames() + ")"));
        } else {
            LOGGER.info("{} engine found: {}", engineName, engine.getFactory().getEngineName());
            System.out.println("SUCCESS: " + engineName + " engine found: " + engine.getFactory().getEngineName());
            
            // Try to execute a simple script
            try {
                Object result = engine.eval(testScript);
                LOGGER.info("Script execution result: {}", result);
                System.out.println("Script execution result: " + result);
            } catch (ScriptException e) {
                LOGGER.error("Error executing " + engineName + ": {}", e.getMessage(), e);
                System.out.println("ERROR: Failed to execute " + engineName + " script: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}