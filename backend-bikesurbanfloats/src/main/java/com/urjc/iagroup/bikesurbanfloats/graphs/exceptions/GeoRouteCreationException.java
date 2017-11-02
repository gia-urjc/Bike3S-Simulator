package com.urjc.iagroup.bikesurbanfloats.graphs.exceptions;

public class GeoRouteCreationException extends Exception {
	

	private static final long serialVersionUID = 1L;

	public GeoRouteCreationException() { 
		  super(); 
	  }
	  
	  public GeoRouteCreationException(String message) {
		  super(message); 
	  }
	  
	  public GeoRouteCreationException(String message, Throwable cause) {
		  super(message, cause); 
	  }
	  
	  public GeoRouteCreationException(Throwable cause) {
		  super(cause);   
	  }
	
}