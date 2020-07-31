package kMeans;

import java.io.*;
import java.util.*;

public class FastKMeans {
	
	Random r;
	
	String file; 
	BufferedReader in;
	BufferedWriter out; 
	
	int numOfClusters;
	int maxIter;
	double threshold;
	int batchSize; 
	
	Point temp[]; 
	Point points[]; 
	Point centers[];
	
	int numOfAttrib, numOfRows; 
	double sse = 0; 
	double lastSSE = Double.POSITIVE_INFINITY; 

	long runtime = 0; 
	long start = 0;
	long end = 0; 
	
	int lastIndex = 0; 
	
	public FastKMeans()
	{
		this.numOfClusters = 8;
		this.maxIter = 100;
		this.threshold = 0.001;
		this.file = "ecoli.txt"; 
		openFiles(); 
		readFile();
		centers = new Point[this.numOfClusters]; 
	}
	public FastKMeans(String file, int numOfClusters, int maxIter, double threshold)
	{
		this.numOfClusters = numOfClusters;
		this.maxIter = maxIter;
		this.threshold = threshold;
		this.file = file; 
		this.batchSize = 8; 
		openFiles();
		readFile();
		centers = new Point[this.numOfClusters]; 
	}
	
	public FastKMeans(String file, int numOfClusters, int maxIter, double threshold, int batchSize)
	{
		this.numOfClusters = numOfClusters;
		this.maxIter = maxIter;
		this.threshold = threshold;
		this.file = file; 
		this.batchSize = batchSize; 
		openFiles();
		readFile();
		centers = new Point[this.numOfClusters]; 
	}
	
	public void run()
	{
		start = System.currentTimeMillis(); 
		r = new Random(); 
		resetData(); 
		generateCenters();

		for(int i = 0; i < maxIter; i++)
		{
			getNewPoints();
			assignClusters();
			updateCenters();
			
			calculateSSE();
			end = System.currentTimeMillis() - start; 
			runtime += end; 
			
			System.out.println("SSE - Iteration " + i + ": " + sse);
			String s = "SSE - Iteration " + i + ": " + sse; 
			writeFile(s);
			
		}
		writeFile("Runtime: " + runtime + " ms"); 
		runtime = 0; 
		
	}
	
	public void getNewPoints()
	{
		for(int i = lastIndex; i < batchSize; i++)
		{
			int row = r.nextInt(numOfRows);
		
			while(temp[row].picked == true)
			{
				row = r.nextInt(numOfRows); 
			}
			
			for(int j = 0; j < temp[i].data.length; j++)
			{
				points[i].data[j] = temp[row].data[j];
				temp[row].picked = true; 
			}
			lastIndex = i; 
		}
	}
	
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
		
		for(int i = 0; i < temp.length; i++)
		{
			temp[i].clusterID = -1; 
			temp[i].picked = false; 
		}
		
		for(int i = 0; i < points.length; i++)
		{
			points[i] = new Point(numOfAttrib); 
			points[i].clusterID = -1; 
			points[i].picked = false; 
		}
		
		sse = 0;
		lastSSE = Double.POSITIVE_INFINITY;
		lastIndex = 0; 
	}
	
	public double getSSE()
	{
		return sse; 
	}
	
	public void generateCenters()
	{
		int row;
		
		for(int i = 0; i < centers.length; i++)
		{
			row = r.nextInt(numOfRows); 
			
			for(int j = 0; j < centers[i].data.length; j++)
			{
				centers[i].data[j] = temp[row].data[j];
			}
			centers[i].clusterID = i; 
		}
	}
	
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
	
	public void calculateSSE()
	{
		this.sse = 0;
		double distance = 0;
		
		for(int i = 0; i < points.length; i++)
		{
			for(int k = 0; k < points[i].data.length; k++)
			{
				distance += Math.pow(Math.abs(centers[points[i].clusterID].data[k] - points[i].data[k]) , 2);
			}
			distance = Math.sqrt(distance);
			
			sse += distance; 
			distance = 0; 
		}
	}
	
	public void pointsPerCluster()
	{
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
			System.out.println("Cluster " + i + ": " + numOfPoints);
		}
	}
	
	public void assignClusters()
	{
		double distance = 0; 
		double smallestDistance = Double.POSITIVE_INFINITY;  
		for(int i = 0; i < points.length; i++)
		{
			for(int j = 0; j < centers.length; j++)
			{
				for(int k = 0; k < points[i].data.length; k++)
				{
					//For every point, calculate the distance to each centroid
					//Take the smallest distance, that is the cluster ID.
					distance += Math.pow(Math.abs(centers[j].data[k] - points[i].data[k]) , 2);
				
				}
				distance = Math.sqrt(distance);
				
				if(distance < smallestDistance)
				{
					points[i].clusterID = centers[j].clusterID; 
					smallestDistance = distance;
				}
				distance = 0; 
			}
			smallestDistance = Double.POSITIVE_INFINITY;
		}
	}
	
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
	
	public void openFiles()
	{
		String outFile = this.file.substring(0, this.file.indexOf('.')); 
		try
		{
			out = new BufferedWriter(new FileWriter(outFile + "_Fast_out.txt"));
			in = new BufferedReader(new FileReader(this.file)); 
		}
		catch(IOException e)
		{
			System.err.println(e);
		}
	}
	
	public void closeFiles()
	{
		try
		{
			out.close(); 
			in.close();
		}
		catch(IOException e)
		{
			System.err.println(e);
		}
	}
	
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

				temp[row] = new Point(numOfAttrib); 
				
				for(int j = 0; j < d.length; j++)
				{
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
