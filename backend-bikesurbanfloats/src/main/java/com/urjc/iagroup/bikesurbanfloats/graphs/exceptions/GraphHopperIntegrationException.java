package com.urjc.iagroup.bikesurbanfloats.graphs.exceptions;
/**
 * It is used when there's an error managing the GraphHopperIntegration.
 * Its message describes which is the concret problem. 
 * @author IAgroup
 *
 */
public class GraphHopperIntegrationException extends Exception {
    

    private static final long serialVersionUID = 1L;

    public GraphHopperIntegrationException() { 
        super();
    }
      
    public GraphHopperIntegrationException(String message) {
          super(message); 
      }
      
    public GraphHopperIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
      
    public GraphHopperIntegrationException(Throwable cause) {
        super(cause);
    }
    
}
