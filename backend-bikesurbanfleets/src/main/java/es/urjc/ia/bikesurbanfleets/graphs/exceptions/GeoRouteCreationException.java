package es.urjc.ia.bikesurbanfleets.graphs.exceptions;


/**
 * It is used when it isn't possible to create a route because less than 2 points have been give. 
 * @author IAgroup
 *
 */
public class GeoRouteCreationException extends Exception {

    private static final long serialVersionUID = 1L;

    public GeoRouteCreationException() {
          super(); 
      }

    public GeoRouteCreationException(String message) {
        super(message);
    }
}