package tk.twpooi.seoultrip;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final int MSG_MESSAGE_PROGRESS_START = 10;
    private static final int MSG_MESSAGE_DATABASE = 1000;
    private static final int MSG_MESSAGE_NEW_DATABASE = 1001;
    private static final int MSG_MESSAGE_UPDATE_DATABASE = 1002;
    private static final int MSG_MESSAGE_DOWNLOAD_DATABASE = 1003;
    private static final int MSG_MESSAGE_CREATE_DATABASE = 1004;
    private static final int MSG_MESSAGE_SUCCESS = 500;
    private static final int MSG_MESSAGE_FAIL = 501;

    private MyHandler handler = new MyHandler();

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private DBVersionCheck dbCheck;
    private MadeNewDB madeNewDB;

    private static final String TABLE_NAME = "AttractionsTbl";

    //
    private TextView progressState;
    private Button retryBtn;
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusColor();


        versionCodeCheck();

        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        progressState = (TextView)findViewById(R.id.progress_state);
        retryBtn = (Button)findViewById(R.id.retry_btn);
        avi = (AVLoadingIndicatorView)findViewById(R.id.avi);
        avi.show();

        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();


        dbCheck = new DBVersionCheck();
        madeNewDB = new MadeNewDB();

        if(setting.getString("DBVersion", null) == null && (!isOnline())){
            progressState.setText(R.string.check_network_status);
            retryBtn.setVisibility(View.VISIBLE);
            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isOnline()) {
                        retryBtn.setVisibility(View.GONE);
                        dbCheck.start();
                    }else{
                        showSnackbar(getResources().getString(R.string.please_retry));
                    }
                }
            });
        }else {
            dbCheck.start();
        }

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    private void progressStrat(){
    }

    private void progressStop(){
    }


    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_PROGRESS_START:
                    progressStrat();
                    break;
                case MSG_MESSAGE_DATABASE:
                    progressState.setText(R.string.newest_database);
                    redirectMainTabActivity();
                    break;
                case MSG_MESSAGE_NEW_DATABASE:
                    if(isOnline()) {
                        progressState.setText(getResources().getText(R.string.create_database) + "\n" + getResources().getText(R.string.please_wait));
                        madeNewDB.start();
                    }else{
                        progressState.setText(R.string.please_wait);
                    }
                    break;
                case MSG_MESSAGE_UPDATE_DATABASE:
                    Toast.makeText(getApplicationContext(), R.string.update_database, Toast.LENGTH_SHORT).show();
                    break;
                case MSG_MESSAGE_DOWNLOAD_DATABASE:
                    progressState.setText(
                            getResources().getText(R.string.create_database) + "\n"
                            + getResources().getText(R.string.please_wait) + "\n"
                            + "(" + getResources().getText(R.string.downloading_database) + ")"
                    );
                    break;
                case MSG_MESSAGE_CREATE_DATABASE:
                    progressState.setText(
                            getResources().getText(R.string.create_database) + "\n"
                            + getResources().getText(R.string.please_wait) + "\n"
                            + "(" + getResources().getText(R.string.creating_database) + ")"
                    );
                    break;
                case MSG_MESSAGE_SUCCESS:
                    progressState.setText(getResources().getText(R.string.success) + "!");
                    showSnackbar(getResources().getText(R.string.success) + "!");
                    redirectMainTabActivity();
                    break;
                case MSG_MESSAGE_FAIL:
                    progressState.setText(getResources().getText(R.string.failed) + "!");
                    showSnackbar(getResources().getText(R.string.failed) + "!");
                    break;
                default:
                    break;
            }
        }
    }


    class DBVersionCheck extends Thread{

        private String version = null;

        public void run(){

            handler.sendMessage(handler.obtainMessage(MSG_MESSAGE_PROGRESS_START));

            String url = Information.MAIN_SERVER_ADDRESS + "versionDB.php";

            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                if ( conn != null ) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ( true ) {
                            String line = br.readLine();
                            if ( line == null )
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            String str = jsonHtml.toString();

            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);

                version = (String)jObject.get("db_version");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String dbversion = setting.getString("DBVersion", null);

            Message msg;
            if(dbversion == null){
                msg = handler.obtainMessage(MSG_MESSAGE_NEW_DATABASE);
                handler.sendMessage(msg);
            }else if(version == null) {
                msg = handler.obtainMessage(MSG_MESSAGE_DATABASE);
                handler.sendMessage(msg);
            }else if(!version.equals(dbversion)){
                msg = handler.obtainMessage(MSG_MESSAGE_UPDATE_DATABASE);
                handler.sendMessage(msg);
            }else{
                msg = handler.obtainMessage(MSG_MESSAGE_DATABASE);
                handler.sendMessage(msg);
            }


        }

        public String getVersion(){
            return version;
        }

    }

    public class MadeNewDB extends Thread{

        public void run() {



            DownloadKoDB ko = new DownloadKoDB();
            ko.start();
            try {
                ko.join();
            } catch (Exception e) {
                e.printStackTrace();
            }

            DownloadEnDB en = new DownloadEnDB();
            en.start();
            try{
                en.join();
            }catch (Exception e){
                e.printStackTrace();
            }

            DownloadConvertDB convertDB = new DownloadConvertDB();
            convertDB.start();
            try{
                convertDB.join();
            }catch (Exception e){
                e.printStackTrace();
            }

            Message msg;
            msg = handler.obtainMessage(MSG_MESSAGE_CREATE_DATABASE);
            handler.sendMessage(msg);

            ArrayList<HashMap<String, String>> result_ko = ko.getResult();
            ArrayList<HashMap<String, String>> result_en = en.getResult();
            ArrayList<HashMap<String, String>> result_convert = convertDB.getResult();

            // create new database
            db = dbHelper.getWritableDatabase();
            dbHelper.onCreate(db);

            boolean state = true; // 성공적으로 저장됬는지 확인하기 위해
            for (HashMap<String, String> r : result_ko) {

                String SQL = "INSERT INTO " + dbHelper.getTABLE_NAME() + " VALUES(null,"
                        + "'" + r.get("title") + "',"
                        + "'" + r.get("sContents") + "',"
                        + "'" + r.get("contents") + "',"
                        + "'" + r.get("address") + "',"
                        + "'" + r.get("district") + "',"
                        + "'" + r.get("telephone") + "',"
                        + "'" + r.get("web") + "',"
                        + "'" + r.get("detail") + "',"
                        + "'" + r.get("url") + "',"
                        + "'" + r.get("mainImage") + "',"
                        + "'" + r.get("subImage") + "',"
                        + "'" + r.get("lat") + "',"
                        + "'" + r.get("lng") + "',"
                        + "'" + r.get("categorize") + "',"
                        + "null"
                        + ");";
                state = state & dbHelper.insert(SQL);
            }
            for (HashMap<String, String> r : result_en) {

                String SQL = "INSERT INTO " + dbHelper.getTABLE_NAME_EN() + " VALUES(null,"
                        + "'" + r.get("title") + "',"
                        + "'" + r.get("sContents") + "',"
                        + "'" + r.get("contents") + "',"
                        + "'" + r.get("address") + "',"
                        + "'" + r.get("district") + "',"
                        + "'" + r.get("telephone") + "',"
                        + "'" + r.get("web") + "',"
                        + "'" + r.get("detail") + "',"
                        + "'" + r.get("url") + "',"
                        + "'" + r.get("mainImage") + "',"
                        + "'" + r.get("subImage") + "',"
                        + "'" + r.get("lat") + "',"
                        + "'" + r.get("lng") + "',"
                        + "'" + r.get("categorize") + "',"
                        + "null"
                        + ");";
                state = state & dbHelper.insert(SQL);
            }
            for (HashMap<String, String> r : result_convert) {

                String SQL = "INSERT INTO " + dbHelper.getCONVERT_TABLE_NAME() + " VALUES("
                        + "'" + r.get("title_ko") + "',"
                        + "'" + r.get("title_en") + "'"
                        + ");";
                state = state & dbHelper.insert(SQL);
            }



            if (state) {
                editor.putString("DBVersion", ko.getVersion());
                editor.commit();
                msg = handler.obtainMessage(MSG_MESSAGE_SUCCESS);
                handler.sendMessage(msg);
            } else {
                msg = handler.obtainMessage(MSG_MESSAGE_FAIL);
                handler.sendMessage(msg);
            }

        }

    }

    class DownloadKoDB extends Thread{

        private String dbVersion = null;

        private ArrayList<HashMap<String, String>> result = new ArrayList<>();

        public void run(){


            Message msg = handler.obtainMessage(MSG_MESSAGE_DOWNLOAD_DATABASE);
            handler.sendMessage(msg);

            String url = Information.MAIN_SERVER_ADDRESS + "downloadDB.php";

            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                if ( conn != null ) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ( true ) {
                            String line = br.readLine();
                            if ( line == null )
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            String str = jsonHtml.toString();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                dbVersion = (String)jObject.get("db_version");
                int count = Integer.parseInt(countTemp);



//                HashMap<String, String> hashTemp = new HashMap<>();
                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, String> hashTemp = new HashMap<>();
                    hashTemp.put("title", (String)temp.get("title"));
                    hashTemp.put("sContents", (String)temp.get("sContents"));
                    hashTemp.put("contents", (String)temp.get("contents"));
                    hashTemp.put("address", (String)temp.get("address"));
                    hashTemp.put("district", (String)temp.get("district"));
                    hashTemp.put("telephone", (String)temp.get("telephone"));
                    hashTemp.put("web", (String)temp.get("web"));
                    hashTemp.put("detail", (String)temp.get("detail"));
                    hashTemp.put("url", (String)temp.get("url"));
                    hashTemp.put("mainImage", (String)temp.get("mainImage"));
                    hashTemp.put("subImage", (String)temp.get("subImage"));
                    hashTemp.put("lat", (String)temp.get("lat"));
                    hashTemp.put("lng", (String)temp.get("lng"));
                    hashTemp.put("categorize", (String)temp.get("categorize"));

                    result.add(hashTemp);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<HashMap<String, String>> getResult(){
            return result;
        }
        public String getVersion(){
            return dbVersion;
        }

    }

    class DownloadEnDB extends Thread{

        private String dbVersion = null;

        private ArrayList<HashMap<String, String>> result = new ArrayList<>();

        public void run(){


            Message msg = handler.obtainMessage(MSG_MESSAGE_DOWNLOAD_DATABASE);
            handler.sendMessage(msg);

            String url = Information.MAIN_SERVER_ADDRESS + "downloadEnDB.php";

            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                if ( conn != null ) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ( true ) {
                            String line = br.readLine();
                            if ( line == null )
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            String str = jsonHtml.toString();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                dbVersion = (String)jObject.get("db_version");
                int count = Integer.parseInt(countTemp);


                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, String> hashTemp = new HashMap<>();
                    hashTemp.put("title", (String)temp.get("title"));
                    hashTemp.put("sContents", (String)temp.get("sContents"));
                    hashTemp.put("contents", (String)temp.get("contents"));
                    hashTemp.put("address", (String)temp.get("address"));
                    hashTemp.put("district", (String)temp.get("district"));
                    hashTemp.put("telephone", (String)temp.get("telephone"));
                    hashTemp.put("web", (String)temp.get("web"));
                    hashTemp.put("detail", (String)temp.get("detail"));
                    hashTemp.put("url", (String)temp.get("url"));
                    hashTemp.put("mainImage", (String)temp.get("mainImage"));
                    hashTemp.put("subImage", (String)temp.get("subImage"));
                    hashTemp.put("lat", (String)temp.get("lat"));
                    hashTemp.put("lng", (String)temp.get("lng"));
                    hashTemp.put("categorize", (String)temp.get("categorize"));

                    result.add(hashTemp);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<HashMap<String, String>> getResult(){
            return result;
        }
        public String getVersion(){
            return dbVersion;
        }

    }

    class DownloadConvertDB extends Thread{

        private String dbVersion = null;

        private ArrayList<HashMap<String, String>> result = new ArrayList<>();

        public void run(){


            Message msg = handler.obtainMessage(MSG_MESSAGE_DOWNLOAD_DATABASE);
            handler.sendMessage(msg);

            String url = Information.MAIN_SERVER_ADDRESS + "downloadConvertDB.php";

            StringBuilder jsonHtml = new StringBuilder();
            try {
                URL phpUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection)phpUrl.openConnection();

                if ( conn != null ) {
                    conn.setConnectTimeout(10000);
                    conn.setUseCaches(false);

                    if ( conn.getResponseCode() == HttpURLConnection.HTTP_OK ) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        while ( true ) {
                            String line = br.readLine();
                            if ( line == null )
                                break;
                            jsonHtml.append(line + "\n");
                        }
                        br.close();
                    }
                    conn.disconnect();
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            String str = jsonHtml.toString();
            try {
                // PHP에서 받아온 JSON 데이터를 JSON오브젝트로 변환
                JSONObject jObject = new JSONObject(str);
                // results라는 key는 JSON배열로 되어있다.
                JSONArray results = jObject.getJSONArray("result");
                String countTemp = (String)jObject.get("num_result");
                dbVersion = (String)jObject.get("db_version");
                int count = Integer.parseInt(countTemp);


                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, String> hashTemp = new HashMap<>();
                    hashTemp.put("title_ko", (String)temp.get("title_ko"));
                    hashTemp.put("title_en", (String)temp.get("title_en"));

                    result.add(hashTemp);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public ArrayList<HashMap<String, String>> getResult(){
            return result;
        }
        public String getVersion(){
            return dbVersion;
        }

    }

    private void redirectMainTabActivity() {
        progressStop();
        dbCheck.interrupt();
        madeNewDB.interrupt();
        startActivity(new Intent(this, MainTabActivity.class));
        finish();
    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this, permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(this, "권한 있음", Toast.LENGTH_LONG).show();
        } else {
//            Toast.makeText(this, "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                //Toast.makeText(this, "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    //Toast.makeText(this, permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void versionCodeCheck(){
        int version;
        try{
            PackageInfo i = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            version = i.versionCode;
            System.out.println("Version Code : " + version);
        }catch(PackageManager.NameNotFoundException e){
            System.out.println("Version Code : " + "Failed!!");
        }
    }

    private boolean isOnline(){
        ConnectivityManager manager =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mobile.isConnected() || wifi.isConnected()){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        avi.hide();
    }

    public void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_status_bar_color));
        }
    }
}
