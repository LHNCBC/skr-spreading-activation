package gov.nih.nlm.skr.SA;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;


public class Utils {
	public static Document parse(List<SentencePredication> sps) {

		Map<Concept,Integer> concepts = new HashMap<Concept,Integer>();
		Map<Concept,String> semtypes = new HashMap<Concept, String>();
		Map<Predication,Map<Sentence,Integer>> predications = new HashMap<Predication,Map<Sentence,Integer>>();
		Map<Concept, List<String>> conceptMap = new HashMap<Concept, List<String>>();
		
		int cnt = 0;

		Element root = new Element("daughterGraph");
		Document doc = new Document(root);


		// Map<String,Concept> seenCuis = new HashMap<String,Concept>();
		for(SentencePredication sp : sps) {
			Predication p = sp.getPredication();
			Sentence snt = sp.getSentence();

			Concept displaySubjConc = new Concept(p.subjectCUI.get(0), p.subject.get(0), p.subjectSemtype.get(0));
			Concept displayObjConc = new Concept(p.objectCUI.get(0), p.object.get(0), p.objectSemtype.get(0));
			
			Concept SubjConc = new Concept(p.subjectCUI.get(0),  p.subject.get(0), null);
			Concept ObjConc = new Concept(p.objectCUI.get(0), p.object.get(0), null);
			// String displaySubjectCUI = p.objectCUI.get(0);
			// String displayObjectCUI = p.subjectCUI.get(0);
			// String subjSemtype = p.subjectSemtype.get(0);
			// String objSemtype = p.objectSemtype.get(0);

			if(displaySubjConc == null || displayObjConc == null)
				continue;


			// seenCuis.put(displaySubjectCUI, displaySubjConc);
			// seenCuis.put(displayObjectCUI, displayObjConc);
			// Set<String> subjSemtypes = getSemtypes(displaySubjects, ptype);
			// Set<String> objSemtypes = getSemtypes(displayObjects, ptype);

		    if (concepts.containsKey(displaySubjConc)) {
		    	cnt = ((Integer)concepts.get(displaySubjConc)).intValue();
				cnt++;
				// concepts.put(displaySubjConc,cnt+1);
			} else {
				concepts.put(displaySubjConc,1);
				// semtypes.put(displaySubjConc,subjSemtype);
			}

			if (concepts.containsKey(displayObjConc)) {
				cnt = ((Integer)concepts.get(displayObjConc)).intValue();
				cnt++;
				// concepts.put(displayObjConc,cnt+1);
			} else {
				concepts.put(displayObjConc,1);
				// semtypes.put(displayObjConc,objSemtype);
			}
			
			if(conceptMap.containsKey(SubjConc)) {
				List semTypes = conceptMap.get(SubjConc);
				if(!semTypes.contains(p.subjectSemtype.get(0)))
					semTypes.add(p.subjectSemtype.get(0));
			} else {
				List<String> newList = new ArrayList<String>();
				newList.add(p.subjectSemtype.get(0));
				conceptMap.put(SubjConc, newList);
			}
			
			if(conceptMap.containsKey(ObjConc)) {
				List semTypes = conceptMap.get(ObjConc);
				if(!semTypes.contains(p.objectSemtype.get(0)))
					semTypes.add(p.objectSemtype.get(0));
			} else {
				List<String> newList = new ArrayList<String>();
				newList.add(p.objectSemtype.get(0));
				conceptMap.put(ObjConc, newList);
			}

			Map<Sentence,Integer> sentences;
			if (predications.containsKey(p)) {
				sentences = predications.get(p);
				if (sentences.containsKey(snt)) {
					int cn = ((Integer)sentences.get(snt)).intValue() + 1;
					sentences.put(snt, cn);
				} else {
					sentences.put(snt, 1);
				}
			}
			else {
				sentences = new HashMap<Sentence,Integer>(); 
				sentences.put(snt, 1);
			}
			predications.put(p,sentences);
		}

		// System.out.println("Size of concepts = " + concepts.size());
		// System.out.println("Size of predications = " + predications.size());
		/* for(Concept c: concepts.keySet()) {

			//log.debug("UI String: " + uiStr);
			Element n = new Element("node");

			n.setAttribute("id", c.cui);
			n.setAttribute("name", c.preferredName);
			System.out.println("Node name = " + c.preferredName); 
			n.setAttribute("semtype", c.type);
			// n.setAttribute("size", ((Integer)concepts.get(c)).toString());			

			root.addContent(n);
		} */
		
		for(Concept c: conceptMap.keySet()) {
			List<String> semTypes = conceptMap.get(c);
			//log.debug("UI String: " + uiStr);
			Element n = new Element("node");

			n.setAttribute("id", c.cui);
			n.setAttribute("name", c.preferredName);
			// System.out.println("Node name = " + c.preferredName); 
			StringBuffer semTypeBuf = new StringBuffer();
			for(String type: semTypes) {
				semTypeBuf.append(" " + type);
			}
			
			n.setAttribute("semtype", semTypeBuf.toString().trim());
			// n.setAttribute("size", ((Integer)concepts.get(c)).toString());			

			root.addContent(n);
		}

		for(Predication p :predications.keySet()){
			String predicate = p.predicate;
			// log.debug("predicate for edge: " + predicate);
			Element e = new Element("edge");
			e.setAttribute("source", p.subjectCUI.get(0));
			e.setAttribute("target", p.objectCUI.get(0));
			e.setAttribute("label", predicate);


			root.addContent(e);
			if (predications.get(p) != null) {
				Map preds = (Map)predications.get(p);
				Iterator sentIter = preds.keySet().iterator();
				while (sentIter.hasNext()) {
					Sentence sent = (Sentence)sentIter.next();
					Element s = new Element("sentence");
					s.setAttribute("id", sent.PMID + "." + sent.TYPE + "." + sent.NUMBER);
					s.setAttribute("text", sent.SENTENCE);
					// s.setAttribute("size", ((Integer)preds.get(sent)).toString());
					e.addContent(s);
				}
			}
		}
		// Element seedE = new Element("seed");
		// root.addContent(seedE);

		return doc;
	}
	
	public static Document parse(String seed1, String seed2) {
		Element root = new Element("seedInfo");
		Document doc = new Document(root);
		Element n1 = new Element("startSeed");
		n1.setAttribute("id", seed1);
		root.addContent(n1);
		Element n2 = new Element("endSeed");
		n2.setAttribute("id", seed2);
		root.addContent(n2);
		return doc;
	}
	
	public static Document parse(List<Concept> finalConcepts, int foo) {
		Element root = new Element("motherGraph");
		Document doc = new Document(root);
		
		for(Concept concept: finalConcepts) {
			Element mnode = new Element("node");
			mnode.setAttribute("CUI", concept.cui);
			mnode.setAttribute("PreferredName", concept.preferredName);
			mnode.setAttribute("Weight", String.valueOf(concept.weight));
			root.addContent(mnode);
		}
		return doc;
	}
}
