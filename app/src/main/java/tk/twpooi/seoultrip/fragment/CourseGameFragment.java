package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ppamorim.dragger.DraggerPosition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.MainTabActivity;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.addcoursegame.AddCourseGameMainActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class CourseGameFragment extends Fragment{

    private FileManager fileManager;

    // Database
    DBHelper dbHelper;

    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private FloatingActionsMenu menu;
    private Context context;
    protected PtrFrameLayout mPtrFrameLayout;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private CourseGameCustomAdapter adapter;
    private int mTotalScrolled = 0;

    ArrayList<HashMap<String,String>> courseList;

    private int checkRefresh;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_course_game, container, false);
        context = container.getContext();
        mToolbar = ((MainTabActivity)getActivity()).getToolbar();

        fileManager = new FileManager(context);

        initUI();

        // Database
        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);


        makeList();

        return view;

    }


    public void initUI(){

        floationMenu();

        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalScrolled += dy;
            }
        });

        setMaterial();

    }

    public void setMaterial(){

        mPtrFrameLayout = (PtrFrameLayout) view.findViewById(R.id.material_style_ptr_frame);

        // header
        final MaterialHeader header = new MaterialHeader(getContext());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, PtrLocalDisplay.dp2px(15), 0, PtrLocalDisplay.dp2px(10));
        header.setPtrFrameLayout(mPtrFrameLayout);

        mPtrFrameLayout.setLoadingMinTime(10);
        mPtrFrameLayout.setDurationToCloseHeader(1500);
        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.addPtrUIHandler(header);

        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, rv, header);
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                makeList();
            }
        });


    }

    public void floationMenu(){

        menu = (FloatingActionsMenu)view.findViewById(R.id.multiple_actions);

        FloatingActionButton gotoUP = (FloatingActionButton)view.findViewById(R.id.gotoup);
        gotoUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainTabActivity)getActivity()).showToolbar();
                rv.scrollToPosition(0);
                menu.toggle();
            }
        });
        gotoUP.setTitle(getResources().getString(R.string.gotoup));

        FloatingActionButton addBtn = (FloatingActionButton)view.findViewById(R.id.add_btn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AddCourseGameMainActivity.class);
                intent.putExtra(AddCourseListActivity.DRAG_POSITION, DraggerPosition.TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                menu.toggle();
            }
        });
        addBtn.setTitle(getResources().getString(R.string.add_short));

   }

    public void makeList(){

        ArrayList<HashMap<String, Object>> list = fileManager.readCourseGameFile();

        checkRefresh = list.size();

        if(checkRefresh == 0){
            TextView notiMsg = (TextView)view.findViewById(R.id.message_notice);
            notiMsg.setVisibility(View.VISIBLE);
            FrameLayout root = (FrameLayout)view.findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.background_color));
        }else{
            TextView notiMsg = (TextView)view.findViewById(R.id.message_notice);
            notiMsg.setVisibility(View.GONE);
            FrameLayout root = (FrameLayout)view.findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.list_background_color));
        }

        adapter = new CourseGameCustomAdapter(context, list, rv, mToolbar, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        mPtrFrameLayout.refreshComplete();
    }

    public void removeGameCourse(int pos){

        ArrayList<HashMap<String, Object>> list = fileManager.readCourseGameFile();
        list.remove(pos);
        fileManager.writeCourseGameFile(list);
        makeList();

    }



    public void startCourseGame(int position){

        ArrayList<HashMap<String, Object>> list = fileManager.readCourseGameFile();
        HashMap<String, Object> course = list.get(position);

        int tryCount = (int)course.get("try");
        tryCount += 1;

        int time = (int)course.get("time");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
        Date date = new Date();
        String startDate = dateFormat.format(date);

        Calendar cal = new GregorianCalendar(Locale.KOREA);
        cal.add(Calendar.HOUR, time);
        String finishDate = dateFormat.format(cal.getTime());

        course.put("try", tryCount);
        course.put("startDate", startDate);
        course.put("finishDate", finishDate);
        course.put("progress", 0);

        list.set(position, course);

        fileManager.writeCourseGameFile(list);

        adapter.updateList(list);

    }

    public void setGiveup(int position){

        int scrollX = rv.computeHorizontalScrollOffset();
        int scrollY = rv.computeVerticalScrollOffset();

        ArrayList<HashMap<String, Object>> list = fileManager.readCourseGameFile();
        HashMap<String, Object> course = list.get(position);

        course.put("startDate", null);
        course.put("finishDate", null);

        list.set(position, course);

        fileManager.writeCourseGameFile(list);

        adapter.updateList(list);

        makeList();

        rv.scrollBy(scrollX, scrollY);

    }

    public void hideViews() {
//        menu.setVisibility(View.INVISIBLE);
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    public void showViews() {
//        menu.setVisibility(View.VISIBLE);
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private int getScrollForRecycler(){
        return mTotalScrolled;
    }

    @Override
    public void onResume(){
        super.onResume();

        int scrollX = rv.computeHorizontalScrollOffset();
        int scrollY = rv.computeVerticalScrollOffset();

        makeList();

        rv.scrollBy(scrollX, scrollY);
    }

}
