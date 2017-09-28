package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.TextRoundCornerProgressBar;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.CustomViewPager;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.GpsInfo;
import tk.twpooi.seoultrip.Information;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-17.
 */
public class DetailCourseGameActivity extends AppCompatActivity {

    private FileManager fileManager;

    private MyHandler handler = new MyHandler();
    private static final int MSG_MESSAGE_PROGRESS_START = 1001;
    private static final int MSG_MESSAGE_PROGRESS_FINISH = 1002;
    private static final int MSG_MESSAGE_SUCCESS = 1003;
    private static final int MSG_MESSAGE_FAILED = 1004;
    private static final int MSG_MESSAGE_MAKE_SHARE_URL_START = 2001;
    private static final int MSG_MESSAGE_MAKE_SHARE_URL_FINISH = 2002;
    private static final int MSG_MESSAGE_MAKE_SHARE_URL_ERROR = 2003;

    private static final int SUCCESS_DISTANCE = 1000;

    public static final String DRAG_POSITION = "drag_position";

    private DraggerView draggerView;
    private Toolbar toolbar;

    // Database
    private DBHelper dbHelper;

    // UI
    private CustomViewPager viewPager;
    private NavigationAdapter mPagerAdapter;
    private DotIndicator dotIndicator;
    private TextRoundCornerProgressBar progressBar;
    private TextView progressCountText;
    private Button checkLocationBtn;
    private Button shareResultBtn;

    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    private SweetAlertDialog wDialog;


    private int position;
    private String language;
    private HashMap<String,Object> courseData;
    private String title;
    private int time;
    private int tryCount;
    private float clearTime;
    static protected int progress;
    private ArrayList<String> attractionList;
    private String mode;
    private boolean isAddress = false;

    private Handler mHandler;
    private CountDownTimer countDownTimer;
    private float remainTime; // 단위 second
    private float totalTime; // 단위 second

    // Location
    private CheckLocation checkLocation;
    static protected LocationManager manager;


    // Facebook
    private CallbackManager callbackManager;
    private String facebook_id = "";
    private String full_name = "";

    @Override
    public void onResume(){
        super.onResume();
        setStatusColor();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_detail_course_game);

        fileManager = new FileManager(getApplicationContext());

        initData();

        init();

