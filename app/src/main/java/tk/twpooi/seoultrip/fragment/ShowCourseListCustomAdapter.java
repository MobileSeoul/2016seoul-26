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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerView;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.detailattraction.DetailActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class ShowCourseListCustomAdapter extends RecyclerView.Adapter<ShowCourseListCustomAdapter.ViewHolder>  implements DraggableItemAdapter<ShowCourseListCustomAdapter.ViewHolder> {

    // Database
    private DBHelper dbHelper;

    // UI
    private Toolbar mToolbar;
    private Context context;
    private DraggerView draggerView;

    // Data
    private ArrayList<HashMap<String, Object>> list;

    private ShowCourseListActivity act;

    // 생성자
    public ShowCourseListCustomAdapter(Context context, ArrayList<String> attractionList, RecyclerView recyclerView, Toolbar toolbar, DraggerView draggerView,ShowCourseListActivity act) {
        this.context = context;
        this.draggerView = draggerView;
        this.act = act;
        this.mToolbar = toolbar;

        this.dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);


        list = new ArrayList<>();
        for(int i=0; i<attractionList.size(); i++){
            HashMap<String, Object> temp = new HashMap<>();
            temp.put("dragID", i);
            temp.put("attraction", attractionList.get(i));
            list.add(temp);
        }


        setHasStableIds(true);


    }

    public void addList(String attraction){

        HashMap<String, Object> temp = new HashMap<>();
        temp.put("dragID", list.size());
        temp.put("attraction", attraction);
        list.add(temp);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_list_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final int pos = position;
        final String title = (String)list.get(position).get("attraction");//courseList.get(position);

        HashMap<String, String> db = dbHelper.getResultSearchTitle(title, act.getLanguage()).get(0);

        // set circle crop image
        String imageUrl = db.get("mainImage");
        Picasso.with(context).load(imageUrl)
                .transform(new CropCircleTransformation()).into(holder.image);

        // set sContents
        String sContents = db.get("sContents");
        if(sContents.length() > 40){
            sContents = sContents.substring(0, 40) + "...";
        }
        holder.tv_content.setText(sContents);

        holder.tv_title.setText(Integer.toString(pos+1) + ". " + title); //제목

        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, DetailActivity.class);
                i.putExtra("title", title);
                i.putExtra("language", act.getLanguage());
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        });

        holder.mOverflowIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.menu_showcourse, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menu_delete:
                                act.deleteAttraction(pos);
                                list.remove(pos);
                                notifyItemRemoved(pos);
                                notifyDataSetChanged();
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
        return (int)list.get(position).get("dragID"); // need to return stable (= not change even after reordered) value
    }
    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        act.moveItem(fromPosition, toPosition);
        HashMap<String, Object> temp = list.remove(fromPosition);
        list.add(toPosition, temp);
        System.out.println("sub : " + list);

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
        return this.list.size();
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

    public final static class ViewHolder extends AbstractDraggableItemViewHolder {
        FrameLayout rl;
        TextView tv_title;
        TextView tv_content;
        ImageView image;
        ImageView mOverflowIcon;

        boolean isOpening = false; // swipe layout open check

        public ViewHolder(View v) {
            super(v);
            rl = (FrameLayout)v.findViewById(R.id.rl);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_content = (TextView)v.findViewById(R.id.tv_content);
            image = (ImageView)v.findViewById(R.id.image);
            mOverflowIcon = (ImageView)v.findViewById(R.id.ic_dot);
        }
    }


}
