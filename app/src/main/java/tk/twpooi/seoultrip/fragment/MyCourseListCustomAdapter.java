package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.*;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class MyCourseListCustomAdapter extends RecyclerView.Adapter<MyCourseListCustomAdapter.ViewHolder> implements DraggableItemAdapter<MyCourseListCustomAdapter.ViewHolder>{

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, Object>> courseList;

    //
    private MyCourseListFragment f;

    // 생성자
    public MyCourseListCustomAdapter(Context context, ArrayList<HashMap<String,Object>> attractionList, RecyclerView recyclerView, Toolbar toolbar, MyCourseListFragment f) {
        this.context = context;
        this.courseList = attractionList;
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

        for(int i=0; i<courseList.size(); i++){
            HashMap<String, Object> temp = courseList.get(i);
            temp.put("dragID", i);
            courseList.set(i, temp);
        }

        setHasStableIds(true);

    }

    public void addList(HashMap<String,Object> addList){

        addList.put("dragID", courseList.size());
        courseList.add(addList);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.mycourse_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = courseList.get(position);
        final int pos = position;
        final String title = (String)noticeData.get("title");
        final String time = Integer.toString((int)noticeData.get("time"));


        holder.tv_title.setText(title); //제목
        holder.time.setText(time + context.getResources().getString(R.string.hour2));

        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f.startCourseListDraggerActivity(DraggerPosition.RIGHT, pos);
            }
        });

        holder.mOverflowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.menu_mycourse, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menu_delete:
                                f.deleteCourse(pos);
                                courseList.remove(pos);
                                notifyItemRemoved(pos);
                                notifyItemRangeChanged(0, courseList.size());
                                break;
                            case R.id.menu_share:
                                Intent intent = new Intent(context, ShareMyCourseActivity.class);
                                intent.putExtra(AddCourseListActivity.DRAG_POSITION, DraggerPosition.TOP);
                                intent.putExtra("position", pos);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                break;
                        }

                        return true;
                    }
                });
                popup.show();
            }
        });

    }

    // D&D
    @Override
    public long getItemId(int position) {
        return (int)courseList.get(position).get("dragID"); // need to return stable (= not change even after reordered) value
    }
    @Override
    public void onMoveItem(int fromPosition, int toPosition) {

        f.moveItem(fromPosition, toPosition);
        HashMap<String, Object> temp = courseList.remove(fromPosition);
        courseList.add(toPosition, temp);

        notifyItemMoved(fromPosition, toPosition);
    }
    @Override
    public boolean onCheckCanStartDrag(ViewHolder holder, int position, int x, int y) {
        return true;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(ViewHolder holder, int position) {
        return null;
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    /////////////////////////////

    @Override
    public int getItemCount() {
        return this.courseList.size();
    }

    private void hideViews() {
        f.hideViews();
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    private void showViews() {
        f.showViews();
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

    public final static class ViewHolder extends AbstractDraggableItemViewHolder {
        RelativeLayout rl;
        TextView tv_title;
        TextView time;
        ImageView mOverflowIcon;

        public ViewHolder(View v) {
            super(v);
            rl = (RelativeLayout)v.findViewById(R.id.rl);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            time = (TextView) v.findViewById(R.id.time);
            mOverflowIcon = (ImageView)v.findViewById(R.id.ic_dot);

        }
    }


}
