package com.brightleafsoftware.casephotographer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.ClientManager;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
import com.salesforce.androidsdk.security.PasscodeManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.ListView;

public class CaseListFragment extends ListFragment {

	private static final String STATE_ACTIVATED_POSITION = "activated_position";
	private static final String TAG = "CasePhotographer/CaseListFragment";
	private Callbacks mCallbacks = sDummyCallbacks;
	private int mActivatedPosition = ListView.INVALID_POSITION;
	private PasscodeManager passcodeManager;
	private RestClient rc;
	private JSONArray records;
	private Context parentContext;
	ProgressDialog pd;
	private JsonCaseAdapter adapter;

	public interface Callbacks {
		public void onItemSelected(String id);
	}

	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	public CaseListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Ensure we have a CookieSyncManager
		CookieSyncManager.createInstance(getActivity());

		// Passcode manager
		passcodeManager = ForceApp.APP.getPasscodeManager();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Hide everything until we are logged in
		// findViewById(R.id.root).setVisibility(View.INVISIBLE);
		startProgressDialog();
		// Bring up passcode screen if needed
		if (passcodeManager.onResume(getActivity())) {

			// Login options
			String accountType = ForceApp.APP.getAccountType();
			LoginOptions loginOptions = new LoginOptions(
					null, // login host is chosen by user through the server
							// picker
					ForceApp.APP.getPasscodeHash(),
					getString(R.string.oauth_callback_url),
					getString(R.string.oauth_client_id), new String[] { "api" });

			// Get a rest client
			new ClientManager(getActivity(), accountType, loginOptions)
					.getRestClient(getActivity(), new RestClientCallback() {
						@Override
						public void authenticatedRestClient(RestClient client) {
							if (client == null) {
								ForceApp.APP.logout(getActivity());
								return;
							}

							CaseListFragment.this.rc = client;
							CasePhotographer.setRc(client);
							getAllCases();
						}
					});
		}
	}

	private void startProgressDialog() {
		pd.setMessage("Downloading Cases from Salesforce");
		pd.setIndeterminate(true);
		pd.show();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		parentContext = getActivity();
		pd = new ProgressDialog(parentContext);
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		adapter = new JsonCaseAdapter(getActivity(),
				records);
		setListAdapter(adapter);
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);
		String Id = null;
		try {
			Id = ((JSONObject) records.get(position)).getString("Id");
			Log.i(TAG, Id);
		} catch (JSONException e) {
			Log.e(TAG, "Failed to parse JSON for list Item!", e);
		}
		mCallbacks.onItemSelected(Id);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	public void setActivateOnItemClick(boolean activateOnItemClick) {
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	public void soslSearch(String searchTerm) {
		// Toast.makeText(getActivity(), searchTerm, Toast.LENGTH_LONG).show();
		try {
			RestRequest soslRequest = RestRequest
					.getRequestForSearch(
							CasePhotographer.APIVERSION,
							"FIND {"
									+ searchTerm
									+ "} IN ALL FIELDS RETURNING Case (CaseNumber, createdDate, Id)");
			adapter.notifyDataSetInvalidated();
			records = new AsyncForceRequest().execute(soslRequest).get();
			adapter.setRecords(records);
			adapter.notifyDataSetChanged();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Encoding Exception Occured. How odd.",
					e);
		} catch (InterruptedException e) {
			Log.e(TAG, "Interupted Execution", e);
		} catch (ExecutionException e) {
			Log.e(TAG, "Execution Exception", e);
		}

	}

	public void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	public void getAllCases() {
		String soql = "select id, subject, casenumber, createddate from Case";
		RestRequest request = null;
		try {
			request = RestRequest.getRequestForQuery(
					CasePhotographer.APIVERSION, soql);
			adapter.notifyDataSetInvalidated();
			records = new AsyncForceRequest().execute(request).get();
			adapter.setRecords(records);
			adapter.notifyDataSetChanged();
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG,
					"Encoding Exception Occured. How odd.",
					e);
		} catch (InterruptedException e) {
			Log.e(TAG, "Interupted Execution", e);
		} catch (ExecutionException e) {
			Log.e(TAG, "Execution Exception", e);
		}
	}

	private class AsyncForceRequest extends
			AsyncTask<RestRequest, Integer, JSONArray> {

		@Override
		protected void onPreExecute() {
			if (!pd.isShowing()) {
				startProgressDialog();
			}
		}

		@Override
		protected JSONArray doInBackground(RestRequest... params) {
			RestResponse response = null;
			JSONArray records = null;

			try {
				response = rc.sendSync(params[0]);
				if (response == null || !response.isSuccess()) {
					return null;
				}

				if (response.asString().charAt(0) == '[') {
					records = response.asJSONArray();
				} else {
					records = response.asJSONObject().getJSONArray("records");
				}
			} catch (JSONException e) {
				Log.e(TAG, "JSON Exception in SF response", e);
			} catch (ParseException e) {
				Log.e(TAG,
						"Parse Exception while interpreting sf returned Json",
						e);
			} catch (IOException e) {
				Log.e(TAG, "IO Exception while making or parsing response", e);
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
}
