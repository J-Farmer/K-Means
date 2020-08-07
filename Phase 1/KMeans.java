import java.io.*;
import java.util.*;

public class KMeans {
		
	Random r;
	
	int numOfClusters;
	int maxIter;
	double threshold;
	
	int numOfAttrib, numOfRows; 
	double sse = 0;
	double initialSSE = 0; 
	double lastSSE = Double.POSITIVE_INFINITY; 
	int numOfIter = 0; 
	
	Point points[]; 
	Point temp[]; //This is used for resetting the data between runs. 
	Point centers[];
		
	//Files
	String file;
	BufferedReader in;
	BufferedWriter out; 
	
	//Timing
	long runtime = 0; 
	long start = 0;
	long end = 0; 
	long dataStart = 0, dataEnd = 0;
	long centerStart = 0, centerEnd = 0; 
	
	public KMeans()
	{
		this.numOfClusters = 8;
		this.maxIter = 100;
		this.threshold = 0.001;
		this.file = "landsat.txt"; 
		centers = new Point[this.numOfClusters];  
		//dataNorm = ""; 
		//centerType = "randomselection";
		openFiles(); 
		readFile();
	}
	
	public KMeans(String file, int numOfClusters, int maxIter, double threshold)
	{
		this.numOfClusters = numOfClusters;
		this.maxIter = maxIter;
		this.threshold = threshold;
		this.file = file; 
		centers = new Point[this.numOfClusters];
		openFiles();
		readFile();
	}
	
	//This is the main function that takes care of the iterative
	//running of the K-Means Algorithm.
	//It will, if needed, reset and normalize the data, 
	//and choose the initial centers.
	public void run()
	{
		r = new Random();
		resetData();
		
		randomSelectionCenters();
		
		start = System.currentTimeMillis(); 
		for(int i = 0; i < maxIter; i++)
		{
			
			
			assignClusters();

			updateCenters();
			
			calculateSSE();
			
			if(i == 0)
				initialSSE = sse; 
			
			end = System.currentTimeMillis() - start;
			runtime += end; 
			
			System.out.println("SSE - Iteration " + (i + 1) + ": " + sse);
			
			writeFile("SSE - Iteration " + (i + 1) + ": " + sse);
			
			numOfIter = i + 1; 
			
			if(checkThreshold())
				break;
		}
		
		
		runtime = 0; 
	}
	
	//This function checks the threshold for terminating the program.
	//It will check the percent change in the SSE from the last to current run. 
	public boolean checkThreshold()
	{
		if(((lastSSE - sse) / lastSSE) < threshold)
		{
			System.out.println("Final SSE: " + sse + '\n');
			
			writeFile("Initial SSE: " + initialSSE); 
			writeFile("Final SSE: " + sse); 
			writeFile(""); 
			
			//writeFile("Data Normilization Type: " + dataNorm); 
			//writeFile("Data Normalization: " + dataEnd + " ms");
			//writeFile("Center Selection Type: " + centerType); 
			//writeFile("Center Selection: " + centerEnd + " ms");
			writeFile("Clustering Runtime: " + runtime + " ms");
			writeFile("Total Runtime: " + (runtime + dataEnd + centerEnd) + " ms"); 
			return true;
		}
		lastSSE = sse;
		return false;
	}
		
	//This is a utility function.
	//Reset the data in between runs. 
	public void resetData()
	{
		for(int i = 0; i < centers.length; i++)
		{
			centers[i] = new Point(numOfAttrib); 
			for(int k = 0; k < centers[i].data.length; k++)
			{
				centers[i].data[k] = 0;
				centers[i].clusterID = -1; 
			}
		}
		
		for(int i = 0; i < points.length; i++)
		{
			for(int j = 0; j < points[i].data.length; j++)
			{
				points[i].data[j] = temp[i].data[j]; 
			}
			points[i].clusterID = -1; 
			points[i].minimumDistance = Double.MAX_VALUE;
			
			for(int k = 0; k < points[i].distanceToCenter.length; k++)
			{
				points[i].distanceToCenter[k] = Double.MAX_VALUE; 
			}
		}
		
		sse = 0;
		lastSSE = Double.POSITIVE_INFINITY;
	}
	
	//Returns the SSE. 
	public double getSSE()
	{
		return sse; 
	}
	
	//Randomly selects centers from the data.
	public void randomSelectionCenters()
	{
		int row;
		for(int i = 0; i < centers.length; i++)
		{
			row = r.nextInt(numOfRows);
			for(int j = 0; j < centers[i].data.length; j++)
			{
				centers[i].data[j] = points[row].data[j];
			}
			centers[i].clusterID = i;
		}
	}
		
	//Finds the distance between two points
	//NOTE: This ignores the square root for the sake of speed.
	//SSE calculation negates square root. 
	public double distance(Point p, Point c)
	{
		double distance = 0; 
		for(int i = 0; i< p.data.length; i++)
		{
			distance += Math.abs(c.data[i] - p.data[i]) * Math.abs(c.data[i] - p.data[i]);
		}
		return distance;
	}
	
	//Calculates the SSE per iteration.
	public void calculateSSE()
	{
		this.sse = 0;
				
		for(int i = 0; i < points.length; i++)
		{
			sse += distance(points[i], centers[points[i].clusterID]);
		}
	}
	
