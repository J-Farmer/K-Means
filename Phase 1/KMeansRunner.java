public class KMeansRunner {

	public static void main(String[] args)
	{
		double SSE = 0, lowestSSE = Double.POSITIVE_INFINITY, lowestInitSSE = Double.POSITIVE_INFINITY;
		int SSErunIndex = Integer.MAX_VALUE, maxIter = Integer.MAX_VALUE, initSSErunIndex = Integer.MAX_VALUE, iterRunIndex = Integer.MAX_VALUE;
		KMeans km = new KMeans("..\\Data Sets\\"+args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]) , Double.parseDouble(args[3]));//, args[5], args[6]); 

		for (int i = 0; i < 100; i++)
		{
			km.writeFile("Run: " + (i+1));
			km.run();
			SSE = km.getSSE(); 

			if(SSE < lowestSSE)
			{
				lowestSSE = SSE;
				SSErunIndex = i + 1; 
			}

			if(km.initialSSE < lowestInitSSE)
			{
				lowestInitSSE = km.initialSSE;
				initSSErunIndex = i + 1; 
			}

			if(km.numOfIter < maxIter)
			{
				maxIter = km.numOfIter;
				iterRunIndex = i + 1; 
			}
			km.writeFile("\n");
		}
		km.writeFile("Best Initial SSE: " + lowestInitSSE + " (Run " + SSErunIndex + ")");
		km.writeFile("Best SSE: " + lowestSSE + " (Run " + initSSErunIndex + ")");
		km.writeFile("Best Number of Iterations: " + maxIter + " (Run " + iterRunIndex + ")");

		km.closeFiles();
	}
}


