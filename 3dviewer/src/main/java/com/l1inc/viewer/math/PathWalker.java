package com.l1inc.viewer.math;

/**
 * Created by Yevhen Paschenko on 4/13/2016.
 */
public class PathWalker {


    private Vector[] path;
    private double speed;
    private Vector position = new Vector();
    private double currentTime;
    private double totalLength;
    private double[] distanceMarkers;
    private boolean finished = true;
    private double angle;
    private int nextPointIndex;
    private double completePercentage;

    public Vector[] getPath() {
        return path;
    }

    public void setPath(final Vector[] path) {
        this.path = path;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(final double speed) {
        this.speed = speed;
    }

    public Vector getPosition() {
        return position;
    }

    public boolean isFinished() {
        return finished;
    }

    public double getAngle() {
        return angle;
    }

    public void start() {
        if (path.length < 2) {
            finished = true;
            return;
        }

        position.x = path[0].x;
        position.y = path[0].y;
        currentTime = 0;
        finished = false;

        totalLength = VectorMath.distance(path);
        distanceMarkers = new double[path.length - 1];
        double currentDistance = 0;
        for (int i = 0; i < path.length - 1; i++) {
            currentDistance += path[i].distance(path[i + 1]);
            distanceMarkers[i] = currentDistance;
        }
        completePercentage = 0;
        calculatePosition();
        calculateAngle();
    }

    public double getCompletePercentage() {
        return completePercentage;
    }

    public void tick(final long timeElapsed) {
        if (finished) {
            return;
        }

        currentTime += timeElapsed / 1000.0;

        calculatePosition();
        calculateAngle();
    }


/*
    public List<Vector> calculatePath(final int msDelta) {
        final List<Vector> retval = new ArrayList<>();

        start();

        final Random random = new Random();

        while (finished == false) {
            calculatePosition();
            retval.add(position);

            final double deviation = msDelta * 0.1 * random.nextFloat() - msDelta * 0.05;
            currentTime += (msDelta + deviation) / 1000.0;
        }

        return retval;
    }*/

    private void calculatePosition() {
        final double currentDistance = currentTime * speed;
        if (currentDistance >= totalLength) {
            finished = true;
            position.x = path[path.length - 1].x;
            position.y = path[path.length - 1].y;
            completePercentage = 1;
            return;
        }

        completePercentage = currentDistance/totalLength;

        int endPoint = 0;
        while (distanceMarkers[endPoint] <= currentDistance) {
            endPoint += 1;
        }
        endPoint += 1;
        final int startPoint = endPoint - 1;

        double prevDistance = 0;
        if (startPoint > 0) {
            prevDistance = distanceMarkers[startPoint - 1];
        }
//		position = path[endPoint]
//				.substracted(path[startPoint])
//				.normalized()
//				.multiplied(currentDistance - prevDistance)
//				.added(path[startPoint]);

		/*position.x = path[endPoint].x;
        position.y = path[endPoint].y;
		VectorMath.substract(position, position, path[startPoint]);
		VectorMath.normalize(position, position);
		VectorMath.multiply(position, position, currentDistance - prevDistance);
		VectorMath.add(position, position, path[startPoint]);*/

        position = VectorMath.added(
                VectorMath.multiplied(VectorMath.normalized(VectorMath.subtracted(path[endPoint], path[startPoint])), currentDistance - prevDistance), path[startPoint]);

        nextPointIndex = endPoint;
    }

    private void calculateAngle() {
        if (finished) {
            return;
        }

        angle = calculateAngle(
                position.x, position.y + 1,
                position.x, position.y,
                path[nextPointIndex].x, path[nextPointIndex].y
        );
    }

    public static double calculateAngle(final double pt1x,
                                        final double pt1y,
                                        final double pt2x,
                                        final double pt2y,
                                        final double pt3x,
                                        final double pt3y) {
        double retval = VectorMath.angle(pt1x, pt1y, pt2x, pt2y, pt3x, pt3y);
        retval = VectorMath.rad2deg(retval);
        if (pt3x < pt2x) {
            retval *= -1;
        }

        return retval;
    }
}
