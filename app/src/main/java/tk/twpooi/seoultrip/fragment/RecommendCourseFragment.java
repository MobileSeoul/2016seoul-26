package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ppamorim.dragger.DraggerPosition;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;
import in.srain.cube.views.ptr.header.MaterialHeader;
import in.srain.cube.views.ptr.util.PtrLocalDisplay;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.Information;
import tk.twpooi.seoultrip.MainTabActivity;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.fragment.search.SearchCourseGameListActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class RecommendCourseFragment extends Fragment{

    private MyHandler handler = new MyHandler();
    private static final int MSG_MESSAGE_MAKE_LIST = 1001;
    private static final int MSG_MESSAGE_PARSE_START = 1002;
    private static final int MSG_MESSAGE_PARSE_FINISH = 1003;
    private static final int MSG_MESSAGE_ENDLESS = 1004;

    // Database
    DBHelper dbHelper;
    private FileManager fileManager;

    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private FloatingActionsMenu menu;
    private Context context;
    protected PtrFrameLayout mPtrFrameLayout;
    private TextView notiMsg;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

    //
    public int page = 0;
    public String column = "hits";
    public int order = 1;
    public String idList = null;

    private ArrayList<HashMap<String, Object>> list;
    private ParsePHP parsePHP;
    private RecommentCourseCustomAdapter mAdapter;
    private boolean isInitParse = true;
    private int totalCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_recommend_course, container, false);
        context = container.getContext();
        mToolbar = ((MainTabActivity)getActivity()).getToolbar();

        fileManager = new FileManager(context);

        initUI();

        // Database
        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);

        startParsing();

        return view;

    }

    public String makeParseUrl(int page, String column, int order, String idList){

        String basicUrl = Information.MAIN_SERVER_ADDRESS + "shareCourseDB.php?";
        String returnUrl = "";

        String page2 = Integer.toString(page);
        if(column == null){
            returnUrl = basicUrl + "page=" + page2;
        }else{
            returnUrl = basicUrl + "page=" + page2 + "&column=" + column + "&order=" + order;
        }

        if(idList != null){
            returnUrl = returnUrl + "&id=" + idList;
        }

        return returnUrl;

    }

    public void startParsing(){

        if(parsePHP != null && (parsePHP.isDaemon() || parsePHP.isAlive())){
            parsePHP.interrupt();
        }

        parsePHP = new ParsePHP();
        parsePHP.start();

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

        notiMsg = (TextView)view.findViewById(R.id.message_notice);

        setMaterial();

    }

    public void setMaterial(){

        mPtrFrameLayout = (PtrFrameLayout) view.findViewById(R.id.material_style_ptr_frame);

        // header
        final MaterialHeader header = new MaterialHeader(getContext());
        int[] colors = getResources().getIntArray(R.array.google_colors);
        header.setColorSchemeColors(colors);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        header.setPadding(0, PtrLocalDisplay.dp2px(15), 0, 0);
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
                page = 0;
                isInitParse = true;
                startParsing();
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

        FloatingActionButton searchBtn = (FloatingActionButton)view.findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.toggle();
                Intent intent = new Intent(context, SearchCourseGameListActivity.class);
                intent.putExtra(AddCourseListActivity.DRAG_POSITION, DraggerPosition.TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        searchBtn.setTitle(getResources().getString(R.string.search));

        FloatingActionButton lineUp = (FloatingActionButton)view.findViewById(R.id.lineup);
        lineUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderByDialog();
                menu.toggle();
            }
        });
        lineUp.setTitle(getResources().getString(R.string.order));

        final FloatingActionButton myShareCourseBtn = (FloatingActionButton)view.findViewById(R.id.my_shared_course);
        myShareCourseBtn.setTitle(getResources().getString(R.string.my_shared_course));
        myShareCourseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idList == null){
                    ArrayList<String> li = fileManager.readSharedFile();
                    String id;
                    if(li.size() == 0){
                        id = "0";
                    }else {
                        id = li.toString();
                        id = id.replaceAll(" ", "").replaceAll(",", ";");
                        id = id.substring(1, id.length() - 1);
                    }

                    idList = id;
                    page = 0;
                    isInitParse = true;
                    startParsing();
                    myShareCourseBtn.setTitle(getResources().getString(R.string.show_all_shared_course));
                    menu.toggle();
                }else{
                    idList = null;
                    page = 0;
                    isInitParse = true;
                    startParsing();
                    myShareCourseBtn.setTitle(getResources().getString(R.string.my_shared_course));
                    menu.toggle();
                }
            }
        });

   }

    public void makeList(){

        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));

        list = parsePHP.getResult(); // 무한스크롤

        if(list.size() == 0){
            notiMsg.setVisibility(View.VISIBLE);
            FrameLayout root = (FrameLayout)view.findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.background_color));
        }else{
            notiMsg.setVisibility(View.GONE);
            FrameLayout root = (FrameLayout)view.findViewById(R.id.root);
            root.setBackgroundColor(ContextCompat.getColor(context, R.color.list_background_color));
        }

        final RecommentCourseCustomAdapter adapter = new RecommentCourseCustomAdapter(context, list, rv, mToolbar, this);

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

        mPtrFrameLayout.refreshComplete();
    }

    public void setAdapterEndlessScroll(){

        ArrayList<HashMap<String, Object>> tempList = parsePHP.getResult();

        for(int i=0; i<tempList.size(); i++){
            list.add(tempList.get(i));
            mAdapter.notifyItemInserted(list.size());
        }
        mAdapter.setLoaded();

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

            String url = makeParseUrl(page, column, order, idList);

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

//                HashMap<String, String> hashTemp = new HashMap<>();
                for ( int i = 0; i < count; ++i ) {
                    JSONObject temp = results.getJSONObject(i);

                    HashMap<String, Object> hashTemp = new HashMap<>();
                    hashTemp.put("id", (String)temp.get("id"));
                    hashTemp.put("language", (String)temp.get("language"));
                    hashTemp.put("title", (String)temp.get("title"));
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

                // finish
                //list = result;


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


    private void OrderByDialog() {
        final String[] stringItems = {
                getResources().getString(R.string.ascending_order) + "(" + getResources().getString(R.string.hit) + ")",
                getResources().getString(R.string.descending_order) + "(" + getResources().getString(R.string.hit) + ")",
                getResources().getString(R.string.ascending_order) + "(" + getResources().getString(R.string.like) + ")",
                getResources().getString(R.string.descending_order) + "(" + getResources().getString(R.string.like) + ")",
                getResources().getString(R.string.ascending_order) + "(" + getResources().getString(R.string.put) + ")",
                getResources().getString(R.string.descending_order) + "(" + getResources().getString(R.string.put) + ")",
                getResources().getString(R.string.ascending_order) + "(" + getResources().getString(R.string.success) + ")",
                getResources().getString(R.string.descending_order) + "(" + getResources().getString(R.string.success) + ")",
                getResources().getString(R.string.ascending_order) + "(" + getResources().getString(R.string.course_title) + ")",
                getResources().getString(R.string.descending_order) + "(" + getResources().getString(R.string.course_title) + ")"
        };
        final ActionSheetDialog dialog = new ActionSheetDialog(context, stringItems, null);
        dialog.title(getResources().getString(R.string.select_order))//
                .titleTextSize_SP(14.5f)//
                .cancelText(getResources().getString(R.string.cancel))
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:{
                        column = "hits";
                        order = 0;
                        break;
                    }
                    case 1:{
                        column = "hits";
                        order = 1;
                        break;
                    }
                    case 2:{
                        column = "likes";
                        order = 0;
                        break;
                    }
                    case 3:{
                        column = "likes";
                        order = 1;
                        break;
                    }
                    case 4:{
                        column = "put";
                        order = 0;
                        break;
                    }
                    case 5:{
                        column = "put";
                        order = 1;
                        break;
                    }
                    case 6:{
                        column = "success";
                        order = 0;
                        break;
                    }
                    case 7:{
                        column = "success";
                        order = 1;
                        break;
                    }
                    case 8:{
                        column = "title";
                        order = 0;
                        break;
                    }
                    case 9:{
                        column = "title";
                        order = 1;
                        break;
                    }
                    default:
                        break;

                }
                page = 0;
                isInitParse = true;
                startParsing();
                dialog.dismiss();
            }
        });
    }

    public void hideViews() {
        menu.setVisibility(View.INVISIBLE);
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    public void showViews() {
        menu.setVisibility(View.VISIBLE);
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

}
