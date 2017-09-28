package tk.twpooi.seoultrip.fragment.search;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ppamorim.dragger.DraggerPosition;
import com.github.ppamorim.dragger.DraggerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.Information;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.RecommentCourseCustomAdapter;

/**
 * Created by tw on 2016-08-17.
 */
public class SearchCourseGameListActivity extends AppCompatActivity {

    public static final String DRAG_POSITION = "drag_position";

    private MyHandler handler = new MyHandler();
    private static final int MSG_MESSAGE_MAKE_LIST = 1001;
    private static final int MSG_MESSAGE_PARSE_START = 1002;
    private static final int MSG_MESSAGE_PARSE_FINISH = 1003;
    private static final int MSG_MESSAGE_ENDLESS = 1004;

    // Database
    private DBHelper dbHelper;

    private DraggerView draggerView;
    private Toolbar toolbar;
    private SearchView searchView;
    private TextView noticeMessage;
    private SweetAlertDialog pDialog;

    public int page = 0;
    private ArrayList<HashMap<String, Object>> list;
    private ParsePHP parsePHP;
    private RecommentCourseCustomAdapter mAdapter;
    private boolean isInitParse = true;
    private int totalCount = 0;

    private String finalQuery = "";


    // Recycle View
    private ObservableRecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

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
        setContentView(R.layout.activity_drag_search_attraction_list);

        dbHelper = new DBHelper(getApplicationContext(), "SeoulTrip.db", null, 1);

        init();

    }

    private void changeSearchViewTextColor(View view) {
        if (view != null) {
            if (view instanceof TextView) {
                ((TextView) view).setTextColor(Color.WHITE);
                return;
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i));
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_search, menu);
        searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #efefef>" + getResources().getString(R.string.please_enter_search_contents) + "</font>"));
        changeSearchViewTextColor(searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                finalQuery = query;

                page = 0;
                isInitParse = true;
                startParsing();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        SearchManager searchManager = (SearchManager)this.getSystemService(Context.SEARCH_SERVICE);
        if(searchManager != null){
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);

        return true;
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
        clearSingleton(); // 프레임 중복 해결
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (ObservableRecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);
        rv.setScrollViewCallbacks(onObservableScrollViewCallbacks);


        noticeMessage = (TextView)findViewById(R.id.message_notice);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbar_title);

    }

    public void setNoticeMessage(boolean check){
        if(check){
            noticeMessage.setVisibility(View.VISIBLE);
            noticeMessage.setText(getResources().getString(R.string.no_search_result));
            RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.background_color));
        }else{
            noticeMessage.setVisibility(View.GONE);
            RelativeLayout root = (RelativeLayout)findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.list_background_color));
        }
    }

    public void startParsing(){

        if(parsePHP != null && (parsePHP.isDaemon() || parsePHP.isAlive())){
            parsePHP.interrupt();
        }

        parsePHP = new ParsePHP();
        parsePHP.start();

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }

    public void makeList(){

        toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));

        list = parsePHP.getResult(); // 무한스크롤

        if(list.size() == 0){
            setNoticeMessage(true);
            showSnackbar(getResources().getString(R.string.no_search_result));
        }else{
            setNoticeMessage(false);
            showSnackbar(list.size() + getResources().getString(R.string.search_result_count));
        }

        final RecommentCourseCustomAdapter adapter = new RecommentCourseCustomAdapter(getApplicationContext(), list, rv, toolbar, null);

        rv.setAdapter(adapter);

        adapter.setOnLoadMoreListener(new RecommentCourseCustomAdapter.OnLoadMoreListener() { // 무한스크롤
            @Override
            public void onLoadMore() {
                list.add(list.get(0));
                adapter.notifyItemInserted(list.size()-1);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        list.remove(list.size()-1);
                        adapter.notifyItemRemoved(list.size());

                        if(totalCount > page*10+10) {
                            page++;
                            startParsing();
                        }
                    }
                });
            }
        });

        mAdapter = adapter; // 무한스크롤

        adapter.notifyDataSetChanged();

    }

    public void setAdapterEndlessScroll(){

        ArrayList<HashMap<String, Object>> tempList = parsePHP.getResult();

        for(int i=0; i<tempList.size(); i++){
            list.add(tempList.get(i));
            mAdapter.notifyItemInserted(list.size());
        }
        mAdapter.setLoaded();

    }

    public String makeParseUrl(int page){

        String basicUrl = Information.MAIN_SERVER_ADDRESS + "searchShareCourseDB.php?";
        String returnUrl = "";

        String page2 = Integer.toString(page);
        returnUrl = basicUrl + "page=" + page2 + "&search=" + finalQuery;

        return returnUrl;

    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_MESSAGE_MAKE_LIST:
                    makeList();
                    pDialog.dismiss();
                    break;
                case MSG_MESSAGE_PARSE_START:
                    pDialog.show();
                    break;
                case MSG_MESSAGE_PARSE_FINISH:
                    pDialog.dismiss();
                    break;
                case MSG_MESSAGE_ENDLESS:
                    setAdapterEndlessScroll();
                    break;
                default:
                    break;
            }
        }
    }

    class ParsePHP extends Thread{

        private ArrayList<HashMap<String, Object>> result = new ArrayList<>();

        public void run(){


            Message msg;
            if(isInitParse) {
                msg = handler.obtainMessage(MSG_MESSAGE_PARSE_START);
                handler.sendMessage(msg);
            }

            String url = makeParseUrl(page);

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
                int count = Integer.parseInt(countTemp);

                if(isInitParse){
                    totalCount = Integer.parseInt((String)jObject.get("total_count"));
                }

                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, Object> hashTemp = new HashMap<>();
                    hashTemp.put("id", (String)temp.get("id"));
                    hashTemp.put("title", (String)temp.get("title"));
                    hashTemp.put("language", (String)temp.get("language"));
                    hashTemp.put("introduce", (String)temp.get("introduce"));
                    hashTemp.put("time", Integer.parseInt((String)temp.get("limit")));
                    hashTemp.put("like", (String)temp.get("like"));
                    hashTemp.put("hits", (String)temp.get("hits"));
                    hashTemp.put("put", (String)temp.get("put"));
                    hashTemp.put("success", (String)temp.get("success"));
                    hashTemp.put("clicked", "0"); // 코스 설명용

                    String[] attractions = ((String)temp.get("attractions")).split(",");
                    ArrayList<String> attTemp = new ArrayList<>();
                    for(String a : attractions){
                        attTemp.add(a);
                    }
                    hashTemp.put("attraction", attTemp);

                    result.add(hashTemp);

                }

                if(!isInitParse) {
                    msg = handler.obtainMessage(MSG_MESSAGE_ENDLESS);
                    handler.sendMessage(msg);
                }else{
                    isInitParse = false;
                    msg = handler.obtainMessage(MSG_MESSAGE_MAKE_LIST);
                    handler.sendMessage(msg);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                msg = handler.obtainMessage(MSG_MESSAGE_PARSE_FINISH);
                handler.sendMessage(msg);
            }



        }

        public ArrayList<HashMap<String, Object>> getResult(){
            return result;
        }

    }

    private ObservableScrollViewCallbacks onObservableScrollViewCallbacks =
            new ObservableScrollViewCallbacks() {
                @Override public void onScrollChanged(int scrollY, boolean firstScroll,
                                                      boolean dragging) {
                    draggerView.setSlideEnabled(scrollY == 0);
                }
                @Override public void onDownMotionEvent() { }
                @Override public void onUpOrCancelMotionEvent(ScrollState scrollState) { }
            };


    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

}
