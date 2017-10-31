package com.urjc.iagroup.bikesurbanfloats.graphs.exceptions;

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