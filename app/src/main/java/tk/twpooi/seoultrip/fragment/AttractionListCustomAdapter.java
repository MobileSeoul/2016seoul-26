package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class AttractionListCustomAdapter extends RecyclerView.Adapter<AttractionListCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, String>> attractionList;
    private ArrayList<HashMap<String, String>> favoriteList;

    // Database
    private DBHelper dbHelper;
    private FileManager fileManager;

    private AttractionListFragment f;

    // 생성자
    public AttractionListCustomAdapter(Context context, ArrayList<HashMap<String,String>> attractionList, RecyclerView recyclerView, Toolbar toolbar, AttractionListFragment f) {
        this.context = context;
        this.attractionList = attractionList;
        this.f = f;

        mToolbar = toolbar;

        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {


            recyclerView.addOnScrollListener(new ScrollListener() {
                @Override
                public void onHide() {
                    hideViews();
                }

                @Override
                public void onShow() {
                    showViews();
                }
            });
        }

        fileManager = new FileManager(context);
        favoriteList = fileManager.readFavorite();

        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);
    }

    public void refreshFavorite(){
        favoriteList = fileManager.readFavorite();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attraction_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,String> noticeData = attractionList.get(position);
        final int pos = position;
        final String title = noticeData.get("title");


        if(f == null){
            holder.rl_button.setVisibility(View.GONE);
        }else{
            holder.rl_button.setVisibility(View.VISIBLE);
        }

        //정보 세팅해서 출력
        if(noticeData.get("mainImage").isEmpty()) {
        } else {
            Picasso.with(context).load(noticeData.get("mainImage")).into(holder.iv_image);
        }



        if(noticeData.get("clicked") == "0"){
            String sCon = noticeData.get("sContents");
            if(sCon.length() > 40){
                sCon = sCon.substring(0, 40) + "...";
            }
            holder.tv_content.setText(sCon); //내용 일부
        }else{
            holder.tv_content.setText(noticeData.get("sContents")); //내용 일부
        }
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noticeData.get("clicked") == "0") {
                    holder.tv_content.setText(noticeData.get("sContents"));
                    attractionList.get(pos).put("clicked", "1");
                } else {
                    String sCon = noticeData.get("sContents");
                    if(sCon.length() > 40){
                        sCon = sCon.substring(0, 40) + "...";
                    }
                    holder.tv_content.setText(sCon);
                    attractionList.get(pos).put("clicked", "0");
                }
            }
        });

        holder.tv_title.setText(noticeData.get("title")); //제목

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("title", noticeData.get("title"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });


        if(isContain(favoriteList, title)){
            holder.favoriteBtn.setChecked(true);
        }else{
            holder.favoriteBtn.setChecked(false);
        }
        holder.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.favoriteBtn.isChecked()){

                    if(!isContain(favoriteList, title)) {
                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put("language", StartActivity.DATABASE_LANGUAGE);
                        temp.put("title", title);
                        favoriteList.add(temp);
                        fileManager.writeFavorite(favoriteList);

                        f.showSnackbar(title + "\n" + context.getResources().getString(R.string.add_favorite));
                    }

                }else{

                    if(isContain(favoriteList, title)){

                        int index = isIndex(favoriteList, title);
                        favoriteList.remove(index);
                        fileManager.writeFavorite(favoriteList);
                        f.showSnackbar(title + "\n" + context.getResources().getString(R.string.remove_favorite));
                    }

                }
            }
        });

        holder.addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddCourseListActivity.class);
                intent.putExtra(AddMyCourseActivity.DRAG_POSITION, DraggerPosition.TOP);
                intent.putExtra("attraction", title);
                intent.putExtra("language", StartActivity.DATABASE_LANGUAGE);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });



    }

    private int isIndex(ArrayList<HashMap<String, String>> list, String search){

        for(int i=0; i<list.size(); i++){
            if(list.get(i).get("title").equals(search)){
                return i;
            }
        }

        return -1;
    }

    private boolean isContain(ArrayList<HashMap<String, String>> list, String search){

        for(HashMap<String, String> temp : list){
            if(temp.get("title").equals(search)){
                return true;
            }
        }

        return false;

    }

    @Override
    public int getItemCount() {
        return this.attractionList.size();
    }

    private void hideViews() {
        if(f == null){
            if(mToolbar != null)
                mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        }else {
            f.hideViews();
        }
    }

    private void showViews() {
        if(f == null){
            if(mToolbar != null)
                mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        }else{
            f.showViews();
        }
    }

    public abstract class ScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                controlsVisible = true;
                scrolledDistance = 0;
            }

            if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
                scrolledDistance += dy;
            }
            // 여기까지 툴바 숨기기
        }

        public abstract void onHide();
        public abstract void onShow();

    }

    public final static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_content;
        LinearLayout contentLayout;
        CardView cv;
        ImageView iv_image;
        RelativeLayout rl_image;
        TextView addCourse;
        ShineButton favoriteBtn;
        RelativeLayout rl_button;


        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_content = (TextView) v.findViewById(R.id.tv_content);
            cv = (CardView) v.findViewById(R.id.cv);
            iv_image = (ImageView) v.findViewById(R.id.iv_image);
            rl_image = (RelativeLayout) v.findViewById(R.id.rl_image);
            contentLayout = (LinearLayout)v.findViewById(R.id.contentLayout);
            addCourse = (TextView)v.findViewById(R.id.addCourse);
            favoriteBtn = (ShineButton)v.findViewById(R.id.favorite_btn);
            rl_button = (RelativeLayout)v.findViewById(R.id.rl_button);
        }
    }

}
