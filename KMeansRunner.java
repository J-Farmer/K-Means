package kMeans;

public class KMeansRunner {

	public static void main(String[] args) 
	{

		double SSE = 0, lowestSSE = Double.POSITIVE_INFINITY, lowestInitSSE = Double.POSITIVE_INFINITY, fmScore = Double.NEGATIVE_INFINITY, jI = Double.NEGATIVE_INFINITY, rI = Double.NEGATIVE_INFINITY;
		int runIndex = Integer.MAX_VALUE, maxIter = Integer.MAX_VALUE;
		
		KMeans km;
		
		if(args.length == 6)
		{
			km = new KMeans(("phase4_data_sets\\" + args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]), args[5], args[6]);
		}
		else
		{
			km = new KMeans(("phase4_data_sets\\" + args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), Double.parseDouble(args[3]));
		}

		for (int i = 0; i < Integer.parseInt(args[4]); i++)
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
			
			//////////////////////////////////////////////////////
			
			if(km.fowlkesMallowIndex > fmScore)
			{
				fmScore = km.fowlkesMallowIndex;
				runIndex = i + 1; 
			}
			
			if(km.randIndex > rI)
			{
				rI = km.randIndex;
				runIndex = i + 1; 
			}
			
			if(km.jaccardIndex > jI)
			{
				jI = km.jaccardIndex;
				runIndex = i + 1; 
			}

			km.writeFile("\n");
		}
		
		km.writeFile("Best Initial SSE: " + lowestInitSSE + "(Run " + runIndex + ")");
		km.writeFile("Best SSE: " + lowestSSE + "(Run " + runIndex + ")");
		km.writeFile("Best Number of Iterations: " + maxIter);
		km.writeFile("Best Jaccard Index: " + jI + "(Run " + runIndex + ")");
		km.writeFile("Best Rand Index: " + rI + "(Run " + runIndex + ")");
		km.writeFile("Best Fowlkes-Mallows Index: " + fmScore);
		
		System.out.println("Best Jaccard Index: " + jI + "(Run " + runIndex + ")");
		System.out.println("Best Rand Index: " + rI + "(Run " + runIndex + ")");
		System.out.println("Best Fowlkes-Mallows Index: " + fmScore + "(Run " + runIndex + ")");
		
		km.closeFiles();
	}
}


