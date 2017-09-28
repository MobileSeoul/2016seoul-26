package tk.twpooi.seoultrip.fragment.addcoursegame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;

import java.lang.reflect.Field;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-17.
 */
public class AddCourseGameMainActivity extends AppCompatActivity {

    private static final String fileName = "mycourse.st";

    public static final String DRAG_POSITION = "drag_position";

    // UI
    private DraggerView draggerView;
    private Toolbar toolbar;
    private SweetAlertDialog pDialog;

    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_add_course_game_main);

        init();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton();
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        setpDialoginit();

        Button myCourse = (Button)findViewById(R.id.my_course_btn);
        Button sharedCourse = (Button)findViewById(R.id.shared_course_btn);
        Button random = (Button)findViewById(R.id.random_btn);
        myCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddCourseGameMainActivity.this, AddCourseGameActivity.class);
                intent.putExtra(AddCourseGameActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("mode", "myCourse");
                startActivity(intent);
            }
        });
        sharedCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddCourseGameMainActivity.this, AddCourseGameActivity.class);
                intent.putExtra(AddCourseGameActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("mode", "sharedCourse");
                startActivity(intent);
            }
        });
        random.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddCourseGameMainActivity.this, AddCourseGameActivity.class);
                intent.putExtra(AddCourseGameActivity.DRAG_POSITION, DraggerPosition.RIGHT);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.putExtra("mode", "random");
                startActivity(intent);
            }
        });


    }

    public void setpDialoginit(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(getResources().getString(R.string.please_wait));
        pDialog.setCancelable(false);
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

    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

}
