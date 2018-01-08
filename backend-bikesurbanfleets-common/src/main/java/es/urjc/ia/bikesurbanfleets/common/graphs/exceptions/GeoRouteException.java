package es.urjc.ia.bikesurbanfleets.common.graphs.exceptions;

/**
 * It is used when there's an error managing the route.
 * Its message describes which is the concret problem.
 * @author IAgroup
 *
 */
public class GeoRouteException extends Exception {
    

    private static final long serialVersionUID = 1L;

    public GeoRouteException() { 
        super();
    }
      
    public GeoRouteException(String message) {
        super(message);
    }

    public GeoRouteException(String message, Throwable cause) {
        super(message, cause);
    }
      
    public GeoRouteException(Throwable cause) {
        super(cause);
    }
    
}