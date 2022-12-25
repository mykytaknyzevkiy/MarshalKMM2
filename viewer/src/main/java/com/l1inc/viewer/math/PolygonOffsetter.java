package com.l1inc.viewer.math;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yevhen Paschenko on 3/22/2016.
 */
public class PolygonOffsetter {

	public static List<Vector> extendPolygon(final List<Vector> pointList, float extension) {
		List<Vector> pointListExtended = new ArrayList<>();

		for (int i = 0; i < pointList.size() - 1; i++) {
			Vector pt0 = getPoint(pointList, i - 1);
			Vector pt1 = getPoint(pointList, i);
			Vector pt2 = getPoint(pointList, i + 1);

			double a1 = VectorMath.angle(pt0, pt1, pt2);
//            double dot = VectorMath.dot(pt0, pt1, pt2);
//            if (dot < 0) {
//                a1 += Math.PI;
//            }

			Vector newPoint = pt2.substracted(pt1)
					.normalized()
					.multiplied(extension)
					.rotated(-a1 / 2 + Math.PI)
					.added(pt1);
			pointListExtended.add(newPoint);
		}
		pointListExtended.add(pointListExtended.get(0));
		return pointListExtended;
	}

	private static Vector getPoint(List<Vector> pointList, int index) {
		Vector retval;

		if (index < 0) {
			retval = pointList.get(pointList.size() + index - 1);
		} else {
			retval = pointList.get(index);
		}

		return retval;
	}


}
