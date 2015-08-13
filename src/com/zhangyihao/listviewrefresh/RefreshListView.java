package com.zhangyihao.listviewrefresh;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshListView extends ListView implements OnScrollListener {

	private View mHeader;
	private View mFooter;
	private TextView mUpdateTimeTv;

	/**头布局文件的高度*/
	private int headerHeight;
	private int firstVisibleItem; // 当前第一个可见的item的位置
	/**listview 当前滚动状态*/
	private int scrollState;
	/**标记，当前是在listview最顶端摁下的*/
	private boolean isRemark;
	/**摁下时的Y值*/
	private int startY;
	/**当前的状态*/
	private int state;
	/**正常状态*/
	private final int NONE = 0;
	/**提示下拉刷新状态*/
	private final int PULL = 1;
	/**提示释放刷新状态*/
	private final int RELESE = 2;
	/**提示正在刷新状态*/
	private final int REFLASHING = 3;

	private int lastVisibleItem; // 当前最后一个可见的item的位置
	private int totalItemCount; // 总数量
	private boolean isLoading; // 是否正在加载

	private ILoadListener mLoadListener;
	private IRefreshListener mRefreshListener;

	public interface ILoadListener {
		public void onLoad();
	}

	public interface IRefreshListener {
		public void onRefresh();
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public RefreshListView(Context context) {
		super(context);
		initView(context);
	}

	@SuppressLint("InflateParams")
	private void initView(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		mHeader = inflater.inflate(R.layout.activity_header, null);
		mFooter = inflater.inflate(R.layout.activity_footer, null);

		measureView(mHeader);
		headerHeight = mHeader.getMeasuredHeight();
		setHeaderTopPadding(mHeader, -headerHeight);// 初始化时，隐藏头布局
		mUpdateTimeTv = (TextView) mHeader.findViewById(R.id.header_lastupdate_time);

		setFooterVisible(false); // 初始化时，隐藏脚布局
		this.addHeaderView(mHeader);
		this.addFooterView(mFooter);
		this.setOnScrollListener(this);
	}

	private void measureView(View view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (params == null) {
			params = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int width = ViewGroup.getChildMeasureSpec(0, 0, params.width);
		int tmpHight = params.height;
		int height;
		if (tmpHight > 0) {
			height = MeasureSpec.makeMeasureSpec(tmpHight, MeasureSpec.EXACTLY);
		} else {
			height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		view.measure(width, height);
	}

	/**
	 * 设置布局的上边距，用于隐藏头部和脚部加载提示
	 * 
	 * @param topPadding
	 */
	private void setHeaderTopPadding(View view, int topPadding) {
		view.setPadding(view.getPaddingLeft(), topPadding,
				view.getPaddingRight(), view.getPaddingBottom());
		view.invalidate();
	}

	private void setFooterVisible(boolean isVisible) {
		if (isVisible) {
			mFooter.findViewById(R.id.footer).setVisibility(View.VISIBLE);
		} else {
			mFooter.findViewById(R.id.footer).setVisibility(View.GONE);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
		this.lastVisibleItem = firstVisibleItem + visibleItemCount;
		this.totalItemCount = totalItemCount;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
		// 如果当前滑动状态为停止状态，且最后一个可见Item索引等于Item总数，表示滑动最后一个Item，需要加载数据了
		if (totalItemCount == lastVisibleItem
				&& SCROLL_STATE_IDLE == scrollState) {
			if (!isLoading) {
				isLoading = true;
				setFooterVisible(true);
				mLoadListener.onLoad();
			}
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN://表示用户开始触摸
			if(0==firstVisibleItem) {
				isRemark = true;
				startY = (int)ev.getY();
			}
			break;
		case MotionEvent.ACTION_MOVE://表示用户在移动(手指或者其他) 
			onMove(ev);
			break;
		case MotionEvent.ACTION_UP://表示用户抬起了手指  
			if(state == RELESE) {//如果此时状态为释放刷新状态，将状态置为正在刷新状态，并加载数据
				state = REFLASHING;
				reflashViewByState();
				mRefreshListener.onRefresh();
			} else if (state == PULL) {//如果此时状态为下拉状态，置为最开始的状态
				state = NONE;
				isRemark = false;
				reflashViewByState();
			}
			break;
		}
		return super.onTouchEvent(ev);
	}
	
	/**
	 * 判断移动过程操作；
	 * 
	 * @param ev
	 */
	private void onMove(MotionEvent ev) {
		if(!isRemark) {
			return;
		}
		int tmpY = (int)ev.getY();
		int space = tmpY - startY; //手指在屏幕间滑动距离
		int topPadding = space - headerHeight;
		int breakPoint = headerHeight + 30;//释放刷新状态和下拉刷新状态临界点
		Log.i("startY", "startY: "+startY);
		Log.i("startY", "space: "+space);
		Log.i("startY", "topPadding: "+topPadding);
		Log.i("startY", "breakPoint: "+breakPoint);
		switch(state) {
		case NONE:
			if(space>0) {
				//如果滑动距离大于0, 表示正在下拉，将状态置为正在下拉
				state = PULL;
				reflashViewByState();
			}
			break;
		case PULL:
			setHeaderTopPadding(mHeader, topPadding);
			//如果在提示下拉刷新状态下，滑动距离超过临界点且为ListView为正在滑动状态，将状态置为释放刷新状态
			if(space>breakPoint && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
				state = RELESE;
				reflashViewByState();
			}
			break;
		case RELESE:
			setHeaderTopPadding(mHeader, topPadding);
			//如果在提示释放刷新状态下，滑动一定距离小于临界点，将状态置为下拉刷新状态
			if(space < breakPoint) {
				state = PULL;
				reflashViewByState();
			} else if (space <= 0) { //如果滑动距离小于0，置为普通状态
				state = NONE;
				isRemark = false;
				reflashViewByState();
			}
			break;
		}
	}

	/**
	 * 根据当前状态，改变界面显示；
	 */
	private void reflashViewByState() {
		TextView tip = (TextView) mHeader.findViewById(R.id.header_tip);
		ImageView arrow = (ImageView) mHeader.findViewById(R.id.header_arrow);
		ProgressBar progress = (ProgressBar) mHeader.findViewById(R.id.header_progress);
		RotateAnimation anim = new RotateAnimation(0, 180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim.setDuration(500);
		anim.setFillAfter(true);
		RotateAnimation anim1 = new RotateAnimation(180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		anim1.setDuration(500);
		anim1.setFillAfter(true);
		switch (state) {
		case NONE:
			arrow.clearAnimation();
			setHeaderTopPadding(mHeader, -headerHeight);
			break;

		case PULL:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("下拉可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim1);
			break;
		case RELESE:
			arrow.setVisibility(View.VISIBLE);
			progress.setVisibility(View.GONE);
			tip.setText("松开可以刷新！");
			arrow.clearAnimation();
			arrow.setAnimation(anim);
			break;
		case REFLASHING:
			setHeaderTopPadding(mHeader, 50);
			arrow.setVisibility(View.GONE);
			progress.setVisibility(View.VISIBLE);
			tip.setText("正在刷新...");
			arrow.clearAnimation();
			break;
		}
	}
	
	public void setLoadListener(ILoadListener loadListener) {
		this.mLoadListener = loadListener;
	}

	public void setRefreshListener(IRefreshListener refreshListener) {
		this.mRefreshListener = refreshListener;
	}

	public void completeLoad() {
		this.isLoading = false;
		setFooterVisible(false);
	}
	
	public void completeRefresh() {
		state = NONE;
		isRemark = false;
		reflashViewByState();
	}

}
