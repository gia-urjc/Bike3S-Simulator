package es.urjc.ia.bikesurbanfleets.common.matrices;

public class MatrixRoute {
    
    private int idOrigin;
    private int idDestination;

    public MatrixRoute() {

    }

    public MatrixRoute(int idOrigin, int idDestination) {
        this.idOrigin = idOrigin;
        this.idDestination = idDestination;
    }

    public int getIdOrigin() {
        return this.idOrigin;
    }

    public int getIdDestination() {
        return this.idDestination;
    }

    @Override
    public String toString() {
        return "{" +
            " idOrigin='" + getIdOrigin() + "'" +
            ", idDestination='" + getIdDestination() + "'" +
            "}";
    }

}