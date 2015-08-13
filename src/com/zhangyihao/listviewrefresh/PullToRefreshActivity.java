package com.zhangyihao.listviewrefresh;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class PullToRefreshActivity extends Activity implements OnRefreshListener<ListView> {

	private ArrayList<String> dataList;  
    private PullToRefreshListView mListView;  
    private MyAdapter mAdapter;  
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pulltorefresh);
		initData();
		initView();
	}
	
	private void initView() {
		mAdapter = new MyAdapter(this, dataList);
		mListView = (PullToRefreshListView)this.findViewById(R.id.pulltorefresh_listview);
		mListView.setAdapter(mAdapter);
		mListView.setMode(Mode.PULL_FROM_START);
		mListView.setOnRefreshListener(this);
	}
	
	private void initData() {
		dataList = new ArrayList<String>();
		for(int i=0; i<5; i++) {
			dataList.add("data..."+i);
		}
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> view) {
		new FinishRefresh().execute();
	}
	
    private class FinishRefresh extends AsyncTask<Void, Void, List<String>>{

		@Override
		protected List<String> doInBackground(Void... params) {
			for(int i=0; i<3; i++) {
				dataList.add("data refresh"+i);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<String> result) {
			mAdapter.onDataChange(dataList);
			mListView.onRefreshComplete();
		}
		
    }  
}
