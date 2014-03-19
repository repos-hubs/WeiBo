package com.kindroid.hub.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.kindroid.hub.entity.User;
import com.kindroid.hub.utils.AsyncImageLoader;
import com.kindroid.hub.utils.AsyncImageLoader.ImageCallback;
import com.kindroid.hub.utils.Utils;

public class AtMeMyselfActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "AtMeMyselfActivity";
	private final int USER_DETAILS_HANDLER = 0;
	
	private ImageView userAvatarImageView;
	private TextView userNameTextView;
	private TextView genderTextView;
	private TextView countryTextView;
	private TextView regionTextView;
	
	private TextView approvedMobile;
	private TextView approvedEmail;
	private TextView approvedSinaWeibo;
	private TextView approvedTencentWeibo;
	private ImageView arrowMobileImageView;
	private ImageView arrowEmailImageView;
	private ImageView arrowSinaWeiboImageView;
	private ImageView arrowTencentWeiboImageView;
	
	private TextView mobileBindTextView;
	private TextView emailBindTextView;
	private TextView sinaWeiboBindTextView;
	private TextView tencentWeiboTextView;
	
	private TextView mobileTextView;
	private TextView emailTextView;
	private TextView sinaTextView;
	
	private ProgressDialog dlgLoading = null;
	
	public static User userInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.at_me_hub_myself);
		
		findViews();
		initComponents();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		showProgressDialog();
		new UserDetails().start();
	}

	Handler dataHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			hideLoadingDialog();
			switch (msg.arg1) {
			case USER_DETAILS_HANDLER:
				User userInfo = (User) msg.obj;
				initUserInfo(userInfo);
				break;

			default:
				break;
			}
		}
		
	};
	
	private void findViews() {
		userAvatarImageView = (ImageView) findViewById(R.id.userAvatarImageView);
		userNameTextView = (TextView) findViewById(R.id.userNameTextView);
		genderTextView = (TextView) findViewById(R.id.genderTextView);
		countryTextView = (TextView) findViewById(R.id.countryTextView);
		regionTextView = (TextView) findViewById(R.id.regionTextView);
		
		approvedMobile = (TextView) findViewById(R.id.approvedMobile);
		approvedEmail = (TextView) findViewById(R.id.approvedEmailTextView);
		approvedSinaWeibo = (TextView) findViewById(R.id.approvedSinaWeiboTextView);
		
		mobileBindTextView = (TextView) findViewById(R.id.mobileBindTextView);
		emailBindTextView = (TextView) findViewById(R.id.emailBindTextView);
		sinaWeiboBindTextView = (TextView) findViewById(R.id.sinaWeiboBindTextView);
		
		arrowMobileImageView = (ImageView) findViewById(R.id.arrowMobileImageView);
		arrowEmailImageView = (ImageView) findViewById(R.id.arrowEmailImageView);
		arrowSinaWeiboImageView = (ImageView) findViewById(R.id.arrowSinaWeiboImageView);
		
		mobileTextView = (TextView) findViewById(R.id.mobileTextView);
		emailTextView = (TextView) findViewById(R.id.emailTextView);
		sinaTextView = (TextView) findViewById(R.id.sinaTextView);
	}
	
	private void initComponents() {
		userAvatarImageView.setOnClickListener(this);
		userNameTextView.setOnClickListener(this);
		genderTextView.setOnClickListener(this);
		countryTextView.setOnClickListener(this);
		
		emailBindTextView.setOnClickListener(this);
		emailTextView.setOnClickListener(this);
	}

	class UserDetails extends Thread {

		@Override
		public void run() {
			userInfo = DataService.getUserDetails(AtMeMyselfActivity.this);
			Message msg = dataHandler.obtainMessage();
			msg.arg1 = USER_DETAILS_HANDLER;
			msg.obj = userInfo;
			dataHandler.sendMessage(msg);
		}
		
	}
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.userAvatarImageView || view.getId() == R.id.userNameTextView 
				|| view.getId() == R.id.genderTextView || view.getId() == R.id.countryTextView) {
			Intent intent = new Intent(AtMeMyselfActivity.this, AtMeMyselfModificationActivity.class);
			startActivity(intent);
		} else if (view.getId() == R.id.emailBindTextView || view.getId() == R.id.emailTextView) {
			if (userInfo.getApprovedEmail() == 0) {
				openEmailBindingDialog();
			}
		}
	}
	
	private void initUserInfo(User userInfo) {
		userNameTextView.setText(userInfo.getNickname());
		if (userInfo.getGender() == 1) {
			genderTextView.setText(getResources().getString(R.string.msg_gender_woman));
		} else if (userInfo.getGender() == 2) {
			genderTextView.setText(getResources().getString(R.string.msg_gender_man));
		} else {
			genderTextView.setText(getResources().getString(R.string.msg_gender_secret));
		}
		//set user avatar
		setUserAvatar(userInfo.getAvatarUrl());
		mobileTextView.setText(userInfo.getTelephone());
		emailTextView.setText(userInfo.getEmail());
		sinaTextView.setText(userInfo.getSinaWeibo());
		
		String unApproved = getResources().getString(R.string.msg_at_me_myself_un_certificate);
		String approved = getResources().getString(R.string.msg_at_me_myself_certificate);
		String unBinded = getResources().getString(R.string.msg_at_me_myself_un_binded);
		String binded = getResources().getString(R.string.msg_at_me_myself_binded);
		
		//bind mobile
		if (userInfo.getApprovedMobile() == 0) {
			mobileBindTextView.setText(unApproved);
			arrowMobileImageView.setVisibility(View.VISIBLE);
		} else {
			mobileBindTextView.setText(approved);
			arrowMobileImageView.setVisibility(View.INVISIBLE);
		}
		
		//bind email
		if (userInfo.getApprovedEmail() == 0) {
			emailBindTextView.setText(unApproved);
			arrowEmailImageView.setVisibility(View.VISIBLE);
		} else {
			emailBindTextView.setText(approved);
			arrowEmailImageView.setVisibility(View.INVISIBLE);
		}
		
		//bind sina weibo
		if (userInfo.getApprovedSinaWeibo() == 0) {
			sinaWeiboBindTextView.setText(unBinded);
			arrowSinaWeiboImageView.setVisibility(View.VISIBLE);
			countryTextView.setVisibility(View.INVISIBLE);
			regionTextView.setVisibility(View.INVISIBLE);
		} else {
			sinaWeiboBindTextView.setText(binded);
			arrowSinaWeiboImageView.setVisibility(View.INVISIBLE);
			countryTextView.setVisibility(View.VISIBLE);
			regionTextView.setVisibility(View.VISIBLE);
			countryTextView.setText(userInfo.getAddress());
		}
		
	}
	
	private void setUserAvatar(String avatarUrl) {
		//user's avatar
		AsyncImageLoader imgLoader = new AsyncImageLoader(AtMeMyselfActivity.this);
		
		Bitmap bmp = imgLoader.loadBitmap(avatarUrl, new ImageCallback() {
			@Override
			public void imageLoaded(Bitmap bitmap, String url) {
				userAvatarImageView.setImageBitmap(bitmap);
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
			userAvatarImageView.setImageBitmap(bmp);
		} else {
			userAvatarImageView.setImageResource(R.drawable.user_default);
		}
	}
	
	private void openEmailBindingDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.at_me_hub_binding_email_dialog, (ViewGroup) findViewById(R.id.bindingEmailBgLayout));
		final Dialog dialog = new Dialog(AtMeMyselfActivity.this, R.style.group_dialog);
		dialog.setContentView(layout);
	    
	    Button confirmBtn = (Button) layout.findViewById(R.id.confirmButton);
	    Button cancelBtn = (Button) layout.findViewById(R.id.cancelButton);
	    final EditText emailEditText = (EditText) layout.findViewById(R.id.emailBindingEditText);
	    
	    final Handler dataHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				hideLoadingDialog();
				switch (msg.arg1) {
				case 0:
					String result = (String) msg.obj;
					if (!TextUtils.isEmpty(result) && result.equals("success")) {
						dialog.dismiss();
						hideLoadingDialog();
						Toast.makeText(AtMeMyselfActivity.this, getResources().getString(R.string.msg_login_email_to_activate), Toast.LENGTH_SHORT).show();
					} else {
						hideLoadingDialog();
						Toast.makeText(AtMeMyselfActivity.this, getResources().getString(R.string.msg_binding_failure_try_again), Toast.LENGTH_SHORT).show();
					}
					break;
				default:
					break;
				}
			}
	    	
	    };
	    
	    final Runnable emailBinding = new Runnable() {
			@Override
			public void run() {
				String email = emailEditText.getText() + "";
				String result = DataService.emailBinding(email, AtMeMyselfActivity.this);
				Message msg = dataHandler.obtainMessage();
				msg.arg1 = 0;
				msg.obj = result;
				dataHandler.sendMessage(msg);
			}
		};
	    
	    confirmBtn.setOnClickListener(
	    	new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String email = emailEditText.getText() + "";
					if (!TextUtils.isEmpty(email)) {
						if (Utils.checkEmail(email)) {
							showProgressDialog();
							new Thread(emailBinding).start();
						} else {
							Toast.makeText(AtMeMyselfActivity.this, getResources().getString(R.string.msg_email_error), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(AtMeMyselfActivity.this, getResources().getString(R.string.msg_email_empty), Toast.LENGTH_SHORT).show();
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
	
	/**
	 * Hide loading dialog
	 */
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			this.dlgLoading.dismiss();
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

}
