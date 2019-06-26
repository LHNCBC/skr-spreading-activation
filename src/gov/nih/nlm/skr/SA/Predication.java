package gov.nih.nlm.skr.SA;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jdom.Element;


public class Predication {
	private static final long serialVersionUID = 1L;

	public String predicate = "";

	public String sentence;

	public String PMID;

	public int SENTENCE_ID;

	public int PREDICATION_ID;

	public List<String> subjectCUI = new ConceptList(1);

	public List<String> objectCUI = new ConceptList(1);

	public List<String> subject = new ConceptList(1);

	public List<String> object = new ConceptList(1);

	public List<String> subjectSemtype = new ConceptList(1);

	public List<String> objectSemtype = new ConceptList(1);

	public boolean novelSubject;

	public boolean novelObject;

	public void addObject(String s) {
		// if (!object.contains(s))
		// April 30 2014
		// To cope with the change made to PREDICATION_AGGREGATE table From SemMedDB 24_2
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			object.add(i);
	}

	public void addObjectCUI(String s) {
		// if (!objectCUI.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			objectCUI.add(i);
	}

	public void addObjectSemtype(String s) {
		// if (!objectSemtype.contains(s))\
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			objectSemtype.add(i);
	}

	public void addSubject(String s) {
		// if (!subject.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			subject.add(i);
	}

	public void addSubjectCUI(String s) {
		// if (!subject.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			subjectCUI.add(i);
	}

	public void addSubjectSemtype(String s) {
		// if (!subjectSemtype.contains(s))
		// String[] ss = s.split("\\|\\|\\|");
		String[] ss = s.split("\\|");
		for (String i : ss)
			subjectSemtype.add(i);
	}

	public String toString() {
		return "{PMID:" + PMID + ";S:"
				+ sentence + "}"; 
	}

	public boolean equals(Object p) {
		if (p == null || !(p instanceof Predication))
			return false;

		return predicate.equals(((Predication) p).predicate)
				&& subjectCUI.equals(((Predication) p).subjectCUI)
				&& objectCUI.equals(((Predication) p).objectCUI);
	}

	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + predicate.hashCode();
		hash = hash * 31 + subjectCUI.hashCode();
		hash = hash * 31 + objectCUI.hashCode();
		return hash;
	}

	private static class ConceptList extends ArrayList<String> {

		private static final long serialVersionUID = 1L;

		public ConceptList(int i) {
			super(i);
		}

		public String toString() {
			if (size() == 1)
				return get(0);
			else {
				StringBuffer sb = new StringBuffer();
				Set<String> s = new HashSet<String>();
				sb.append(get(0));
				s.add(get(0));
				for (int i = 1; i < size(); i++)
					if (!s.contains(get(i))) {
						sb.append(" | ");
						sb.append(get(i));
						s.add(get(i));
					}
				return sb.toString();
			}
		}
	}


    /* public Element toXml(String lang) {
    	Element predNode = new Element("Predication");
    	Element idNode = new Element("PredicationId");
    	idNode.setText(Long.toString(getPredicationId().longValue()));
    	predNode.addContent(idNode);
    	
    	Element predicateNode = new Element("Predicate");
    	predicateNode.setText(this.predicate);
    	Element typeNode = new Element("Type");
    	typeNode.setText(this.);  	
    	
    	predNode.addContent(predicateNode);
    	predNode.addContent(typeNode);
    	
    	Iterator iter = getPredicationArgumentSet().iterator();
    	while (iter.hasNext()) {
    		PredicationArgument pa = (PredicationArgument)iter.next();
    		Element paNode = pa.toXml(lang);
    		predNode.addContent(paNode);
    	}      	
    	return predNode;
    } */

}
