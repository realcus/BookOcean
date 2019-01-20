package com.book.ocean.activity;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.book.ocean.R;
import com.book.ocean.adapter.MyViewpaerAdapter;
import com.book.ocean.fragment.ContentFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private MyViewpaerAdapter myViewpaerAdapter;

    //tab标题
    private List<String> titles = new ArrayList<>();

    //fragments
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initDatas();
        initEvents();
    }

    private void initViews() {

        mToolbar = findViewById(R.id.tb);
        tabLayout = findViewById(R.id.tl);
        mViewPager = findViewById(R.id.vp);
        mToolbar.setTitle(getResources().getString(R.string.app_name));
        setSupportActionBar(mToolbar);

    }

    private void initDatas() {

        titles.add("名著");
        titles.add("小说");
        titles.add("传记");
        titles.add("散文");
        titles.add("艺术");
        titles.add("哲学");
        titles.add("军事");
        titles.add("经济");
        titles.add("历史");
        titles.add("地理");

        Fragment fragment1 = ContentFragment.newInstance("名著","名著");
        Fragment fragment2 = ContentFragment.newInstance("小说","小说");
        Fragment fragment3 = ContentFragment.newInstance("传记","传记");
        Fragment fragment4 = ContentFragment.newInstance("散文","散文");
        Fragment fragment5 = ContentFragment.newInstance("艺术","艺术");
        Fragment fragment6 = ContentFragment.newInstance("哲学","哲学");
        Fragment fragment7 = ContentFragment.newInstance("军事","军事");
        Fragment fragment8 = ContentFragment.newInstance("经济","经济");
        Fragment fragment9 = ContentFragment.newInstance("历史","历史");
        Fragment fragment10 = ContentFragment.newInstance("地理","地理");
        fragments.add(fragment1);
        fragments.add(fragment2);
        fragments.add(fragment3);
        fragments.add(fragment4);
        fragments.add(fragment5);
        fragments.add(fragment6);
        fragments.add(fragment7);
        fragments.add(fragment8);
        fragments.add(fragment9);
        fragments.add(fragment10);



    }

    private void initEvents() {

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_search:
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_about:
                        Intent intent2 = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(intent2);
                        break;
                }
                return false;
            }
        });

        myViewpaerAdapter = new MyViewpaerAdapter(getSupportFragmentManager(), titles, fragments);

        mViewPager.setAdapter(myViewpaerAdapter);
        mViewPager.setOffscreenPageLimit(9);
        tabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
