package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;
import com.sackcentury.shinebuttonlib.ShineButton;


import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class FavoriteListCustomAdapter extends RecyclerView.Adapter<FavoriteListCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, String>> attractionList;
    private FavoriteListFragment f;

    // Database
    private DBHelper dbHelper;
    private FileManager fileManager;

    // 생성자
    public FavoriteListCustomAdapter(Context context, ArrayList<HashMap<String,String>> attractionList, RecyclerView recyclerView, Toolbar toolbar, FavoriteListFragment f) {
        this.context = context;
        this.attractionList = attractionList;
        this.f = f;

        mToolbar = toolbar;

        fileManager = new FileManager(context);

        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String language = attractionList.get(position).get("language");
        final String title = attractionList.get(position).get("title");

        final HashMap<String,String> noticeData = dbHelper.getResultSearchTitle(title, language).get(0);
        final int pos = position;

        //정보 세팅해서 출력

        holder.tv_title.setText(noticeData.get("title")); //제목
        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("title", noticeData.get("title"));
                i.putExtra("isFavorite", true);
                i.putExtra("language", language);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        if(isContain(attractionList, title)){
            holder.favoriteBtn.setChecked(true);
        }else{
            holder.favoriteBtn.setChecked(false);
        }

        holder.favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!holder.favoriteBtn.isChecked()){

                    attractionList.remove(pos);
                    fileManager.writeFavorite(attractionList);
                    notifyItemRemoved(pos);
                    f.showSnackbar(title + "\n" + context.getResources().getString(R.string.remove_favorite));
                    f.makeList();
                }
            }
        });

        holder.addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddCourseListActivity.class);
                intent.putExtra(AddMyCourseActivity.DRAG_POSITION, DraggerPosition.TOP);
                intent.putExtra("attraction", title);
                intent.putExtra("language", language);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });


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
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
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
        TextView addCourse;
        ShineButton favoriteBtn;
        RelativeLayout rl;


        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            addCourse = (TextView)v.findViewById(R.id.add_course);
            favoriteBtn = (ShineButton)v.findViewById(R.id.favorite_btn);
            rl = (RelativeLayout)v.findViewById(R.id.rl);
        }
    }

}
