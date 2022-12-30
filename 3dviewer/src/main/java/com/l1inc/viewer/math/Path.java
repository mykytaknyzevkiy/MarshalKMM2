package com.l1inc.viewer.math;

/**
 * Created by Yevhen Paschenko on 5/19/2016.
 */
public class Path {

	private Vector[] path;

	public Vector getPosition(final float factor) {
		Vector retval = null;
		final double totalLength = VectorMath.distance(path);
		final double factorLength = totalLength * factor;
		double currentLength = 0;
		double currentFactor = 0;
		for (int i = 0; i < path.length - 1; i++) {
			final double len = VectorMath.distance(path[i], path[i + 1]);
			if (len + currentLength > factorLength) {
				retval = path[i + 1]
						.substracted(path[i])
						.normalized()
						.multiplied(totalLength * (factor - currentFactor))
						.added(path[i]);
				break;
			}

			currentFactor += len / totalLength;
			currentLength += len;
		}

		if (retval == null) {
			retval = path[path.length-1];
		}

		return retval;
	}

	public Path(final Vector[] path) {
		this.path = path;
	}
}
