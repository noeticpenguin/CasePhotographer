///*
// * Copyright (c) 2011, salesforce.com, inc.
// * All rights reserved.
// * Redistribution and use of this software in source and binary forms, with or
// * without modification, are permitted provided that the following conditions
// * are met:
// * - Redistributions of source code must retain the above copyright notice, this
// * list of conditions and the following disclaimer.
// * - Redistributions in binary form must reproduce the above copyright notice,
// * this list of conditions and the following disclaimer in the documentation
// * and/or other materials provided with the distribution.
// * - Neither the name of salesforce.com, inc. nor the names of its contributors
// * may be used to endorse or promote products derived from this software without
// * specific prior written permission of salesforce.com, inc.
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// */
//package com.brightleafsoftware.casephotographer;
//
//import java.io.UnsupportedEncodingException;
//
//import org.json.JSONArray;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.webkit.CookieSyncManager;
//import android.widget.ListView;
//import android.widget.Toast;
//
//import com.salesforce.androidsdk.app.ForceApp;
//import com.salesforce.androidsdk.rest.ClientManager;
//import com.salesforce.androidsdk.rest.ClientManager.LoginOptions;
//import com.salesforce.androidsdk.rest.ClientManager.RestClientCallback;
//import com.salesforce.androidsdk.rest.RestClient;
//import com.salesforce.androidsdk.rest.RestRequest;
//import com.salesforce.androidsdk.rest.RestResponse;
//import com.salesforce.androidsdk.security.PasscodeManager;
//import com.salesforce.androidsdk.util.EventsObservable;
//import com.salesforce.androidsdk.util.EventsObservable.EventType;
//
///**
// * Main activity
// */
//public class MainActivity extends Activity {
//
//	private PasscodeManager passcodeManager;
//	private AlertDialog logoutConfirmationDialog;
//	private static final int LOGOUT_DIALOG_ID = 1;
//	private static final String TAG = "CasePhotographer/MainActivity";
//	private static final String apiVersion = "v25.0";
//	private RestClient rc;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		// Ensure we have a CookieSyncManager
//		CookieSyncManager.createInstance(this);
//
//		// Passcode manager
//		passcodeManager = ForceApp.APP.getPasscodeManager();
//
//		// Setup view
//		setContentView(R.layout.main);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//
//		// Hide everything until we are logged in
//		findViewById(R.id.root).setVisibility(View.INVISIBLE);
//
//		// Bring up passcode screen if needed
//		if (passcodeManager.onResume(this)) {
//
//			// Login options
//			String accountType = ForceApp.APP.getAccountType();
//			LoginOptions loginOptions = new LoginOptions(
//					null, // login host is chosen by user through the server
//							// picker
//					ForceApp.APP.getPasscodeHash(),
//					getString(R.string.oauth_callback_url),
//					getString(R.string.oauth_client_id), new String[] { "api" });
//
//			// Get a rest client
//			new ClientManager(this, accountType, loginOptions).getRestClient(
//					this, new RestClientCallback() {
//						@Override
//						public void authenticatedRestClient(RestClient client) {
//							if (client == null) {
//								ForceApp.APP.logout(MainActivity.this);
//								return;
//							}
//
//							// Show everything
//							findViewById(R.id.root).setVisibility(View.VISIBLE);
//
//							//User is Authenticated, Application logic goes here.
//							MainActivity.this.rc = client;
//							getCases();
//						}
//					});
//		}
//	}
//
//	@SuppressWarnings("deprecation")
//	@Override
//	protected Dialog onCreateDialog(int id) {
//		switch (id) {
//		case LOGOUT_DIALOG_ID:
//			logoutConfirmationDialog = new AlertDialog.Builder(this)
//					.setTitle(R.string.logout_title)
//					.setMessage(R.string.logout_message)
//					.setPositiveButton(R.string.logout_yes,
//							new DialogInterface.OnClickListener() {
//								@Override
//								public void onClick(DialogInterface dialog,
//										int which) {
//									ForceApp.APP.logout(MainActivity.this);
//								}
//							}).setNegativeButton(R.string.logout_cancel, null)
//					.create();
//			return logoutConfirmationDialog;
//		}
//		return super.onCreateDialog(id);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.activity_main, menu);
//		return true;
//	}
//
//	@SuppressWarnings("deprecation")
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
////		Log.i(TAG, "Menu Item Clicked: " + item.getItemId() + " -> Name: "
////				+ item.toString());
//		boolean returnVal = false;
//		switch (item.getItemId()) {
//		case R.id.menu_logout:
//			showDialog(LOGOUT_DIALOG_ID);
//			break;
//		case R.id.menu_new_picture:
//			Intent i = new Intent(getApplicationContext(), CaseListActivity.class);
//			startActivity(i);
//		default:
//			Toast.makeText(this, "Got click: " + item.getItemId(),
//					Toast.LENGTH_SHORT).show();
//			break;
//		}
//		return returnVal;
//	}
//
//	@Override
//	public void onUserInteraction() {
//		passcodeManager.recordUserInteraction();
//	}
//
//	@Override
//	public void onPause() {
//		passcodeManager.onPause(this);
//		super.onPause();
//	}
//
//	/**
//	 * Called when "Logout" button is clicked.
//	 * 
//	 * @param v
//	 */
//	public void onLogoutClick(View v) {
//		ForceApp.APP.logout(this);
//	}
//	
//	public void getCases(){
//		
//		String soql = "select id, subject, casenumber, createddate from Case";
//		RestRequest request = null;
//		try {
//			request = RestRequest.getRequestForQuery(apiVersion, soql);
//		} catch (UnsupportedEncodingException e) {
//			Log.e(TAG, "Encoding Exception Occured. How odd.", e);
//		}
//		Log.d(TAG, request.toString());
//		rc.sendAsync(request, new RestClient.AsyncRequestCallback() {
//					
//			@Override
//			public void onError(Exception exception) {
//				Log.e(TAG, "Error was recieved while attempting REST call", exception);
//				EventsObservable.get().notifyEvent(EventType.RenditionComplete);
//			}
//
//			@Override
//			public void onSuccess(RestRequest request, RestResponse response) {
//				try {
//					//REST API call was successful. Add business logic. 	            
//					if (response == null || response.asJSONObject() == null)
//						return;
//							
//					JSONArray records = response.asJSONObject().getJSONArray("records");
//		
//					if (records.length() == 0)
//						return;
//					
//					ListView lv = (ListView) findViewById(R.id.caseList);
//					JsonCaseAdapter caseAdapter = new JsonCaseAdapter(MainActivity.this, records);
//					lv.setAdapter(caseAdapter);
//					
//	                EventsObservable.get().notifyEvent(EventType.RenditionComplete);
//				} catch (Exception e) {
//					Log.e(TAG, "Error was recieved while attempting REST call", e);
//				}
//			}
//		});
//	}
//	
//}
