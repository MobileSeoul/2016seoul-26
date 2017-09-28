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
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.github.ppamorim.dragger.DraggerPosition;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;

import java.util.ArrayList;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.MainTabActivity;
import tk.twpooi.seoultrip.R;

/**
 * Created by tw on 2016-08-16.
 */
public class MyCourseListFragment extends Fragment{

    private static final String fileName = "mycourse.st";

    private FileManager fileManager;

    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private FloatingActionButton addBtn;
    private Context context;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private RecyclerViewDragDropManager dragMgr;
    private MyCourseListCustomAdapter adapter;


    private int listSize = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_mycourse_list, container, false);
        context = container.getContext();
        mToolbar = ((MainTabActivity)getActivity()).getToolbar();

        fileManager = new FileManager(context);

        initUI();

        makeList();

        return view;

    }



    public void initUI(){

        floationMenu();

        setpDialoginit();

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);

        dragMgr = new RecyclerViewDragDropManager();

        dragMgr.setInitiateOnMove(false);
        dragMgr.setInitiateOnLongPress(true);



    }

    public void setpDialoginit(){
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(getResources().getString(R.string.please_wait));
        pDialog.setCancelable(false);
    }

    public void floationMenu(){

        addBtn = (FloatingActionButton) view.findViewById(R.id.add_btn);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddDraggerActivity(DraggerPosition.TOP);
            }
        });

   }

    public void deleteCourse(int pos){
        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        list.remove(pos);
        fileManager.writeMyCourse(list);
        listSize = list.size();
        showSnackbar(getResources().getString(R.string.remove) + " " + getResources().getString(R.string.complete));
    }


    public void makeList(){

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        listSize = list.size();

        if(list.size() == 0){
            TextView msgNoti = (TextView)view.findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.VISIBLE);
        }else{
            TextView msgNoti = (TextView)view.findViewById(R.id.message_notice);
            msgNoti.setVisibility(View.GONE);
        }

        adapter = new MyCourseListCustomAdapter(context, list, rv, mToolbar, this);

        try {
            rv.setAdapter(dragMgr.createWrappedAdapter(adapter));
        }catch (Exception e){
            e.printStackTrace();
        }

        dragMgr.attachRecyclerView(rv);

    }


    public void moveItem(int fromPosition, int toPosition){
        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        HashMap<String, Object> temp = list.get(fromPosition);
        list.set(fromPosition, list.get(toPosition));
        list.set(toPosition, temp);
        fileManager.writeMyCourse(list);

    }


    private void startAddDraggerActivity(DraggerPosition dragPosition) {
        Intent intent = new Intent(context, AddMyCourseActivity.class);
        intent.putExtra(AddMyCourseActivity.DRAG_POSITION, dragPosition);
        startActivityNoAnimation(intent);
    }
    public void startCourseListDraggerActivity(DraggerPosition dragPosition, int pos) {
        Intent intent = new Intent(context, ShowCourseListActivity.class);
        intent.putExtra(AddMyCourseActivity.DRAG_POSITION, dragPosition);
        intent.putExtra("position", pos);
        startActivityNoAnimation(intent);
    }
    private void startActivityNoAnimation(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void hideViews() {
        addBtn.setVisibility(View.INVISIBLE);
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }

    public void showViews() {
        addBtn.setVisibility(View.VISIBLE);
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }

    @Override
    public void onResume() {

        ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();
        if(list.size() != listSize){
            if (adapter != null) {
                while(listSize < list.size()) {
                    listSize++;
                    adapter.addList(list.get(listSize - 1));
                    adapter.notifyItemInserted(listSize);
                }
            }
        }

        super.onResume();
    }


}
