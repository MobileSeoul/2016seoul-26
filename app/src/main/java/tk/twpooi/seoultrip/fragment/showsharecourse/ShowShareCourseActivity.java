package tk.twpooi.seoultrip.fragment.showsharecourse;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;
import com.sackcentury.shinebuttonlib.ShineButton;

import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.IncreaseShareCourseItem;
import tk.twpooi.seoultrip.fragment.ShowMapActivity;

/**
 * Created by tw on 2016-08-15.
 */
public class ShowShareCourseActivity extends AppCompatActivity {

    private FileManager fileManager;

    private DBHelper dbHelper;

    // UI
    private ViewPager viewPager;
    private NavigationAdapter mPagerAdapter;
    private DotIndicator dotIndicator;
    private TextView courseTitleText;

    // Button UI
    private LinearLayout li_button;
    private ImageView addCourseBtn;
    private ImageView showMapBtn;
    private boolean isCourseGame;
    private boolean isFromRecommend;

    private ArrayList<String> attractionTitleList;
    private static final int startPage = 0;

    private String whereMode = null;
    private HashMap<String,Object> course;
    private String id;
    private String language;
    private String courseTitle;
    private int limit;
    private String introduce;
    private ArrayList<String> attraction; // 명소 이름만
    private ArrayList<HashMap<String, String>> attractionList; // 명소에 대한 모든 정보

    private IncreaseShareCourseItem increaseShareCourseItem;

