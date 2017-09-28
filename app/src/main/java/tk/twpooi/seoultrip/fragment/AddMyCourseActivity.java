package tk.twpooi.seoultrip.fragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.FileManager;
import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;

/**
 * Created by tw on 2016-08-17.
 */
public class AddMyCourseActivity extends AppCompatActivity {

    private FileManager fileManager;

    public static final String DRAG_POSITION = "drag_position";

    private DraggerView draggerView;
    private Toolbar toolbar;
    private InputMethodManager imm;

    private MaterialEditText title;
    private MaterialEditText time;
    private Button submit;

    private boolean isTitle = false;
    private boolean isTime = false;

    private String language;

    @Override protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drag_add_mycourse);
        setStatusColor();

        Intent i = getIntent();
        fileManager = new FileManager(getApplicationContext());
        language = i.getStringExtra("language");
        if(language == null || language.equals("")){
            language = StartActivity.DATABASE_LANGUAGE;
        }

        init();

    }

    public void linearOnClick(View v) {
        imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(time.getWindowToken(), 0);
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

    private void init(){

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);

        draggerView = new DraggerView(getApplicationContext());
        clearSingleton();
        draggerView = (DraggerView)findViewById(R.id.dragger_view);
        draggerView.setDraggerPosition((DraggerPosition)getIntent().getSerializableExtra(DRAG_POSITION));

        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); // 화면터치시 키보드 숨김 관련

        title = (MaterialEditText)findViewById(R.id.title);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isTitle = (title.getText().toString().length()>=1 && title.getText().toString().length() <=20);
                setButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        time = (MaterialEditText)findViewById(R.id.time);
        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    String t = time.getText().toString();
                    int t2 = Integer.parseInt(t);
                    isTime = (t2 >= 1 && t2 <= 48);
                }catch (Exception e){
                    isTime = false;
                }finally {
                    setButtonEnabled();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        submit = (Button)findViewById(R.id.submit);
        submit.setEnabled(false);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<HashMap<String, Object>> list = fileManager.readMyCourse();

                HashMap<String, Object> temp = new HashMap<String, Object>();

                String mTitle = title.getText().toString();
                int mTime = Integer.parseInt(time.getText().toString());
                ArrayList<String> attractionList = new ArrayList<String>();

                temp.put("title", mTitle);
                temp.put("time", mTime);
                temp.put("attraction", attractionList);
                temp.put("language", language);
                temp.put("isShare", false);
                temp.put("isGame", false);

                list.add(temp);

                fileManager.writeMyCourse(list);

                onBackPressed();
            }
        });


    }

    public void setButtonEnabled(){

        if(isTime && isTitle) {
            submit.setTextColor(getResources().getColor(R.color.white));
            submit.setEnabled(true);
        }else{
            submit.setTextColor(getResources().getColor(R.color.dark_gray));
            submit.setEnabled(false);
        }

    }

    private void setStatusColor(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.dark_status_bar_color));
        }
    }

    @Override public void onBackPressed() {
        draggerView.closeActivity();
    }

}
