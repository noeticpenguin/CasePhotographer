package com.brightleafsoftware.casephotographer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.ParseException;
import org.json.JSONException;

import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class CaseDetailActivity extends FragmentActivity {

	private static final int LOGOUT_DIALOG_ID = 0;
	private static final int GETIMAGE = 1;
	private static final String TAG = "CasePhotographer/CaseDetailActivity";
	private Uri fileUri = Uri.EMPTY;
	private String caseId = "";
	private String attachmentId = "";
	public static int ninetyPWidth;
	public static int ninetyPHeight;
	private CaseDetailFragment cdf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_case_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			Bundle arguments = new Bundle();
			arguments.putString(CaseDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(CaseDetailFragment.ARG_ITEM_ID));
			caseId = getIntent().getStringExtra(CaseDetailFragment.ARG_ITEM_ID);
			cdf = new CaseDetailFragment();
			cdf.setArguments(arguments);
			FragmentManager fm = getSupportFragmentManager();
			android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.case_detail_container, cdf, "CaseDetails");
			ft.commit();
		}
		overridePendingTransition(android.R.anim.fade_in,
				android.R.anim.fade_out);
		
		Rect displayRectangle = new Rect();
		Window window = this.getWindow();
		window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
		ninetyPWidth = (int) (displayRectangle.width() * 0.9f);
		ninetyPHeight = (int) (displayRectangle.height() * 0.9f);
	}

	public static int getZoomSize() {
		return (ninetyPWidth > ninetyPHeight) ? ninetyPWidth : ninetyPHeight;
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
			fireTakePictureIntent();
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

	private File createTemporaryFile(String part, String ext) throws Exception {
		File tempDir = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		return File.createTempFile(part, ext, tempDir);
	}

	private byte[] readBitmapAsByteArray(String path) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[32 * 1024];
		Bitmap bm = BitmapFactory.decodeFile(path, options);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm = Bitmap.createScaledBitmap(bm, 1024, 768, true);
		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] b = baos.toByteArray();
		return b;
	}

	private void fireTakePictureIntent() {
		File photo = null;
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		try {
			photo = createTemporaryFile("CasePhotographer", ".jpg");
			photo.delete();
		} catch (Exception e) {
			Log.e(TAG, "Can't create file to take picture!", e);
		}
		fileUri = Uri.fromFile(photo);
		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		// start camera intent
		startActivityForResult(takePictureIntent, GETIMAGE);
	}

	private void moveImageToCacheDir() {
		File cacheDir = getExternalCacheDir();
		File oldName = new File(fileUri.getPath());
		File newName = new File(cacheDir + "/" + attachmentId + ".jpg");
		oldName.renameTo(newName);
		fileUri = Uri.fromFile(newName);
		// Log.i(TAG, "Moved " + oldName.getPath() + " to " +
		// newName.getPath());
	}
	
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		if (savedInstanceState == null) {
			savedInstanceState = new Bundle();
		}
		
		getSupportFragmentManager().putFragment(savedInstanceState, "caseDetails" , cdf);
		savedInstanceState.putString("tmpFilename", fileUri.getPath());
		savedInstanceState.putString("caseId", caseId);
		savedInstanceState.putString("attachmentId", attachmentId);	
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		this.fileUri = Uri.fromFile(new File(savedInstanceState.getString("tmpFilename")));
		this.caseId = savedInstanceState.getString("caseId");
		this.attachmentId = savedInstanceState.getString("attachmentId");
		this.cdf = (CaseDetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, "caseDetails");
	}

	// called after camera intent finished
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == GETIMAGE && resultCode == RESULT_OK) {

			// upload the image to SF
			// Log.i(TAG, fileUri.getPath());
			byte[] filebytes = null;

			filebytes = readBitmapAsByteArray(fileUri.getPath());
			String objectType = "Attachment";
			Map<String, Object> fields = new HashMap<String, Object>();
			fields.put("Name", fileUri.getLastPathSegment());
			fields.put("ContentType", "image/jpeg");
			fields.put("ParentId", caseId);
			fields.put("Body", Base64.encodeToString(filebytes, Base64.DEFAULT));

			try {
				RestRequest request = RestRequest.getRequestForCreate(
						CasePhotographer.APIVERSION, objectType, fields);
				new AsyncUploadImageForceRequest().execute(request);
			} catch (Exception e) {
				Log.e(TAG, "Failed to Upload! Image", e);
			}

		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	private class AsyncUploadImageForceRequest extends
			AsyncTask<RestRequest, Integer, Integer> {
		ProgressDialog pd = new ProgressDialog(CaseDetailActivity.this);
		RestResponse response = null;

		@Override
		protected void onPreExecute() {
			pd.setMessage("Attaching Image to Case: " + caseId);
			pd.setIndeterminate(true);
			pd.show();
		}

		@Override
		protected Integer doInBackground(RestRequest... params) {
			try {
				response = CasePhotographer.rc.sendSync(params[0]);
				attachmentId = response.asJSONObject().getString("id");
				Log.d(TAG, attachmentId);
			} catch (IOException e) {
				Log.e(TAG, "Failed to attach image to case", e);
			} catch (ParseException e) {
				Log.e(TAG, "Failed to parse results of upload", e);
			} catch (JSONException e) {
				Log.e(TAG, "A JSON Exception occured", e);
			}

			return response.getStatusCode();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPostExecute(Integer statusCode) {
			moveImageToCacheDir();
			pd.dismiss();
			cdf.refreshImages();
		}

	}

}
