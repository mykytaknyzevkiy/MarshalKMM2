package com.l1inc.viewer;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Yevhen Paschenko on 9/13/2016.
 */
public class HoleWithinCourse implements Serializable {

	private String courseId;
	private int holeNumber;


	public String getCourseId() {
		return courseId;
	}

	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}

	public int getHoleNumber() {
		return holeNumber;
	}

	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof HoleWithinCourse) {
			final HoleWithinCourse otherHole = (HoleWithinCourse)object;
			return courseId.equals(otherHole.courseId) && holeNumber == otherHole.holeNumber;
		}

		return false;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "id_course: %s, holeNumber: %d", courseId, holeNumber);
	}
}