package com.book.ocean.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.book.ocean.R;
import com.book.ocean.adapter.AnnotationAdapter;
import com.book.ocean.bean.Annotation;
import com.book.ocean.net.BaseAsyncHttp;
import com.book.ocean.net.HttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**

 */
public class AnnotationListActivity extends Activity{

    private ListView mLvAnnotation;
    private List<Annotation> mAnnotations=new ArrayList<Annotation>();
    private AnnotationAdapter mAdapter;
    private String bookid,bookname;
    private SwipeRefreshLayout mSrLayout;

    private int hasNum=0; //已经加载的数量

    //记录上次滚动之后的第一个可见item和最后一个item
    private int mFirstVisibleItem = -1;
    private int mLastVisibleItem = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);

        bookid=getIntent().getStringExtra("id");
        bookname=getIntent().getStringExtra("name");
        this.getActionBar().setTitle("《"+bookname+"》的笔记");

        mSrLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        mSrLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                hasNum=0;
                reqAnnotationList(0,20);
            }
        });
        mSrLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mLvAnnotation=(ListView)findViewById(R.id.lv_annotation);
        mLvAnnotation.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // 判断滚动到底部
                        if (mLvAnnotation.getLastVisiblePosition() == (mLvAnnotation.getCount() - 1)) {
                            mSrLayout.setRefreshing(true);
                            reqAnnotationList(hasNum,20);
                        }
                        break;
                }
            }
        });
        mLvAnnotation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(AnnotationListActivity.this,AnnotaionDetailActivity.class);

                Bundle bundle = new Bundle();
                bundle.putSerializable("annotation",mAnnotations.get(position));
                bundle.putString("name",bookname);
                intent.putExtra("annotation_bundle",bundle);
//                intent.putExtra("annotation",mAnnotations.get(position));
                startActivity(intent);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override public void run() {
                hasNum=0;
                mSrLayout.setRefreshing(true);
                reqAnnotationList(0,20);
            }
        }, 200);
        mAdapter=new AnnotationAdapter(AnnotationListActivity.this,mAnnotations);
        mLvAnnotation.setAdapter(mAdapter);
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
    private void reqAnnotationList(int start,int count){
        RequestParams params=new RequestParams();
        params.put("start",start);
        params.put("count",count);
        BaseAsyncHttp.getReq("/v2/book/" + bookid + "/annotations", params, new HttpResponseHandler() {
            @Override
            public void jsonSuccess(JSONObject resp) {
                if(hasNum==0)
                    mAnnotations.clear();
                JSONArray jsons=resp.optJSONArray("annotations");
                for (int i=0;i<jsons.length();i++){
                    Annotation annotation=new Annotation();
                    annotation.setAuthor(jsons.optJSONObject(i).optJSONObject("author_user").optString("name"));
                    annotation.setAuthorHead(jsons.optJSONObject(i).optJSONObject("author_user").optString("avatar"));
                    annotation.setAbstract(jsons.optJSONObject(i).optString("abstract"));
                    annotation.setCheapter(jsons.optJSONObject(i).optString("chapter"));
                    annotation.setContent(jsons.optJSONObject(i).optString("content"));
                    annotation.setPage(jsons.optJSONObject(i).optInt("page_no"));
                    annotation.setTime(jsons.optJSONObject(i).optString("time"));
                    mAnnotations.add(annotation);
                }
                if(mAnnotations.size()==0) {
                    Toast.makeText(AnnotationListActivity.this,"没有发现本书的读书笔记",Toast.LENGTH_SHORT).show();
                    AnnotationListActivity.this.finish();
                }

                mAdapter.setList(mAnnotations);
                mAdapter.notifyDataSetChanged();
                if(hasNum==resp.optInt("total"))
                    Toast.makeText(AnnotationListActivity.this,"没有更多的读书笔记",Toast.LENGTH_SHORT).show();
                hasNum = mAnnotations.size();
                mSrLayout.setRefreshing(false);

            }

            @Override
            public void jsonFail(JSONObject resp) {
                Toast.makeText(AnnotationListActivity.this,"网络出错",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getActionBar().setHomeButtonEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
