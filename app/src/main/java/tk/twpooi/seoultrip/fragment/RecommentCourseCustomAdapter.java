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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.showsharecourse.ShowShareCourseActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class RecommentCourseCustomAdapter extends RecyclerView.Adapter<RecommentCourseCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, Object>> courseList;

    // Database
    private DBHelper dbHelper;
    private FileManager fileManager;

    // 무한 스크롤
    private OnLoadMoreListener onLoadMoreListener;
    private int visibleThreshold = 3;
    private int lastVisibleItem, totalItemCount;
    private boolean loading = false;

    private RecommendCourseFragment f;

    // 생성자
    public RecommentCourseCustomAdapter(Context context, ArrayList<HashMap<String,Object>> courseList, RecyclerView recyclerView, Toolbar toolbar, RecommendCourseFragment f) {
        this.context = context;
        this.courseList = courseList;
        this.f = f;

        mToolbar = toolbar;
        fileManager = new FileManager(context);

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
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recomment_course_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = courseList.get(position);
        final int pos = position;
        final String title = (String)noticeData.get("title");
        String startLanguage = (String)noticeData.get("language");
        String finishLangeage = (String)noticeData.get("language");

        ArrayList<String> attractionList = (ArrayList<String>)noticeData.get("attraction");
        String startAttraction = attractionList.get(0);
        String finishAttraction = attractionList.get(attractionList.size()-1);

        if(!startLanguage.equals(StartActivity.DATABASE_LANGUAGE)){
            String tempTitle = dbHelper.getConvertTitle(startAttraction, startLanguage);
            if(tempTitle != null){
                startLanguage = StartActivity.DATABASE_LANGUAGE;
                startAttraction = tempTitle;
            }
        }
        if(!finishLangeage.equals(StartActivity.DATABASE_LANGUAGE)){
            String tempTitle = dbHelper.getConvertTitle(finishAttraction, finishLangeage);
            if(tempTitle != null){
                finishLangeage = StartActivity.DATABASE_LANGUAGE;
                finishAttraction = tempTitle;
            }
        }

        HashMap<String, String> startAttractionList = dbHelper.getResultSearchTitle(startAttraction, startLanguage).get(0);
        HashMap<String, String> finishAttractionList = dbHelper.getResultSearchTitle(finishAttraction, finishLangeage).get(0);

        holder.title.setText(title);
        holder.limit.setText(Integer.toString((Integer) noticeData.get("time")) + context.getResources().getString(R.string.hour2));

        // 시작 명소
        Picasso.with(context).load(startAttractionList.get("mainImage"))
                .transform(new CropCircleTransformation()).into(holder.startImage);
        holder.tv_startTitle.setText(context.getResources().getString(R.string.start) + " : " + startAttraction);
        String sContents = startAttractionList.get("sContents");
        if(sContents.length() > 40){
            sContents = sContents.substring(0, 40) + "...";
        }
        holder.tv_startContent.setText(sContents);

        // 도착 명소
        Picasso.with(context).load(finishAttractionList.get("mainImage"))
                .transform(new CropCircleTransformation()).into(holder.finishImage);
        holder.tv_finishTitle.setText(context.getResources().getString(R.string.arrival) + " : " + finishAttraction);
        sContents = finishAttractionList.get("sContents");
        if(sContents.length() > 40){
            sContents = sContents.substring(0, 40) + "...";
        }
        holder.tv_finishContent.setText(sContents);

        // 조회수
        holder.hit.setText(context.getResources().getString(R.string.hit) + " : " + noticeData.get("hits"));
        // 성공수
        holder.success.setText(context.getResources().getString(R.string.success) + " : " + noticeData.get("success"));
        // 담기수
        holder.put.setText(context.getResources().getString(R.string.put) + " : " + noticeData.get("put"));
        // 관심수
        holder.like.setText(context.getResources().getString(R.string.like) + " : " + noticeData.get("like"));

        if(((String)noticeData.get("introduce")).length() == 0){
            holder.view_introduce.setVisibility(View.GONE);
            holder.rl_introduce.setVisibility(View.GONE);
        }else{

            if(noticeData.get("clicked").equals("0")) {
                sContents = (String)noticeData.get("introduce");
                if (sContents.length() > 40) {
                    sContents = sContents.substring(0, 40) + "...";
                }
                holder.introduce.setText(sContents);
            }else{
                holder.introduce.setText((String)noticeData.get("introduce"));
            }

        }

        holder.rl_introduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(noticeData.get("clicked").equals("0")){
                    noticeData.put("clicked", "1");
                    holder.introduce.setText((String)noticeData.get("introduce"));
                }else{
                    noticeData.put("clicked", "0");
                    String sContents = (String)noticeData.get("introduce");
                    if (sContents.length() > 40) {
                        sContents = sContents.substring(0, 40) + "...";
                    }
                    holder.introduce.setText(sContents);
                }
            }
        });

        holder.gap.setText(String.format(context.getResources().getString(R.string.attraction_count), attractionList.size()));

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> temp = fileManager.readSharedFile();

                Intent intent = new Intent(context, ShowShareCourseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("course", noticeData);
                intent.putExtra("id", (String)courseList.get(pos).get("id"));
                intent.putExtra("isFromRecommend", true);
                if(temp.contains((String)courseList.get(pos).get("id"))){
                    intent.putExtra("isCourseGame", true);
                }
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return this.courseList.size();
    }

    // 툴바 숨기고 보이기
    private void hideViews() {
        if(f == null){
            mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        }else{
            f.hideViews();
        }
    }
    private void showViews() {
        if(f == null){
            mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        }else{
            f.showViews();
        }
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
        TextView hit;
        TextView success;
        TextView put;
        TextView like;
        View view_introduce;
        RelativeLayout rl_introduce;
        TextView introduce;
        TextView gap;

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
            hit = (TextView)v.findViewById(R.id.hit_text);
            success = (TextView)v.findViewById(R.id.success_text);
            put = (TextView)v.findViewById(R.id.put_text);
            like = (TextView)v.findViewById(R.id.like_text);
            view_introduce = (View)v.findViewById(R.id.view_introduce);
            rl_introduce = (RelativeLayout)v.findViewById(R.id.rl_introduce);
            introduce = (TextView)v.findViewById(R.id.introduce);
            gap = (TextView)v.findViewById(R.id.gap_text);
        }
    }

}
