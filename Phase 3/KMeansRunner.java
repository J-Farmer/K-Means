package kMeans;

public class KMeansRunner {

	public static void main(String[] args) 
	{

		double SSE = 0, lowestSSE = Double.POSITIVE_INFINITY, lowestInitSSE = Double.POSITIVE_INFINITY;
		int runIndex = Integer.MAX_VALUE, maxIter = Integer.MAX_VALUE;
		//KMeans km = new KMeans(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]), args[5], args[6]); 

		for(int numOfClusters = 2; numOfClusters < Integer.parseInt(args[1]); numOfClusters++)
		{
			double daviesBouldin = Double.MAX_VALUE, silhouette = Double.MIN_VALUE, ch = Double.MIN_VALUE; 
			KMeans km = new KMeans(args[0], numOfClusters, 100, 0.001, "minmax", "maximin"); 

			km.writeFile("Number of Clusters: " + numOfClusters);

			for (int i = 0; i < 100; i++)
			{
				km.writeFile("Run: " + (i+1));
				km.run();
				SSE = km.getSSE(); 

				if(SSE < lowestSSE)
				{
					lowestSSE = SSE;
					runIndex = i + 1; 
				}

				if(km.initialSSE < lowestInitSSE)
				{
					lowestInitSSE = km.initialSSE;
					runIndex = i + 1; 
				}

				if(km.numOfIter < maxIter)
				{
					maxIter = km.numOfIter;
					runIndex = i + 1; 
				}

				if(km.silhouetteWidthIndex > silhouette)
				{
					silhouette = km.silhouetteWidthIndex;
					runIndex = i + 1; 
				}

				if(km.daviesBouldinIndex < daviesBouldin)
				{
					daviesBouldin = km.daviesBouldinIndex;
					runIndex = i + 1; 
				}

				if(km.chIndex > ch)
				{
					ch = km.chIndex; 
					runIndex = i + 1; 
				}

				km.writeFile("\n");
			}
			km.writeFile("Best Initial SSE: " + lowestInitSSE + "(Run " + runIndex + ")");
			km.writeFile("Best SSE: " + lowestSSE + "(Run " + runIndex + ")");
			km.writeFile("Best Number of Iterations: " + maxIter);
			km.writeFile("Silhouette Width Index: " + silhouette);
			km.writeFile("Calinski-Harabaz Index: " + ch);
			km.writeFile("Davies-Bouldin Index: " + daviesBouldin);
			km.closeFiles();
		}
	}

}
