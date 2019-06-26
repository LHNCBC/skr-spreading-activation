package gov.nih.nlm.skr.SA;

public class Sentence {
    String PMID;
    String TYPE;
    Integer NUMBER;
    String SENTENCE;
    
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Sentence))
			return false;

		return PMID.equals(((Sentence) o).PMID)
				&& TYPE.equals(((Sentence) o).TYPE)
				&& NUMBER.equals(((Sentence) o).NUMBER);
	}
	
	public int hashCode() {
		int hash = 1;
		hash = hash * 31 + PMID.hashCode();
		hash = hash * 31 + TYPE.hashCode();
		hash = hash * 31 + NUMBER;
		return hash;
	}
}
