package tk.twpooi.seoultrip.fragment.addcoursegame;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.getbase.floatingactionbutton.FloatingActionButton;

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
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class AddToMyCourseFragment extends Fragment{

    // Database
    DBHelper dbHelper;
    private FileManager fileManager;

    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private FloatingActionButton fab;
    private Context context;
    protected PtrFrameLayout mPtrFrameLayout;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

    ArrayList<HashMap<String,String>> courseList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_add_course_game, container, false);
        context = container.getContext();
        mToolbar = ((AddCourseGameActivity)getActivity()).getToolbar();

        fileManager = new FileManager(context);

        initUI();

        // Database
        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);

        makeList();

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
                makeList();
            }
        });


    }

    public void floationMenu(){

        fab = (FloatingActionButton)view.findViewById(R.id.fab_search);

        fab.setVisibility(View.GONE);

   }

    public void makeList(){

        ArrayList<HashMap<String, Object>> tempList = fileManager.readMyCourse();

        ArrayList<Integer> index = new ArrayList<>();
        for(int i=0; i<tempList.size(); i++){
            int size = ((ArrayList<String>)tempList.get(i).get("attraction")).size();
            if(size >=3 && size <= 10){
                index.add(i);
            }
        }

        ArrayList<HashMap<String, Object>> realList = new ArrayList<>();
        for(int i : index){
            realList.add(tempList.get(i));
        }

        final AddCourseGameCustomAdapter adapter = new AddCourseGameCustomAdapter(context, realList, rv, mToolbar, ((AddCourseGameActivity)getActivity()));

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        mPtrFrameLayout.refreshComplete();
    }


    public void hideViews() {
//        fab.setVisibility(View.INVISIBLE);
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    public void showViews() {
//        menu.setVisibility(View.VISIBLE);
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

}
