package gov.nih.nlm.skr.SA;
import java.util.Comparator;
public	class ConcCompHigh implements Comparator {

		public int compare(Object o1, Object o2) {
			ConcDegreeF cd1 = (ConcDegreeF) o1;
			ConcDegreeF cd2 = (ConcDegreeF) o2;
			if(cd1.degree > cd2.degree)
				return -1;
			else if(cd1.degree == cd2.degree)
				return 0;
			else
				return 1;
		}
}
