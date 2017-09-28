package tk.twpooi.seoultrip.startfragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class SelectLangFragment extends Fragment{


    // UI
    private View view;
    private Context context;

    private Button btnKorean;
    private Button btnEnglish;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        image = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // UI
        view = inflater.inflate(R.layout.fragment_start_select_language, container, false);
        context = container.getContext();

        setting = context.getSharedPreferences("setting", 0);
        editor = setting.edit();

        init();

        return view;

    }

    @Override
    public void onStart(){
        super.onStart();
    }

    private void init(){

        btnKorean = (Button)view.findViewById(R.id.btn_ko);
        btnEnglish = (Button)view.findViewById(R.id.btn_en);
        setButtonOff(btnKorean);
        setButtonOff(btnEnglish);

        btnKorean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonOff(btnEnglish);
                setButtonOn(btnKorean);
                StartActivity.DATABASE_LANGUAGE = "ko";
                editor.putString("db_lang", "ko");
                editor.commit();
            }
        });
        btnEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButtonOff(btnKorean);
                setButtonOn(btnEnglish);
                StartActivity.DATABASE_LANGUAGE = "en";
                editor.putString("db_lang", "en");
                editor.commit();
            }
        });

    }

    private void setButtonOff(Button btn){

        btn.setTextColor(ContextCompat.getColor(context, R.color.white));
        btn.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_gray));

    }

    private void setButtonOn(Button btn){

        btn.setTextColor(ContextCompat.getColor(context, R.color.white));
        btn.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }



}
