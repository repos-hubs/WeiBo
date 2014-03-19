package com.kindroid.hub.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.kindroid.hub.R;
import com.kindroid.hub.entity.GroupFriend;


public class AccordionView extends ScrollView {
	/**
	 * children list
	 */
	private List<AccordionItemView> items = null;
	/**
	 * current selected child (expanded item)
	 */
	private AccordionItemView selectedItem = null;
	
	private LinearLayout container = null;

	private ProgressDialog dlgLoading = null;
	/**
	 * /item Click event listener
	 */
	private OnTabClickListener onTabClickListener = null;
	private OnGridItemClickListener onGridItemClickListener = null;
	private OnItemEditClickListener onItemEditClickListener = null;
	public void setOnGridItemClickListener(
			OnGridItemClickListener onGridItemClickListener) {
		this.onGridItemClickListener = onGridItemClickListener;
	}
	


	public void setOnItemEditClickListener(
			OnItemEditClickListener onItemEditClickListener) {
		this.onItemEditClickListener = onItemEditClickListener;
	}

	/**
	 * Selected item Changed Listener
	 */
	private OnSelectedItemChangedListener onSelectedTabChangedListener = null;

	/**
	 * @description TODO
	 * @param context
	 * @param attrs
	 */
	public AccordionView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.items = new ArrayList<AccordionItemView>();

		// inflate view from /res/layout/accordion_item.xml
		String infServiString = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(infServiString);
		inflater.inflate(R.layout.accordion, this, true);
		
		this.container = (LinearLayout) super.findViewById(R.id.pnl_accrodion);
	}
	/**
	 * <p>function description</p>
	 *
	 * @param onTabClickListener
	 */
	public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
		this.onTabClickListener = onTabClickListener;
	}
	
	/**
	 * <p>function description</p>
	 *
	 * @param onSelectedTabChangedListener
	 */
	public void setOnSelectedTabChangedListener(OnSelectedItemChangedListener onSelectedTabChangedListener) {
		this.onSelectedTabChangedListener = onSelectedTabChangedListener;
	}

	public static interface OnTabClickListener {
		public abstract void onTabClick(int id);
	}
	
	public static interface OnSelectedItemChangedListener {
		public abstract void onSelectedItemChanged(AccordionItemView item);
	}
	
	public static interface OnGridItemClickListener {
		public abstract void onGridItemClick(GroupFriend item);
	}
	
	public static interface OnItemEditClickListener {
		public abstract void onItemEditClick(GroupFriend item);
	}
	
	/**
	 * collapse all other items
	 * @param id
	 */
	public void collapseAllWithout(String id) {
		for(AccordionItemView aiv : this.items){
			if(!id.equalsIgnoreCase(aiv.getTag().toString())) {
				aiv.collapse();
			}
		}
	}
	
	/**
	 * collapse all other items
	 * @param view
	 */
	public void collapseAllWithout(AccordionItemView view) {
//		this.collapseAllWithout(view.getTag().toString());
	}
	
	/**
	 * get specify item
	 * @param id
	 * @return
	 */
	public AccordionItemView getItem(String id) {
		//AccordionItemView view = null;
		for(AccordionItemView aiv : this.items){
			if(id.equalsIgnoreCase(aiv.getTag().toString())) {
				return aiv;
			}
		}
		return null;
	}
	
	public void removeContainerViews() {
		this.container.removeAllViews();
		this.items.clear();
	}

	/**
	 * <p>function description</p>
	 *
	 * @param item
	 */
	public void addItem(GroupFriend item){
		AccordionItemView aiv = new AccordionItemView(super.getContext(), null);
		aiv.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
		aiv.init(item);
		aiv.setTag(item);
		this.container.addView(aiv);
		this.items.add(aiv);
		
		aiv.setOnClickListener(new AccordionItemView.OnClickListener() {
			
			@Override
			public void onClick(AccordionItemView view, GroupFriend item) {
				// TODO Auto-generated method stub
				if(view.isExpanded()) {
					view.collapse();
				} else {
//					showLoadingDialog();
					view.expand();
//					hideLoadingDialog();
				}
				// TODO Auto-generated method stub
				if(AccordionView.this.onTabClickListener != null) {
					AccordionView.this.onTabClickListener.onTabClick(view.getId());
				}
				if(view != AccordionView.this.selectedItem) {
					AccordionView.this.selectedItem = (AccordionItemView) view;
					if(AccordionView.this.onSelectedTabChangedListener != null){
						AccordionView.this.onSelectedTabChangedListener.onSelectedItemChanged(view);
					}
				}
			}
		});
		
		
//		aiv.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				
//				AccordionItemView item = (AccordionItemView) v;
//				if(item.isExpanded()) {
//					item.collapse();
//				} else {
////					showLoadingDialog();
//					item.expand();
////					hideLoadingDialog();
//				}
//				// TODO Auto-generated method stub
//				if(AccordionView.this.onTabClickListener != null) {
//					AccordionView.this.onTabClickListener.onTabClick(v.getId());
//				}
//				if(v != AccordionView.this.selectedItem) {
//					AccordionView.this.selectedItem = (AccordionItemView) v;
//					if(AccordionView.this.onSelectedTabChangedListener != null){
//						AccordionView.this.onSelectedTabChangedListener.onSelectedItemChanged((AccordionItemView) v);
//					}
//				}
//			}
//		});
		aiv.setOnItemClickListener(new AccordionItemView.OnItemClickListener() {
			@Override
			public void onItemClick(GroupFriend item) {
				// TODO Auto-generated method stub
				if(AccordionView.this.onGridItemClickListener != null)
					AccordionView.this.onGridItemClickListener.onGridItemClick(item);
			}
		});
		aiv.setOnEditClickListener(new AccordionItemView.OnEditClickListener() {
			
			@Override
			public void onEditClick(GroupFriend item) {
				// TODO Auto-generated method stub
				if(AccordionView.this.onItemEditClickListener != null)
					AccordionView.this.onItemEditClickListener.onItemEditClick(item);
				
			}
		});
	}
	
	/**
	 * Show loading dialog
	 *//*
	private void showLoadingDialog() {
		if (this.dlgLoading == null) {
			this.dlgLoading = ProgressDialog.show(getContext(), "", getResources().getText(R.string.msg_loading), true, true);
		} else {
			this.dlgLoading.show();
		}
	}
		
	*//**
	 * Hide loading dialog
	 *//*
	private void hideLoadingDialog() {
		if (dlgLoading != null) {
			
			this.dlgLoading.dismiss();
		}
	}*/
}
