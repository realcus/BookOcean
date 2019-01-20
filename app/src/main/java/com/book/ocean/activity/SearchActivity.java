package com.book.ocean.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.book.ocean.R;
import com.book.ocean.adapter.SearchAdapter;
import com.book.ocean.bean.Book;
import com.book.ocean.common.KeyboardUtils;
import com.book.ocean.net.BaseAsyncHttp;
import com.book.ocean.net.HttpResponseHandler;
import com.book.ocean.view.CircularProgressView;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity {

    private ListView mLvSearch;
    private SearchAdapter mAdapter;

    private List<Book> mBooks=new ArrayList<Book>();
    private EditText mEtContent;
    private CircularProgressView progressView;
    private Thread updateThread;
    private RelativeLayout mRlBtn;

    //记录上次滚动之后的第一个可见item和最后一个item
    private int mFirstVisibleItem = -1;
    private int mLastVisibleItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mEtContent=(EditText)findViewById(R.id.et_search_content);
        mLvSearch=(ListView)findViewById(R.id.lv_search);
        mRlBtn=(RelativeLayout)findViewById(R.id.rl_search_btn);

        mRlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAnimationThreadStuff(100);
                KeyboardUtils.closeKeyBoard(SearchActivity.this);
                getRequestData(mEtContent.getText().toString());
            }
        });
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        mAdapter=new SearchAdapter(this,mBooks);
        mLvSearch.setAdapter(mAdapter);

        mLvSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(SearchActivity.this,BookViewActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putParcelable("book", mBooks.get(i));
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        });
        mLvSearch.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

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
        });

        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")){
                    mRlBtn.setVisibility(View.GONE);
                }else{
                    mRlBtn.setVisibility(View.VISIBLE);
                }
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

    public void getRequestData(String str){
        RequestParams params=new RequestParams();
        params.put("q",str.trim());
        BaseAsyncHttp.getReq("/v2/book/search",params,new HttpResponseHandler() {
            @Override
            public void jsonSuccess(JSONObject resp) {
                mBooks.clear();
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
                Toast.makeText(SearchActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateToView(){
        mAdapter.setData(mBooks);
        mAdapter.notifyDataSetChanged();
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

    private void startAnimationThreadStuff(long delay)
    {
        if(updateThread != null && updateThread.isAlive())
            updateThread.interrupt();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progressView.setVisibility(View.VISIBLE);
                progressView.setProgress(0f);
                progressView.startAnimation(); // Alias for resetAnimation, it's all the same
                updateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (progressView.getProgress() < progressView.getMaxProgress() && !Thread.interrupted()) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    progressView.setProgress(progressView.getProgress() + 10);
                                }
                            });
                            SystemClock.sleep(250);
                        }
                    }
                });
                updateThread.start();
            }
        }, delay);
    }
}
