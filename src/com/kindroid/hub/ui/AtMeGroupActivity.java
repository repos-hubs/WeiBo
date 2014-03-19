package com.kindroid.hub.ui;

import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kindroid.hub.R;
import com.kindroid.hub.data.DataService;
import com.kindroid.hub.entity.GroupFriend;
import com.kindroid.hub.widget.AccordionItemView;
import com.kindroid.hub.widget.AccordionView;
import com.kindroid.hub.widget.AccordionView.OnGridItemClickListener;
import com.kindroid.hub.widget.AccordionView.OnItemEditClickListener;
import com.kindroid.hub.widget.AccordionView.OnSelectedItemChangedListener;

public class AtMeGroupActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "AtMeGroupActivity";
	private final int ADD_GROUP_HANDLER = 1;
	
	private AccordionView acdCategory = null;
	private TextView addGroupTextView;
	private ProgressDialog dlgLoading = null;
	/**
	 * pagination
	 */
	private int lastItem = 0;
	private int PAGE_INDEX = 1;
	private final static int PAGE_SIZE = 15;
	private int currentPage = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.at_me_hub_group);
		findViews();
		
		new LoadGroup().start();
	}
	
	private void findViews() {
		addGroupTextView = (TextView) findViewById(R.id.addGroupTextView);
		addGroupTextView.setOnClickListener(this);
		// get category accordion control
		this.acdCategory = (AccordionView) super.findViewById(R.id.acd_category);
		this.acdCategory.setOnSelectedTabChangedListener(new OnSelectedItemChangedListener() {
			@Override
			public void onSelectedItemChanged(AccordionItemView view) {
				AtMeGroupActivity.this.acdCategory.collapseAllWithout(view);
			}
		});
		this.acdCategory.setOnGridItemClickListener(new OnGridItemClickListener() {
			@Override
			public void onGridItemClick(GroupFriend item) {
			}
		});
		this.acdCategory.setOnItemEditClickListener(new OnItemEditClickListener() {
			
			@Override
			public void onItemEditClick(GroupFriend item) {
				Log.v(TAG, "item edit:" + item.getId());
			}
		});
	}
	
	private Handler dataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			hideLoadingDialog();
			switch (msg.arg1) {
			case 0:
				@SuppressWarnings("unchecked")
				List<GroupFriend> categories = (List<GroupFriend>) msg.obj;
				for (int i = 0; i < categories.size(); i++) {
					//System.out.println(categories.get(i).getName());
//					AccordionItemView aiv = new AccordionItemView(BrowseActivity.super.getApplicationContext(), null);
//					aiv.init(categories.get(i));
					AtMeGroupActivity.this.acdCategory.addItem(categories.get(i));
				}
				break;
			case ADD_GROUP_HANDLER:
				String result = (String) msg.obj;
			default:
				break;
			}
		}
	};
	/**
	 * thread using to load group
	 */
	class LoadGroup extends Thread {
		public void run() {
			// instance a message with code 2
			Message msg = AtMeGroupActivity.this.dataHandler.obtainMessage();
			msg.arg1 = -1;// -1:error; 0:ok;

			// call webservice get response data
			List<GroupFriend> data = DataService.getGroupsByUser(currentPage, AtMeGroupActivity.this);
			if (data != null) {
				msg.arg1 = 0;
				msg.obj = data;
			}

			// send message
			AtMeGroupActivity.this.dataHandler.sendMessage(msg);
		}
	}
	/*public Runnable thdGroup = new Runnable() {
		public void run() {
			// instance a message with code 2
			Message msg = AtMeGroupActivity.this.dataHandler.obtainMessage();
			msg.arg1 = -1;// -1:error; 0:ok;

			// call webservice get response data
			List<GroupFriend> data = DataService.getGroupsByUser(currentPage);
			if (data != null) {
				msg.arg1 = 0;
				msg.obj = data;
			}

			// send message
			AtMeGroupActivity.this.dataHandler.sendMessage(msg);
		}
	};*/
	
	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.addGroupTextView) {
			openAddGroupDialog();
		}
	}
	
	private void openAddGroupDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.at_me_hub_group_add_group_dialog, (ViewGroup) findViewById(R.id.addGroupBgLayout));
		final Dialog dialog = new Dialog(AtMeGroupActivity.this, R.style.group_dialog);
		dialog.setContentView(layout);
	    
	    Button confirmBtn = (Button) layout.findViewById(R.id.confirmButton);
	    Button cancelBtn = (Button) layout.findViewById(R.id.cancelButton);
	    final EditText groupNameEditText = (EditText) layout.findViewById(R.id.groupNameEditText);
	    
	    confirmBtn.setOnClickListener(
	    	new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					String groupName = groupNameEditText.getText() + "";
					if (!TextUtils.isEmpty(groupName.trim())) {
						String result = DataService.createGroup(groupName.trim(), AtMeGroupActivity.this);
						if (!TextUtils.isEmpty(result) && result.equals("success")) {
							dialog.dismiss();
							AtMeGroupActivity.this.acdCategory.removeContainerViews();
							showProgressDialog();
							new LoadGroup().start();
						} else {
							Toast.makeText(AtMeGroupActivity.this, getResources().getString(R.string.msg_add_group_failure), Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(AtMeGroupActivity.this, getResources().getString(R.string.msg_add_group_not_empty), Toast.LENGTH_SHORT).show();
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
