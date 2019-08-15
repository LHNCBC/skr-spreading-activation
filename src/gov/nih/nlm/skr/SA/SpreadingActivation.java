/**
 * Main class for Spreading Activation
 * 
 * @author Dongwook Shin
 *
 * - The algorithm accepts four arguments from the command line.
   - The first argument of the algorithm indicates if the second and third arguments are provided as as UMLS preferred names or UMLS concept identifiers (CUIs). If it is given as "NAME", it means that the following two arguments are taken as preferred names. If it is "CUI", it means the following two are taken as CUIs. 
   - The second and third arguments are two seed nodes with which the spreading activation algorithm starts.
   - The fourth argument is the name of the output file where the daughter graph is generated in XML format. If the fourth argument is missing, the daughter graph is generated as standard output.
   - The Spreading Activation algorithm generates the mother graph first, which displays each concept in a separate line.
 */

package gov.nih.nlm.skr.SA;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.output.Format;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import java.util.Scanner;

import java.util.Properties;
import java.io.*;

public class SpreadingActivation {


	
	static public void main(String[] argv) { 

		int distance = 0;
		int motherNodes = 0;
		int daughterNodes = 0;
		int daughterGraphs = 0;
		int repetition = 3;
		
		try {
			SpreadingActivation sa = new SpreadingActivation();
			Properties prop = new Properties();
			SAXBuilder builder = new SAXBuilder();
			InputStream is = sa.getClass().getClassLoader().getResourceAsStream("resources/typedefs.xml"); 
			// InputStream is = sa.getClass().getClassLoader().getResourceAsStream("typedefs.xml"); 
			Document doc = builder.build(is);
			Element root = doc.getRootElement(); 
			Iterator sgiter = root.getChild("SemanticGroups").getChildren("SemanticGroup").iterator();
			Map<String,SemanticGroup> semanticGroupMappings = new HashMap<String,SemanticGroup>();
			while (sgiter.hasNext()) {
				Element s = (Element)sgiter.next();
				SemanticGroup sg = new SemanticGroup(s.getAttributeValue("name"), s.getAttributeValue("abbreviation"));
				List<String> slist = new ArrayList<String>();
				Iterator stiter = s.getChildren("SemanticType").iterator();
				while (stiter.hasNext()) {
					String st = ((Element)stiter.next()).getAttributeValue("abbreviation");
					slist.add(st);
					// System.out.println( s.getAttributeValue("abbreviation") + " -> " + st);
					// semanticTypeMappings.put(st, sg);
				}
				sg.setSemanticTypes(slist);
				semanticGroupMappings.put(s.getAttributeValue("abbreviation"), sg);
			}
			
			// input = new FileInputStream("config.properties");
		    // input = SpreadingActivation.class.getResourceAsStream("/config.properties");
		    prop.load(SpreadingActivation.class.getClassLoader().getResourceAsStream("config.properties"));
			// input = loader.getResourceAsStream("/myProp.properties");
			// prop.load(new FileInputStream("config.properties"));
			String dbConnectionString = prop.getProperty("connectionString");
			String dbName = prop.getProperty("database");
			String dbUserName = prop.getProperty("username");
			String dbPassword = prop.getProperty("password");

			String ranking = prop.getProperty("ranking");
			String semtypeProp = prop.getProperty("semtype");
			String semgroupProp = prop.getProperty("semgroup");
			String predicateProp = prop.getProperty("predicate");
			String negationProp = prop.getProperty("includeNegation");
			boolean negationInclude = false;
			
			String[] semtypes = null;
			if(semtypeProp != null)
				semtypes = semtypeProp.split(",");
			
			String[] semgroups = null;
			if(semgroupProp != null)
				semgroups = semgroupProp.split(",");
			List<String> semTypesList = new ArrayList<String>();
			// StringBuffer semgroupBuf = new StringBuffer();
			if(semgroups != null && semgroups.length > 0) { // If semgroups are defined
				for(int pos = 0; pos < semgroups.length; pos++) {
					List<String> thisSemGroup = semanticGroupMappings.get(semgroups[pos]).getSemanticTypes();
					semTypesList.addAll(thisSemGroup);
											
				}				
				// semtypes =  semgroupBuf.toString().split(",");
			}
			
			if(negationProp != null) {
				if(negationProp.startsWith("y") || negationProp.startsWith("Y"))
					negationInclude = true;
			}
			
			String[] predicates = null;
			if(predicateProp != null)
				predicates = predicateProp.split(",");

			String motherNodesStr = prop.getProperty("motherNodes");
			String daughterNodesStr = prop.getProperty("daughterNodes");
			String daughterGraphsStr = prop.getProperty("daughterGraphs");
			
			if(motherNodesStr == null || motherNodesStr.equals(""))
				motherNodes = 40;  // default value
			else
				motherNodes = Integer.parseInt(motherNodesStr.trim());
			
			if(daughterNodesStr == null || daughterNodesStr.equals(""))
				daughterNodes = 5;  // default value
			else
				daughterNodes = Integer.parseInt(daughterNodesStr.trim()); 
			
			if(daughterGraphsStr == null || daughterGraphsStr.equals(""))
				daughterGraphs = 10;  // default value
			else
				daughterGraphs = Integer.parseInt(daughterGraphsStr); 
			
			String distanceStr = prop.getProperty("distance");			
			if(distanceStr == null || distanceStr.equals(""))
				distance = 1;  // default value
			else
				distance = Integer.parseInt(distanceStr.trim());
			
			String repetitionStr = prop.getProperty("repetition");			
			if(repetitionStr == null || repetitionStr.equals(""))
				repetition = 3;  // default value
			else
				repetition = Integer.parseInt(repetitionStr.trim());

			String nameType = argv[0];
			String seedConcept1 = argv[1];
			String seedConcept2 = argv[2];
			String seed1OtherName = null;
			String seed2OtherName = null;
			

			System.out.println(seedConcept1 + " -> " +  seedConcept2);
			PredicationList plist = new PredicationList(dbConnectionString, dbName, dbUserName, dbPassword);
			List<Predication> firstLevel = plist.getPredications(nameType, seedConcept1, seedConcept2, semTypesList, predicates, negationInclude);
			List<Predication> finalLevel = null;
			if(distance ==2) {
				finalLevel = (List<Predication>) plist.getPredications(nameType, seedConcept1, seedConcept2, firstLevel, semTypesList, predicates, negationInclude);
				// log.debug("# of second level predications = " + finalLevel .size());
			} else
				finalLevel = firstLevel;

	    // for (int i = 0; i < secondLevel .size(); i++) {
	    //    APredication apred = (APredication)secondLevel.get(i);
			if(finalLevel == null || finalLevel.size() < 1) {
				System.out.println("\n No predication is retrieved from the database, so Spreading Activation algorithm can not proceed further.");
				System.out.println("Please revise the configuration in config.properties file and run it again.");
				return;
			}

	    Vector SA = new Vector();
	    Hashtable weightTable = new Hashtable();

	   // System.out.println("Size of final predication = " +  finalLevel.size());
			// TableTriple tablepair = generateWeightInfoTable(firstLevel, seedConcept1);
	    if(nameType.equalsIgnoreCase("CUI")) { // find concept names of seeds when CUI are given
	    	int index = 0;
	    	boolean found1 = false;
	    	boolean found2 = false;
	    	while(index < finalLevel.size()) {
	    		Predication pred = finalLevel.get(index);
	    		if(!found1 && pred.subjectCUI.get(0).equals(seedConcept1)) {
	    			seed1OtherName = pred.subject.get(0);
	    			found1 = true;
	    			// System.out.println("converted Seed Name 1 = " + seed1OtherName);
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} else if(!found1 && pred.objectCUI.get(0).equals(seedConcept1)) {
	    			seed1OtherName = pred.object.get(0);
	    			found1 = true;
	    			// System.out.println("converted Seed Name 1 = " + seed1OtherName);
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} 
	    		
	    		if(!found2 && pred.subjectCUI.get(0).equals(seedConcept2)) {
	    			seed2OtherName = pred.subject.get(0);
	    			// System.out.println("converted Seed Name 2 = " + seed2OtherName);
	    			found2 = true;
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} else if(!found2 && pred.objectCUI.get(0).equals(seedConcept2)) {
	    			seed2OtherName = pred.object.get(0);
	    			// System.out.println("converted Seed Name 2 = " + seed2OtherName);
	    			found2 = true;
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} 
	    		
	    		if(found1 && found2)
	    			break;
	    		else
	    			index++;
	    	}
	    } else { // find CUI of seeds when concept names are given
	    	int index = 0;
	    	boolean found1 = false;
	    	boolean found2 = false;
	    	while(index < finalLevel.size()) {
	    		Predication pred = finalLevel.get(index);
	    		if(!found1 && pred.subject.get(0).equals(seedConcept1)) {
	    			seed1OtherName = pred.subjectCUI.get(0);
	    			found1 = true;
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} else if(!found1 && pred.object.get(0).equals(seedConcept1)) {
	    			seed1OtherName = pred.objectCUI.get(0);
	    			found1 = true;
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} 
	    		
	    		if(!found2 && pred.subject.get(0).equals(seedConcept2)) {
	    			seed2OtherName = pred.subjectCUI.get(0);
	    			found2 = true;
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} else if(!found2 && pred.object.get(0).equals(seedConcept2)) {
	    			seed2OtherName = pred.objectCUI.get(0);
	    			found2 = true;
	    			// System.out.println("converted See Name = " + seedConcept1);
	    			// break;
	    		} 
	    		
	    		if(found1 && found2)
	    			break;
	    		else
	    			index++;
	    	}
	    }
	    
			// TableTriple tablepair = generateWeightInfoTable(finalLevel, seedConcept1.toLowerCase());
		TableTriple tablepair = null;
	    if(nameType.equalsIgnoreCase("NAME")) {
			tablepair = generateWeightInfoTable(finalLevel, seedConcept1);
			// System.out.println("seed1 name = " + seedConcept1);
	    } else {
	    	tablepair = generateWeightInfoTable(finalLevel, seed1OtherName);
	    	// System.out.println("seed1 name = " + seed1OtherName);
	    }
		
	    if(tablepair == null) {
	    	System.out.println("Weight table information is null and cannot proceed further computation");
	    	return;
	    }
	    Hashtable edgeWeightInfoTable = tablepair.edgeWeightTable;
			Hashtable connectivityTable = tablepair.connectivityTable;
			Vector nodeVec = tablepair.nodeVec;

			Enumeration e = connectivityTable.keys();
			/* while(e.hasMoreElements()) {
				String key = (String) e.nextElement();
				log.debug("element in connectivity table = " + key);
			} */
			Hashtable newWeightTable = new Hashtable();
			int round = 0;
			// String aNode = seedConcept1.toLowerCase();
			String aNode = null;
			if(nameType.equalsIgnoreCase("NAME"))
				aNode = seedConcept1;
			else
				aNode = seed1OtherName;
			while(round < repetition) {
				Vector newNodeVec = (Vector) nodeVec.clone();
				while(true) {
					String curNode = aNode;
					float valueOfTheNode  = (Integer) connectivityTable.get(curNode);
					Hashtable edgeWeightTable = (Hashtable) edgeWeightInfoTable.get(curNode);
					float activation = valueOfTheNode;
					Enumeration keys = edgeWeightTable.keys();
					float weightSum = 0;
					while(keys.hasMoreElements()) {
						String concept = (String) keys.nextElement();
						float valueInBoth = (Integer) edgeWeightTable.get(concept);
						float valueOfTheOtherNode = (Integer) connectivityTable.get(concept);
						float pmi = (Float) (valueInBoth)/(Float) (valueOfTheNode * valueOfTheOtherNode);
						float weight = 0;
						if(weightTable.containsKey(concept))
							weight = (Float) weightTable.get(concept);
						else
							weight = 1;
						weightSum = weightSum + pmi * weight;
					}
					activation = activation + weightSum;
					newWeightTable.put(curNode, activation);
					if(!nodeVec.isEmpty())
						aNode = (String) nodeVec.remove(0);
					else
						break;
				}
				round++;
				weightTable = null;
				weightTable = newWeightTable;
				newWeightTable = new Hashtable();
				nodeVec = newNodeVec;
			}
			ArrayList concDegreeList = new ArrayList();
	        Enumeration conceptEnum = weightTable.keys();
	    	Comparator concComp = null;
	    	
	    	Queue pqueue = null;
	    	if(ranking.equals("FrequencyLow")) {
	    		concComp = new ConcCompLow();	
	    		pqueue = new PriorityQueue(1000, concComp);
	    	} if(ranking.equals("FrequencyHigh")) {
	    		concComp = new ConcCompHigh();	
	    		pqueue = new PriorityQueue(1000, concComp);
	    	} else if (ranking.equals("Random")) {	    		
	    		pqueue = new LinkedBlockingQueue();
	    	}
	    	// HashSet survivedSet = new HashSet();
	        while (conceptEnum.hasMoreElements()) {
	        	String concept = (String) conceptEnum.nextElement();
	        	float value = (Float) weightTable.get(concept);
	        	ConcDegreeF cdf = new ConcDegreeF(concept, value);
	        	pqueue.add(cdf);
	        }
	        
	        boolean seed1found = false;
	        boolean seed2found = false;
	        for (int ii=0; ii < motherNodes; ii++) {
	        	ConcDegreeF highestConF = (ConcDegreeF) pqueue.poll();
	        	if(highestConF != null) {
	        		// Integer degree = Math.round(highestConF.degree);
	        		// ConcDegree highestCon = new ConcDegree(highestConF.conc, degree);
		        	String concept = highestConF.getConcWithST();
	        		concDegreeList.add(highestConF);
	        		if(nameType.equalsIgnoreCase("NAME") && concept.equals(seedConcept1))
	        			seed1found = true;
	        		else if(nameType.equalsIgnoreCase("CUI") && concept.equals(seed1OtherName))
	        			seed1found = true;
	        		
	        		if(nameType.equalsIgnoreCase("NAME") && concept.equals(seedConcept1))
	        			seed2found = true;
	        		else if(nameType.equalsIgnoreCase("CUI") && concept.equals(seed1OtherName))
	        			seed2found = true;

	        	} else
	        		break;
	        }		        
	        
	        int additionalNum = 0;
	        if(seed1found)
	        	additionalNum++;
	        if(seed2found)
	        	additionalNum++;
	        
	        /*
	         * This is to make the total number of mother graph nodes is always two plus of the mother nodes given in the conf.properties
	         * If the seed nodes are already chosen, add two in addition to the number of mother graph nodes
	         */
	        for (int ii=0; ii < additionalNum; ii++) { // add the additional concept in case seed concepts are already selected
	        	ConcDegreeF highestConF = (ConcDegreeF) pqueue.poll();
	        	if(highestConF != null) {
	        		// Integer degree = Math.round(highestConF.degree);
	        		// ConcDegree highestCon = new ConcDegree(highestConF.conc, degree);
		        	String concept = highestConF.getConcWithST();
	        		concDegreeList.add(highestConF);	        		
	        	} else
	        		break;
	        }	
	        
	        float seed1Weight = 0; 
	        float seed2Weight = 0;
	        boolean mseed1found = seed1found;
	        boolean mseed2found = seed2found;
	        while (!mseed1found || !mseed2found) { // find the degree of seed1 and seed2
	        	ConcDegreeF aConF = (ConcDegreeF) pqueue.poll();
	        	if(aConF == null)
	        		break; // repeat until the end of the queue
		        String concept = aConF.getConcWithST();
	        	if(!mseed1found && nameType.equalsIgnoreCase("NAME") && concept.equals(seedConcept1)) {
	        		mseed1found = true;
	        		seed1Weight = aConF.degree;
	        	} else if(!mseed1found && nameType.equalsIgnoreCase("CUI") && concept.equals(seed1OtherName)) {
	        		mseed1found = true;
	        		seed1Weight = aConF.degree;
	        	}
	        	
	        	if(!mseed2found && nameType.equalsIgnoreCase("NAME") && concept.equals(seedConcept2)) {
        			mseed2found = true;
        			seed2Weight = aConF.degree;
	        	} else if(!mseed2found && nameType.equalsIgnoreCase("CUI") && concept.equals(seed2OtherName)) {
        			mseed2found = true;
        			seed2Weight = aConF.degree;
	        	}
	        }
	        
	        int index = 0;
	        System.out.println("\nMother Graph information:\n");
	        System.out.println("CUI \t : Concept\t\t : Weight\n");  // print concepts in mother graphs
	        List<Concept> finalMotherConcepts = new ArrayList<Concept>();
	        for(Object o : concDegreeList) {
	        	ConcDegreeF cf = (ConcDegreeF) o;
	        	// if(index < daughterNodes)
	        	//	concepts.add(cf.getConcWithST());
	        	index++;
	        	String thisCUI = null;
	        	
		    	index = 0;
		    	while(index < finalLevel.size()) {
		    		Predication pred = finalLevel.get(index);
		    		if(pred.subject.get(0).equals(cf.getConcWithST())) {
		    			thisCUI = pred.subjectCUI.get(0);
		    			// System.out.println("converted See Name = " + seedConcept1);
		    			break;
		    		} else if(pred.object.get(0).equals(cf.getConcWithST())) {
		    			thisCUI = pred.objectCUI.get(0);
		    			// System.out.println("converted See Name = " + seedConcept1);
		    			break;
		    		} else
		    			index++;
		    	}	        	
	        	
	        	System.out.println(thisCUI  + " : " +  cf.getConcWithST() + "\t : " + cf.degree);  // print concepts in mother graph
	        	Concept motherConcept = new Concept(thisCUI, cf.getConcWithST(), cf.degree);
	        	finalMotherConcepts.add(motherConcept);
	        } 
	        
	        if(!seed1found) {
	        	if(nameType.equalsIgnoreCase("NAME")) {
	        		System.out.println(seed1OtherName  + " : " +  seedConcept1 + "\t : " + seed1Weight);  // print concepts in mother graph
	        		Concept motherConcept = new Concept(seed1OtherName, seedConcept1, seed1Weight);
		        	finalMotherConcepts.add(motherConcept);
	        	} else  {
	        		System.out.println(seedConcept1 + " : " +  seed1OtherName  + " : " +  "\t : " + seed1Weight);  // print concepts in mother graph
	        		Concept motherConcept = new Concept(seedConcept1, seed1OtherName, seed1Weight);
		        	finalMotherConcepts.add(motherConcept);
	        	}
	        }
	        
	        if(!seed2found) {
	        	if(nameType.equalsIgnoreCase("NAME")) {
	        		System.out.println(seed2OtherName  + " : " +  seedConcept2 + "\t : " + seed2Weight);  // print concepts in mother graph
	        		Concept motherConcept = new Concept( seed2OtherName, seedConcept2, seed2Weight);
		        	finalMotherConcepts.add(motherConcept);
	        	} else  {
	        		System.out.println(seedConcept2 + " : " +  seed2OtherName  + " : " +  "\t : " + seed2Weight);  // print concepts in mother graph
	        		Concept motherConcept = new Concept(seedConcept2, seed2OtherName, seed2Weight);
		        	finalMotherConcepts.add(motherConcept);
	        	}
	        }
	        
	        StringBuffer daughterBuf = new StringBuffer();
	        daughterBuf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<spreadingActivation>\n");
	        Document seedXml = Utils.parse(seedConcept1, seedConcept2);
        	XMLOutputter xmloutput = new XMLOutputter();      	
        	StringWriter graphWriter = new StringWriter();
        	xmloutput.setFormat(Format.getPrettyFormat());
        	xmloutput.output(seedXml, graphWriter);
        	String graphString = graphWriter.toString();
        	int headIndex = graphString.indexOf(">"); // find the XML head that needs to be eliminated
        	// System.out.println(graphString);
        	daughterBuf.append(graphString.substring(headIndex+1) + "\n");
        	
	        Document motherXml = Utils.parse(finalMotherConcepts, 1);
        	xmloutput = new XMLOutputter();      	
        	graphWriter = new StringWriter();
        	xmloutput.setFormat(Format.getPrettyFormat());
        	xmloutput.output(motherXml, graphWriter);
        	graphString = graphWriter.toString();
        	headIndex = graphString.indexOf(">"); // find the XML head that needs to be eliminated
        	// System.out.println(graphString);
        	daughterBuf.append(graphString.substring(headIndex+1) + "\n");
        	
	        for(int loop=0; loop < daughterGraphs; loop++) {
	        	// Generate daughter nodes using random number
		        seed1found = false;
		        seed2found = false;
	        	System.out.println("\nGenerating daughter graph (" + loop + ")");
	        	System.out.println("\tConcepts included:");
		        Vector<String> concepts = new Vector<String>();
	        	Set<Integer> alreadyChosen = new HashSet<Integer>();
	        	for(int randGen=0; randGen < daughterNodes; randGen++) {
	        		boolean found = false;
        			ConcDegreeF cf = null; 
	        		while(!found) {
		        		int randomNum = (int)(Math.random() * motherNodes); 
		        		// System.out.println("random number = " + randomNum);
		        		if(!alreadyChosen.contains(randomNum)) {
		        			cf = (ConcDegreeF) concDegreeList.get(randomNum);
		        			alreadyChosen.add(randomNum);
		        			found = true;
		        			break;
		        		}
	        		}
	        		if(nameType.equalsIgnoreCase("NAME") && cf.getConcWithST().equals(seedConcept1))
	        			seed1found = true;
	        		else if(nameType.equalsIgnoreCase("CUI") && cf.getConcWithST().equals(seed1OtherName))
	        			seed1found = true;
	        		if(nameType.equalsIgnoreCase("NAME") && cf.getConcWithST().equals(seedConcept2))
	        			seed2found = true;
	        		else if(nameType.equalsIgnoreCase("CUI") && cf.getConcWithST().equals(seed2OtherName))
	        			seed2found = true;
	        			
	        		concepts.add(cf.getConcWithST());
		        	System.out.println("\t\t" + cf.getConcWithST());

	        	} // for
	        	additionalNum = 0;
		        if(seed1found)
		        	additionalNum++;
		        if(seed2found)
		        	additionalNum++;
	        	
		        for(int randGen=0; randGen < additionalNum; randGen++) { // add additional nodes if seed nodes are already included in daughter nodes
	        		boolean found = false;
        			ConcDegreeF cf = null;
	        		while(!found) {
		        		int randomNum = (int)(Math.random() * motherNodes); 
		        		// System.out.println("random number = " + randomNum);
		        		if(!alreadyChosen.contains(randomNum)) {
		        			cf = (ConcDegreeF) concDegreeList.get(randomNum);
		        			alreadyChosen.add(randomNum);
		        			found = true;
		        			break;
		        		}
	        		}        			
	        		concepts.add(cf.getConcWithST());
		        	System.out.println("\t\t" + cf.getConcWithST());

	        	} // for
		        
		        if(!seed1found) {
		        	if(nameType.equalsIgnoreCase("NAME")) {
		        		concepts.add(seedConcept1); // print concepts in mother graph
			        	System.out.println("\t\t" + seedConcept1);
		        	} else  {
		        		concepts.add(seed1OtherName);  // print concepts in mother graph
			        	System.out.println("\t\t" + seed1OtherName);
		        	}

		        }
		        
		        if(!seed2found) {
		        	if(nameType.equalsIgnoreCase("NAME")) {
		        		concepts.add(seedConcept2); // print concepts in mother graph
		        		System.out.println("\t\t" + seedConcept2);
		        	} else  {
		        		concepts.add(seed2OtherName);  // print concepts in mother graph
		        		System.out.println("\t\t" + seed2OtherName);
		        	}
		        }
		        
	        	List<SentencePredication> splist = plist.getSentencePredications(concepts, predicates,negationInclude);
	        	// System.out.println("Num of sentence predication = " + splist.size());
        	
	        	// Document graphXml = Utils.parse(splist, seedConcept1, seedConcept2, finalMotherConcepts);
	        	Document graphXml = Utils.parse(splist);
	        	xmloutput = new XMLOutputter();
	        	graphWriter = new StringWriter();
	        	xmloutput.setFormat(Format.getPrettyFormat());
	        	xmloutput.output(graphXml, graphWriter);
	        	graphString = graphWriter.toString();

	        	headIndex = graphString.toString().indexOf(">"); // find the XML head that needs to be eliminated
	        	// System.out.println(graphString);
	        	daughterBuf.append(graphString.substring(headIndex+1) + "\n");

	        }
	        daughterBuf.append("</spreadingActivation>\n");
        	PrintWriter out = null;
        	if(argv[3] != null ) {
        		out = new PrintWriter(new BufferedWriter(new FileWriter(argv[3])));
        		out.println(daughterBuf.toString());
        		out.close();
        	} else 
        		System.out.println(daughterBuf.toString());
	        
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	static TableTriple generateWeightInfoTable(List<Predication> predications, String seed) {
		Hashtable edgeWeightInfoTable = new Hashtable();
	    Hashtable predTableToken = new Hashtable();
	    Hashtable predTablePerPMID = new Hashtable();
	    Hashtable predTableTypi = new Hashtable();
	    Hashtable conceptTable = new Hashtable();
	    Hashtable conceptRevTable = new Hashtable();
	    Hashtable predtable = new Hashtable();
	    int PMID = 0;
	    int prevPMID = 0;
	    int concId = 0;
	    Hashtable ConnectivityTable = new Hashtable();
	    Hashtable wConnectivityTable = new Hashtable();

		for (int i = 0; i < predications.size(); i++) {
	      Predication apred = (Predication)predications.get(i);
	      String SubjConcept = (String)apred.subject.get(0);
	      String ObjConcept = (String)apred.object.get(0);
	      String SubjST = (String)apred.subjectSemtype.get(0);
	      String ObjST = (String)apred.objectSemtype.get(0);
	      String predicate = apred.predicate;
	      // Ignore the following semantic types
	      // if(SubjST.equals("mamm") || SubjST.equals("humn") || ObjST.equals("mamm") || ObjST.equals("humn") || predicate.equals("PROCESS_OF") || filteredConceptSet.contains(SubjConcept.toLowerCase()) || filteredConceptSet.contains(ObjConcept.toLowerCase()))
	      //	  continue;

	      /* APredicationWOR ap = new APredicationWOR(SubjConcept, SubjST, ObjConcept, ObjST);
	      HashSet predSet = null;
	      if ((predSet = (HashSet)predtable.get(ap)) != null) {
	        predSet.add(apred);
	      } else {
	        predSet = new HashSet();
	        predSet.add(apred);
	        predtable.put(ap, predSet);
	      } */

	      // String subjWithST = new String(SubjConcept + "|" + SubjST);
	      // String objWithST = new String(ObjConcept + "|" + ObjST);
	      // String subjWithST = new String(SubjConcept).toLowerCase();
	      // String objWithST = new String(ObjConcept).toLowerCase();
	      String subjWithST = new String(SubjConcept);
	      String objWithST = new String(ObjConcept);

	      // Counting predication frequency, but in spreading activation, this is not necessary
	      Hashtable subjWeightTable = (Hashtable) edgeWeightInfoTable.get(subjWithST);
	      Hashtable objWeightTable = (Hashtable) edgeWeightInfoTable.get(objWithST);
	      if (subjWeightTable  == null) {
	    	  // ConcDegree cd = new ConcDegree(predicate, 1); // Make a pair with predicate name and its frequence
	    	  subjWeightTable = new Hashtable();
	    	  subjWeightTable.put(objWithST, 1); // Add the first predicate connecting subject and object
	    	  edgeWeightInfoTable.put(subjWithST,subjWeightTable);
	    	  // log.debug(subjObjPair);
	      } else {
	    	  Integer edgeFreq = (Integer) subjWeightTable.remove(objWithST);
	    	  if(edgeFreq == null) { // subject and object are found, but the predicate name is not found
	    		  subjWeightTable.put(objWithST, 1); // Add the first predicate connecting subject and object
	    	  } else { // same predicate
	    		  Integer newEdgeFreq = edgeFreq + 1;
	    		  subjWeightTable.put(objWithST, newEdgeFreq);
	    	  }
	      }

	      if (objWeightTable  == null) {
	    	  // ConcDegree cd = new ConcDegree(predicate, 1); // Make a pair with predicate name and its frequence
	    	  objWeightTable = new Hashtable();
	    	  objWeightTable.put(subjWithST, 1); // Add the first predicate connecting subject and object
	    	  edgeWeightInfoTable.put(objWithST,objWeightTable);
	    	  // log.debug(subjObjPair);
	      } else {
	    	  Integer edgeFreq = (Integer) objWeightTable.remove(subjWithST);
	    	  if(edgeFreq == null) { // subject and object are found, but the predicate name is not found
	    		  objWeightTable.put(subjWithST, 1); // Add the first predicate connecting subject and object
	    	  } else { // same predicate
	    		  Integer newEdgeFreq = edgeFreq + 1;
	    		  objWeightTable.put(subjWithST, newEdgeFreq);
	    	  }
	      }

	        // Finding the adjancect list of a node
	        HashSet adjacentSet1 = null;
	        ArrayList connectedConceptList = null;
	  		// String css = new String(SubjConcept + "|" + SubjST);
	  		// String cso = new String(ObjConcept + "|" + ObjST);
	  		// String css = new String(SubjConcept).toLowerCase();
	  		// String cso = new String(ObjConcept).toLowerCase();
	  		String css = new String(SubjConcept);
	  		String cso = new String(ObjConcept);

	  		if(SubjConcept != null && SubjConcept.compareTo("") != 0 && (adjacentSet1 = (HashSet) ConnectivityTable.get(css)) != null) {
	  			// System.out.println(css.toString() + "is already found");
	  			connectedConceptList = (ArrayList) wConnectivityTable.get(css);
	  			connectedConceptList.add(cso);
	  			if(!adjacentSet1.contains(cso)) {
	  				adjacentSet1.add(cso);
	  				// System.out.println(cso.toString() + "is already found");
	  			}
	  		} else if (SubjConcept != null && SubjConcept.compareTo("") != 0) {
	  			adjacentSet1 = new HashSet();
	  			adjacentSet1.add(cso);
	  			ConnectivityTable.put(css,adjacentSet1);
	  			connectedConceptList = new ArrayList();
	  			connectedConceptList.add(cso);
	  			wConnectivityTable.put(css,connectedConceptList); // need to calculated the weighted connectivity

	  		}

	  		HashSet adjacentSet2 = null;
	  		if(ObjConcept != null && ObjConcept.compareTo("") != 0 && (adjacentSet2 = (HashSet) ConnectivityTable.get(cso)) != null) {
	  			 // System.out.println(cso.toString() + "is already found");
	  			connectedConceptList = (ArrayList) wConnectivityTable.get(cso);
	  			connectedConceptList.add(css);
	  			if(!adjacentSet2.contains(css)) {
	  				adjacentSet2.add(css);
	  				// System.out.println(css.toString() + "is already found");
	  			}
	  		} else if (ObjConcept != null && ObjConcept.compareTo("") != 0) {
	  			adjacentSet2 = new HashSet();
	  			adjacentSet2.add(css);
	  			ConnectivityTable.put(cso,adjacentSet2);
	  			connectedConceptList = new ArrayList();
	  			connectedConceptList.add(css);
	  			wConnectivityTable.put(cso,connectedConceptList); // need to calculated the weighted connectivity
	  		}
	    }
	    // log.debug("----- End of Subject Object pair added ---------");
		Enumeration e = ConnectivityTable.keys();
		Hashtable vConnectivityTable = new Hashtable();
		Vector fullNodeVec = new Vector();
		while(e.hasMoreElements()) {
			String key = (String) e.nextElement();
			HashSet adjSet = (HashSet) ConnectivityTable.get(key);
			if(!fullNodeVec.contains(key))
				fullNodeVec.add(key);
			Integer value = adjSet.size();
			vConnectivityTable.put(key, value);
			// log.debug("element in connectivity table = " + key);
		}
	    Vector nodeVec = new Vector();
	    nodeVec.add(seed);
		String key = null;
		LinkedList queue = new LinkedList();
		queue.add(seed);
		while(true) {
			if(queue.isEmpty())
				break;
			else
				key = (String) queue.remove();
			HashSet adjSet = (HashSet) ConnectivityTable.get(key);
			if(adjSet != null ) {
				Iterator it = adjSet.iterator();
				while(it.hasNext()) {
					String concept = (String) it.next();
					if(!nodeVec.contains(concept)) {
						nodeVec.add(concept);
						queue.add(concept);
					}
				}
			}
		}

		return new TableTriple(vConnectivityTable, edgeWeightInfoTable, nodeVec);
	}
}
	
class TableTriple { 
		Hashtable connectivityTable;
		Hashtable edgeWeightTable;
		Vector nodeVec;

		TableTriple(Hashtable c, Hashtable e, Vector vec) {
			connectivityTable = c;
			edgeWeightTable = e;
			nodeVec = vec;
		}
}
	

