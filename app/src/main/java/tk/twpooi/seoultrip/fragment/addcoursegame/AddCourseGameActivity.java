package tk.twpooi.seoultrip.fragment.addcoursegame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-17.
 */
public class AddCourseGameActivity extends AppCompatActivity {

    private FileManager fileManager;

    public static final String DRAG_POSITION = "drag_position";

    // UI
    private DraggerView draggerView;
    private Toolbar toolbar;
    private SweetAlertDialog pDialog;
    private ViewPager viewPager;
    private NavigationAdapter mPagerAdapter;
    private TextView completeBtn;

    public String mode;

    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_add_course_game);

        fileManager = new FileManager(getApplicationContext());

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        if(mode == null){
            finish();
        }else {
            init();
        }

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
        completeBtn = (TextView)findViewById(R.id.complete_btn);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton();
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        setpDialoginit();

        // view pager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(1);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), mode);
        viewPager.setAdapter(mPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }


    public void completeBtnClicked(ArrayList<HashMap<String, Object>> saveList){

        ArrayList<HashMap<String, Object>> savedList = fileManager.readCourseGameFile();

        for(HashMap<String, Object> s : saveList){
            s.put("startDate", null);
            s.put("finishDate", null);
            s.put("try", 0);
            s.put("isSuccess", false);
            s.put("mode", mode);
            s.put("progress", 0);
            s.put("clearTime", 0f);
            savedList.add(s);
        }

        fileManager.writeCourseGameFile(savedList);

        if(saveList != null && saveList.size() > 0) {
//            Toast.makeText(getApplicationContext(), "성공적으로 추가하였습니다.", Toast.LENGTH_SHORT).show();
            showSnackbar(getResources().getString(R.string.success_add));
        }

    }

    public void showSnackbar(String msg){

    }

    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private String mode;

        public NavigationAdapter(FragmentManager fm, String mode){
            super(fm);
            this.mode = mode;
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %1;

            if(pattern == 0){
                if(mode.equals("myCourse")){
                    f = new AddToMyCourseFragment();
                }else if(mode.equals("sharedCourse")) {
                    f = new AddToSharedCourseFragment();
                }else if(mode.equals("random")){
                    f = new AddToRandomFragment();
                }else{
                    f = new Fragment();
                }
            }else{
                f = new Fragment();
            }

            return f;
        }

        @Override
        public int getCount(){
            return 1;
        }

    }

    public String getMode(){
        return mode;
    }
    public TextView getCompleteBtn(){
        return completeBtn;
    }
    public void setCompleteBtnOnClick(View.OnClickListener onClick){
        completeBtn.setOnClickListener(onClick);
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

    public Toolbar getToolbar(){
        return this.toolbar;
    }
    public void hideCompleteBtn(){
        completeBtn.setVisibility(View.GONE);
    }
    public void showCompleteBtn(){
        completeBtn.setVisibility(View.VISIBLE);
    }
    public void setStatusColorDark(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_status_bar_color));
        }
    }
    public void setToolbarColorLight(){
        toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
    }
    public void setToolbarColorDark(){
        toolbar.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_tab_bar_color));
    }

    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        }
        setToolbarColorLight();
    }

}
