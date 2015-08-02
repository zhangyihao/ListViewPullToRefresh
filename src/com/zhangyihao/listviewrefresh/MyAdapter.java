package com.zhangyihao.listviewrefresh;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MyAdapter extends BaseAdapter {
	private List<String> dataList;
	LayoutInflater inflater;
	
	public MyAdapter(Context context, List<String> dataList) {
		super();
		this.dataList = dataList;
		inflater = LayoutInflater.from(context);
	}
	
	public void onDataChange(List<String> dataList) {
		this.dataList = dataList;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint({ "ViewHolder", "InflateParams" })
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.activity_item, null);
		TextView textView = (TextView)convertView.findViewById(R.id.item_title);
		String value = dataList.get(position);
		textView.setText(value);
		return convertView;
	}
}
