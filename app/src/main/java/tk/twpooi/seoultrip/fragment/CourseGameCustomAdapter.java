package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.showsharecourse.ShowShareCourseActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class CourseGameCustomAdapter extends RecyclerView.Adapter<CourseGameCustomAdapter.ViewHolder> {

    // UI
    private Toolbar mToolbar;
    private Context context;

    // Data
    private ArrayList<HashMap<String, Object>> courseList;

    // Database
    private DBHelper dbHelper;

    private CourseGameFragment f;

    // 생성자
    public CourseGameCustomAdapter(Context context, ArrayList<HashMap<String,Object>> courseList, RecyclerView recyclerView, Toolbar toolbar, CourseGameFragment f) {
        this.context = context;
        this.courseList = courseList;
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

        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //recycler view에 반복될 아이템 레이아웃 연결
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_game_custom_item,null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final HashMap<String,Object> noticeData = courseList.get(position);
        final int pos = position;
        final String title = (String)noticeData.get("title");
        String startLanguage = (String)noticeData.get("language");
        String finishLangeage = (String)noticeData.get("language");
        final int limit = (int)noticeData.get("time");
        int tryCount = (int)noticeData.get("try");
        int progress = (int)noticeData.get("progress");


        String startDate = (String)noticeData.get("startDate");
        String finishDate = (String)noticeData.get("finishDate");
        final String currentDate = getCurrentDate();

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
        holder.limit.setText(Integer.toString(limit) + context.getResources().getString(R.string.time));

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

        // 오버레이 관련
        if(startDate == null){
            holder.rl_overlay.setVisibility(View.VISIBLE);
            holder.rl_timeEnable.setVisibility(View.GONE);
        }else if(checkDate(currentDate, startDate) && checkDate(finishDate, currentDate)){
            holder.rl_overlay.setVisibility(View.GONE);
            holder.rl_timeEnable.setVisibility(View.VISIBLE);

            holder.enableTime.setText(context.getResources().getString(R.string.start_time) + " : " + getTimeText(startDate));
            holder.disableTime.setText(context.getResources().getString(R.string.finish_time) + " : " + getTimeText(finishDate));
        }else{
            holder.rl_overlay.setVisibility(View.VISIBLE);
            holder.rl_timeEnable.setVisibility(View.GONE);
        }

        if(tryCount == 0){
            holder.btn1.setText(context.getResources().getString(R.string.start));
        }else{
            holder.btn1.setText(context.getResources().getString(R.string.restart));
        }
        holder.btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.rl_overlay.setVisibility(View.GONE);
                f.startCourseGame(pos);
                holder.rl_timeEnable.setVisibility(View.VISIBLE);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
                Calendar cal = new GregorianCalendar(Locale.KOREA);
                cal.add(Calendar.HOUR, limit);
                String fDate = dateFormat.format(cal.getTime());
                holder.enableTime.setText(context.getResources().getString(R.string.start_time) + " : " + getTimeText(getCurrentDate()));
                holder.disableTime.setText(context.getResources().getString(R.string.finish_time) + " : " + getTimeText(fDate));

                Intent intent = new Intent(context, DetailCourseGameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AddMyCourseActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                intent.putExtra("course", courseList.get(pos));
                intent.putExtra("position", pos);
                context.startActivity(intent);
            }
        });

        holder.btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowShareCourseActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("course", noticeData);
                intent.putExtra("mode", (String)noticeData.get("mode"));
                context.startActivity(intent);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(pos);