	//Assigns the points to a cluster.
	//Based on Euclidean Distance to every center.
	public void assignClusters()
	{
		double distance = 0; 
		double smallestDistance = Double.MAX_VALUE;
		for(int i = 0; i < points.length; i++)
		{
			for(int j = 0; j < centers.length; j++)
			{
				distance = distance(centers[j], points[i]);
				
				if(distance < smallestDistance)
				{
					points[i].clusterID = centers[j].clusterID; 
					smallestDistance = distance;
				}
				distance = 0; 
			}
			smallestDistance = Double.MAX_VALUE;
		}
	}
	
	//Computes the mean of points in cluster.
	//Moves center to geometric mean of cluster.
	public void updateCenters()
	{
		int numOfPoints = 0; 
		double[] temp; 
		
		temp = new double[numOfAttrib]; 
				
		for(int i = 0; i < centers.length; i++)
		{	
			for(int k = 0; k < temp.length; k++)
			{
				temp[k] = 0;
			}
			
			for(int j = 0; j < points.length; j++)
			{
				//For each cluster, calculate the geometric mean of the points (d1, d2, ..., dn) in cluster.
				//Then, move the centroid to that location. 
				if(points[j].clusterID == centers[i].clusterID)
				{
					for(int k = 0; k < points[j].data.length; k++)
						temp[k] += points[j].data[k];
					
					numOfPoints++; 
				}
			}
			for(int k = 0; k < centers[i].data.length; k++)
			{
				centers[i].data[k] = (temp[k] / numOfPoints);
			}
			
			numOfPoints = 0; 
		}
	}
	
	//This will print all points in each cluster.
	//If needed, should be called per iteration.
	public void printClusters()
	{
		for(int i = 0; i < centers.length; i++)
		{
			System.out.println("Cluster " + i);
			for(int j = 0; j < points.length; j++)
			{	
				if(points[j].clusterID == i)
				{
					for(int k = 0; k < points[j].data.length; k++)
						System.out.print(points[j].data[k] + " "); 	
				}
				else
					continue; 
				System.out.println(); 
			}
			System.out.println("--------------------------\n");
		}
	}
	
	//This will give the number of points per cluster.
	public int[] pointsPerCluster(boolean print)
	{
		int pointNum[] = new int[this.numOfClusters]; 
		for(int i = 0; i < centers.length; i++)
		{
			int numOfPoints = 0;
			for(int k = 0; k < points.length; k++)
			{
				if(points[k].clusterID == centers[i].clusterID)
				{
					numOfPoints++; 
				}
			}
			if(print)
				System.out.println("Cluster " + i + ": " + numOfPoints);
			pointNum[i] = numOfPoints; 
		}
		
		return pointNum; 
	}
	
	//This will print the individual centers of the clusters. 
	public void printCenters()
	{
		for(int i = 0; i < centers.length; i++)
		{ 
			System.out.print("Center " + centers[i].clusterID  + ": ");
			for(int j = 0; j < centers[i].data.length; j++)
			{
				System.out.print(centers[i].data[j] + " "); 
			}
			System.out.println();
		}
		System.out.println();
	}
	
	//This will print the data that is read from the file.
	//Can also print normalized data. 
	public void printData()
	{
		for(int i = 0; i < points.length; i++)
		{
			for(int j = 0; j < points[i].data.length; j++)
			{
				System.out.print(points[i].data[j] + " ");
			}
			System.out.println();
		}
	}
	
	//Opens the files for reading/writing.
	//Creates a directory for organization. 
	public void openFiles()
	{
		int startIndex = this.file.indexOf("\\")+1;
		String outFile = this.file.substring(startIndex, this.file.indexOf('.'));
		
		String filePath = "Results\\";
		
		try
		{
			File f = new File(filePath+outFile);
			if(!f.exists())
				f.mkdirs();
			out = new BufferedWriter(new FileWriter(filePath + outFile + "\\" + outFile +  "_out.txt"));
			in = new BufferedReader(new FileReader(this.file));
		}
		catch(IOException e)
		{
			System.err.println("Error on File Open: " + e);
		}
	}
	
	//Closes the files at the end of the program.
	public void closeFiles()
	{
		try
		{
			out.close(); 
			in.close();
		}
		catch(IOException e)
		{
			System.err.println("Error on File Close: " + e);
		}
	}
	
	//Reads the data in from the file into an array.
	public void readFile()
	{	
		try
		{
			String line = null;
			line = in.readLine();
			
			String[] s = line.split(" ");
			
			numOfRows = Integer.parseInt(s[0]);
			numOfAttrib = Integer.parseInt(s[1]);
			
			points = new Point[numOfRows];
			temp = new Point[numOfRows];
			
			int row = 0;
			
			while((line = in.readLine()) != null)
			{
				String[] d = line.split(" ");

				points[row] = new Point(numOfAttrib);
				points[row].distanceToCenter = new double[numOfClusters];
				
				for(int i = 0; i < numOfClusters; i++)
				{
					points[row].distanceToCenter[i] = Double.MAX_VALUE;
					points[row].minimumDistance = Double.MAX_VALUE;
				}
				
				temp[row] = new Point(numOfAttrib);
				
				for(int j = 0; j < d.length; j++)
				{
					points[row].data[j] = Double.parseDouble(d[j]);
					temp[row].data[j] = Double.parseDouble(d[j]);
				}
				row++;
			}
		}
	
		catch(IOException e)
		{
			 System.err.println("Error on Input: " + e);
		}
	}
	
	//Writes a line to the file, including the newline.  
	public void writeFile(String line)
	{	
		try
		{
			out.write(line + '\n');
			out.flush();
		}
		catch(IOException e)
		{
			 System.err.println("Error on Output: " + e);
		}
	}	

}
