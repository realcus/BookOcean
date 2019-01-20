package com.book.ocean.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.book.ocean.R;
import com.book.ocean.activity.BookViewActivity;
import com.book.ocean.adapter.SearchAdapter;
import com.book.ocean.bean.Book;
import com.book.ocean.common.MyUtil;
import com.book.ocean.net.BaseAsyncHttp;
import com.book.ocean.net.HttpResponseHandler;
import com.book.ocean.view.CircularProgressView;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link ContentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContentFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ListView mListView;
    private SearchAdapter mAdapter;
    private List<Book> mBooks = new ArrayList<>();

    private PullRefreshLayout layout;
    private CircularProgressView progressView;
    private View loadMoreView;

    public int last_index;
    public int total_index;

    //记录上次滚动之后的第一个可见item和最后一个item
    private int mFirstVisibleItem = -1;
    private int mLastVisibleItem = -1;

    public boolean isLoading = false;//表示是否正处于加载状态


    public ContentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ContentFragment.
     */
    public static Fragment newInstance(String param1, String param2) {
        ContentFragment fragment = new ContentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();

    }

    private void initView() {

        View view = getView();
        mListView = view.findViewById(R.id.lv);
        layout = view.findViewById(R.id.swipeRefreshLayout);
        progressView = view.findViewById(R.id.progress_view);

        loadMoreView = getActivity().getLayoutInflater().inflate(R.layout.load_more_layout, null);
        loadMoreView.setVisibility(View.VISIBLE);

    }


    private void initData() {

        getRequestData(mParam1,0,10);

//        initInfos();

        mAdapter=new SearchAdapter(getActivity(),mBooks);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(getActivity(),BookViewActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("book", mBooks.get(i));
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(last_index == total_index && (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE))
                {
                    //表示此时需要显示刷新视图界面进行新数据的加载(要等滑动停止)
                    if(!isLoading)
                    {
                        //不处于加载状态的话对其进行加载
                        isLoading = true;
                        //设置刷新界面可见
                        loadMoreView.setVisibility(View.VISIBLE);
                        getRequestData(mParam1,last_index,5);
                    }
                }


            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                last_index = firstVisibleItem+visibleItemCount;
                total_index = totalItemCount;


                //listview第一次载入时，两者都为-1
                boolean shouldAnimate = (mFirstVisibleItem != -1) && (mLastVisibleItem != -1);
                //滚动时最后一个item的位置
                int lastVisibleItem = firstVisibleItem + visibleItemCount -1;
                if(shouldAnimate){//第一次不需要加载动画
                    int indexAfterFist =0;
                    //如果出现这种情况，说明是在向上scroll，如果scroll比较快的话，一次可能出现多个新的view，我们需要用循环
                    //去获取所有这些view，然后执行动画效果
                    while(firstVisibleItem + indexAfterFist < mFirstVisibleItem){
                        View animateView = view.getChildAt(indexAfterFist);//获取item对应的view
                        doAnimate(animateView, false);
                        indexAfterFist ++;
                    }

                    int indexBeforeLast = 0;
                    //向下scroll, 情况类似，只是计算view的位置时不一样
                    while(lastVisibleItem - indexBeforeLast > mLastVisibleItem){
                        View animateView = view.getChildAt(lastVisibleItem - indexBeforeLast - firstVisibleItem);
                        doAnimate(animateView, true);
                        indexBeforeLast ++;
                    }
                }

                mFirstVisibleItem = firstVisibleItem;
                mLastVisibleItem = lastVisibleItem;

            }
        });
        mListView.addFooterView(loadMoreView,null,false);


        // listen refresh event
        layout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // start refresh
                layout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBooks.clear();
                        getRequestData(mParam1,0,10);
                        MyUtil.toastMessage(getActivity(),"刷新成功！");

                        layout.setRefreshing(false);
                    }
                }, 2000);

            }
        });


    }

    /**
     * 执行动画
     * @param view
     * @param scrollDown
     */
    private void doAnimate(View view, boolean scrollDown)
    {
        //我们这里先写一个最简单地动画，GROW
        ViewPropertyAnimator animator = view.animate().setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator());

        view.setPivotX(view.getWidth()/2);
        view.setPivotY(view.getHeight()/2);
        view.setScaleX(0.01f);
        view.setScaleY(0.01f);

        animator.scaleX(1.0f).scaleY(1.0f);
        animator.start();
    }



    public void getRequestData(String str,int start,int count){
        RequestParams params=new RequestParams();
        params.put("tag",str.trim());
        params.put("start",start+"");
        params.put("count",count+"");
        BaseAsyncHttp.getReq("/v2/book/search",params,new HttpResponseHandler() {
            @Override
            public void jsonSuccess(JSONObject resp) {
//                mBooks.clear();
                progressView.setVisibility(View.GONE);
                JSONArray jsonbooks=resp.optJSONArray("books");
                for (int i=0;i<jsonbooks.length();i++){
                    Book mBook=new Book();
                    mBook.setId(jsonbooks.optJSONObject(i).optString("id"));
                    mBook.setRate(jsonbooks.optJSONObject(i).optJSONObject("rating").optDouble("average"));
                    mBook.setReviewCount(jsonbooks.optJSONObject(i).optJSONObject("rating").optInt("numRaters"));
                    String authors="";
                    for (int j=0;j<jsonbooks.optJSONObject(i).optJSONArray("author").length();j++){
                        authors=authors+" "+jsonbooks.optJSONObject(i).optJSONArray("author").optString(j);
                    }
                    mBook.setAuthor(authors);
                    String tags="";
                    for (int j=0;j<jsonbooks.optJSONObject(i).optJSONArray("tags").length();j++){
                        tags=tags+" "+jsonbooks.optJSONObject(i).optJSONArray("tags").optJSONObject(j).optString("name");
                    }
                    mBook.setTag(tags);
                    mBook.setAuthorInfo(jsonbooks.optJSONObject(i).optString("author_intro"));
                    mBook.setBitmap(jsonbooks.optJSONObject(i).optString("image"));
                    mBook.setId(jsonbooks.optJSONObject(i).optString("id"));
                    mBook.setTitle(jsonbooks.optJSONObject(i).optString("title"));
                    mBook.setPublisher(jsonbooks.optJSONObject(i).optString("publisher"));
                    mBook.setPublishDate(jsonbooks.optJSONObject(i).optString("pubdate"));
                    mBook.setISBN(jsonbooks.optJSONObject(i).optString("isbn13"));
                    mBook.setSummary(jsonbooks.optJSONObject(i).optString("summary"));
                    mBook.setPage(jsonbooks.optJSONObject(i).optString("pages"));
                    mBook.setPrice(jsonbooks.optJSONObject(i).optString("price"));
                    mBook.setContent(jsonbooks.optJSONObject(i).optString("catalog"));
                    mBook.setUrl(jsonbooks.optJSONObject(i).optString("ebook_url"));
                    mBooks.add(mBook);
                }
                updateToView();
            }
            @Override
            public void jsonFail(JSONObject resp) {
                MyUtil.toastMessage(getActivity(),"网络出错！");
            }
        });
    }

    public void updateToView(){
        mAdapter.setData(mBooks);
        mAdapter.notifyDataSetChanged();
        if(isLoading){
            loadComplete();
        }

    }
    /**
     * 加载完成
     */
    public void loadComplete()
    {
        loadMoreView.setVisibility(View.GONE);//设置刷新界面不可见
        isLoading = false;//设置正在刷新标志位false
//        MainActivity.this.invalidateOptionsMenu();
//        mListView.removeFooterView(loadMoreView);//如果是最后一页的话，则将其从ListView中移出
    }


}
