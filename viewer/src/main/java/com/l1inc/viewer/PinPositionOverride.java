package com.l1inc.viewer;

import android.location.Location;

import org.joda.time.DateTime;

/**
 * Created by Yevhen Paschenko on 7/1/2016.
 */
public class PinPositionOverride {

	private int holeNumber;
	private DateTime dateTime;
	private Location pinPosition;

	public int getHoleNumber() {
		return holeNumber;
	}

	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Location getPinPosition() {
		return pinPosition;
	}

	public void setPinPosition(Location pinPosition) {
		this.pinPosition = pinPosition;
	}
}