    public String getId(){
        return id;
    }
    public String getLanguage(){
        return language;
    }
    public String getCourseTitle(){
        return courseTitle;
    }
    public int getLimit(){
        return limit;
    }
    public String getIntroduce(){
        return introduce;
    }
    public HashMap<String, Object> getCourse(){
        return course;
    }
    public ArrayList<HashMap<String, String>> getAttractionList(){
        return attractionList;
    }



    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_share_course);

        fileManager = new FileManager(getApplicationContext());

        initData();

        initUI();

    }

    private void initData(){
        Intent intent = getIntent();
        course = (HashMap<String, Object>)intent.getSerializableExtra("course");
        isCourseGame = intent.getBooleanExtra("isCourseGame", false);
        isFromRecommend = intent.getBooleanExtra("isFromRecommend", false);
        whereMode = intent.getStringExtra("mode");


        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        id = intent.getStringExtra("id");
        courseTitle = (String)course.get("title");
        limit = (Integer)course.get("time");
        introduce = (String)course.get("introduce");
        language = (String)course.get("language");

        attractionTitleList = new ArrayList<>();
        attractionTitleList.add("코스");

        attractionList = new ArrayList<>();
        attraction = (ArrayList<String>)course.get("attraction");
        for(String s : attraction){
            attractionTitleList.add(s);
            String tempLanguage = language;
            if(!tempLanguage.equals(StartActivity.DATABASE_LANGUAGE)){
                String tempTitle = dbHelper.getConvertTitle(s, tempLanguage);
                if(tempTitle != null){
                    tempLanguage = StartActivity.DATABASE_LANGUAGE;
                    s = tempTitle;
                }
            }

            HashMap<String, String> temp = dbHelper.getResultSearchTitle(s, tempLanguage).get(0);
            temp.put("language", tempLanguage);
            attractionList.add(temp);
        }

        hits();
    }

    private void likes(boolean isLike){
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        if(isLike){
            map.put("mode", "1");
        }else{
            map.put("mode", "0");
        }
        map.put("type", "likes");
        IncreaseShareCourseItem increaseShareCourseItem = new IncreaseShareCourseItem(map);
        increaseShareCourseItem.start();
        try{
            increaseShareCourseItem.join();
        }catch (Exception e){}
        increaseShareCourseItem.interrupt();
    }

    private void puts(){
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("mode", "1");
        map.put("type", "put");
        IncreaseShareCourseItem increaseShareCourseItem = new IncreaseShareCourseItem(map);
        increaseShareCourseItem.start();
        try{
            increaseShareCourseItem.join();
        }catch (Exception e){}
        increaseShareCourseItem.interrupt();
    }

    private void hits(){
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("mode", "1");
        map.put("type", "hits");
        increaseShareCourseItem = new IncreaseShareCourseItem(map);
        increaseShareCourseItem.start();
    }

    private void initUI() {

        RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        dotIndicator = (DotIndicator)findViewById(R.id.main_indicator_ad);

        viewPager.setOffscreenPageLimit(attractionTitleList.size());
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), attractionList, attractionTitleList.size());
        viewPager.setAdapter(mPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dotIndicator.setSelectedItem(position%attractionTitleList.size(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initIndicator();

        // 상단 바

        courseTitleText = (TextView)findViewById(R.id.course_title);
        courseTitleText.setText(courseTitle);

        showMapBtn = (ImageView)findViewById(R.id.show_map_btn);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ShowMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("attraction", attraction);
                intent.putExtra("language", language);
                intent.putExtra("isPolyline", true);
                startActivity(intent);
            }
        });


        // Button UI
        addCourseBtn = (ImageView)findViewById(R.id.add_course_btn);
        addCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSharedCourseToMyCourse();
            }
        });

        li_button = (LinearLayout)findViewById(R.id.li_button);
        if(isCourseGame){
            addCourseBtn.setVisibility(View.GONE);
        }


        final ShineButton interestBtn = (ShineButton)findViewById(R.id.interest_btn);
        if(isFromRecommend){
            interestBtn.setVisibility(View.VISIBLE);
            ArrayList<String> interestList = fileManager.readInterestListFile();
            interestBtn.setChecked(interestList.contains(id)); // 이미 관심 표시를한 코스일 경우
            interestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(interestBtn.isChecked()){ // 활성화 되었을 경우
                        likes(true);
                        ArrayList<String> interestList = fileManager.readInterestListFile();
                        if(!interestList.contains(id)) {
                            interestList.add(id);
                            fileManager.writeInterestListFile(interestList);
                        }
                    }else{
                        likes(false);
                        ArrayList<String> interestList = fileManager.readInterestListFile();
                        if(interestList.contains(id)){
                            interestList.remove(id);
                            fileManager.writeInterestListFile(interestList);
                        }
                    }
                }
            });
        }else{
            interestBtn.setVisibility(View.GONE);
        }


    }

    private void initIndicator(){
        dotIndicator.setSelectedDotColor(Color.parseColor("#FF4081"));
        dotIndicator.setUnselectedDotColor(Color.parseColor("#CFCFCF"));
        dotIndicator.setNumberOfItems(attractionTitleList.size());
    }

    private void SaveSharedCourseToMyCourse(){

        SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getResources().getString(R.string.information))
                .setContentText(courseTitle + " " + getResources().getString(R.string.are_you_save_course))
                .setCancelText(getResources().getString(R.string.cancel))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .setConfirmText(getResources().getString(R.string.ok))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        boolean result = false;

                        try {
                            HashMap<String, Object> temp = new HashMap<String, Object>();
                            temp.put("title", course.get("title"));
                            temp.put("language", language);
                            temp.put("time", course.get("time"));
                            temp.put("attraction", course.get("attraction"));
                            if(whereMode == null){
                                temp.put("isShare", true);
                            }else if(whereMode.equals("random")){
                                temp.put("isShare", false);
                            }else {
                                temp.put("isShare", true);
                            }
                            temp.put("isGame", false);

                            ArrayList<HashMap<String, Object>> tempList = fileManager.readMyCourse();
                            tempList.add(temp);
                            fileManager.writeMyCourse(tempList);

                            result = true;

                        }catch (Exception e){
                            e.printStackTrace();
                            result = false;

                        }

                        if(result){
                            puts(); // db에 저장
                            sDialog.setTitleText(getResources().getString(R.string.success))
                                    .setContentText(getResources().getString(R.string.success_save))
                                    .setConfirmText(getResources().getString(R.string.ok))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                            showSnackbar(getResources().getString(R.string.save_to_my_course));
                                        }
                                    })
                                    .showCancelButton(false)
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        }else{
                            sDialog.setTitleText(getResources().getString(R.string.failed))
                                    .setContentText(getResources().getString(R.string.fail_save))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            sweetAlertDialog.dismiss();
                                        }
                                    })
                                    .showCancelButton(false)
                                    .changeAlertType(SweetAlertDialog.WARNING_TYPE);
                        }


                    }
                });
                pDialog.show();


    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }




    // Tab Adapter Class (used initUI())
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter{

        ArrayList<HashMap<String, String>> attr;
        private int size;

        public NavigationAdapter(FragmentManager fm, ArrayList<HashMap<String, String>> attr, int size){
            super(fm);
            this.attr = attr;
            this.size = size;
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %size;

            if(pattern == 0){
                f = new CourseInfoFragment();
            }
            else{
                f = new SimpleAttractionFragment();
                Bundle bdl = new Bundle(1);
                bdl.putSerializable("attr", attr.get(pattern-1));
                f.setArguments(bdl);
            }

            return f;
        }

        @Override
        public int getCount(){
            return size;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(increaseShareCourseItem != null && (increaseShareCourseItem.isAlive() || increaseShareCourseItem.isDaemon())) {
            increaseShareCourseItem.interrupt();
        }
    }

}
