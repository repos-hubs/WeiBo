package com.kindroid.hub.ui;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import com.kindroid.hub.R;


import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UpgradeDialog extends AlertDialog implements View.OnClickListener {
	private View mView;
	private ProgressBar mProgressBar;
	private TextView mRleaseNote;

	private Button downloadOkBtn;
	private Button downloadCancelBtn;

	private int currentSize = 0;
	private int totalSize = 0;
	private boolean downloadCanceled = false;
	private boolean downloadFinished = false;
	
	private String downUrl;
	private String releaseNote;

	private Handler mProgressHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mProgressBar.setMax(totalSize);
				mProgressBar.setProgress(currentSize);
				if (!downloadFinished) {
					if (downloadCanceled) {
						dismiss();
					}
				} else {
					//dismiss();
				}
				break;
			case 1:
				dismiss();
				break;
			case 2:
				dismiss();
				break;
			}
		}
	};

	protected UpgradeDialog(Context context) {
		super(context);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.downloadOkBtn) {
			mProgressBar.setVisibility(View.VISIBLE);
			new DownloadingThread().start();
			downloadOkBtn.setVisibility(View.GONE);
		} else if (v.getId() == R.id.downloadCancelBtn) {
			downloadCanceled = true;
			dismiss();
		} 
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setLayout(R.layout.download_dialog);
		setTitle(R.string.app_name);
		super.onCreate(savedInstanceState);
	}

	private void setLayout(int layoutResId) {
		setView(mView = getLayoutInflater().inflate(layoutResId, null));
		onReferenceViews(mView);
	}
	
	public void reset(String downUrl, String note) {
		this.downUrl = downUrl;
		this.releaseNote = note;
		mRleaseNote.setText(releaseNote);
		mProgressBar.setVisibility(View.INVISIBLE);
		downloadOkBtn.setVisibility(View.VISIBLE);
		mProgressBar.setProgress(0);
		downloadCanceled = false;
		downloadFinished = false;
		currentSize = 0;
		totalSize = 0;
	}

	private void onReferenceViews(View view) {
		mProgressBar = (ProgressBar) view.findViewById(R.id.downloadingProgress);
		downloadOkBtn = (Button) view.findViewById(R.id.downloadOkBtn);
		downloadCancelBtn = (Button) view.findViewById(R.id.downloadCancelBtn);
		mRleaseNote = (TextView) view.findViewById(R.id.releaseNote);

		downloadOkBtn.setOnClickListener(this);
		downloadCancelBtn.setOnClickListener(this);
	}

	public static final int UPDATE_REQUEST_CODE = 1;

	private class DownloadingThread extends Thread {
		public void run() {
			mProgressBar.setVisibility(View.VISIBLE);

			String fileName = Environment.getExternalStorageDirectory() + "/KindroidHub.apk";
			try {
				FileOutputStream out = new FileOutputStream(fileName, false);

				URL url = new URL(downUrl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();

				InputStream inputStream = connection.getInputStream();

				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
						out);
				BufferedInputStream bufferedInputStream = new BufferedInputStream(
						inputStream);
				totalSize = connection.getContentLength();
				byte[] buf = new byte[4096];
				int bytesRead = 0;

				while (bytesRead >= 0 && !downloadCanceled) {
					bufferedOutputStream.write(buf, 0, bytesRead);
					bytesRead = bufferedInputStream.read(buf);
					currentSize += bytesRead;
					mProgressHandler.sendEmptyMessage(0);
				}

				bufferedOutputStream.flush();

				if ((currentSize < totalSize - 1) || (totalSize <= 0)) {
					downloadFinished = false;
					if (downloadCanceled) {
						mProgressHandler.sendEmptyMessage(1);
					} else {
						mProgressHandler.sendEmptyMessage(2);
					}
				} else {
					downloadFinished = true;
				}

				if (downloadFinished) {
					dismiss();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(fileName)),"application/vnd.android.package-archive");
					getContext().startActivity(intent);
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				mProgressHandler.sendEmptyMessage(0);
			}
		}
	}
}
