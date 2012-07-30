package com.brightleafsoftware.casephotographer;

import android.util.Log;

public class U {
	
	private static final String TAG = "CasePhotographer/Utility Class";
	
	public static int getZoomSize() {
		if(CasePhotographer.ninetyPWidth > CasePhotographer.ninetyPHeight) {
			Log.i(TAG, "Returning " + CasePhotographer.ninetyPWidth);
			return CasePhotographer.ninetyPWidth;
		} else {
			Log.i(TAG, "Returning " + CasePhotographer.ninetyPHeight);
			return CasePhotographer.ninetyPHeight;
		}
	}

}
