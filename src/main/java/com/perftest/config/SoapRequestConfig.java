package com.perftest.config;

import java.util.List;
import java.util.Map;

/**
 * Configuration class for SOAP requests.
 * <p>
 * This class contains all the configuration parameters needed for a SOAP request,
 * including endpoint, method, headers, SOAP envelope, and response validation options.
 */
public class SoapRequestConfig extends RequestConfig {
    
    /**
     * The SOAP envelope to be sent in the request.
     * This can be either an inline XML string or a reference to a file.  
     */
    private String soapEnvelope;
    
    /**
     * The path to the template file containing the SOAP envelope.
     */
    private String templateFile;
    
    /**
     * Variables to be substituted in the template file.
     */
    private Map<String, Object> templateVariables;
    
    /**
     * The SOAP action header value.
     */
    private String soapAction;
    
    /**
     * XPath validation configuration for SOAP responses.
     */
    private List<XPathValidation> xpath;
    
    /**
     * Response validation configuration.
     * This property is used to keep the YAML mapping consistent
     * with the configuration structure used in other request types.
     */
    private Map<String, Object> responseValidation;
    
    /**
     * Nested class for XPath validation configuration.
     */
    public static class XPathValidation {
        private String expression;
        private String expected;
        private Map<String, String> namespaces;
        
        public String getExpression() {
            return expression;
        }
        
        public void setExpression(String expression) {
            this.expression = expression;
        }
        
        public String getExpected() {
            return expected;
        }
        
        public void setExpected(String expected) {
            this.expected = expected;
        }
        
        public Map<String, String> getNamespaces() {
            return namespaces;
        }
        
        public void setNamespaces(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }
    }
    
    public String getSoapEnvelope() {
        return soapEnvelope;
    }
    
    public void setSoapEnvelope(String soapEnvelope) {
        this.soapEnvelope = soapEnvelope;
    }
    
    public String getSoapAction() {
        return soapAction;
    }
    
    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }
    
    public List<XPathValidation> getXpath() {
        return xpath;
    }
    
    public void setXpath(List<XPathValidation> xpath) {
        this.xpath = xpath;
    }
    
    public Map<String, Object> getResponseValidation() {
        return responseValidation;
    }
    
    public void setResponseValidation(Map<String, Object> responseValidation) {
        this.responseValidation = responseValidation;
    }
    
    /**
     * Gets the template file path.
     * 
     * @return The template file path
     */
    public String getTemplateFile() {
        return templateFile;
    }
    
    /**
     * Sets the template file path.
     * 
     * @param templateFile The template file path to set
     */
    public void setTemplateFile(String templateFile) {
        this.templateFile = templateFile;
    }
    
    /**
     * Gets the template variables map.
     * 
     * @return The template variables map
     */
    public Map<String, Object> getTemplateVariables() {
        return templateVariables;
    }
    
    /**
     * Sets the template variables map.
     * 
     * @param templateVariables The template variables map to set
     */
    public void setTemplateVariables(Map<String, Object> templateVariables) {
        this.templateVariables = templateVariables;
    }
}