package com.brightleafsoftware.casephotographer;

import com.flurry.android.FlurryAgent;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.security.PasscodeManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("unused")
public class CaseListActivity extends FragmentActivity implements
		CaseListFragment.Callbacks {

	private boolean mTwoPane;
	private PasscodeManager passcodeManager;
	private AlertDialog logoutConfirmationDialog;
	private static final int LOGOUT_DIALOG_ID = 1;
	private static final String TAG = "CasePhotographer/CaseListActivity";
	private static final int ABOUT_DIALOG_ID = 2;
	private RestClient rc;
	private ActionMode mMode;
	private String soslSearchString;
	private CaseDetailFragment cdf;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_case_list);

		if (findViewById(R.id.case_detail_container) != null) {
			mTwoPane = true;
			((CaseListFragment) getSupportFragmentManager().findFragmentById(
					R.id.case_list)).setActivateOnItemClick(true);
		}
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			Bundle arguments = new Bundle();
			arguments.putString(CaseDetailFragment.ARG_ITEM_ID, id);
			FragmentManager fm = getSupportFragmentManager();
			android.support.v4.app.FragmentTransaction ft = fm
					.beginTransaction();

			if (cdf != null) {
				cdf = new CaseDetailFragment();
				ft.replace(R.id.case_detail_container, cdf, "CaseDetails");
			} else {
				cdf = new CaseDetailFragment();
				ft.add(R.id.case_detail_container, cdf, "CaseDetails");
			}
			cdf.setArguments(arguments);
			ft.commit();
		} else {
			Intent detailIntent = new Intent(this, CaseDetailActivity.class);
			detailIntent.putExtra(CaseDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOGOUT_DIALOG_ID:
			logoutConfirmationDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.logout_title)
					.setMessage(R.string.logout_message)
					.setPositiveButton(R.string.logout_yes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									ForceApp.APP.logout(CaseListActivity.this);
								}
							}).setNegativeButton(R.string.logout_cancel, null)
					.create();
			return logoutConfirmationDialog;
		case ABOUT_DIALOG_ID:
			LinearLayout view = (LinearLayout) View.inflate(this, R.layout.about, null);
			TextView tv = (TextView) view.findViewById(R.id.message);
			tv.setText(Html.fromHtml(getString(R.string.aboutDialogBody)));
			AlertDialog aboutDialog = new AlertDialog.Builder(this)
					.setView(view)
					.setNegativeButton(R.string.aboutDismiss, null).create();
			return aboutDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setTitle(R.string.title_case_list);
		getMenuInflater().inflate(R.menu.case_list_activity_menu, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean returnVal = false;
		switch (item.getItemId()) {
		case R.id.menu_logout:
			showDialog(LOGOUT_DIALOG_ID);
			break;
		case R.id.search:
			FlurryAgent.logEvent("Search Action Mode Initiated");
			mMode = startActionMode(new SearchActionMode());
			break;
		case R.id.menu_new_picture:
			cdf.fireTakePictureIntent();
			break;
		case R.id.menu_about:
			showDialog(ABOUT_DIALOG_ID);
			break;
		default:
			Toast.makeText(this, "Got click: " + item.getItemId(),
					Toast.LENGTH_SHORT).show();
			break;
		}
		return returnVal;
	}

	private final class SearchActionMode implements ActionMode.Callback {
		LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getTitle().equals(
					getString(R.string.search_action_mode_title))) {
				EditText s = (EditText) findViewById(R.id.etsearchbar);
				soslSearchString = s.getText().toString();
				imm.hideSoftInputFromWindow(s.getWindowToken(), 0);
			}
			CaseListFragment clf = (CaseListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.case_list);
			clf.soslSearch(soslSearchString);
			return true;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			View sb = li.inflate(R.layout.searchbar, null);
			sb.findViewById(R.id.etsearchbar).requestFocus();
			imm.toggleSoftInput(R.id.etsearchbar,
					InputMethodManager.SHOW_IMPLICIT);
			mode.setCustomView(sb.findViewById(R.id.etsearchbar));
			menu.add(R.string.search_action_mode_title)
					.setIcon(R.drawable.search)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mMode = null;
			CaseListFragment clf = (CaseListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.case_list);
			clf.getAllCases();
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

	}

}
