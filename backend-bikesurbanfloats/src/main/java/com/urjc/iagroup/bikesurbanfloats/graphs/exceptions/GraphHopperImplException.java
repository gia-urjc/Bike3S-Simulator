package com.urjc.iagroup.bikesurbanfloats.graphs.exceptions;

public class GraphHopperImplException extends Exception {
	

	private static final long serialVersionUID = 1L;

	public GraphHopperImplException() { 
		  super(); 
	  }
	  
	  public GraphHopperImplException(String message) {
		  super(message); 
	  }
	  
	  public GraphHopperImplException(String message, Throwable cause) {
		  super(message, cause); 
	  }
	  
	  public GraphHopperImplException(Throwable cause) {
		  super(cause);   
	  }
	
}
