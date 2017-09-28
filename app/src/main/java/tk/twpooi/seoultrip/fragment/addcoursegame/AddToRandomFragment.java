package tk.twpooi.seoultrip.fragment.addcoursegame;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;
import tk.twpooi.seoultrip.DBHelper;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class AddToRandomFragment extends Fragment{

    // Database
    private DBHelper dbHelper;


    // UI
    private View view;
    private Toolbar mToolbar;
    private SweetAlertDialog pDialog;
    private Context context;

    // EditUI
    private MaterialEditText courseCount;
    private DiscreteSeekBar seekBar;
    private TextView createBtn;
    private MaterialEditText title;
    private MaterialEditText time;
    private Button submitBtn;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;

    private boolean isCount = false;
    private boolean isTitle = false;
    private boolean isTime = false;

    private ArrayList<HashMap<String, Object>> randomCourse;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // UI
        view = inflater.inflate(R.layout.fragment_add_course_game_random, container, false);
        context = container.getContext();
        mToolbar = ((AddCourseGameActivity)getActivity()).getToolbar();
        ((AddCourseGameActivity)getActivity()).hideCompleteBtn();
        ((AddCourseGameActivity)getActivity()).setStatusColorDark();
        ((AddCourseGameActivity)getActivity()).setToolbarColorDark();

        initUI();

        // Database
        dbHelper = new DBHelper(context, "SeoulTrip.db", null, 1);

        return view;

    }


    public void initUI(){

        pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);

        mLinearLayoutManager = new LinearLayoutManager(context);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) view.findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(mLinearLayoutManager);

        seekBar = (DiscreteSeekBar)view.findViewById(R.id.seekbar);

        createBtn = (TextView)view.findViewById(R.id.create_btn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCount = true;
                makeList();
                checkSubmitBtn();
            }
        });
        title = (MaterialEditText)view.findViewById(R.id.title);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int size = title.getText().toString().length();
                if(size >=1 && size <= 20){
                    isTitle = true;
                }else{
                    isTitle = false;
                }
                checkSubmitBtn();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        time = (MaterialEditText)view.findViewById(R.id.time);
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    int time2 = Integer.parseInt(time.getText().toString());
                    if (time2 >= 1 && time2 <= 48) {
                        isTime = true;
                    } else {
                        isTime = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    isTime = false;
                }finally {
                    checkSubmitBtn();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submitBtn = (Button)view.findViewById(R.id.submit_btn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<HashMap<String, Object>> tempList = new ArrayList<HashMap<String, Object>>();
                HashMap<String, Object> temp = new HashMap<String, Object>();

                temp.put("clicked", "0");
                temp.put("like", 0);
                temp.put("title", title.getText().toString());
                temp.put("language", StartActivity.DATABASE_LANGUAGE);
                temp.put("time", Integer.parseInt(time.getText().toString()));
                temp.put("introduce", "");
                temp.put("id", getShareID());
                temp.put("put", 0);
                temp.put("hits", 0);
                temp.put("attraction", randomCourse.get(0).get("attraction"));
                temp.put("success", 0);

                tempList.add(temp);

                AddCourseGameActivity activity = ((AddCourseGameActivity)getActivity());

                activity.completeBtnClicked(tempList);
                activity.onBackPressed();
            }
        });
        submitBtn.setEnabled(false);

    }

    public String getShareID(){

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", java.util.Locale.getDefault());
        Date date = new Date();
        String strDate = dateFormat.format(date);
        String randomNumber = Integer.toString((int) (Math.random() * (999 - 100 + 1)) + 100);

        String id = strDate + randomNumber;

        return id;
    }

    public void checkCreateBtn(){
        try {
            int count = Integer.parseInt(courseCount.getText().toString());
            isCount = false;
            if (count >= 3 && count <= 10) {
                createBtn.setTextColor(getResources().getColor(R.color.black));
                createBtn.setEnabled(true);
            } else {
                createBtn.setTextColor(getResources().getColor(R.color.white));
                createBtn.setEnabled(false);
            }
        }catch (Exception e){
            isCount = false;
            createBtn.setTextColor(getResources().getColor(R.color.white));
            createBtn.setEnabled(false);
        }finally {
            checkSubmitBtn();
        }

    }

    public void checkSubmitBtn(){

        if(isCount && isTitle && isTime){
            submitBtn.setEnabled(true);
            submitBtn.setTextColor(getResources().getColor(R.color.white));
        }else{
            submitBtn.setEnabled(false);
            submitBtn.setTextColor(getResources().getColor(R.color.dark_gray));
        }

    }

    public void makeList(){

        randomCourse = madeRandomCourse();

        final AddCourseGameCustomAdapter adapter = new AddCourseGameCustomAdapter(context, randomCourse, rv, mToolbar, ((AddCourseGameActivity)getActivity()));

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private ArrayList<HashMap<String, Object>> madeRandomCourse(){

        ArrayList<HashMap<String, Object>> tempList = new ArrayList<>();

        ArrayList<HashMap<String, String>> db = dbHelper.getResultAll(StartActivity.DATABASE_LANGUAGE);
        int count = db.size();

        Random r = new Random();
        ArrayList<Integer> index = new ArrayList<>();
        for(int i=0; i<seekBar.getProgress(); i++){
            while(true){
                int random = r.nextInt(count);
                if(!index.contains(random)){
                    index.add(random);
                    break;
                }
            }
        }

        ArrayList<String> attraction = new ArrayList<>();
        for(int i : index){
            attraction.add(db.get(i).get("title"));
        }
        HashMap<String, Object> temp = new HashMap<>();
        temp.put("title", "랜덤");
        temp.put("language", StartActivity.DATABASE_LANGUAGE);
        temp.put("time", 0);
        temp.put("attraction", attraction);
        temp.put("isShare", false);
        temp.put("isGame", false);

        tempList.add(temp);

        return tempList;

    }


    public void hideViews() {
//        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
    }
    public void showViews() {
//        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
    }

}
