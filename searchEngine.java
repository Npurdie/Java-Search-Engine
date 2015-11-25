import java.util.*;
import java.io.*;

// This class implements a google-like search engine
public class searchEngine {
	// this will contain a set of pairs (String, LinkedList of Strings)
    public HashMap<String,LinkedList<String>> wordIndex;		
    public directedGraph internet;             // this is our internet graph
    
    // Constructor initializes everything to empty data structures
    // It also sets the location of the internet files
    searchEngine() {
	// Below is the directory that contains all the internet files
	htmlParsing.internetFilesLocation = "internetFiles";
	wordIndex = new HashMap<String, LinkedList<String> > ();		
	internet = new directedGraph();				
    }
    
    // Returns a String description of a searchEngine
    public String toString () {
	return "wordIndex:\n" + wordIndex + "\ninternet:\n" + internet;
    }
    
    // This does a graph traversal of the internet, starting at the given url.
    // For each new vertex seen, it updates the wordIndex, the internet graph,
    // and the set of visited vertices.
    void traverseInternet(String url) throws Exception {
    	internet.addVertex(url);		//add vertex to map
    	internet.setVisited(url,true);	//avoids DDOS by only visiting a webpage once
    		
    	LinkedList<String> content = htmlParsing.getContent(url);	//parse the current url and put it in the word index hashmap
    	Iterator<String> itr1 = content.iterator();

    	while (itr1.hasNext())	{	//for all words in webpage content
    		String s = itr1.next();
    		if (wordIndex.containsKey(s))	{
    			if (!wordIndex.get(s).contains(url))	{
    				wordIndex.get(s).addLast(url);		//add word to wordIndex
    			}
    		}
    		else	{	//if website isnt part of wordlist yet, add it
    			LinkedList<String> newList = new LinkedList<String>();
    			newList.addLast(url);
    			wordIndex.put(s,newList);
    		}
    	}

    	LinkedList<String> links = htmlParsing.getLinks(url);	//parse current url for links, into a linkedList
    	Iterator<String> itr = links.iterator();

    	while (itr.hasNext()) {		//for every link found on webpage
			String s = itr.next();
			internet.addEdge(url, s);
			if (!(internet.getVisited(s)))	{
				traverseInternet(s);		//recursive call of traverseInternet
			}
		}
    }
    
    void computePageRanks() {
    	/*
		* Page Ranks are calculated from this equation : P(v) = 0.5+ 0.5*(P(v1)/O(v1) + P(v2)/O(v2) + ... + P(vn)/O(vn))
		* Where P(v) is the pageRank of vertext v and v1, v2, ... vn is every vertex that points to v
		*/

    	//initialize all pageranks to 1
    	Iterator<String> itr1 = internet.getVertices().iterator() ;
    	while (itr1.hasNext()) {
			internet.setPageRank(itr1.next(), 1);
		}

    	for (int i=0; i<100; i++)	{	//this is not exact but repeat 100 times for ~ convergence
    		Iterator<String> itr = internet.getVertices().iterator() ;

	    	while (itr.hasNext()) {		//repeat for every url visited
	    		String vertex = itr.next();
	    		double temp = 0.0;
	    		Iterator<String> itr2 = internet.getEdgesInto(vertex).iterator();

				while (itr2.hasNext()) {	//repeat for every website that points to this url
					String s = itr2.next();
					temp = temp + internet.getPageRank(s)/internet.getOutDegree(s);		//temp is the sum of the pageRanks/outDegree of every site that points to the url
				}
				internet.setPageRank(vertex, 0.5+0.5*temp);  // calculate pageRank
			}
		}	
    }
    
    String getBestURL(String query) {	//based on a querry, return the most relevent url based on it's score calculated in computePageRank
    	LinkedList<String> queryMatch = new LinkedList<String>();

    	if (wordIndex.containsKey(query)) {		//check for sites containing the query
    		queryMatch = wordIndex.get(query);
    	}
    	else return "";	// return an empty string if not websites contain the query
    	
    	String highestRank = "";
		Iterator<String> itr = queryMatch.iterator() ;

    	while (itr.hasNext()) {		//for all urls that contain the query
    		String temp = itr.next();
			if(internet.getPageRank(temp) > internet.getPageRank(highestRank))	{	// parse the list continuously adding a higher ranked url if it exists
				highestRank = temp;
			}
		}
		return highestRank;
    }
    
    
	
    public static void main(String args[]) throws Exception{		
		searchEngine mySearchEngine = new searchEngine();
		mySearchEngine.traverseInternet("http://www.cs.mcgill.ca");
		mySearchEngine.computePageRanks();

		BufferedReader stndin = new BufferedReader(new InputStreamReader(System.in));
		String query;
		do {
		    System.out.print("Enter query: ");
		    query = stndin.readLine();
		    if ( query != null && query.length() > 0 ) {
			System.out.println("Best site = " + mySearchEngine.getBestURL(query));
		    }
		} while (query!=null && query.length()>0);				
    } // end of main
}