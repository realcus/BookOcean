package com.book.ocean.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.book.ocean.R;
import com.book.ocean.bean.Book;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 */
public class SearchAdapter extends BaseAdapter {

        private Context context;
        private List<Book> mlist;

        public SearchAdapter(Context context, List<Book> mlist) {
            this.context=context;
            this.mlist=mlist;
        }

        @Override
        public int getCount() {
            return mlist.size();
        }

        @Override
        public Object getItem(int arg0) {
            return arg0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if(convertView==null) {
                convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.item_search, null);
                holder=new ViewHolder();
                holder.name=(TextView)convertView.findViewById(R.id.tv_search_item_title);
                holder.author=(TextView)convertView.findViewById(R.id.tv_search_item_author);
                holder.score=(TextView)convertView.findViewById(R.id.tv_search_item_score);
                holder.scorenumber=(TextView)convertView.findViewById(R.id.tv_search_item_score_number);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder)convertView.getTag();
            }
            holder.icon=(ImageView)convertView.findViewById(R.id.iv_search_icon);
            holder.name.setText(mlist.get(position).getTitle());
            holder.author.setText("作者:"+mlist.get(position).getAuthor()+"/"+mlist.get(position).getPublisher()+"/"+
                    mlist.get(position).getPublishDate()+"/"+mlist.get(position).getPrice()+"元");
            holder.score.setText(mlist.get(position).getRate()+"分");
            holder.scorenumber.setText("("+mlist.get(position).getReviewCount()+"人评论)");
            ImageLoader.getInstance().displayImage(mlist.get(position).getBitmap(),holder.icon);
            return convertView;
        }


        public void setData(List<Book> list){
            mlist=list;
        }


    class ViewHolder{
        ImageView icon;
        TextView name;
        TextView author;
        TextView score;
        TextView scorenumber;
    }

}
