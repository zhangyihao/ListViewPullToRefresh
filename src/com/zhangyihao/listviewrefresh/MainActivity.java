package com.zhangyihao.listviewrefresh;

import java.util.ArrayList;
import java.util.List;

import com.zhangyihao.listviewrefresh.RefreshListView.ILoadListener;
import com.zhangyihao.listviewrefresh.RefreshListView.IRefreshListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends Activity implements ILoadListener, IRefreshListener{

	private List<String> dataList;
	private MyAdapter adapter;
	private RefreshListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initData();
		initView();
	}
	
	private void initData() {
		dataList = new ArrayList<String>();
		for(int i=0; i<20; i++) {
			dataList.add("data..."+i);
		}
	}
	
	private void initView() {
		adapter = new MyAdapter(MainActivity.this, dataList);
		listView = (RefreshListView)this.findViewById(R.id.main_listView);
		listView.setLoadListener(this);
		listView.setRefreshListener(this);
		listView.setAdapter(adapter);
	}
	
	@Override
	public void onRefresh() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				getRefreshData();
				adapter.onDataChange(dataList);
				listView.completeRefresh();
			}
			
		}, 2000);
	}

	@Override
	public void onLoad() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				getLoadData();
				adapter.onDataChange(dataList);
				listView.completeLoad();
			}
			
		}, 2000);
	}
	
	public void getLoadData() {
		dataList.add("上拉加载。。");
		dataList.add("上拉加载。。。");
	}
	
	public void getRefreshData() {
		dataList.add(0, "下拉刷新。。");
		dataList.add(0, "下拉刷新。。。");
	}
	
}
