package tk.twpooi.seoultrip.fragment.detailattraction.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class RecommendAttractionListCustomAdapter extends RecyclerView.Adapter<RecommendAttractionListCustomAdapter.ViewHolder> {

    // UI
    private Context context;

    // Data
    private ArrayList<HashMap<String, String>> attractionList;


    // 생성자
    public RecommendAttractionListCustomAdapter(Context context, ArrayList<HashMap<String, String>> attractionList, RecyclerView recyclerView) {
        this.context = context;
        this.attractionList = attractionList;


    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recommend_attraction_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,String> noticeData = attractionList.get(position);
        final int pos = position;

        //정보 세팅해서 출력
        if(noticeData.get("mainImage").isEmpty()) {

        } else {
            Picasso.with(context).load(noticeData.get("mainImage")).into(holder.iv_image);
        }

        holder.tv_title.setText(noticeData.get("title")); //제목

    }

    @Override
    public int getItemCount() {
        return this.attractionList.size();
    }


    public final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        ImageView iv_image;


        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.title);
            iv_image = (ImageView) v.findViewById(R.id.image);

        }
    }

}
