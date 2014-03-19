package com.kindroid.hub.ui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.data.UserDefaultInfo;

public class HubTribeWriteWeibo extends Activity implements View.OnClickListener {
	private static final String TAG = "HubTribeWriteWeibo";
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_PHOTO_LIBRARY = 2;
	final int MAX_WORDS = 140;						//请允许输入最多字数
	private final int RELEASE_WEIBO = 0;
	
	private ImageView cameraImageView;
	private Button releaseButton;
	private EditText releaseWeiboEditText;
	private ImageView backBtn;
	private ProgressDialog dlgLoading = null;
	private TextView canInputWordsView;
	
	private File mImageFile;
	private Uri mImageUri;
	private boolean canReplay = false;	
	public static String userName = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hub_tribe_write_weibo);
		
		findViews();
	}
	
	Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case RELEASE_WEIBO:
				String result = (String) msg.obj;
				if (result.equals("success")) {
					Toast.makeText(HubTribeWriteWeibo.this, getResources().getString(R.string.msg_send_reply_success), Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			default:
				break;
			}
		}
		
	};
	
	private void findViews() {
		cameraImageView = (ImageView) findViewById(R.id.cameraImageView);
		cameraImageView.setOnClickListener(this);
		releaseButton = (Button) findViewById(R.id.releaseButton);
		releaseButton.setOnClickListener(this);
		releaseWeiboEditText = (EditText) findViewById(R.id.releaseWeiboEditText);
		canInputWordsView = (TextView) findViewById(R.id.canInputWordsView);
		backBtn = (ImageView) findViewById(R.id.btn_back);
		backBtn.setOnClickListener(this);
		
		if (!TextUtils.isEmpty(userName)) {
			releaseWeiboEditText.setText("@" + userName + " ");
		}
		
		final TextWatcher watcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
      
            @Override  
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  
            }
            
            @Override  
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	String inputWords = releaseWeiboEditText.getText().toString();
            	if (inputWords.length() <= MAX_WORDS) {
            		canReplay = true;
            		String formatedWord = String.valueOf(MAX_WORDS - inputWords.length());
            		canInputWordsView.setText(formatedWord);
            	} else {
            		canReplay = false;
            		canInputWordsView.setText("0");
            		Toast.makeText(HubTribeWriteWeibo.this, R.string.too_more_words, Toast.LENGTH_SHORT).show();
            	}
            }              
        };
        releaseWeiboEditText.addTextChangedListener(watcher);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.cameraImageView) {
			createInsertPhotoDialog();
		}
		if (view.getId() == R.id.releaseButton) {
			String token = UserDefaultInfo.getWeiboId(this);
			if (!TextUtils.isEmpty(token)) {
				String releaseStr = releaseWeiboEditText.getText() + "";
				if (!TextUtils.isEmpty(releaseStr.trim())) {
					if (canReplay) {
						showProgressDialog();
						new ReleaseWeibo().start();
					} else {
						Toast.makeText(HubTribeWriteWeibo.this, R.string.too_more_words, Toast.LENGTH_SHORT).show();
					}
					
				} else {
					Toast.makeText(HubTribeWriteWeibo.this, getResources().getString(R.string.msg_release_not_empty), Toast.LENGTH_SHORT).show();
				}
			} else {
				Intent intent = new Intent(HubTribeWriteWeibo.this, UserLogin.class);
				startActivity(intent);
			}
		}
		
		if (view.getId() == R.id.btn_back) {
			finish();
		}
	}
	
	class ReleaseWeibo extends Thread {
		@Override
		public void run() {
//			String result = DataService.releaseWeibo((releaseWeiboEditText.getText() + "").trim(), HubTribeWriteWeibo.this);
			String result = DataService.releaseWeiboToSina((releaseWeiboEditText.getText() + "").trim(), HubTribeWriteWeibo.this);
			Message msg = dataHandler.obtainMessage();
			msg.arg1 = RELEASE_WEIBO;
			msg.obj = result;
			dataHandler.sendMessage(msg);
		}
	} 
	
	protected void createInsertPhotoDialog() {

		final CharSequence[] items = { getString(R.string.write_label_take_a_picture),
				getString(R.string.write_label_choose_a_picture) };

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.write_label_insert_picture));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					openImageCaptureMenu();
					break;
				case 1:
					openPhotoLibraryMenu();
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void openImageCaptureMenu() {
		try {
			// TODO: API < 1.6, images size too small
			mImageFile = new File(Environment.getExternalStorageDirectory(), "upload.jpg");
			mImageUri = Uri.fromFile(mImageFile);
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	protected void openPhotoLibraryMenu() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, REQUEST_PHOTO_LIBRARY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
	    	/*Intent intent = HubTribeWriteWeibo.createImageIntent(this, mImageUri);
	    	intent.setClass(this, HubTribeWriteWeibo.class);

	        startActivity(intent);  */
	        
	        //打开发送图片界面后将自身关闭
	        finish();
	    } else if (requestCode == REQUEST_PHOTO_LIBRARY && resultCode == RESULT_OK){
	    	mImageUri = data.getData();

	    	/*Intent intent = HubTribeWriteWeibo.createImageIntent(this, mImageUri);
	    	intent.setClass(this, HubTribeWriteWeibo.class);

	    	startActivity(intent); */ 	

	        //打开发送图片界面后将自身关闭
	        finish();
	    }
	}
	
	/**
	 * Show loading dialog
	 */
	private void showProgressDialog() {
		dlgLoading = new ProgressDialog(this);
		dlgLoading.setTitle(getResources().getText(R.string.app_name));
		dlgLoading.setMessage(getResources().getText(R.string.progress_message));
		dlgLoading.setIndeterminate(true);
		dlgLoading.show();
	}
		
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			this.dlgLoading.dismiss();
		}
	}
}
