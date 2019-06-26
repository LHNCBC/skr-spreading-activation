package gov.nih.nlm.skr.SA;
public class ConcDegree {
		String concWithST;
		Integer degree;

		public ConcDegree(String c, Integer d) {
			concWithST = c;
			degree = d;
		}

		public boolean equals(Object o) {
			ConcDegree cd = (ConcDegree) o;
			if(concWithST.compareTo(cd.concWithST) == 0)
				return true;
			else
				return false;
		}

		public int hashCode() {
			return concWithST.hashCode();
		}

		public String getConcWithST() {
			return concWithST;
		}

		public void setConcWithST(String st) {
			concWithST = st;
		}

		public Integer getDegree() {
			return degree;
		}

		public void setDegree(Integer d) {
			degree = d;
		}

}
