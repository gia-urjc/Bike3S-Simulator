package com.urjc.iagroup.bikesurbanfloats.util;

import java.awt.geom.Rectangle2D;

import com.urjc.iagroup.bikesurbanfloats.config.SystemInfo;

public class BoundingBox {
	
	private Rectangle2D.Double rectangle;
	private RandomUtil random;
	
	public BoundingBox(RandomUtil random) {
		this.rectangle = new Rectangle2D.Double();
		this.random = random;
	}
	
	public BoundingBox(GeoPoint position, double lengthLongitude, double lengthLatitude) {
		double x = position.getLongitude();
		double y = position.getLatitude();
		double width = lengthLongitude;
		double height = lengthLatitude;
		this.rectangle = new Rectangle2D.Double(x, y, width, height);
	}
	
	public GeoPoint getPosition() {
		return new GeoPoint(rectangle.getY(), rectangle.getX());
	}
	
	public double getLengthLongitude() {
		return this.rectangle.getWidth();
	}
	
	public double getLengthLatitude() {
		return this.rectangle.getHeight();
	}
	
	public void setPosition(GeoPoint position) {
		this.rectangle.x = position.getLongitude();
		this.rectangle.y = position.getLatitude();
	}
	 
	public GeoPoint randomPoint() {
		double x = random.nextDouble(rectangle.getX(), rectangle.getX() + rectangle.getWidth());
		double y = random.nextDouble(rectangle.getY(), rectangle.getY() + rectangle.getHeight());
		return new GeoPoint(y, x);
	}

}
