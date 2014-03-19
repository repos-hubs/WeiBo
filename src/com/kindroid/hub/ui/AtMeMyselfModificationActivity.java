package com.kindroid.hub.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import weibo4android.User;
import weibo4android.Weibo;
import weibo4android.WeiboException;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.data.UserDefaultInfo;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;
import com.kindroid.hub.utils.Utils;

public class AtMeMyselfModificationActivity extends Activity implements View.OnClickListener {
	private final static String TAG = "AtMeMyselfModificationActivity";
	private static final int USER_CHOOSE_AVATAR = 1;
	private static final int ADDRESS_HANDLER = 0;

	private Button mLayoutButton;
	private ImageView mUserAvater;
	private TextView nickNameTextView;
	private TextView genderTextView;
	private TextView nickNameClickTextView;
	private TextView genderClickTextView;
	private TextView addressTextView;
	private ImageView backBtn;
	private ImageView searchBtn;
	private ImageView locatingImageView;
	
	public static User userInfo;
	private ProgressDialog dlgLoading = null;
	
	//gender dialog views
	private ImageView girlImageView;
	private ImageView manImageView;
	private TextView girlTextView;
	private TextView manTextView;
	private Button confirmBtn;
	private Button cancelBtn;
	
	private boolean girlTag;
	private boolean manTag;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ADDRESS_HANDLER:
				String address = (String) msg.obj;
				Log.d(TAG, "====================>" + address);
				break;
			case 10:
				Bitmap largePhoto = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp.jpg");
				mUserAvater.setImageBitmap(largePhoto);
				Toast.makeText(AtMeMyselfModificationActivity.this, R.string.user_update_avatar_success, Toast.LENGTH_SHORT).show();
				break;
			case -10:
				Toast.makeText(AtMeMyselfModificationActivity.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			case 11:
				dlgLoading.dismiss();
				Intent intent = new Intent(AtMeMyselfModificationActivity.this, HubIndex.class);
				startActivity(intent);
				break;
			case 12:
				dlgLoading.dismiss();
				Toast.makeText(AtMeMyselfModificationActivity.this, R.string.user_register_connect_error, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_myself_modification);
		findViews();
		initComponents();
	}
	
	private void findViews() {
		mLayoutButton = (Button) findViewById(R.id.logoutButton);
		mLayoutButton.setOnClickListener(this);
		mUserAvater = (ImageView) findViewById(R.id.userAvatarImageView);
		mUserAvater.setOnClickListener(this);
		
		nickNameTextView = (TextView) findViewById(R.id.nickNameTextView);
		genderTextView = (TextView) findViewById(R.id.genderTextView);
		nickNameClickTextView = (TextView) findViewById(R.id.nickNameClickTextView);
		genderClickTextView = (TextView) findViewById(R.id.genderClickTextView);
		addressTextView = (TextView) findViewById(R.id.addressTextView);
		locatingImageView = (ImageView) findViewById(R.id.locatingImageView);
		locatingImageView.setOnClickListener(this);
		
		backBtn = (ImageView) findViewById(R.id.btn_back);
		backBtn.setOnClickListener(this);
		
		searchBtn = (ImageView) findViewById(R.id.btn_search);
		searchBtn.setOnClickListener(this);
	}
	
	private void initComponents() {
		if (userInfo != null) {
			nickNameTextView.setText(userInfo.getName());
			if ("f".equals(userInfo.getGender())) {
				genderTextView.setText(getResources().getString(R.string.msg_gender_woman));
			} else if ("m".equals(userInfo.getGender())) {
				genderTextView.setText(getResources().getString(R.string.msg_gender_man));
			} else {
				genderTextView.setText(getResources().getString(R.string.msg_gender_secret_modify));
			}
			setUserAvatar(userInfo.getProfileImageURL().toString());
			nickNameTextView.setOnClickListener(this);
			nickNameClickTextView.setOnClickListener(this);
			genderTextView.setOnClickListener(this);
			addressTextView.setText(userInfo.getLocation());
			genderClickTextView.setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.logoutButton) {
			showProgressDialog();
			new UserLogout().start();
		} else if (view.getId() == R.id.userAvatarImageView) {
			Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
			photoPickerIntent.setType("image/*");
			photoPickerIntent.putExtra("crop", "true");
			photoPickerIntent.putExtra("outputX", 90);
			photoPickerIntent.putExtra("outputY", 90);
			photoPickerIntent.putExtra("aspectX", 1);
			photoPickerIntent.putExtra("aspectY", 1);
			photoPickerIntent.putExtra("return-data", true);
			photoPickerIntent.putExtra("noFaceDetection", true);
			photoPickerIntent.putExtra("scale", true);
			try {
				startActivityForResult(photoPickerIntent, USER_CHOOSE_AVATAR);
			} catch (ActivityNotFoundException ex) {
				ex.printStackTrace();
			}
			return;
		} else if (view.getId() == R.id.nickNameTextView || view.getId() == R.id.nickNameClickTextView) {
			openNickNameDialog();
		} else if (view.getId()  == R.id.genderTextView || view.getId() == R.id.genderClickTextView) {
//			openChooseGenderDialog();
		} else if (view.getId() == R.id.btn_back) {
			finish();
		} else if (view.getId() == R.id.btn_search) {
			Intent intent = new Intent(AtMeMyselfModificationActivity.this, AtMeSearchActivity.class);
			startActivity(intent);
		} 
		if (view.getId() == R.id.locatingImageView) {
			//start thread to locating address
//			new Locating().start();
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == USER_CHOOSE_AVATAR) {
			if (data == null) {
				Log.w(TAG, "Null data, but RESULT_OK, from image picker!");
				return;
			}

			Bitmap photo = (Bitmap)data.getParcelableExtra("data");
			if (photo != null) {
			    byte[] byteArray=ConvertUtils.bitmapToBytes(resizeFile(photo));
				new UpdateUserIcon(byteArray).start();
			}
		}
	}
	
