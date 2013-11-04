package ch.tkuhn.hashuri.rdf;

import java.util.Comparator;

import org.openrdf.model.Resource;

public class ResourceComparator implements Comparator<Resource> {

	public ResourceComparator() {
	}

	@Override
	public int compare(Resource r1, Resource r2) {
		if (r1 == null && r2 == null) {
			return 0;
		} else if (r1 == null && r2 != null) {
			return -1;
		} else if (r1 != null && r2 == null) {
			return 1;
		}
		return StatementComparator.compareResource(r1, r2);
	}

}
