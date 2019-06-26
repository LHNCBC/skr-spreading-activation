package gov.nih.nlm.skr.SA;

public class ConcDegreeF {
	String conc;
	Float degree;

	public ConcDegreeF(String c, Float d) {
		conc = c;
		degree = d;
	}

	public boolean equals(Object o) {
		ConcDegreeF cd = (ConcDegreeF) o;
		if(conc.compareTo(cd.conc) == 0)
			return true;
		else
			return false;
	}

	public int hashCode() {
		return conc.hashCode();
	}

	public String getConcWithST() {
		return conc;
	}

	public void setConcWithST(String st) {
		conc = st;
	}

	public Float getDegree() {
		return degree;
	}

	public void setDegree(Float d) {
		degree = d;
	}
	
	public String toString() {
		return new String(conc + " : " + degree);
	}
}
