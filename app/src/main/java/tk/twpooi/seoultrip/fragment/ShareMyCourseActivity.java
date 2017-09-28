package tk.twpooi.seoultrip.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;
import com.rengwuxian.materialedittext.MaterialEditText;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.Information;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-17.
 */
public class ShareMyCourseActivity extends AppCompatActivity {

    private FileManager fileManager;

    private MyHandler handler = new MyHandler();
    private static final int MSG_MESSAGE_SHARE_COURSE_START = 1001;
    private static final int MSG_MESSAGE_SHARE_COURSE_COMPLETE = 1002;
    private static final int MSG_MESSAGE_SHARE_COURSE_FAILED = 1003;
    private int position; // 코스 공유에서 공유 여부 체크를 위한 변수

    public static final String DRAG_POSITION = "drag_position";

    // UI
    private DraggerView draggerView;
    private Toolbar toolbar;
    private SweetAlertDialog pDialog;
    private SweetAlertDialog sDialog;
    private InputMethodManager imm;

    private MaterialEditText introduce;
    private Button submit;

    // Database
    private ShareCourse shareCourse;

    // Data
    private String successID;

    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_share_mycourse);
        setStatusColor();

        Intent intent = getIntent();
        position = intent.getIntExtra("position", -1);

        fileManager = new FileManager(getApplicationContext());

        init();

        checkShareValid();

    }

    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton();
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); // 화면터치시 키보드 숨김 관련

        setpDialoginit();

        introduce = (MaterialEditText)findViewById(R.id.introduce);
        introduce.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submit = (Button)findViewById(R.id.submit);
        submit.setEnabled(true);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareList();
            }
        });

        sDialog =  new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getResources().getString(R.string.failed))
                .setConfirmText(getResources().getString(R.string.ok))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        draggerView.closeActivity();
                    }
                });


    }

    private void checkShareValid(){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> attractionList = list.get(position);

        ArrayList<String> attr = (ArrayList<String>) attractionList.get("attraction");

        if((boolean)attractionList.get("isShare")){
            sDialog.setContentText(getResources().getString(R.string.already_share)).show();
        }else if(attr.size() < 3 || attr.size() > 10){
            sDialog.setContentText(getResources().getString(R.string.share_notice_message)).show();
        }

    }

    public void setpDialoginit(){
        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(getResources().getString(R.string.please_wait));
        pDialog.setCancelable(false);
    }


    public String getShareID(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        String randomNumber = Integer.toString((int) (Math.random() * (999 - 100 + 1)) + 100);

        String id = strDate + randomNumber;

        return id;
    }

    public void shareList(){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> attractionList = list.get(position);

        if(!(boolean)attractionList.get("isShare")) {

            String language = (String) attractionList.get("language");
            String title = (String) attractionList.get("title");
            String limit = Integer.toString((int) attractionList.get("time"));
            ArrayList<String> attr = (ArrayList<String>) attractionList.get("attraction");

            if (attr.size() >= 3 && attr.size() <= 10) {

                // 명소 보으기
                String attractions = "";
                for (String s : attr) {
                    attractions = attractions + s + ",";
                }
                attractions = attractions.substring(0, attractions.length() - 1);
                String id = getShareID();
                successID = id;

                HashMap<String, String> map = new HashMap<>();
                map.put("id", id);
                map.put("language", language);
                map.put("title", title);
                map.put("limit", limit);
                map.put("introduce", introduce.getText().toString());
                map.put("attractions", attractions);

                pDialog.dismiss();
                setpDialoginit();

                shareCourse = new ShareCourse(map);
                shareCourse.start();
            } else {
                new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getResources().getString(R.string.failed))
                        .setContentText(getResources().getString(R.string.share_notice_message))
                        .setConfirmText(getResources().getString(R.string.ok))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                draggerView.closeActivity();
                            }
                        })
                        .show();
            }
        }else{
            new SweetAlertDialog(getApplicationContext(), SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getResources().getString(R.string.failed))
                    .setContentText(getResources().getString(R.string.already_share))
                    .setConfirmText(getResources().getString(R.string.ok))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            draggerView.closeActivity();
                        }
                    })
                    .show();
        }
    }

    private class ShareCourse extends Thread{

        private boolean result;
        private HashMap<String, String> map;

        public ShareCourse(HashMap<String, String> map){
            this.map = map;
            result = false;
        }

        public void run(){

            Message msg = handler.obtainMessage(MSG_MESSAGE_SHARE_COURSE_START);
            handler.sendMessage(msg);

            String addr = Information.MAIN_SERVER_ADDRESS + "saveCourse.php";
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

                conn.disconnect();
            } catch (MalformedURLException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(response.equals("1")){
                msg = handler.obtainMessage(MSG_MESSAGE_SHARE_COURSE_COMPLETE);
                handler.sendMessage(msg);
            }else{
                msg = handler.obtainMessage(MSG_MESSAGE_SHARE_COURSE_FAILED);
                handler.sendMessage(msg);
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

        public boolean getResult(){
            return result;
        }

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_SHARE_COURSE_START:
                    pDialog.show();
                    break;
                case MSG_MESSAGE_SHARE_COURSE_COMPLETE:
                    pDialog.setTitleText(getResources().getString(R.string.success))
                            .setContentText(getResources().getString(R.string.success_share))
                            .setConfirmText(getResources().getString(R.string.close))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    draggerView.closeActivity();
                                }
                            })
                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    shareCourse.interrupt();
                    checkCourseShared(); // 공유됨으로 수정
                    break;
                case MSG_MESSAGE_SHARE_COURSE_FAILED:
                    pDialog.setTitleText(getResources().getString(R.string.failed))
                            .setContentText(getResources().getString(R.string.fail_share))
                            .setConfirmText(getResources().getString(R.string.close))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    draggerView.closeActivity();
                                }
                            })
                            .changeAlertType(SweetAlertDialog.WARNING_TYPE);
                    shareCourse.interrupt();
                    break;
                default:
                    break;
            }
        }
    }

    public void checkCourseShared(){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> courseList = list.get(position);
        courseList.put("isShare", true);
        fileManager.writeMyCourse(list);

        ArrayList<String> sharedList = fileManager.readSharedFile();
        sharedList.add(successID);
        fileManager.writeSharedFile(sharedList);

    }


    public void linearOnClick(View v) {
        imm.hideSoftInputFromWindow(introduce.getWindowToken(), 0);
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

    public void setButtonEnabled(){

        if(introduce.isCharactersCountValid()) {
            submit.setTextColor(getResources().getColor(R.color.white));
            submit.setEnabled(true);
        }else{
            submit.setTextColor(getResources().getColor(R.color.dark_gray));
            submit.setEnabled(false);
        }

    }

    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sDialog.isShowing()) {
            sDialog.dismiss();
        }
        if(pDialog.isShowing()) {
            pDialog.dismiss();
        }
        if(shareCourse != null && (shareCourse.isAlive() || shareCourse.isDaemon())) {
            shareCourse.interrupt();
        }
    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_status_bar_color));
        }
    }

}