        if(progress<attractionList.size()) {
            setCounterText();
        }

    }

    private void initData(){
        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);
        courseData = (HashMap<String, Object>)intent.getSerializableExtra("course");

        mode = (String)courseData.get("mode");
        title = (String)courseData.get("title");
        language = (String)courseData.get("language");
        time = (int)courseData.get("time");
        totalTime = time*60*60;
        tryCount = (int)courseData.get("try");
        attractionList = (ArrayList<String>)courseData.get("attraction");
        progress = (int)courseData.get("progress");
        clearTime = (float)courseData.get("clearTime");

        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_course_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.give_up_btn) {

            new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getResources().getString(R.string.ok))
                    .setContentText(getResources().getString(R.string.are_you_give_up))
                    .setConfirmText(getResources().getString(R.string.give_up))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.hide();
                            ArrayList<HashMap<String, Object>> list = fileManager.readCourseGameFile();
                            list.remove(position);
                            fileManager.writeCourseGameFile(list);
                            sDialog.dismiss();
                            finish();
                        }
                    })
                    .setCancelText(getResources().getString(R.string.cancel))
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.hide();
                        }
                    }).show();
            return true;
        }else if(id == R.id.show_map_btn){
            Intent intent = new Intent(getApplicationContext(), ShowMapActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("attraction", attractionList);
            intent.putExtra("language", language);
            intent.putExtra("isPolyline", true);
            startActivity(intent);
            return true;
        }else if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton(); // 프레임 중복 해결
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));


        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (CustomViewPager)findViewById(R.id.viewPager);
        dotIndicator = (DotIndicator)findViewById(R.id.main_indicator_ad);

        setpDialoginit();

        viewPager.setOffscreenPageLimit(attractionList.size());
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), attractionList, attractionList.size());
        viewPager.setAdapter(mPagerAdapter);

        if(progress>=attractionList.size()){
            viewPager.setCurrentItem(attractionList.size()-1);
        }else {
            viewPager.setCurrentItem(progress);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                dotIndicator.setSelectedItem(position, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initIndicator();

        sDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getResources().getString(R.string.ok))
                .setContentText(getResources().getString(R.string.are_you_continue))
                .setConfirmText(getResources().getString(R.string.yes))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.hide();
                        increaseProgress();
                    }
                })
                .setCancelText(getResources().getString(R.string.no))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.hide();
                    }
                });

        setwDialoginit();

        progressCountText = (TextView)findViewById(R.id.progress_count);
        setProgressCountText();

        checkLocationBtn = (Button)findViewById(R.id.check_location_btn);
        checkLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAddress) {
                    try {
                        checkLocation.interrupt();
                    } catch (Exception e) {
                    }
                    checkLocation = new CheckLocation();
                    checkLocation.start();
                }else{
                    sDialog.show();
                }
            }
        });

        shareResultBtn = (Button)findViewById(R.id.share_result);


        countDownTimer = new CountDownTimer(12 * 10000000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setCounterText();
            }

            @Override
            public void onFinish() {
                countDownTimer.start();
            }
        };
        if(progress<attractionList.size()) {
            countDownTimer.start();
        }

        setProgressBar();
        setCheckLocationBtn();

    }

    private void initIndicator(){
        dotIndicator.setSelectedDotColor(Color.parseColor("#FF4081"));
        dotIndicator.setUnselectedDotColor(Color.parseColor("#CFCFCF"));
        dotIndicator.setNumberOfItems(attractionList.size());
        if(progress>= attractionList.size()) {
            dotIndicator.setSelectedItem(attractionList.size()-1, true);
        }else{
            dotIndicator.setSelectedItem(progress, true);
        }
    }

    public String getLanguage(){
        return language;
    }

    private void setFinish(){

        clearTime = totalTime - remainTime;

        courseData.put("clearTime", clearTime);

        ArrayList<HashMap<String, Object>> tempList = fileManager.readCourseGameFile();
        tempList.set(position, courseData);
        fileManager.writeCourseGameFile(tempList);

        SharedPreferences setting = getSharedPreferences("setting", 0);
        SharedPreferences.Editor editor = setting.edit();
        int successCount = setting.getInt("successCourseGameCount", 0);
        successCount+=1;
        editor.putInt("successCourseGameCount", successCount);
        editor.commit();

        countDownTimer.cancel();
//        checkLocationBtn.setEnabled(false);
        checkLocationBtn.setText(getResources().getString(R.string.complete));
        checkLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        shareResultBtn.setVisibility(View.VISIBLE);
        shareResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkFacebookLogin();
            }
        });
        if(mode.equals("sharedCourse")){
            String id = (String)courseData.get("id");
            successes(id);
        }

    }

    private void loginFacebook(){

        LoginManager.getInstance().logInWithReadPermissions(DetailCourseGameActivity.this, Arrays.asList("public_profile", "user_friends", "email"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showSnackbar(getResources().getString(R.string.success_login) + "!");
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    facebook_id=profile.getId();
                    full_name=profile.getName();
                }

                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    String profile_name=object.getString("name");
                                    long fb_id=object.getLong("id"); //use this for logout
                                    //Start new activity or use this info in your project.
                                    //Go another activity
                                } catch (JSONException e) {
                                }

                            }

                        });

                request.executeAsync();

                MakeShareCondition makeShareCondition = new MakeShareCondition();
                makeShareCondition.start();
            }

            @Override
            public void onCancel() {
                showSnackbar(getResources().getString(R.string.cancel_login));
            }

            @Override
            public void onError(FacebookException error) {
                showSnackbar(getResources().getString(R.string.error_login) + "\n" + error.toString());
            }
        });

    }

    private void checkFacebookLogin(){

        Profile profile = Profile.getCurrentProfile();

        if(profile != null){
            facebook_id=profile.getId();
            full_name=profile.getName();
            MakeShareCondition makeShareCondition = new MakeShareCondition();
            makeShareCondition.start();
        }else{
            loginFacebook();
        }

    }

    private class MakeShareCondition extends Thread{

        public void run(){

            Message msg = handler.obtainMessage(MSG_MESSAGE_MAKE_SHARE_URL_START);
            handler.sendMessage(msg);

            String url = makeShareUrl();

            if(url != null) {

                msg = handler.obtainMessage(MSG_MESSAGE_MAKE_SHARE_URL_FINISH);
                handler.sendMessage(msg);

                shareResultToFacebook(url);
            }else{
                msg = handler.obtainMessage(MSG_MESSAGE_MAKE_SHARE_URL_ERROR);
                handler.sendMessage(msg);
            }


        }

    }

    private void shareResultToFacebook(String url){

        System.out.println(url);

        Profile profile = Profile.getCurrentProfile();
        facebook_id=profile.getId();
        full_name=profile.getName();

        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
        if(ShareDialog.canShow(ShareLinkContent.class)){
            String attractionText = attractionList.toString();
            String msg = String.format(getResources().getString(R.string.fb_mention), full_name, title, makeTimeText(clearTime), attractionText);

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("SeoulTrip " + title + " " + getResources().getString(R.string.course))
                    .setContentDescription(msg)
                    .setContentUrl(Uri.parse(url))
                    .build();

            shareDialog.show(linkContent);
        }


    }

    private String makeShareUrl(){

        String url = Information.SUB_SERVER_ADDRESS + "shareFBCourse.php?id=";
        String id;

        String att = "";
        for(int i=0; i<attractionList.size(); i++){
            att += attractionList.get(i);
            if(i < attractionList.size()-1){
                att += ",";
            }
        }

        // 중복된 코스가 이미 생성되었는지 확인
        HashMap<String, String> map = new HashMap<>();
        map.put("type", "checkAttraction");
        map.put("attraction", att);
        SaveServer saveServer = new SaveServer(map);
        saveServer.start();
        try{
            saveServer.join();
        }catch (Exception e){}
        String check = saveServer.getResult();
        if(check == null){
            return null;
        }
        saveServer.interrupt();

        if(check.equals("0")){
            while (true) { // 중복된 id가 있는지 확인
                id = makeRandomString(10);
                map.clear();
                map.put("type", "checkID");
                map.put("ids", id);
                saveServer = new SaveServer(map);
                saveServer.start();
                try{
                    saveServer.join();
                }catch (Exception e){}
                String check2 = saveServer.getResult();
                saveServer.interrupt();
                if(check2.equals("0")){
                    break;
                }
                if(check2 == null){
                    return null;
                }
            }

        }else{
            id = check;
            url += id;
            return url;
        }

        // 새로운 DB 저장
        map.clear();
        map.put("type", "save");
        map.put("ids", id);
        map.put("attraction", att);
        map.put("title", title);
        map.put("language", language);
        saveServer = new SaveServer(map);
        saveServer.start();
        try{
            saveServer.join();
        }catch (Exception e){}
        String result = saveServer.getResult();
        saveServer.interrupt();

        if(result.equals("1")){
        }
        if(result == null){
            return null;
        }

        url += id;
        return url;
    }

    public class SaveServer extends Thread{

        private String result;
        private HashMap<String, String> map;

        public SaveServer(HashMap<String, String> map){
            this.map = map;
        }

        public void run(){

            String addr = Information.SUB_SERVER_ADDRESS + "shareFBCourse.php";
            String response = new String();

            try {
                URL url = new URL(addr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 URL에 연결

                conn.setConnectTimeout(10000); // 타임아웃: 10초
                conn.setUseCaches(false); // 캐시 사용 안 함
                conn.setRequestMethod("POST"); // POST로 연결
                conn.setDoInput(true);
                conn.setDoOutput(true);

                if (map != null) { // 웹 서버로 보낼 매개변수가 있는 경우우
                    OutputStream os = conn.getOutputStream(); // 서버로 보내기 위한 출력 스트림
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); // UTF-8로 전송
                    bw.write(getPostString(map)); // 매개변수 전송
                    bw.flush();
                    bw.close();
                    os.close();
                }

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 연결에 성공한 경우
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 서버의 응답을 읽기 위한 입력 스트림

                    while ((line = br.readLine()) != null) // 서버의 응답을 읽어옴
                        response += line;
                }

                result = response;

                conn.disconnect();
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        private String getPostString(HashMap<String, String> map) {
            StringBuilder result = new StringBuilder();
            boolean first = true; // 첫 번째 매개변수 여부

            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (first)
                    first = false;
                else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
                    result.append("&");

                try { // UTF-8로 주소에 키와 값을 붙임
                    result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    result.append("=");
                    result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                } catch (UnsupportedEncodingException ue) {
                    ue.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return result.toString();
        }

        public String getResult(){
            return result;
        }

    }


    private void setProgressBar(){

        progressBar = (TextRoundCornerProgressBar)findViewById(R.id.progress);
        progressBar.setMax(100);
        progressBar.setProgress(0);

    }

    public void setpDialoginit(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(getResources().getString(R.string.information));
        pDialog.setContentText(getResources().getString(R.string.checking_location) + "\n" + getResources().getString(R.string.please_wait));
        pDialog.setCancelable(false);
    }

    public void setwDialoginit(){
        wDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText(getResources().getString(R.string.please_wait));
    }

    private void setCheckLocationBtn(){
        if(progress < attractionList.size()) {
            String temp = dbHelper.getResultSearchTitle(attractionList.get(progress), language).get(0).get("address");
            if (temp == null || temp.equals("")) {
                isAddress = false;
            } else {
                isAddress = true;
            }

            if (isAddress) {
                checkLocationBtn.setText(getResources().getString(R.string.check_location));
            } else {
                checkLocationBtn.setText(getResources().getString(R.string.pass));
            }
        }else{
            checkLocationBtn.setText(getResources().getString(R.string.complete));
            checkLocationBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
            shareResultBtn.setVisibility(View.VISIBLE);
            shareResultBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkFacebookLogin();
                }
            });

            String text = makeTimeText(totalTime - clearTime);

            float percentage = (totalTime-clearTime)/totalTime*100;
            progressBar.setProgress(percentage);
            progressBar.setProgressText(text);
        }
    }

    private void setCounterText(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
        Date date = new Date();
        String currentDate = dateFormat.format(date);

        String finishDate = (String)courseData.get("finishDate");

        try{
            Date current = dateFormat.parse(currentDate);
            Date finish = dateFormat.parse(finishDate);

            Calendar startCal = Calendar.getInstance();
            Calendar finishCal = Calendar.getInstance();

            startCal.setTime(current);
            finishCal.setTime(finish);

            long millis = finishCal.getTimeInMillis() - startCal.getTimeInMillis();

            remainTime = millis/1000;

            String text = makeTimeText(remainTime);

            float percentage = remainTime/totalTime*100;
            progressBar.setProgress(percentage);
            progressBar.setProgressText(text);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String makeTimeText(float time){

        int hours = (int)((time/(60*60)) % 24);
        int minutes = (int)((time/(60)) % 60);
        int seconds = (int)((time) % 60);

        String text;
        if(hours == 0){
            if(minutes == 0){
                text = String.format(getResources().getString(R.string.time_s), seconds);
            }else{
                text = String.format(getResources().getString(R.string.time_ms), minutes, seconds);
            }
        }else{
            text = String.format(getResources().getString(R.string.time_hms), hours, minutes, seconds);
        }

        return text;

    }

    public void setProgressUI(){
        if(progress <= attractionList.size()) {
            DetailCourseGameFragment f = (DetailCourseGameFragment) mPagerAdapter.getItemAt(progress - 1);
            f.setOverlayVisiable();

            viewPager.setCurrentItem(progress);
            setProgressCountText();
        }

    }

    public void increaseProgress(){


        if(progress < attractionList.size()) {
            progress += 1;
            courseData.put("progress", progress);

            ArrayList<HashMap<String, Object>> tempList = fileManager.readCourseGameFile();
            tempList.set(position, courseData);
            fileManager.writeCourseGameFile(tempList);

            setProgressUI();

            if(progress>= attractionList.size()){
                setFinish();
            }else{
                setCheckLocationBtn();
            }

        }else{
            setFinish();
        }

    }

    private void setProgressCountText(){
        String current = "";
        if(progress < attractionList.size()) {
            current = String.format(getResources().getString(R.string.current_progress), attractionList.size(), progress);
        }else{
            current = String.format(getResources().getString(R.string.current_progress), attractionList.size(), attractionList.size());
        }
        progressCountText.setText(current);
    }

    private void successes(String id){
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("mode", "1");
        map.put("type", "success");
        IncreaseShareCourseItem increaseShareCourseItem = new IncreaseShareCourseItem(map);
        increaseShareCourseItem.start();
        try{
            increaseShareCourseItem.join();
        }catch (Exception e){}
        increaseShareCourseItem.interrupt();
    }


    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_PROGRESS_START:
                    pDialog.show();
                    break;
                case MSG_MESSAGE_PROGRESS_FINISH:
                    pDialog.hide();
                    break;
                case MSG_MESSAGE_FAILED:
                    pDialog.setTitleText(getResources().getString(R.string.failed))
                            .setContentText(
                                    getResources().getString(R.string.fail_location_check) + "\n"
                                    + getResources().getString(R.string.check_permission) + "\n"
                                    + getResources().getString(R.string.fail_location_message)
                            )
                            .setConfirmText(getResources().getString(R.string.close))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    pDialog.hide();
                                    setpDialoginit();
                                }
                            })
                            .setCancelText(getResources().getString(R.string.setting))
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                            .changeAlertType(SweetAlertDialog.WARNING_TYPE);
                    break;
                case MSG_MESSAGE_SUCCESS:
                    checkDistance();

                    break;
                case MSG_MESSAGE_MAKE_SHARE_URL_START:
                    wDialog.show();
                    break;
                case MSG_MESSAGE_MAKE_SHARE_URL_FINISH:
                    wDialog.hide();
                    break;
                case MSG_MESSAGE_MAKE_SHARE_URL_ERROR:
                    wDialog.setTitleText(getResources().getString(R.string.error))
                            .setContentText(
                                    getResources().getString(R.string.no_respond_server) + "\n"
                                    + getResources().getString(R.string.please_wait)
                            )
                            .setConfirmText(getResources().getString(R.string.close))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    wDialog.dismiss();
                                    setwDialoginit();
                                }
                            })
                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    break;
                default:
                    break;
            }
        }
    }

    private void checkDistance(){

        int distance = (int)checkLocation.getDistance();

        if(distance <= SUCCESS_DISTANCE){

            pDialog.setTitleText(getResources().getString(R.string.success))
                    .setContentText(String.format(getResources().getString(R.string.distance_to_attraction), distance))
                    .setConfirmText(getResources().getString(R.string.close))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            pDialog.hide();
                            increaseProgress();
                            setpDialoginit();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

        }else{

            pDialog.setTitleText(getResources().getString(R.string.failed))
                    .setContentText(String.format(getResources().getString(R.string.distance_to_attraction_failed), distance, SUCCESS_DISTANCE))
                    .setConfirmText(getResources().getString(R.string.close))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            pDialog.hide();
                            setpDialoginit();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);

        }



    }

    private class CheckLocation extends Thread{

        private double distance = -1;

        public void run(){

            Message msg = handler.obtainMessage(MSG_MESSAGE_PROGRESS_START);
            handler.sendMessage(msg);

            double distance = distance = checkDangerousPermissions();
            this.distance = distance;

            if(distance >= 0){
                msg = handler.obtainMessage(MSG_MESSAGE_SUCCESS);
                handler.sendMessage(msg);
            }else{
                msg = handler.obtainMessage(MSG_MESSAGE_FAILED);
                handler.sendMessage(msg);
            }

        }

        public double getDistance(){
            return distance;
        }

    }


    private double checkDangerousPermissions() {

        double lat = 0;
        double lng = 0;

        String[] permissions = {
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;

            Looper.prepare();

            LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            GpsInfo gpsInfo = new GpsInfo(getApplicationContext());

            if(gpsInfo.isGetLocation()){
                lat = gpsInfo.getLatitude();
                lng = gpsInfo.getLongitude();
            }

            gpsInfo.stopUsingGPS();



        if(lat != 0 && lng != 0){
            String attraction = attractionList.get(progress);
            HashMap<String, String> attr = dbHelper.getResultSearchTitle(attraction, language).get(0);

            Double attractionLat = Double.parseDouble((String)attr.get("lat"));
            Double attractionLng = Double.parseDouble((String)attr.get("lng"));

            int distance = (int)getDistance(lat, lng, attractionLat, attractionLng);

            return Math.abs(distance);
        }else{
            return  -1;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
//                    Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private double getDistance(double lat1, double lng1, double lat2, double lng2){

        double theta = lng1 - lng2;
        double dist = Math.sin(degTorad(lat1)) * Math.sin(degTorad(lat2)) + Math.cos(degTorad(lat1)) * Math.cos(degTorad(lat2)) * Math.cos(degTorad(theta));

        dist = Math.acos(dist);
        dist = radTodeg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1609.344;

        return dist;

    }
    private double degTorad(double deg){
        return (deg * Math.PI / 180.0);
    }
    private double radTodeg(double rad){
        return (rad * 180 / Math.PI);
    }

    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        ArrayList<String> attr;
        private int size;

        public NavigationAdapter(FragmentManager fm, ArrayList<String> attr, int size){
            super(fm);
            this.attr = attr;
            this.size = size;
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %size;

            int progress = DetailCourseGameActivity.progress;

            f = new DetailCourseGameFragment();
            Bundle bdl = new Bundle(1);
            bdl.putSerializable("title", attr.get(pattern));
            bdl.putInt("position", pattern);
            if(pattern < progress){
                bdl.putBoolean("visiable", true);
            }
            f.setArguments(bdl);

            return f;
        }

        @Override
        public int getCount(){
            return size;
        }

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
    public void onDestroy(){
        super.onDestroy();
        try{
            countDownTimer.cancel();
        }catch (Exception e){

        }
        countDownTimer = null;

        try{
            pDialog.dismiss();
        }catch (Exception e){}
        try{
            wDialog.dismiss();
        }catch (Exception e){}
    }

    private String versionCodeCheck(){
        int version;
        try{
            PackageInfo i = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = i.versionCode;
            return Integer.toString(version);
        }catch(PackageManager.NameNotFoundException e){
            return "";
        }
    }

    public static String makeRandomString(int len){

        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();

        StringBuilder sb = new StringBuilder(len);
        for( int i = 0; i < len; i++ )
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb.toString();
    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_status_bar_color));
        }
    }

}
