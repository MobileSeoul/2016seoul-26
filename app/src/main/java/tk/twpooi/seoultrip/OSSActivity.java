package tk.twpooi.seoultrip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;

import tk.twpooi.seoultrip.fragment.DividerItemDecoration;

public class OSSActivity extends AppCompatActivity {

    private RelativeLayout root;

    // Recycle View
    private RecyclerView rv;
    private LinearLayoutManager mLinearLayoutManager;
    private OSSCustomAdapter adapter;

    private String type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noti_license);

        Intent i = getIntent();
        type = i.getStringExtra("type");

        init();

        makeList();

    }

    private void init(){

        root = (RelativeLayout)findViewById(R.id.root);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setHasFixedSize(true);
        rv.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL_LIST));
        rv.setLayoutManager(mLinearLayoutManager);


    }

    private void makeList(){

        ArrayList<HashMap<String, String>> list = makeData();

        adapter = new OSSCustomAdapter(getApplicationContext(), list, getWindow().getDecorView().getRootView());

        rv.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    private ArrayList<HashMap<String, String>> makeData(){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        if(type.equals("oss")) {

            HashMap<String, String> temp = new HashMap<>();
            temp.put("title", "picasso");
            temp.put("web", "https://github.com/square/picasso");
            temp.put("author", "Square(http://square.github.io)");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "ShineButton");
            temp.put("web", "https://github.com/ChadCSong/ShineButton");
            temp.put("author", "Chad Song");
            temp.put("license", "MIT License");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "FlycoDialog");
            temp.put("web", "https://github.com/H07000223/FlycoDialog_Master");
            temp.put("author", "Flyco");
            temp.put("license", "MIT License");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "Android-ObservableScrollView");
            temp.put("web", "https://github.com/ksoichiro/Android-ObservableScrollView");
            temp.put("author", "Soichiro Kashima");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "NineOldAndroids");
            temp.put("web", "https://github.com/JakeWharton/NineOldAndroids");
            temp.put("author", "Jake Wharton");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "android-Ultra-Pull-To-Refresh");
            temp.put("web", "https://github.com/liaohuqiu/android-Ultra-Pull-To-Refresh");
            temp.put("author", "Huqiu Liao");
            temp.put("license", "MIT License");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "Dragger");
            temp.put("web", "https://github.com/ppamorim/Dragger");
            temp.put("author", "Pedro Paulo Amorim");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "GuillotineMenu");
            temp.put("web", "https://github.com/Yalantis/GuillotineMenu");
            temp.put("author", "Yalantis");
            temp.put("license", "MIT License");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "NavigationTabBar");
            temp.put("web", "https://github.com/DevLight-Mobile-Agency/NavigationTabBar");
            temp.put("author", "DevLight");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "sweet-alert-dialog");
            temp.put("web", "https://github.com/pedant/sweet-alert-dialog");
            temp.put("author", "书呆子(pedant)");
            temp.put("license", "MIT License");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "android-floating-action-button");
            temp.put("web", "https://github.com/futuresimple/android-floating-action-button");
            temp.put("author", "Base(http://lab.getbase.com/)");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "MaterialEditText");
            temp.put("web", "https://github.com/rengwuxian/MaterialEditText");
            temp.put("author", "Kai Zhu");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "picasso-transformations");
            temp.put("web", "https://github.com/wasabeef/picasso-transformations");
            temp.put("author", "Daichi Furiya");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "SlidingIntroScreen");
            temp.put("web", "https://github.com/MatthewTamlin/SlidingIntroScreen");
            temp.put("author", "Matthew Tamlin");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "AVLoadingIndicatorView");
            temp.put("web", "https://github.com/81813780/AVLoadingIndicatorView");
            temp.put("author", "Jack Wang");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "discreteSeekBar");
            temp.put("web", "https://github.com/AnderWeb/discreteSeekBar/blob/master/sample/src/main/java/org/adw/samples/discreteseekbar/MainActivity.java");
            temp.put("author", "AnderWeb");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "Android-RoundCornerProgressBar");
            temp.put("web", "https://github.com/akexorcist/Android-RoundCornerProgressBar");
            temp.put("author", "Akexorcist");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "android-advancedrecyclerview");
            temp.put("web", "https://github.com/h6ah4i/android-advancedrecyclerview");
            temp.put("author", "Haruki Hasegawa");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "PhotoView");
            temp.put("web", "https://github.com/chrisbanes/PhotoView");
            temp.put("author", "Chris Banes");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "KenBurnsView");
            temp.put("web", "https://github.com/flavioarfaria/KenBurnsView");
            temp.put("author", "Flávio Faria");
            temp.put("license", "Apache License Version 2.0");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "MIT License");
            temp.put("web", "");
            temp.put("author", "MIT License");
            temp.put("license", getString(R.string.mit_license));
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "Apache License 2.0");
            temp.put("web", "");
            temp.put("author", "Apache License Version 2.0, January 2004");
            temp.put("license", getString(R.string.apache2_0_license));
            list.add(temp);
        }else{

            HashMap<String, String> temp = new HashMap<>();
            temp.put("title", "서울시 분류별 관광명소 현황 (한국어)");
            temp.put("web", "http://data.seoul.go.kr/openinf/sheetview.jsp?infId=OA-2665&tMenu=11");
            temp.put("author", "서울 열린데이터 광장");
            temp.put("license", "CC BY");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "서울시 분류별 관광명소 현황 (영어)");
            temp.put("web", "http://data.seoul.go.kr/openinf/sheetview.jsp?infId=OA-2666&tMenu=11");
            temp.put("author", "서울 열린데이터 광장");
            temp.put("license", "CC BY");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "서울시 주요 관광명소 이미지 주소 (URL)정보");
            temp.put("web", "http://data.seoul.go.kr/openinf/fileview.jsp?infId=OA-13006&tMenu=11");
            temp.put("author", "서울 열린데이터 광장");
            temp.put("license", "CC BY-ND");
            list.add(temp);

            temp = new HashMap<>();
            temp.put("title", "관광지");
            temp.put("web", "http://korean.visitseoul.net/attractions");
            temp.put("author", "VISIT SEOUL.NET");
            temp.put("license", "http://english.visitseoul.net/attractions");
            list.add(temp);

        }



        return list;

    }

}
