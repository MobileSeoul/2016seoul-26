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
import android.widget.TextView;

import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-17.
 */
public class AddCourseListActivity extends AppCompatActivity {

    private FileManager fileManager;

    public static final String DRAG_POSITION = "drag_position";

    // UI
    private DraggerView draggerView;
    private Toolbar toolbar;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

    private String attraction;
    private String language;

    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_add_course_list);

        Intent i = getIntent();
        attraction = i.getStringExtra("attraction");
        language = i.getStringExtra("language");

        fileManager = new FileManager(getApplicationContext());

        init();

        makeList();

    }

    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);


        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

        TextView addBtn = (TextView)findViewById(R.id.add_course);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddDraggerActivity(DraggerPosition.TOP);
            }
        });

    }

    public void makeList(){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();

        if(list.size() == 0){
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.VISIBLE);
        }else{
            TextView msgNoti = (TextView)findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.GONE);
        }

        final AddCourseListCustomAdapter adapter = new AddCourseListCustomAdapter(getApplicationContext(), list, rv, toolbar, attraction, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public String getLanguage(){
        return language;
    }


    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }



    private void startAddDraggerActivity(DraggerPosition dragPosition) {
        Intent intent = new Intent(getApplicationContext(), AddMyCourseActivity.class);
        intent.putExtra(AddMyCourseActivity.DRAG_POSITION, dragPosition);
        intent.putExtra("language", language);
        startActivityNoAnimation(intent);
    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
        makeList();
        super.onResume();
    }

}
