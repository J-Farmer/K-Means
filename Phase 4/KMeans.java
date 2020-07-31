package kMeans;

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
	int idealClusters = 0; 
	
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
	long sStart = 0, sEnd = 0;
	long chStart = 0, chEnd = 0;
	long dbStart = 0, dbEnd = 0; 
	
	//Options
	String dataNorm; 
	String centerType;
	
	double daviesBouldinIndex;
	double silhouetteWidthIndex;
	double chIndex; 
	
	double jaccardIndex;
	double randIndex;
	double fowlkesMallowIndex;
	
	public KMeans()
	{
		this.numOfClusters = 8;
		this.maxIter = 100;
		this.threshold = 0.001;
		this.file = "landsat.txt"; 
		centers = new Point[this.numOfClusters]; 
		dataNorm = ""; 
		centerType = "randomselection";
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
		dataNorm = ""; 
		centerType = "randomselection";
		openFiles();
		readFile();
	}
	
	public KMeans(String file, int numOfClusters, int maxIter, double threshold, String norm, String centerType)
	{
		this.numOfClusters = numOfClusters;
		this.maxIter = maxIter;
		this.threshold = threshold;
		this.file = file; 
		centers = new Point[this.numOfClusters];
		dataNorm = norm; 
		this.centerType = centerType;
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
		
		dataStart = System.currentTimeMillis();
		setDataNorm(); 
		dataEnd = System.currentTimeMillis() - dataStart;  
		
		centerStart = System.currentTimeMillis();
		setCenterType();
		centerEnd = System.currentTimeMillis() - centerStart; 
		
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
			
			System.out.println("SSE - Iteration " + i + ": " + sse);
			
			writeFile("SSE - Iteration " + i + ": " + sse);
			
			numOfIter = i + 1; 
			
			if(checkThreshold())
				break;
		}
		
		confusionMatrix(); 
		
