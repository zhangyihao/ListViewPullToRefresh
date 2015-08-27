# ListViewPullToRefresh
Android project . Refreshing the ListView when user pull from top of mobile and load more data when push


##第一种实现： 通过监听ListView的OnScrollListener事件实现
 1. MainActivity
 2. MyAdapter ListView自定义适配器，继承自BaseAdapter
 3. RefreshListView 自定义ListView，继承自ListView

>上拉加载原理：<br/>
> &nbsp;&nbsp;&nbsp;&nbsp;当滑动ListView时，判断屏幕上最后一个Item位置是否和总Item数量相等，如果不想等，表示未滑动到底部；相等标识滑动到底部，需要加载数据了<br/><br/>
>下拉刷新原理：<br/>
>通过重写onTouchEvent()方法，监听用户在ListView上的触摸事件。

##第二种实现：使用第三方开源库Android-PullToRefresh
pullToRefresh 通过setMode来设置是否可以上拉下拉<br/>
&nbsp;&nbsp;&nbsp;&nbsp;Mode.BOTH：同时支持上拉下拉<br/>
&nbsp;&nbsp;&nbsp;&nbsp;Mode.PULL_FROM_START：只支持下拉Pulling Down<br/>
&nbsp;&nbsp;&nbsp;&nbsp;Mode.PULL_FROM_END：只支持上拉Pulling Up<br/>
也可以用 ptr:ptrMode="both"<br/>
可选值为：disabled（禁用下拉刷新），pullFromStart（仅支持下拉刷新），pullFromEnd（仅支持上拉刷新），both（二者都支持），manualOnly（只允许手动触发）

如果Mode设置成Mode.BOTH，需要设置刷新Listener为OnRefreshListener2，并实现onPullDownToRefresh()、onPullUpToRefresh()两个方法。 

如果Mode设置成Mode.PULL_FROM_START或Mode.PULL_FROM_END，需要设置刷新Listener为OnRefreshListener，同时实现onRefresh()方法。

当然也可以设置为OnRefreshListener2，但是Mode.PULL_FROM_START的时候只调用onPullDownToRefresh()方法，Mode.PULL_FROM的时候只调用onPullUpToRefresh()方法.

如果想上拉、下拉刷新的时候 做一样的操作，那就用OnRefreshListener，上拉下拉的时候都调用

如果想上拉、下拉做不一样的的操作，那就在setOnRefreshListener时 用new OnRefreshListener2<ListView>

当然如果想自己设置上拉下拉中的文字 可以这样
    
	ILoadingLayout startLabels = pullToRefresh.getLoadingLayoutProxy(true, false);
    startLabels.setPullLabel("下拉刷新...");// 刚下拉时，显示的提示
    startLabels.setRefreshingLabel("正在载入...");// 刷新时
    startLabels.setReleaseLabel("放开刷新...");// 下来达到一定距离时，显示的提示
      
    ILoadingLayout endLabels = pullToRefresh.getLoadingLayoutProxy(false, true);
    endLabels.setPullLabel("上拉刷新...");// 刚下拉时，显示的提示
    endLabels.setRefreshingLabel("正在载入...");// 刷新时
    endLabels.setReleaseLabel("放开刷新...");// 下来达到一定距离时，显示的提示  

下拉上拉 图标和文字 位置改动是在PullToRefresh源代码中改的即:PullToRefreshListView.handleStyledAttributes 中lp的Gravity改为CENTER_VERTICAL

如果想要改动图标和文字的距离和布局 在这library项目下这两个文件改pull_to_refresh_header_horizontal.xml、pull_to_refresh_header_vertical.xml

旋转的效果，一般常用的还有，一个箭头倒置的效果，其实也很简单，一个属性：
ptr:ptrAnimationStyle="flip"
去掉 ptr:ptrDrawable="@drawable/ic_launcher"这个属性，如果你希望用下图默认的箭头，你也可以自定义。

ptr:ptrAnimationStyle的取值：flip（翻转动画）， rotate（旋转动画） 。 
ptr:ptrDrawable则就是设置图标了。

####常用的一些属性

当然了，pull-to-refresh在xml中还能定义一些属性：

ptrMode，ptrDrawable，ptrAnimationStyle这三个上面已经介绍过。

ptrRefreshableViewBackground 设置整个mPullRefreshListView的背景色

ptrHeaderBackground 设置下拉Header或者上拉Footer的背景色

ptrHeaderTextColor 用于设置Header与Footer中文本的颜色

ptrHeaderSubTextColor 用于设置Header与Footer中上次刷新时间的颜色

ptrShowIndicator如果为true会在mPullRefreshListView中出现icon，右上角和右下角，挺有意思的。

ptrHeaderTextAppearance ， ptrSubHeaderTextAppearance分别设置拉Header或者上拉Footer中字体的类型颜色等等。

ptrRotateDrawableWhilePulling当动画设置为rotate时，下拉是是否旋转。

ptrScrollingWhileRefreshingEnabled刷新的时候，是否允许ListView或GridView滚动。觉得为true比较好。

ptrListViewExtrasEnabled 决定了Header，Footer以何种方式加入mPullRefreshListView，true为headView方式加入，就是滚动时刷新头部会一起滚动。

最后2个其实对于用户体验还是挺重要的，如果设置的时候考虑下~。其他的属性自己选择就好。

注：上述属性很多都可以代码控制，如果有需要可以直接mPullRefreshListView.set属性名 查看

###使用上得一些注意点
1. 设置列表项单击事件
mSchemeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Object item = parent.getAdapter().getItem(position);//注意此处是先获取到Adapter，然后才取Item
	}
			
});

2. 设置列表项长按事件
mSchemeListView.getRefreshableView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		return true;
	}		
});
