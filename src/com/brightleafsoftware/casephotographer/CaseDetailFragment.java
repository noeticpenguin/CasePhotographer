package com.brightleafsoftware.casephotographer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.security.PasscodeManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieSyncManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class CaseDetailFragment extends Fragment {

	public static final String ARG_ITEM_ID = "item_id";
	public static final String TAG = "CasePhotographer/CaseDetailFragment";
	private String mItem;
	private PasscodeManager passcodeManager;
	private RestClient rc;
	private JSONArray caseRecord;
	private AsyncTask<RestRequest, Integer, JSONArray> caseDetailsAFR;
	private AsyncTask<RestRequest, Integer, JSONArray> imageDetailsAFR;
	private ProgressDialog wait;
	private SFImageAdapter adapter;
	private ArrayList<String> images;
	private View rootView;
	public CaseDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = getArguments().getString(ARG_ITEM_ID);
		}

		super.onCreate(savedInstanceState);
		// Ensure we have a CookieSyncManager
		CookieSyncManager.createInstance(getActivity());

		// Passcode manager
		passcodeManager = ForceApp.APP.getPasscodeManager();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		wait = new ProgressDialog(getActivity());
	}
	
	public JSONArray parseIntoJSONArray(AsyncForceRequest afr) {
		JSONArray retval = null;
		try {
			retval = afr.get();
		} catch (InterruptedException e) {
			Log.e(TAG, "Interupted Exception occured", e);
		} catch (ExecutionException e) {
			Log.e(TAG, "Execution Exception occured", e);
		}
		return retval;
	}
	
	public ArrayList<String> parseIntoArrayList(AsyncBinaryForceRequest afr) {
		ArrayList<String> retval = null;
		try {
			retval = afr.get();
		} catch (InterruptedException e) {
			Log.e(TAG, "Interupted Exception occured", e);
		} catch (ExecutionException e) {
			Log.e(TAG, "Execution Exception occured", e);
		}
		return retval;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		startIndeterminateProgressDialog();

		// Bring up passcode screen if needed
		if (passcodeManager.onResume(getActivity())) {
			this.rc = CasePhotographer.rc;
		}

		// Setup the ui.
		rootView = inflater.inflate(R.layout.fragment_case_detail,
				container, false);

		getDataForFragmentView();
		
		// Grab the horizontal list view to show the images.
		HorizontalListView caseImages = (HorizontalListView) rootView
				.findViewById(R.id.HLV);
		caseImages.setVisibility(View.VISIBLE); //make it visible
		adapter = new SFImageAdapter(getActivity(), images);
		caseImages.setAdapter(adapter); //set it's Adapter!

		if (caseRecord != null) {
			JSONObject cr = null;
			int altRowCounter = 0;
			TableLayout table = (TableLayout) rootView
					.findViewById(R.id.case_detail);
			
			try {
				cr = caseRecord.getJSONObject(0);
			} catch (JSONException e) {
				Log.e(TAG, "JSON Excpetion", e);
			}

			generateTableViewFromJson(cr, altRowCounter, table);
		}
		wait.dismiss();
		return rootView;
	}

	private void generateTableViewFromJson(JSONObject cr, int altRowCounter,
			TableLayout table) {
		Iterator<?> keys = cr.keys();
		while (keys.hasNext()) {
			altRowCounter++;
			String key = (String) keys.next();
			if (key == "attributes") {
				continue;
			}
			String value = null;
			try {
				value = cr.getString(key);
				value = (value == "null") ? "Not set" : value;
			} catch (JSONException e) {
				Log.e(TAG, "JSON Excpetion", e);
			}
			// create a new TableRow
			TableRow row = new TableRow(getActivity());
			if (altRowCounter % 2 == 0) {
				row.setBackgroundColor(getResources().getColor(
						R.color.altrow));
			}
			// create a new TextView
			TextView keyTV = new TextView(getActivity());
			keyTV.setText(key);
			keyTV.setPadding(5, 5, 5, 5);
			TextView valueTV = new TextView(getActivity());

			valueTV.setSingleLine(false);
			valueTV.setHorizontallyScrolling(false);
			valueTV.setLayoutParams(new TableRow.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
					1f));
			valueTV.setText(value);
			// add the TextView and the CheckBox to the new TableRow
			row.addView(keyTV);
			row.addView(valueTV);

			// add the TableRow to the TableLayout
			table.addView(row, new TableLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

	private void getDataForFragmentView() {
		// This is a three-step process
		// 1: get the case details
		// 2: get a list of attachment objects with this case ID as the ParentId
		// 3: Snag the actual attachments.
		
		caseDetailsAFR = new AsyncForceRequest()
				.execute(getCaseDetailsRequest());
		caseRecord = parseIntoJSONArray((AsyncForceRequest) caseDetailsAFR);
		// Fetches the list of attached objects.
		imageDetailsAFR = new AsyncForceRequest()
				.execute(getCaseImagesRequest());
		JSONArray imageDetails = parseIntoJSONArray( (AsyncForceRequest) imageDetailsAFR);

		int numOfImagesToFetch = (imageDetails != null) ? imageDetails.length()
				: 0;
		RestRequest[] imageRequests = new RestRequest[numOfImagesToFetch];
		if (numOfImagesToFetch > 0) {
			for (int i = 0; i < numOfImagesToFetch; i++) {
				String path = null;
				try {
					path = imageDetails.getJSONObject(i).getString("Body");
				} catch (JSONException e) {
					Log.e(TAG, "Failed to parse JSON", e);
				}
				imageRequests[i] = new RestRequest(RestRequest.RestMethod.GET,
						path, null);
			}
		}
		images = parseIntoArrayList( (AsyncBinaryForceRequest) new AsyncBinaryForceRequest().execute(imageRequests));
	}

	private void startIndeterminateProgressDialog() {
		wait.setIndeterminate(true);
		wait.setTitle("Fetching Details");
		wait.show();
	}

	/**
	 * @return
	 */
	private RestRequest getCaseDetailsRequest() {
		// TODO Make this Reflective - pull the fields from Sf
		String soql = "SELECT AccountId,CaseNumber,ClosedDate,"
				+ "ContactId,CreatedById,CreatedDate,Description,Id,"
				+ "IsClosed,IsDeleted,IsEscalated,LastModifiedById,"
				+ "LastModifiedDate,Origin,OwnerId,ParentId,Priority,Reason,"
				+ "Status,Subject,SuppliedCompany,SuppliedEmail,SuppliedName,"
				+ "SuppliedPhone,SystemModstamp,Type FROM Case where id = '"
				+ mItem + "'";
		RestRequest request = null;
		try {
			request = RestRequest.getRequestForQuery(
					CasePhotographer.APIVERSION, soql);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Encoding Exception Occured. How odd.", e);
		}
		return request;
	}

	private RestRequest getCaseImagesRequest() {
		String soql = "SELECT Body,BodyLength,ContentType,Description,Id,Name "
				+ "FROM Attachment where isPrivate = false and parentId = '"
				+ mItem + "'";
		RestRequest request = null;
		try {
			request = RestRequest.getRequestForQuery(
					CasePhotographer.APIVERSION, soql);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "Encoding Exception Occured. How odd.", e);
		}
		return request;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void refreshImages(){
		adapter.notifyDataSetInvalidated();
		getDataForFragmentView();
		adapter.setImages(images);
		adapter.notifyDataSetChanged();
		rootView.invalidate();
	}
	
	private class AsyncForceRequest extends
			AsyncTask<RestRequest, Integer, JSONArray> {
		ProgressDialog pd = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			pd.setMessage("Downloading Cases from Salesforce");
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected JSONArray doInBackground(RestRequest... params) {
			RestResponse response = null;
			try {
				response = CaseDetailFragment.this.rc.sendSync(params[0]);
			} catch (IOException e) {
				Log.e(TAG, "IO Exception while making rest request", e);
			}

			JSONArray records = null;
			try {
				if (response == null || response.asJSONObject() == null)
					return null;
				records = response.asJSONObject().getJSONArray("records");
			} catch (ParseException e) {
				Log.e(TAG,
						"Parse Exception while interpreting sf returned Json",
						e);
			} catch (JSONException e) {
				Log.e(TAG, "JSON Exception in SF response", e);
			} catch (IOException e) {
				Log.e(TAG, "IO Exception while parsing response", e);
			}

			if (records == null || records.length() == 0) {
				return null;
			} else {
				return records;
			}

		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(JSONArray result) {
			if (pd.isShowing()) {
				pd.dismiss();
			}
		}

	}

	private class AsyncBinaryForceRequest extends
			AsyncTask<RestRequest, Integer, ArrayList<String>> {
		ProgressDialog pd = new ProgressDialog(getActivity());
		ArrayList<String> imagePaths = new ArrayList<String>();

		@Override
		protected void onPreExecute() {
			pd.setMessage("Downloading Images from Salesforce");
			pd.setIndeterminate(true);
			pd.show();
		}

		private void writeImageToCache(Bitmap bm, String filename) {
			OutputStream fOut = null;
			File file = new File(getActivity().getExternalCacheDir(), filename
					+ ".jpg");
			try {
				fOut = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
				fOut.flush();
				fOut.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "FILE NOT FOUND " + file.getAbsolutePath().toString(), e);
			} catch (IOException e) {
				Log.e(TAG, "IO Exception", e);
			}
		}

		@Override
		protected ArrayList<String> doInBackground(RestRequest... params) {
			for (RestRequest rr : params) {
				RestResponse response = null;

				try {
					String[] filepath = rr.getPath().split("/");
					String filename = filepath[filepath.length - 2];
					File cacheFilename = new File(getActivity()
							.getExternalCacheDir(), filename + ".jpg");
					if (cacheFilename.exists()) {
						Log.i(TAG, "Decoding bitmap from cache.");
						imagePaths.add(cacheFilename.getPath());
					} else {
						response = CasePhotographer.rc.sendSync(rr);
						Bitmap newImage = BitmapFactory.decodeByteArray(
								response.asBytes(), 0,
								response.asBytes().length);
						writeImageToCache(newImage, filename);
						imagePaths.add(cacheFilename.getPath());

					}
				} catch (IOException e) {
					Log.e(TAG, "IO Exception while making rest request", e);
				}
			}

			return imagePaths;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(ArrayList<String> images) {
			if (pd.isShowing()) {
				pd.dismiss();
			}
		}

	}

}
