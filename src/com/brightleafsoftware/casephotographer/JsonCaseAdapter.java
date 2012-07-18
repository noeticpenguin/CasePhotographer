package com.brightleafsoftware.casephotographer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class JsonCaseAdapter extends BaseAdapter {

	private JSONArray items;
	private Context ctx;
	private LayoutInflater li;
	private final static String TAG = "CasePhotographer/JSONCaseAdapter";

	public JsonCaseAdapter(Context ctx, JSONArray incomingArray) {
		this.items = incomingArray;
		this.ctx = ctx;
		this.li = (LayoutInflater) ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setRecords(JSONArray incomingArray) {
		this.items = incomingArray;
	}
	
	public int getCount() {
		return (items != null) ? items.length() : 0;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = li.inflate(R.layout.listitem, null);
		}

		try {
			JSONObject jobj = items.getJSONObject(position);
			// set Properties here.
			// number, prob type, product topics, case sub, severity level, case
			// desc, created by, date/time
			TextView top = (TextView) v.findViewById(R.id.toptext);

			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
			Date cdate = formatter.parse(jobj.getString("CreatedDate"));
			final String dateStr = DateFormat.getDateFormat(
					ctx.getApplicationContext()).format(cdate);
			top.setText(jobj.getString("CaseNumber") + " Created on " + dateStr);
			TextView bottom = (TextView) v.findViewById(R.id.bottomtext);
			bottom.setText(jobj.getString("Subject"));
		} catch (JSONException e) {
			Log.e(TAG, "JSON Exception Occured: ", e);
		} catch (ParseException e) {
			Log.e(TAG, "Failed to parse date from salesforce", e);
		}

		return v;
	}

}
