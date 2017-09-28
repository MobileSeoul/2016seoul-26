package tk.twpooi.seoultrip.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-17.
 */
public class ShowCourseListActivity extends AppCompatActivity {

    private FileManager fileManager;

    private String language;

    public static final String DRAG_POSITION = "drag_position";

    private DraggerView draggerView;
    private Toolbar toolbar;
    private Button addtionBtn;
    private ImageView showMapBtn;

    private HashMap<String, Object> course;
    private ArrayList<String> attractionList;
    private int position;
    private String title;
    private String attraction;

    private Boolean isShowAddBtn;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerViewDragDropManager dragMgr;
    private ShowCourseListCustomAdapter adapter;

    public String getLanguage(){
        return language;
    }

    private void clearSingleton(){
        try{
            Field field = DraggerView.class.getDeclaredField("singleton");
            field.setAccessible(true);
            field.set(this, null);
        }catch(NoSuchFieldException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_course_list);
        setStatusColor();

        fileManager = new FileManager(getApplicationContext());

        init();

        makeList();

    }

    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton(); // 프레임 중복 해결
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

        dragMgr = new RecyclerViewDragDropManager();
        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);

        Intent i = getIntent();
        position = i.getIntExtra("position", 0);
        attraction = i.getStringExtra("attraction");
        isShowAddBtn = i.getBooleanExtra("isShowAddBtn", false);


        course = fileManager.readMyCourse().get(position);
        language = (String)course.get("language");
        title = (String)course.get("title");
        attractionList = (ArrayList<String>)course.get("attraction");


        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
        addtionBtn = (Button)findViewById(R.id.addtion_btn);
        if(isShowAddBtn && (!attractionList.contains(attraction))){
            addtionBtn.setVisibility(View.VISIBLE);
            addtionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveAttraction();
                }
            });
        }else{
            addtionBtn.setVisibility(View.GONE);
        }

        showMapBtn = (ImageView)findViewById(R.id.show_map_btn);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShowMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("attraction", attractionList);
                intent.putExtra("language", language);
                intent.putExtra("isPolyline", true);
                startActivity(intent);
            }
        });

    }

    private void saveAttraction(){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> hashTemp = list.get(position);
        ArrayList<String> attractionList = (ArrayList<String>)hashTemp.get("attraction");


        attractionList.add(attraction);

        if(attractionList.size() == 0){
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.VISIBLE);
        }else{
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.GONE);
        }

        hashTemp.put("attraction", attractionList);

        list.set(position, hashTemp);
        if(fileManager.writeMyCourse(list)){
            addtionBtn.setVisibility(View.GONE);
        }

        this.attractionList = attractionList;
        adapter.addList(attraction);
        adapter.notifyItemInserted(attractionList.size());

    }

    public void deleteAttraction(int pos){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> hashTemp = list.get(position);
        ArrayList<String> attractionList = (ArrayList<String>)hashTemp.get("attraction");


        String tempAttraction = attractionList.remove(pos);

        if(attractionList.size() == 0){
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.VISIBLE);
        }else{
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.GONE);
        }

        hashTemp.put("attraction", attractionList);

        list.set(position, hashTemp);
        if(fileManager.writeMyCourse(list) && isShowAddBtn && (tempAttraction.equals(attraction))){ // 저장
            addtionBtn.setVisibility(View.VISIBLE);
        }

        this.attractionList = attractionList;

    }

    public void makeList(){

        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

        dragMgr = new RecyclerViewDragDropManager();
        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        ArrayList<String> attractionList = (ArrayList<String>)list.get(position).get("attraction");

        if(attractionList.size() == 0){
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.VISIBLE);
        }else{
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.GONE);
        }

        if(isShowAddBtn && (!attractionList.contains(attraction))){
            addtionBtn.setVisibility(View.VISIBLE);
            addtionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveAttraction();
                }
            });
        }else{
            addtionBtn.setVisibility(View.GONE);
        }

        adapter = new ShowCourseListCustomAdapter(getApplicationContext(), attractionList, rv, toolbar, draggerView, this);

        rv.setAdapter(dragMgr.createWrappedAdapter(adapter));

        dragMgr.attachRecyclerView(rv);
    }

    public void moveItem(int fromPosition, int toPosition){
        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> hashTemp = list.get(position);
        ArrayList<String> attractionList = (ArrayList<String>)hashTemp.get("attraction");

        String temp = attractionList.remove(fromPosition);
        attractionList.add(toPosition, temp);


        hashTemp.put("attraction", attractionList);
        this.attractionList = attractionList;

        list.set(position, hashTemp);
        fileManager.writeMyCourse(list);

    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_status_bar_color));
        }
    }

    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

}
