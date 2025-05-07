package com.perftest.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.List;

/**
 * Utility class to check and diagnose JavaScript and Groovy script engine issues.
 * This is particularly useful for development environments using Java 21+.
 */
public class ScriptEngineChecker {

    /**
     * Main method to run the script engine checker from the command line.
     * Usage: java -cp target/jmeter-dsl-framework-1.0.0.jar:target/lib/* com.perftest.utils.ScriptEngineChecker
     *
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        checkScriptEngines();
    }
    
    /**
     * Check available script engines and their support for JavaScript and Groovy.
     * Prints detailed information about available engines, their versions, and supported language features.
     */
    public static void checkScriptEngines() {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        
        System.out.println("=== Script Engine Availability Check ===");
        System.out.println("JVM Version: " + System.getProperty("java.version"));
        System.out.println("JVM Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Total Script Engines Found: " + factories.size());
        System.out.println();
        
        boolean nashorn = false;
        boolean groovy = false;
        
        for (ScriptEngineFactory factory : factories) {
            System.out.println("Engine Name: " + factory.getEngineName());
            System.out.println("Engine Version: " + factory.getEngineVersion());
            System.out.println("Language Name: " + factory.getLanguageName());
            System.out.println("Language Version: " + factory.getLanguageVersion());
            
            System.out.print("Supported Extensions: ");
            for (String ext : factory.getExtensions()) {
                System.out.print(ext + " ");
            }
            System.out.println();
            
            System.out.print("Supported MIME Types: ");
            for (String mimeType : factory.getMimeTypes()) {
                System.out.print(mimeType + " ");
            }
            System.out.println();
            
            System.out.print("Supported Names: ");
            for (String name : factory.getNames()) {
                System.out.print(name + " ");
                if (name.equalsIgnoreCase("javascript") || name.equalsIgnoreCase("nashorn") || 
                    name.equalsIgnoreCase("js")) {
                    nashorn = true;
                }
                if (name.equalsIgnoreCase("groovy")) {
                    groovy = true;
                }
            }
            System.out.println("\n");
            
            try {
                // Try to create the engine
                ScriptEngine engine = factory.getScriptEngine();
                if (engine != null) {
                    System.out.println("--> Successfully created engine instance of type: " + engine.getClass().getName());
                } else {
                    System.out.println("--> Failed to create engine instance: returned null");
                }
            } catch (Exception e) {
                System.out.println("--> Error creating engine instance: " + e.getMessage());
            }
            
            System.out.println("\n" + "=".repeat(40) + "\n");
        }
        
        // Overall status
        System.out.println("=== Script Engine Status for JMeter DSL Framework ===");
        System.out.println("JavaScript/Nashorn Engine: " + (nashorn ? "AVAILABLE ✅" : "NOT AVAILABLE ❌"));
        System.out.println("Groovy Engine: " + (groovy ? "AVAILABLE ✅" : "NOT AVAILABLE ❌"));
        
        if (!nashorn && !groovy) {
            System.out.println("\nCRITICAL ERROR: Both JavaScript and Groovy engines are missing!");
            System.out.println("The Performance Automation Framework requires at least one of these script engines.");
            System.out.println("\nRecommended fix:");
            System.out.println("1. Ensure you have the following dependencies in your pom.xml:");
            System.out.println("   - org.openjdk.nashorn:nashorn-core:15.4");
            System.out.println("   - org.apache.groovy:groovy-jsr223:4.0.15");
            System.out.println("   - org.apache.groovy:groovy:4.0.15");
            System.out.println("2. Run 'mvn clean package' to update dependencies");
        } else if (!nashorn) {
            System.out.println("\nWARNING: JavaScript/Nashorn engine is missing!");
            System.out.println("Some legacy script features may not work correctly.");
            System.out.println("\nRecommended fix:");
            System.out.println("- Add org.openjdk.nashorn:nashorn-core:15.4 to your pom.xml");
        } else if (!groovy) {
            System.out.println("\nWARNING: Groovy engine is missing!");
            System.out.println("JSR223 PostProcessors may not function correctly.");
            System.out.println("\nRecommended fix:");
            System.out.println("- Add org.apache.groovy:groovy-jsr223:4.0.15 to your pom.xml");
        } else {
            System.out.println("\nAll required script engines are available! ✅");
        }
    }
}