package tk.twpooi.seoultrip.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.ppamorim.dragger.DraggerPosition;


import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.MainTabActivity;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;
import tk.twpooi.seoultrip.fragment.search.SearchAttractionListActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class AttractionListFragment extends Fragment{

    // Database
    DBHelper dbHelper;

    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private FloatingActionsMenu menu;
    private Context context;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private ArrayList<HashMap<String,String>> attractionList;
    private ArrayList<String> attractionTitle;
    private AttractionListCustomAdapter adapter;

    private String[] stringItems = new String[27];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_attraction_list, container, false);
        context = container.getContext();
        mToolbar = ((MainTabActivity)getActivity()).getToolbar();

        // Database
        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);
        attractionList = dbHelper.getResultAll(StartActivity.DATABASE_LANGUAGE);

        attractionTitle = new ArrayList<>();
        for(HashMap<String, String> hashMap : attractionList){
            attractionTitle.add(hashMap.get("title"));
        }

        initUI();

        makeList(attractionList);

        return view;

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
                Intent intent = new Intent(context, SearchAttractionListActivity.class);
                intent.putExtra(AddCourseListActivity.DRAG_POSITION, DraggerPosition.TOP);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        searchBtn.setTitle(getResources().getString(R.string.search));

        FloatingActionButton showMapBtn = (FloatingActionButton)view.findViewById(R.id.showMapBtn);
        showMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.toggle();
                Intent intent = new Intent(context, ShowMapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("attraction", attractionTitle);
                intent.putExtra("language", StartActivity.DATABASE_LANGUAGE);
                startActivity(intent);
            }
        });
        showMapBtn.setTitle(getResources().getString(R.string.map));

        FloatingActionButton lineUp = (FloatingActionButton)view.findViewById(R.id.lineup);
        lineUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderByDialog();
                menu.toggle();
            }
        });
        lineUp.setTitle(getResources().getString(R.string.order));

        FloatingActionButton districtBtn = (FloatingActionButton)view.findViewById(R.id.districtBtn);
        districtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DistrictDialog();
                menu.toggle();
            }
        });
        districtBtn.setTitle(getResources().getString(R.string.show_district));

   }

    public void makeList(ArrayList<HashMap<String, String>> list){

        for(HashMap<String, String> temp : list){
            System.out.println(temp.get("categorize"));
        }

        adapter = new AttractionListCustomAdapter(context, list, rv, mToolbar, this);

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }

    public void hideViews() {
        menu.setVisibility(View.INVISIBLE);
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    public void showViews() {
        menu.setVisibility(View.VISIBLE);
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    private void DistrictDialog() {

        if(StartActivity.DATABASE_LANGUAGE.equals("ko")){
            stringItems[0] = "전체";
            stringItems[1] = "강남구";
            stringItems[2] = "강동구";
            stringItems[3] = "강북구";
            stringItems[4] = "강서구";
            stringItems[5] = "관악구";
            stringItems[6] = "광진구";
            stringItems[7] = "구로구";
            stringItems[8] = "금천구";
            stringItems[9] = "노원구";
            stringItems[10] = "도봉구";
            stringItems[11] = "동대문구";
            stringItems[12] = "동작구";
            stringItems[13] = "마포구";
            stringItems[14] = "서대문구";
            stringItems[15] = "서초구";
            stringItems[16] = "성동구";
            stringItems[17] = "성북구";
            stringItems[18] = "송파구";
            stringItems[19] = "양천구";
            stringItems[20] = "영등포구";
            stringItems[21] = "용산구";
            stringItems[22] = "은평구";
            stringItems[23] = "종로구";
            stringItems[24] = "중구";
            stringItems[25] = "중랑구";
            stringItems[26] = "미분류";
        }else{
            stringItems[0] = "All";
            stringItems[1] = "Dobong-gu";
            stringItems[2] = "Dongdaemun-gu";
            stringItems[3] = "Dongjak-gu";
            stringItems[4] = "Eunpyeong-gu";
            stringItems[5] = "Gangbuk-gu";
            stringItems[6] = "Gangdong-gu";
            stringItems[7] = "Gangnam-gu";
            stringItems[8] = "Gangseo-gu";
            stringItems[9] = "Geumcheon-gu";
            stringItems[10] = "Guro-gu";
            stringItems[11] = "Gwanak-gu";
            stringItems[12] = "Gwangjin-gu";
            stringItems[13] = "Jongro-gu";
            stringItems[14] = "Jung-gu";
            stringItems[15] = "Jungnang-gu";
            stringItems[16] = "Mapo-gu";
            stringItems[17] = "Nowon-gu";
            stringItems[18] = "Seocho-gu";
            stringItems[19] = "Seodaemun-gu";
            stringItems[20] = "Seongbuk-gu";
            stringItems[21] = "Seongdong-gu";
            stringItems[22] = "Songpa-gu";
            stringItems[23] = "Yongsan-gu";
            stringItems[24] = "Yangcheon-gu";
            stringItems[25] = "Yeongdeungpo-gu";
            stringItems[26] = "Unclassified";
        }
        final ActionSheetDialog dialog = new ActionSheetDialog(context, stringItems, null);
        dialog.title(getResources().getString(R.string.select_district))//
                .titleTextSize_SP(14.5f)//
                .cancelText(getResources().getString(R.string.cancel))
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                String district = stringItems[position];
                attractionList = dbHelper.getResultDistrict(district, StartActivity.DATABASE_LANGUAGE);
                makeList(attractionList);
                ((MainTabActivity)getActivity()).showToolbar();
                rv.scrollToPosition(0);
                dialog.dismiss();
            }
        });
    }

    private void OrderByDialog() {
        final String[] stringItems = {getResources().getString(R.string.ascending_order), getResources().getString(R.string.descending_order)};
        final ActionSheetDialog dialog = new ActionSheetDialog(context, stringItems, null);
        dialog.title(getResources().getString(R.string.select_order))//
                .titleTextSize_SP(14.5f)//
                .cancelText(getResources().getString(R.string.cancel))
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                String orderBy = stringItems[position];
                makeList(dbHelper.getResultOrderBy(orderBy, StartActivity.DATABASE_LANGUAGE));
                ((MainTabActivity)getActivity()).showToolbar();
                rv.scrollToPosition(0);
                dialog.dismiss();
            }
        });
    }

    public void startCourseListDraggerActivity(DraggerPosition dragPosition, String attraction) {
        Intent intent = new Intent(context, AddCourseListActivity.class);
        intent.putExtra(AddMyCourseActivity.DRAG_POSITION, dragPosition);
        intent.putExtra("attraction", attraction);
        startActivityNoAnimation(intent);
    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshList();
    }

    public void refreshList(){
        adapter.refreshFavorite();
    }
}
