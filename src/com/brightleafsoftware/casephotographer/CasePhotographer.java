/*
 * Copyright (c) 2011, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.brightleafsoftware.casephotographer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.UUID;

import com.flurry.android.FlurryAgent;
import com.salesforce.androidsdk.app.ForceApp;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.security.Encryptor;
import com.salesforce.androidsdk.ui.SalesforceR;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Application class for our application
 */
public class CasePhotographer extends ForceApp {

	private SalesforceR salesforceR = new SalesforceRImpl();
	private static Context context;
	public static final String APIVERSION = "v25.0";
	public static final String FLURRYAPIKEY = "GSRNKDX3WJY32T6M726J";
	public static RestClient rc;
	public static final String TAG = "Case Photographer";
	public static int ninetyPWidth;
	public static int ninetyPHeight;
	private static String INSTALLATION = "InstallationId";
	private static String sInstallationID;
	
	public void onCreate() {
		super.onCreate();
		CasePhotographer.setContext(getApplicationContext());
		
		DisplayMetrics metrics = new DisplayMetrics();
		metrics = context.getResources().getDisplayMetrics();
		ninetyPHeight = metrics.heightPixels;
		ninetyPWidth = metrics.widthPixels;
	}

	public static void setRc(RestClient rc) {
		CasePhotographer.rc = rc;
	}
	
	/**
	 * @return the context
	 */
	public static Context getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	private static void setContext(Context context) {
		CasePhotographer.context = context;
	}

	@Override
	public Class<? extends Activity> getMainActivityClass() {
		return CaseListActivity.class;
	}

	@Override
	protected String getKey(String name) {
		return Encryptor.hash(name + "x;lksalk1jsadihh23lia;lsdhasd2", name
				+ "112;kaslkxs0-12;skcxn1203ph");
	}

	@Override
	public SalesforceR getSalesforceR() {
		return salesforceR;
	}
	
    public synchronized static String id(Context context) {
        if (sInstallationID == null) {  
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sInstallationID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sInstallationID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
    	HashMap<String,String> params = new HashMap<String,String>();
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
    	params.put("UUID", id);
    	FlurryAgent.logEvent("NewInstall", params);
        out.write(id.getBytes());
        out.close();
    }
	
}
