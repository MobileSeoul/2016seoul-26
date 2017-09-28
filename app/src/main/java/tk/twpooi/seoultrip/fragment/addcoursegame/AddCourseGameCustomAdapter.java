package tk.twpooi.seoultrip.fragment.addcoursegame;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.showsharecourse.ShowShareCourseActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class AddCourseGameCustomAdapter extends RecyclerView.Adapter<AddCourseGameCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, Object>> courseList;
    private ArrayList<Integer> saveIndex;

    // Database
    private DBHelper dbHelper;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    private AddCourseGameActivity activity;

    // 생성자
    public AddCourseGameCustomAdapter(final Context context, final ArrayList<HashMap<String,Object>> courseList, RecyclerView recyclerView, Toolbar toolbar, final AddCourseGameActivity activity) {
        this.context = context;
        this.courseList = courseList;
        this.activity = activity;

        saveIndex = new ArrayList<>();

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

        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);

        activity.getCompleteBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<HashMap<String, Object>> list = new ArrayList<>();
                for(int i : saveIndex){
                    list.add(courseList.get(i));
                }
                activity.completeBtnClicked(list);
                activity.onBackPressed();
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_course_game_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = courseList.get(position);
        final int pos = position;
        final String title = (String)noticeData.get("title");
        String startLanguage = (String)noticeData.get("language");
        String finishLangeage = (String)noticeData.get("language");

        int limit = (int)noticeData.get("time");
        final ArrayList<String> attractionList = (ArrayList<String>)noticeData.get("attraction");
        String start = attractionList.get(0);
        String finish = attractionList.get(attractionList.size()-1);

        if(!startLanguage.equals(StartActivity.DATABASE_LANGUAGE)){
            String tempTitle = dbHelper.getConvertTitle(start, startLanguage);
            if(tempTitle != null){
                startLanguage = StartActivity.DATABASE_LANGUAGE;
                start = tempTitle;
            }
        }
        if(!finishLangeage.equals(StartActivity.DATABASE_LANGUAGE)){
            String tempTitle = dbHelper.getConvertTitle(finish, finishLangeage);
            if(tempTitle != null){
                finishLangeage = StartActivity.DATABASE_LANGUAGE;
                finish = tempTitle;
            }
        }

        HashMap<String, String> startAttractionList = dbHelper.getResultSearchTitle(start, startLanguage).get(0);
        HashMap<String, String> finishAttractionList = dbHelper.getResultSearchTitle(finish, finishLangeage).get(0);

        holder.title.setText(title);
        holder.limit.setText(Integer.toString(limit) + context.getResources().getString(R.string.hour2));

        // 시작 명소
        Picasso.with(context).load(startAttractionList.get("mainImage"))
                .transform(new CropCircleTransformation()).into(holder.startImage);
        holder.tv_startTitle.setText(context.getResources().getString(R.string.start) + " : " + start);
        String sContents = startAttractionList.get("sContents");
        if(sContents.length() > 40){
            sContents = sContents.substring(0, 40) + "...";
        }
        holder.tv_startContent.setText(sContents);

        // 도착 명소
        Picasso.with(context).load(finishAttractionList.get("mainImage"))
                .transform(new CropCircleTransformation()).into(holder.finishImage);
        holder.tv_finishTitle.setText(context.getResources().getString(R.string.arrival) + " : " + finish);
        sContents = finishAttractionList.get("sContents");
        if(sContents.length() > 40){
            sContents = sContents.substring(0, 40) + "...";
        }
        holder.tv_finishContent.setText(sContents);

        holder.gap.setText(String.format(context.getResources().getString(R.string.attraction_count), attractionList.size()));



        if(saveIndex.contains(pos)){
            holder.rl_overlay.setVisibility(View.GONE);
            holder.ll_cancel_layout.setVisibility(View.VISIBLE);
        }else{
            holder.rl_overlay.setVisibility(View.VISIBLE);
            holder.ll_cancel_layout.setVisibility(View.GONE);
        }

        holder.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.rl_overlay.setVisibility(View.GONE);
                holder.ll_cancel_layout.setVisibility(View.VISIBLE);
                saveIndex.add(pos);
            }
        });
        holder.infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleCourseInformation(noticeData);
            }
        });
        holder.ll_cancel_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.rl_overlay.setVisibility(View.VISIBLE);
                holder.ll_cancel_layout.setVisibility(View.GONE);
                saveIndex.remove((Object)pos);
            }
        });

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSimpleCourseInformation(noticeData);
            }
        });

        if(activity.mode.equals("random")){
            holder.rl_overlay.setVisibility(View.GONE);
        }

    }

    private void showSimpleCourseInformation(HashMap<String,Object> course){
        Intent intent = new Intent(context, ShowShareCourseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("course", course);
        if(activity.getMode().equals("myCourse") || activity.getMode().equals("random")) {
            intent.putExtra("isCourseGame", true);
        }
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return this.courseList.size();
    }

    // 툴바 숨기고 보이기
    private void hideViews() {
        //mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    private void showViews() {
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    // 무한 스크롤
    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }
    public interface OnLoadMoreListener {
        void onLoadMore();
    }
    public void setLoaded() {
        loading = false;
    }



    public abstract class ScrollListener extends RecyclerView.OnScrollListener {
        private static final int HIDE_THRESHOLD = 20;
        private int scrolledDistance = 0;
        private boolean controlsVisible = true;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            totalItemCount = linearLayoutManager.getItemCount();
            lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                // End has been reached
                // Do something
                loading = true;
                if (onLoadMoreListener != null) {
                    onLoadMoreListener.onLoadMore();
                }
            }
            // 여기까지 무한 스크롤

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
        LinearLayout contentLayout;
        TextView title;
        TextView limit;
        ImageView startImage;
        TextView tv_startTitle;
        TextView tv_startContent;
        ImageView finishImage;
        TextView tv_finishTitle;
        TextView tv_finishContent;
        TextView gap;

        RelativeLayout rl_overlay;
        TextView selectBtn;
        TextView infoBtn;

        LinearLayout ll_cancel_layout;
        TextView cancelBtn;

        public ViewHolder(View v) {
            super(v);
            contentLayout = (LinearLayout)v.findViewById(R.id.contentLayout);
            title = (TextView)v.findViewById(R.id.title);
            limit = (TextView)v.findViewById(R.id.limit_text);
            startImage = (ImageView)v.findViewById(R.id.start_image);
            tv_startTitle = (TextView)v.findViewById(R.id.tv_start_title);
            tv_startContent = (TextView)v.findViewById(R.id.tv_start_content);
            finishImage = (ImageView)v.findViewById(R.id.finish_image);
            tv_finishTitle = (TextView)v.findViewById(R.id.tv_finish_title);
            tv_finishContent = (TextView)v.findViewById(R.id.tv_finish_content);
            gap = (TextView)v.findViewById(R.id.gap_text);

            rl_overlay = (RelativeLayout)v.findViewById(R.id.rl_overlay);
            selectBtn = (TextView) v.findViewById(R.id.select_btn);
            infoBtn = (TextView) v.findViewById(R.id.info_btn);

            ll_cancel_layout = (LinearLayout)v.findViewById(R.id.ll_cancel_select);
            cancelBtn = (TextView)v.findViewById(R.id.cancel_select_btn);
        }
    }

}
