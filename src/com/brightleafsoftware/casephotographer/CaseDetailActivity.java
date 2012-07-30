package com.brightleafsoftware.casephotographer;

import java.io.File;
import com.salesforce.androidsdk.app.ForceApp;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CaseDetailActivity extends FragmentActivity {

	private static final int LOGOUT_DIALOG_ID = 0;
	private static final String TAG = "CasePhotographer/CaseDetailActivity";
	public static int ninetyPWidth;
	public static int ninetyPHeight;
	private CaseDetailFragment cdf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "*********************************************************");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_case_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			Bundle arguments = new Bundle();
			arguments.putString(CaseDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(CaseDetailFragment.ARG_ITEM_ID));
			cdf = new CaseDetailFragment();
			cdf.setArguments(arguments);
			FragmentManager fm = getSupportFragmentManager();
			android.support.v4.app.FragmentTransaction ft = fm
					.beginTransaction();
			ft.add(R.id.case_detail_container, cdf, "CaseDetails");
			ft.commit();
		}
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpTo(this,
					new Intent(this, CaseListActivity.class));
			break;
		case R.id.menu_logout:
			showDialog(LOGOUT_DIALOG_ID);
			break;
		case R.id.menu_new_picture:
			cdf.fireTakePictureIntent();
			break;
		default:
			Toast.makeText(this, "Got click: " + item.getItemId(),
					Toast.LENGTH_SHORT).show();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog logoutConfirmationDialog;
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
									ForceApp.APP
											.logout(CaseDetailActivity.this);
								}
							}).setNegativeButton(R.string.logout_cancel, null)
					.create();
			return logoutConfirmationDialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
			savedInstanceState = new Bundle();
		}

		getSupportFragmentManager().putFragment(savedInstanceState,
				"caseDetails", cdf);
		savedInstanceState.putString("tmpFilename", cdf.getFileUri().getPath());
		savedInstanceState.putString("caseId", cdf.getCaseId());
		savedInstanceState.putString("attachmentId", cdf.getAttachmentId());
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		this.cdf = (CaseDetailFragment) getSupportFragmentManager()
				.getFragment(savedInstanceState, "caseDetails");
		cdf.setFileUri(Uri.fromFile(new File(savedInstanceState
				.getString("tmpFilename"))));
		cdf.setCaseId(savedInstanceState.getString("caseId"));
		cdf.setAttachmentId(savedInstanceState.getString("attachmentId"));
	}

}