//                f.removeGameCourse(pos);
            }
        });

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailCourseGameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AddMyCourseActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                intent.putExtra("course", courseList.get(pos));
                intent.putExtra("position", pos);
                context.startActivity(intent);
            }
        });

        holder.giveUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f.setGiveup(pos);
            }
        });

        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(pos);
            }
        });
        if(progress >= attractionList.size()){
            holder.rl_overlay.setVisibility(View.GONE);
            holder.rl_timeEnable.setVisibility(View.GONE);
            holder.fm_success_overlay.setVisibility(View.VISIBLE);
            holder.fm_success_overlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailCourseGameActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(AddMyCourseActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                    intent.putExtra("course", courseList.get(pos));
                    intent.putExtra("position", pos);
                    context.startActivity(intent);
                }
            });
        }

    }

    private void showDeleteDialog(int position){
        final int pos = position;
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(context.getResources().getString(R.string.ok))
                .setContentText(context.getResources().getString(R.string.are_you_remove))
                .setConfirmText(context.getResources().getString(R.string.remove))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.hide();
                        f.removeGameCourse(pos);
                    }
                })
                .setCancelText(context.getResources().getString(R.string.cancel))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.hide();
                    }
                }).show();

    }

    private boolean checkDate(String a, String b){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());

        try {
            Date a2 = dateFormat.parse(a);
            Date b2 = dateFormat.parse(b);

            if (a2.compareTo(b2) >= 0) {
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            return false;
        }
    }

    private String getTimeText(String s){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());

        try{

            Date d = dateFormat.parse(s);

            dateFormat = new SimpleDateFormat(
                    String.format(context.getResources().getString(R.string.date_time), "MM", "dd", "HH", "mm")
//                    "MM" + context.getResources().getString(R.string.month)
//                            + " dd" + context.getResources().getString(R.string.day)
//                            + " HH" + context.getResources().getString(R.string.hour)
//                            + " mm" + context.getResources().getString(R.string.minute)
                    , java.util.Locale.getDefault());

            String text = dateFormat.format(d);

            String locale = Locale.getDefault().getLanguage();
            if(!locale.equals("ko")){
                String date = text.substring(0, 2);
                try{
                    int month = Integer.parseInt(date);
                    switch (month){
                        case 1:
                            date = "Jan";
                            break;
                        case 2:
                            date = "Feb";
                            break;
                        case 3:
                            date = "Mar";
                            break;
                        case 4:
                            date = "Apr";
                            break;
                        case 5:
                            date = "May";
                            break;
                        case 6:
                            date = "Jun";
                            break;
                        case 7:
                            date = "Jul";
                            break;
                        case 8:
                            date = "Aug";
                            break;
                        case 9:
                            date = "Sep";
                            break;
                        case 10:
                            date = "Oct";
                            break;
                        case 11:
                            date = "Nov";
                            break;
                        case 12:
                            date = "Dec";
                            break;
                        default:
                            break;
                    }
                }catch(Exception e){

                }
                text = date + text.substring(2);
            }

            return text;

        }catch (Exception e){
            return null;
        }

    }

    private String getCurrentDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
        Date date = new Date();
        String startDate = dateFormat.format(date);

        return startDate;
    }

    @Override
    public int getItemCount() {
        return this.courseList.size();
    }

    // 툴바 숨기고 보이기
    private void hideViews() {
        f.hideViews();
        //mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    private void showViews() {
        f.showViews();
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    public void updateList(ArrayList<HashMap<String, Object>> courseList){
        this.courseList = courseList;
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
        TextView btn1;
        TextView btn2;
        TextView deleteBtn;

        RelativeLayout rl_timeEnable;
        TextView enableTime;
        TextView disableTime;
        Button giveUpBtn;

        FrameLayout fm_success_overlay;
        ImageView closeBtn;

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
            btn1 = (TextView) v.findViewById(R.id.btn1);
            btn2 = (TextView) v.findViewById(R.id.btn2);
            deleteBtn = (TextView)v.findViewById(R.id.delete_btn);

            rl_timeEnable = (RelativeLayout)v.findViewById(R.id.rl_vitalization);
            enableTime = (TextView)v.findViewById(R.id.enable_time);
            disableTime = (TextView)v.findViewById(R.id.disable_time);
            giveUpBtn = (Button)v.findViewById(R.id.give_up_btn);

            fm_success_overlay = (FrameLayout)v.findViewById(R.id.fm_success_overlay);
            closeBtn = (ImageView)v.findViewById(R.id.close_btn);
        }
    }

}
