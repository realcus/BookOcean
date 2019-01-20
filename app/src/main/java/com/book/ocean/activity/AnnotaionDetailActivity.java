package com.book.ocean.activity;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.book.ocean.R;
import com.book.ocean.bean.Annotation;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AnnotaionDetailActivity extends AppCompatActivity {

    private String bookname;

    private TextView tvTime,tvPage,tvUserName,tvCheapter,tvContent;
    private ImageView ivHeadIcon;
    private Annotation annotation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotaion_detail);

        initViews();
        initDatas();
    }


    private void initViews() {

        tvTime = findViewById(R.id.tv_annotation_time);
        tvPage=findViewById(R.id.tv_annotation_page);
        tvCheapter=findViewById(R.id.tv_annotation_cheapter);
        tvUserName=findViewById(R.id.tv_annotation_user);
        ivHeadIcon=findViewById(R.id.iv_annotation_head_icon);
        tvContent = findViewById(R.id.tv_annotation_detail);

    }

    private void initDatas() {

        ActionBar supportActionBar = getSupportActionBar();

        Bundle annotation_bundle = getIntent().getBundleExtra("annotation_bundle");
        annotation = (Annotation) annotation_bundle.getSerializable("annotation");
        bookname=annotation_bundle.getString("name");

        supportActionBar.setTitle("《"+bookname+"》的笔记");
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        tvCheapter.setText(annotation.getCheapter());
        tvUserName.setText(annotation.getAuthor());
        tvTime.setText(annotation.getTime());
        tvPage.setText(annotation.getPage()+"页");
        tvContent.setText(annotation.getContent());
        ImageLoader.getInstance().displayImage(annotation.getAuthorHead(),ivHeadIcon);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

}