	private Bitmap resizeFile(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		int newWidth = 64;
		int newHeight = 64;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();

		matrix.postScale(scaleWidth, scaleHeight);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height, matrix, true);
		return resizedBitmap;
	}
	
	public class UpdateUserIcon extends Thread {
		public byte[] data;

		public UpdateUserIcon(byte[] data) {
			this.data = data;
		}

		public void run() {
			System.setProperty("weibo4j.oauth.consumerKey",Constant.CONSUMER_KEY);
			System.setProperty("weibo4j.oauth.consumerSecret",Constant.CONSUMER_SECRET);
			Weibo weibo = new Weibo();
			try {
				File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/temp.jpg");
				FileOutputStream fileOutputStream=new FileOutputStream(file);
				fileOutputStream.write(data);
				fileOutputStream.flush();
				fileOutputStream.close();
				weibo.setToken(UserDefaultInfo.getWeiboToken(AtMeMyselfModificationActivity.this), UserDefaultInfo.getWeiboTokenSecret(AtMeMyselfModificationActivity.this));
				User user = weibo.updateProfileImage(file);
				if (user.getStatus() != null) {
					mHandler.sendEmptyMessage(10);
				}else {
					mHandler.sendEmptyMessage(-10);
				}
			} catch (WeiboException e) {
				mHandler.sendEmptyMessage(-10);
				e.printStackTrace();
			} catch (IOException e) {
				mHandler.sendEmptyMessage(-10);
				e.printStackTrace();
			} catch (Exception ex){
				mHandler.sendEmptyMessage(-10);
				ex.printStackTrace();
			}
		}
	}
	
	public class UserLogout extends Thread {
		public void run() {
			System.setProperty("weibo4j.oauth.consumerKey",Constant.CONSUMER_KEY);
			System.setProperty("weibo4j.oauth.consumerSecret",Constant.CONSUMER_SECRET);
			Weibo weibo = new Weibo();
			try {
				weibo.setToken(UserDefaultInfo.getWeiboToken(AtMeMyselfModificationActivity.this), UserDefaultInfo.getWeiboTokenSecret(AtMeMyselfModificationActivity.this));
				User user = weibo.endSession();
				if (user != null) {
					UserDefaultInfo.putWeiboToken(AtMeMyselfModificationActivity.this, null);
					UserDefaultInfo.putWeiboTokenSecret(AtMeMyselfModificationActivity.this, null);
					mHandler.sendEmptyMessage(11);
				}
			} catch (WeiboException e) {
				mHandler.sendEmptyMessage(12);
				e.printStackTrace();
			}
		}
	}
	
	//set user default avatar
	private void setUserAvatar(String avatarUrl) {
		//user's avatar
		AsyncImageLoader imgLoader = new AsyncImageLoader(AtMeMyselfModificationActivity.this);
		
		Bitmap bmp = imgLoader.loadBitmap(avatarUrl, new ImageCallback() {
			@Override
			public void imageLoaded(Bitmap bitmap, String url) {
				mUserAvater.setImageBitmap(bitmap);
			}
			
			public void imageLoaded(Drawable drawable, String url) {
			}
			@Override
			public void imageLoaded(Bitmap bitmap, int position, String url) {
				
			}
			@Override
			public void imageLoaded(Drawable drawable, int position, String url) {
			}
		});
		if(bmp != null) {
			mUserAvater.setImageBitmap(bmp);
		} else {
			mUserAvater.setImageResource(R.drawable.user_default);
		}
	}
	
	class Locating extends Thread {

		@Override
		public void run() {
			Message msg = mHandler.obtainMessage();
			String ip = Utils.getLocalIpAddress();
			String address = DataService.locatingAddress(ip, AtMeMyselfModificationActivity.this);
			msg.what = ADDRESS_HANDLER;
			msg.obj = address;
			mHandler.sendMessage(msg);
		}
		
	}
	
	private void openNickNameDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.at_me_hub_modify_nick_name_dialog, (ViewGroup) findViewById(R.id.nickNameBgLayout));
		final Dialog dialog = new Dialog(AtMeMyselfModificationActivity.this, R.style.group_dialog);
		dialog.setContentView(layout);
	    
	    Button confirmBtn = (Button) layout.findViewById(R.id.confirmButton);
	    Button cancelBtn = (Button) layout.findViewById(R.id.cancelButton);
	    final EditText nickNameEditText = (EditText) layout.findViewById(R.id.nickNameEditText);
	    
	    confirmBtn.setOnClickListener(
	    	new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String nickName = nickNameEditText.getText() + "";
					if (!TextUtils.isEmpty(nickName.trim())) {
						if (nickName.length() <= 20) {
							
							showProgressDialog();
//							String result = DataService.modifyNickName(nickName.trim(), AtMeMyselfModificationActivity.this);
							String result = DataService.modifyNickNameToSina(nickName.trim(), AtMeMyselfModificationActivity.this);
							if (!TextUtils.isEmpty(result) && result.equals("success")) {
								dialog.dismiss();
								nickNameTextView.setText(nickName.trim());
							} else {
								Toast.makeText(AtMeMyselfModificationActivity.this, getResources().getString(R.string.msg_modify_failure), Toast.LENGTH_SHORT).show();
							}
							hideLoadingDialog();
						} else {
							Toast.makeText(AtMeMyselfModificationActivity.this, getResources().getString(R.string.user_register_nickname_valid), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(AtMeMyselfModificationActivity.this, getResources().getString(R.string.msg_nick_name_not_empty), Toast.LENGTH_SHORT).show();
					}
				}
			}
	    );
	    cancelBtn.setOnClickListener(
	    	new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (dialog.isShowing()) {
						dialog.hide();
						dialog.dismiss();
					}
				}
			}
	    );
	    
	    dialog.show();
	}
	
	//选择性别
	public void openChooseGenderDialog() {
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.at_me_hub_modify_gender_dialog, (ViewGroup) findViewById(R.id.nickNameBgLayout));  
	    initGenderView(layout);
	    genderViewListenser(layout);
	}
	
	//初始化性别弹出框控件
	public void initGenderView(View view) {
		girlImageView = (ImageView) view.findViewById(R.id.girlImageView);
		girlTextView = (TextView) view.findViewById(R.id.girlTextView);
		manImageView = (ImageView) view.findViewById(R.id.manImageView);
		manTextView = (TextView) view.findViewById(R.id.manTextView);
		
		confirmBtn = (Button) view.findViewById(R.id.confirmButton);
		cancelBtn = (Button) view.findViewById(R.id.cancelButton);
	}
	
	class GenderOnClickListener implements View.OnClickListener {
		
		@Override
		public void onClick(View view) {
			unSelectedRadio();
			switch (view.getId()) {
			case R.id.girlImageView:
				girlTag = true;
				girlImageView.setImageResource(R.drawable.radio_button_selected);
				break;
			case R.id.girlTextView:
				girlTag = true;
				girlImageView.setImageResource(R.drawable.radio_button_selected);
				break;
			case R.id.manImageView:
				manTag = true;
				manImageView.setImageResource(R.drawable.radio_button_selected);
				break;
			case R.id.manTextView:
				manTag = true;
				manImageView.setImageResource(R.drawable.radio_button_selected);
				break;
			default:
				break;
			}
		}
		
	}
	
	//改变单选框状态
	public void unSelectedRadio() {
		girlImageView.setImageResource(R.drawable.radio_button_unselected);
		manImageView.setImageResource(R.drawable.radio_button_unselected);
		girlTag = false;
		manTag = false;
		
	}
	
	private void genderViewListenser(View view) {
		girlImageView.setOnClickListener(new GenderOnClickListener());
		girlTextView.setOnClickListener(new GenderOnClickListener());
		manImageView.setOnClickListener(new GenderOnClickListener());
		manTextView.setOnClickListener(new GenderOnClickListener());
		final Dialog dialog = new Dialog(AtMeMyselfModificationActivity.this, R.style.group_dialog);
		dialog.setContentView(view);
		
		confirmBtn.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					int gender = 0;
					if (girlTag) {
						gender = 1;
					} else if (manTag) {
						gender = 2;
					}
					showProgressDialog();
					String result = DataService.modifyGender(gender, AtMeMyselfModificationActivity.this);
					if (!TextUtils.isEmpty(result) && result.equals("success")) {
						dialog.dismiss();
						//set gender text view
						if (girlTag) {
							genderTextView.setText(getResources().getString(R.string.msg_gender_woman));
						} else if (manTag) {
							genderTextView.setText(getResources().getString(R.string.msg_gender_man));
						}
					} else {
						Toast.makeText(AtMeMyselfModificationActivity.this, getResources().getString(R.string.msg_modify_failure), Toast.LENGTH_SHORT).show();
					}
					hideLoadingDialog();
				}
			}
		);
		cancelBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (dialog.isShowing()) {
							dialog.hide();
							dialog.dismiss();
						}
					}
				}
		);
		
		dialog.show();
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
