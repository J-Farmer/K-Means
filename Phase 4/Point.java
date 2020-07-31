package kMeans;

class Point
{
	double[] data; 
	int clusterID;
	boolean picked; 
	double[] distanceToCenter;
	double minimumDistance; 
	double silhouetteCoef; 
	int idealLabel; 
	
	public Point(int numOfAttrib)
	{
		data = new double[numOfAttrib]; 
		clusterID = -1;
		picked = false;
		silhouetteCoef = Double.NaN; 
		idealLabel = Integer.MIN_VALUE; 
	}
}