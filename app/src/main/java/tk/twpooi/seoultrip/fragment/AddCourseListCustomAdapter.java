package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class AddCourseListCustomAdapter extends RecyclerView.Adapter<AddCourseListCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, Object>> courseList;
    private String attraction;
    private AddCourseListActivity act;

    private String compareLanguage;

    // 생성자
    public AddCourseListCustomAdapter(Context context, ArrayList<HashMap<String,Object>> attractionList, RecyclerView recyclerView, Toolbar toolbar, String attraction, AddCourseListActivity act) {
        this.context = context;
        this.courseList = attractionList;
        this.attraction = attraction;
        this.act = act;

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

        compareLanguage = act.getLanguage();

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_course_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = courseList.get(position);
        final int pos = position;
        final String title = (String)noticeData.get("title");
        final String time = Integer.toString((int)noticeData.get("time"));
        String language = (String)noticeData.get("language");

        holder.tv_title.setText(title); //제목
        holder.time.setText(time);

        if(language.equals(compareLanguage)){
            holder.tv_title.setTextColor(ContextCompat.getColor(context, R.color.dark_gray));
            holder.time.setTextColor(ContextCompat.getColor(context, R.color.dark_gray));
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ShowCourseListActivity.class);
                    intent.putExtra(AddCourseListActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                    intent.putExtra("position", pos);
                    intent.putExtra("attraction", attraction);
                    intent.putExtra("isShowAddBtn", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }else{
            holder.tv_title.setTextColor(ContextCompat.getColor(context, R.color.light_gray));
            holder.time.setTextColor(ContextCompat.getColor(context, R.color.light_gray));
            holder.rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }




    }

    @Override
    public int getItemCount() {
        return this.courseList.size();
    }

    private void hideViews() {
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
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
        RelativeLayout rl;
        TextView tv_title;
        TextView time;

        public ViewHolder(View v) {
            super(v);
            rl = (RelativeLayout)v.findViewById(R.id.rl);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            time = (TextView) v.findViewById(R.id.time);
        }
    }


}
