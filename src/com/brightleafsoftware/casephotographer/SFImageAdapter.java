package com.brightleafsoftware.casephotographer;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class SFImageAdapter extends BaseAdapter {

	private ArrayList<String> images;
	private Context ctx;
	private final static String TAG = "CasePhotographer/JSONCaseAdapter";
	private static final int THUMBNAIL_SIZE = 512; // 384;
	private static int ZOOM_SIZE = CaseDetailActivity.getZoomSize();
	
	public SFImageAdapter(Context ctx, ArrayList<String> images) {
		this.images = images;
		this.ctx = ctx;
		Log.i(TAG, ""+ZOOM_SIZE);
	}

	public void setImages(ArrayList<String> incoming) {
		this.images = incoming;
	}
	
	public int getCount() {
		if (images != null) {
			return images.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private Bitmap getThumb(String path, int thumbSize) {
		BitmapFactory.Options inputBounds = new BitmapFactory.Options();
		inputBounds.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, inputBounds);
		if ((inputBounds.outWidth == -1) || (inputBounds.outHeight == -1))
			return null;

		int originalSize = (inputBounds.outHeight > inputBounds.outWidth) ? inputBounds.outHeight
				: inputBounds.outWidth;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[32 * 1024];
		options.inSampleSize = originalSize / thumbSize;
		return BitmapFactory.decodeFile(path, options);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		if (v == null) {
			v = new ImageView(ctx);
		} else {
			v = (ImageView) convertView;
		}

		((ImageView) v).setImageBitmap(getThumb(images.get(position),THUMBNAIL_SIZE));
		v.setOnClickListener(new zoomClickListener(position));
		return v;
	}

	private class zoomClickListener implements OnClickListener {

		private int position;

		public zoomClickListener(int position) {
			this.position = position;
		}

		public void onClick(View v) {
			ImageView zoomView = new ImageView(ctx);
			zoomView.setImageBitmap(getThumb(images.get(position),ZOOM_SIZE));
			AlertDialog zoomImage = new AlertDialog.Builder(ctx).setView(zoomView)
					.setNegativeButton(R.string.dismiss, null).create();
			zoomImage.show();
		}

	}
}