//		sStart = System.currentTimeMillis();
//		silhouetteWidthIndex();
//		sEnd = System.currentTimeMillis() - sStart;
//		writeFile("Silhouette Time: " + sEnd + "ms");
//		
//		chStart= System.currentTimeMillis();
//		calinskiHarabaszIndex();
//		chEnd = System.currentTimeMillis() - chStart;
//		writeFile("C.H. Time: " + chEnd + "ms");
//		
//		dbStart= System.currentTimeMillis();
//		daviesBouldinIndex();
//		dbEnd = System.currentTimeMillis() - dbStart;
//		writeFile("D.B. Time: " + dbEnd + "ms"); 
		
		runtime = 0; 
	}
	
	public void setCenterType()
	{
		if(centerType.toLowerCase().equals("randomselection") || centerType.toLowerCase().equals("") || centerType.toLowerCase().equals("none") || centerType.toLowerCase().equals("random selection"))
			randomSelectionCenters();
		else if(centerType.toLowerCase().equals("randompartition") || centerType.toLowerCase().equals("randompartitions") || centerType.toLowerCase().equals("random partition"))
			randomPartitionCenters();
		else if(centerType.toLowerCase().equals("maximin") || centerType.toLowerCase().equals("maxmin"))
			maximinCenters();
		else
		{
			System.err.println("Invalid Center Selection Type: " + centerType);
			System.exit(-1);
		}
	}
	
	public void setDataNorm()
	{
		if(dataNorm.toLowerCase().equals("zscore") || dataNorm.toLowerCase().equals("z-score"))
			zScoreNorm(); 
		else if(dataNorm.toLowerCase().equals("minmax") || dataNorm.toLowerCase().equals("min-max"))
			minMaxNorm();
		else if(dataNorm.toLowerCase().equals("") || dataNorm.toLowerCase().equals("none") || dataNorm.toLowerCase().equals("default"))
			;
		else
		{
			System.err.println("Invalid Data Scaling Type: " + dataNorm);
			System.exit(-1);
		}
	}
	
	public void confusionMatrix()
	{		
		int[][] matrix = new int [this.numOfClusters][this.numOfClusters];
		
		int truePositive = 0, falsePositive = 0, falseNegative = 0, trueNegative = 0;
		
		for(int p = 0; p < points.length - 1; p++)
		{
			for(int j = p + 1; j < points.length; j++)
			{
				if(points[p].clusterID == points[j].clusterID && points[p].idealLabel == points[j].idealLabel)
					truePositive++;
				else if(points[p].clusterID == points[j].clusterID && points[p].idealLabel != points[j].idealLabel)
					falsePositive++;
				else if(points[p].clusterID != points[j].clusterID && points[p].idealLabel == points[j].idealLabel)
					falseNegative++;
				else
					trueNegative++;	
			}
		}
		
		for(int i = 0; i < points.length; i++)
		{
			matrix[points[i].clusterID][points[i].idealLabel]++;
		}
		
		for(int i = 0; i < this.numOfClusters; i++)
		{
			for(int j = 0; j < this.numOfClusters; j++)
			{
				System.out.print(matrix[i][j] + "\t");
			}
			System.out.println();
		}
		
		System.out.println("True Positive: " + truePositive);
		System.out.println("True Negative: " + trueNegative);
		System.out.println("False Positive: " + falsePositive);
		System.out.println("False Negative: " + falseNegative);
		
		jaccardIndex(truePositive, falsePositive, falseNegative); 
		randIndex(truePositive, trueNegative, falsePositive, falseNegative);
		fowlkesMallowsScore(truePositive, falsePositive, falseNegative);
	}
	
	public void jaccardIndex(int tp, int fp, int fn)
	{
		double ji = tp / (double)(tp + fn + fp);
		
		this.jaccardIndex = ji; 
		
		System.out.println("Jaccard Index: " + ji);
	}
	
	public void randIndex(int tp, int tn, int fp, int fn)
	{
		double randIndex = (tp + tn) / (double)(tp + fp + fn + tn);
		
		this.randIndex = randIndex; 
		
		System.out.println("Rand Index: " + randIndex);
	}
	
	public void fowlkesMallowsScore(int tp, int fp, int fn)
	{
		double precision = tp / (double)(tp + fp);
		double recall = tp / (double)(tp + fn);  
		
		double fowlkesMallowsScore = Math.sqrt(precision * recall); 
		
		this.fowlkesMallowIndex = fowlkesMallowsScore; 
		
		System.out.println("Fowlkes-Mallows Score: " + fowlkesMallowsScore + "\n");
	}
	
	
	public void silhouetteWidthIndex()
	{
		double innerMean = 0, outerMean = 0, silhouetteWidth = 0;
		int numOfPoints = 0, nextCluster = 0;
		
		for(int clusters = 0; clusters < centers.length; clusters++)
		{
			for(int point = 0; point < points.length; point++)
			{
				points[point].distanceToCenter[clusters] = distance(points[point], centers[clusters]); 
			}
		}
		
		//for every point, calculate the silhouette coefficient
		for(int i = 0; i < points.length; i++)
		{
			//inner mean
			for(int j = 0; j < points.length; j++)
			{
				if(points[j].clusterID == points[i].clusterID)
				{
					innerMean += distance(points[i], points[j]);
					numOfPoints++; 
				}
			}
			innerMean /= numOfPoints; 
			
			numOfPoints = 0;
			
			double minDistance = Double.MAX_VALUE; 
			//find next closest center...
			for(int k = 0; k < points[i].distanceToCenter.length; k++)
			{
				nextCluster = 0; 
				
				if((points[i].distanceToCenter[k] < minDistance) && k != points[i].clusterID)
				{
					minDistance = points[i].distanceToCenter[k];
					nextCluster = k;
				}
			}
			
			//outer mean
			for(int j = 0; j < points.length; j++)
			{
				//Cluster ID should equal to the next closest cluster...
				if((points[j].clusterID == nextCluster))
				{
					outerMean += distance(points[i], points[j]);
					numOfPoints++; 
				}
			}
			outerMean /= numOfPoints;
			
			
			silhouetteWidth += (outerMean - innerMean) / Math.max(outerMean, innerMean);
			
			numOfPoints = 0;
			outerMean = 0;
			innerMean = 0;
			  
		}
		
		silhouetteWidth /= this.numOfRows;
		
		System.out.println("Silhouette Width Index: " + silhouetteWidth + "\n"); 
		
		this.silhouetteWidthIndex = silhouetteWidth; 	
	}
	
	public void calinskiHarabaszIndex()
	{
		double chIndex = 0; 
		double withinCluster = 0, betweenCluster = 0;
		
		int[] pointsPerCluster = pointsPerCluster(false);  
		
		//This is the mean of all centers
		Point centerMean = new Point(this.numOfAttrib); 
		
		for(int c = 0; c < centerMean.data.length; c++)
		{
			centerMean.data[c] = 0; 
		}
		
		//compute the within-cluster scatter (is just the sse of the final iteration)
		withinCluster = this.sse;
		
		//compute the mean of all cluster centers.
		for(int i = 0; i < numOfClusters; i++)
		{
			for(int j = 0; j < centerMean.data.length; j++)
			{
				centerMean.data[j] += centers[i].data[j];
			}
		}
		
		for(int i = 0; i < centerMean.data.length; i++)
		{
			centerMean.data[i] /= this.numOfClusters; 
		}
		
		//Compute the SSE for the centers.
		for(int i = 0; i < this.numOfClusters; i++)
		{
			betweenCluster += pointsPerCluster[i] * distance(centerMean, centers[i]); 
		}
		
		chIndex = (betweenCluster / withinCluster) * ((this.numOfRows - this.numOfClusters) / (this.numOfClusters - 1));
		
		System.out.println("Calinski-Harabasz Index: " + chIndex + "\n"); 
		
		this.chIndex = chIndex; 
	}
	
	public void daviesBouldinIndex()
	{
		double[] meanDistance = new double[this.numOfClusters]; 
		double separations = Double.MIN_VALUE;
		double lastValue = 0, daviesBouldinIndex = 0;
		
		int[] pointsPerCluster = pointsPerCluster(false);  
		
		//calculate mean distance of points belonging to the cluster (within group scatter)
		
		for(int i = 0; i < this.numOfClusters; i++)
		{
			for(int j = 0; j<this.numOfRows; j++)
			{
				if(points[j].clusterID == i)
				{
					meanDistance[i] = Math.sqrt(distance(points[j], centers[i]));
				}
			}
			
			meanDistance[i] /= pointsPerCluster[i];
		}
		
		//Calculate the separation between two clusters
		
		for(int i = 0; i < this.numOfClusters; i++)
		{
			for(int j = 0; j < this.numOfClusters; j++)
			{
				if(i == j)
					continue; 
				else
				{
					separations = (meanDistance[i] + meanDistance[j]) / Math.sqrt(distance(centers[i], centers[j]));
					
					if(separations > lastValue)
					{
						lastValue = separations; 
					}
				}	
			}
			
			daviesBouldinIndex += lastValue; 
		}
		
		daviesBouldinIndex /= this.numOfClusters; 
		
		System.out.println("Davies-Bouldin Index: " + daviesBouldinIndex);
		
		this.daviesBouldinIndex = daviesBouldinIndex; 
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
			
			writeFile("Data Normilization Type: " + dataNorm); 
			writeFile("Data Normalization: " + dataEnd + " ms");
			writeFile("Center Selection Type: " + centerType); 
			writeFile("Center Selection: " + centerEnd + " ms");
			writeFile("Clustering Runtime: " + runtime + " ms");
			writeFile("Total Runtime: " + (runtime + dataEnd + centerEnd) + " ms"); 
			return true;
		}
		lastSSE = sse;
		return false;
	}
	
	//This function normalizes data to a 0 - 1 range. 
	public void minMaxNorm()
	{
		//For each attribute, compute the minimum and maximum, 
		//subtract the min feature from the current feature, and divide by (max - min)
		double min[] = new double[numOfAttrib];
		double max[] = new double[numOfAttrib]; 
		
		for(int i = 0; i < numOfAttrib; i++)
		{
			min[i] = Double.MAX_VALUE;
			max[i] = Double.MIN_VALUE;
		}
		
		for(int i = 0; i < points[i].data.length; i++)
		{
			for(int j = 0; j < points.length; j++)
			{
				min[i] = Math.min(min[i], points[j].data[i]);
				max[i] = Math.max(max[i], points[j].data[i]);
			}
		}
		
		for(int i = 0; i < points.length; i++)
		{
			for(int j = 0; j < points[i].data.length; j++)
			{
				if(max[j] != min[j])
				{
					points[i].data[j] = (points[i].data[j] - min[j]) / (max[j] - min[j]);
					
				}
				else
					 points[i].data[j] = 0;
			}
		}
	}
	
	//This function computes the normalized Z-Score for the data.
	public void zScoreNorm()
	{
		//compute the mean and std dev of each attribute,
		//subtract the mean from the value of the feature
		//and divide by the std dev. 
		double mean[] = new double[numOfAttrib]; 
		double stdDev[] = new double[numOfAttrib]; 
		
		for(int j = 0; j < points.length; j++)
		{
			for(int k = 0; k < points[j].data.length; k++)
			{
				mean[k] += points[j].data[k];
			}
		}
		for(int k = 0; k < mean.length; k++)
		{
			mean[k] = mean[k] / numOfRows;
		}
		
		for(int j = 0; j < points.length; j++)
		{
			for(int k = 0; k < points[j].data.length; k++)
			{
				stdDev[k] += (points[j].data[k] - mean[k]) * (points[j].data[k] - mean[k]); 
			}
		}
		
		for(int i = 0; i < stdDev.length; i++)
		{
			stdDev[i] = Math.sqrt(stdDev[i] / (numOfRows  - 1));
		}
				
		for(int i = 0; i < points.length; i++)
		{
			for(int j = 0; j < points[i].data.length; j++)
			{
				if(stdDev[j] == 0)
				{
					points[i].data[j] = 0;
				}
				else
					points[i].data[j] = ((points[i].data[j] - mean[j]) / stdDev[j]);
			}
		}
		System.out.println();
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
	
	//Random partition center selection. 
	public void randomPartitionCenters()
	{
		int numOfPoints = 0; 
		double[] temp= new double[numOfAttrib]; 
		
		for(int i = 0; i < points.length; i++)
		{
			points[i].clusterID = r.nextInt(numOfClusters);
		}
		
		for(int i = 0; i < centers.length; i++)
		{
			centers[i].clusterID = i; 
		}
				
		for(int i = 0; i < centers.length; i++)
		{	
			for(int k = 0; k < temp.length; k++)
			{
				temp[k] = 0; 
			}
			
			for(int j = 0; j < points.length; j++)
			{
				if(points[j].clusterID == centers[i].clusterID)
				{
					for(int k = 0; k < points[j].data.length; k++)
					{
						temp[k] += points[j].data[k];
					}
					
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
	
	//Find greatest point from all centers, . 
	public void maximinCenters()
	{
		double lastMinDistance = Double.MIN_VALUE;
		int index = 0;
		int row = r.nextInt(numOfRows);
		
		//Get a random center.
		for(int i = 0; i < points[0].data.length; i++)
		{
			centers[0].data[i] = points[row].data[i];
		}
		centers[0].clusterID = 0;
		
		//For every point, calculate the distance to every center.
		//Take the minimum of this (find the closest center).
		//Take the maximum of that list of values as your new kth center...
		
		for(int k = 1; k < centers.length; k++)
		{
			//This finds the distance from each point to each center. 
			for(int i = 0; i < points.length; i++)
			{
				points[i].distanceToCenter[k-1] = distance(points[i], centers[k-1]);
				
				if(points[i].distanceToCenter[k-1] < points[i].minimumDistance)
				{
					points[i].minimumDistance = points[i].distanceToCenter[k-1]; 
				}
				
				if(points[i].minimumDistance > lastMinDistance)
				{
					index = i;
					lastMinDistance = points[i].minimumDistance;
				}
			}
			
			//This sets the new center
			for(int i = 0; i < centers[0].data.length; i++)
			{
				centers[k].data[i] = points[index].data[i];
			}
			centers[k].clusterID = k;

			lastMinDistance = Double.MIN_VALUE; 
			index = 0;
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
		String outFile = this.file.substring(0, this.file.indexOf('.')); 
		try
		{
			File f = new File(outFile); 
			if(!f.exists())
				f.mkdir(); 
			out = new BufferedWriter(new FileWriter(outFile + "_"+ numOfClusters + "_" + dataNorm + "_" + centerType + "_out.txt"));
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
			
			String[] s = line.split("\\s+");
			
			if(s[0].equals(""))
			{
				for(int i = 0; i < s.length - 1; i++)
				{
					s[i] = s[i+1]; 
				}
			}
			
			numOfRows = Integer.parseInt(s[0]); 
			numOfAttrib = (Integer.parseInt(s[1]) - 1); 
			idealClusters = Integer.parseInt(s[2]);
			
			points = new Point[numOfRows]; 
			temp = new Point[numOfRows]; 
			
			int row = 0;
			
			while((line = in.readLine()) != null)
			{
				String[] d = line.split("\\s+");
				
				if(d[0].equals(""))
				{
					for(int i = 0; i < d.length - 2; i++)
					{
						d[i] = d[i+1]; 
					}
					
				}
				
				points[row] = new Point(numOfAttrib);
				points[row].distanceToCenter = new double[numOfClusters]; 
				points[row].idealLabel = Integer.parseInt(d[d.length - 1]); 
				
				for(int i = 0; i < numOfClusters; i++)
				{
					points[row].distanceToCenter[i] = Double.MAX_VALUE; 
					points[row].minimumDistance = Double.MAX_VALUE; 
				}
				
				temp[row] = new Point(numOfAttrib);
				
				for(int j = 0; j < numOfAttrib; j++)
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
