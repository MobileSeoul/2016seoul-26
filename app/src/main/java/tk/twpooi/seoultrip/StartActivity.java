package tk.twpooi.seoultrip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import tk.twpooi.seoultrip.startfragment.InitImageFragment;
import tk.twpooi.seoultrip.startfragment.LoginFragment;
import tk.twpooi.seoultrip.startfragment.SelectLangFragment;

public class StartActivity extends AppCompatActivity {

    private static final int tempSize = 7;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    // UI
    private ViewPager viewPager;
    private NavigationAdapter mPagerAdapter;
    private DotIndicator dotIndicator;

    // Facebook
    private CallbackManager callbackManager;
    private String facebook_id = "";
    private String full_name = "";
    private boolean isLoginSuccess;

    public static String DATABASE_LANGUAGE = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_start);
        setStatusColorLight();


        setting = getSharedPreferences("setting", 0);
        editor = setting.edit();

        boolean isFirst = setting.getBoolean("first1", true);

        DATABASE_LANGUAGE = setting.getString("db_lang", "en");
        if(isFirst){
            initUI();
        }else{
            redirectMainActivity();
        }
    }

    private void initUI(){

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        dotIndicator = (DotIndicator)findViewById(R.id.main_indicator_ad);

        viewPager.setOffscreenPageLimit(tempSize);
        mPagerAdapter = new NavigationAdapter(getSupportFragmentManager(), tempSize);
        viewPager.setAdapter(mPagerAdapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(position == tempSize-2){
                    String lang = setting.getString("db_lang", null);
                    if(lang == null){
                        viewPager.setCurrentItem(position);
                    }
                }

                if(position == tempSize-1){
                    setStatusColorDark();
                }else{
                    setStatusColorLight();
                }
            }

            @Override
            public void onPageSelected(int position) {
                dotIndicator.setSelectedItem(position%tempSize, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        initIndicator();

    }

    private void initIndicator(){
        dotIndicator.setSelectedDotColor(Color.parseColor("#FF4081"));
        dotIndicator.setUnselectedDotColor(Color.parseColor("#CFCFCF"));
        dotIndicator.setNumberOfItems(tempSize);
    }

    public void setFirst(){
        editor.putBoolean("first1", false);
        editor.commit();
    }


    // Tab Adapter Class (used initUI())
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        private int size;

        public NavigationAdapter(FragmentManager fm, int size){
            super(fm);
            this.size = size;
        }

        @Override
        protected Fragment createItem(int position){
            Fragment f;
            final int pattern = position %size;

            if(pattern == size-1){
                f = new LoginFragment();
            }else if(pattern == size-2){
                f = new SelectLangFragment();
            }else {
                f = new InitImageFragment();
                Bundle bdl = new Bundle(1);
                bdl.putInt("position", pattern);
                f.setArguments(bdl);
            }

            return f;
        }

        @Override
        public int getCount(){
            return size;
        }

    }


    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.snackbar_color));
        snackbar.show();
    }


    public void redirectMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void loginFacebook(){


        LoginManager.getInstance().logInWithReadPermissions(StartActivity.this, Arrays.asList("public_profile", "user_friends", "email"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                showSnackbar(getResources().getString(R.string.success_login) + "!");
                Profile profile = Profile.getCurrentProfile();
                if (profile != null) {
                    facebook_id=profile.getId();
//                    f_name=profile.getFirstName();
//                    m_name=profile.getMiddleName();
//                    l_name=profile.getLastName();
                    full_name=profile.getName();
//                    profile_image=profile.getProfilePictureUri(400, 400).toString();
                }


                GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
//                                    email_id=object.getString("email");
//                                    gender=object.getString("gender");
                                    String profile_name=object.getString("name");
                                    long fb_id=object.getLong("id"); //use this for logout
                                    //Start new activity or use this info in your project.
                                    //Go another activity
                                } catch (JSONException e) {
                                    // TODO Auto-generated catch block
                                    //  e.printStackTrace();
                                }

                            }

                        });

                request.executeAsync();

                setFirst();
                redirectMainActivity();
            }

            @Override
            public void onCancel() {
                showSnackbar(getResources().getString(R.string.cancel_login));
            }

            @Override
            public void onError(FacebookException error) {
                showSnackbar(getResources().getString(R.string.error_login) + "\n" + error.toString());
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void setStatusColorLight(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.init_background));
        }
    }
    public void setStatusColorDark(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        }
    }

    private void printKeyHash(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "tk.twpooi.seoultrip",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }
}
