package tk.twpooi.seoultrip.startfragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;

import java.util.Locale;

import tk.twpooi.seoultrip.R;
import tk.twpooi.seoultrip.StartActivity;

/**
 * Created by tw on 2016-08-16.
 */
public class LoginFragment extends Fragment{


    // UI
    private View view;
    private Context context;

    private ImageView fbLogin;

    // Facebook
    private CallbackManager callbackManager;
    private String facebook_id = "";
    private String full_name = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        image = getArguments().getString("url");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        FacebookSdk.sdkInitialize(context);
        callbackManager = CallbackManager.Factory.create();
        // UI
        view = inflater.inflate(R.layout.fragment_start_login, container, false);

        init();

        return view;

    }

    @Override
    public void onStart(){
        super.onStart();
    }

    private void init(){

        fbLogin = (ImageView)view.findViewById(R.id.fb_login);
        setFBImageLang(fbLogin);
        TextView noneLogin = (TextView)view.findViewById(R.id.none_login);

        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartActivity)getActivity()).loginFacebook();
            }
        });

        noneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StartActivity)getActivity()).setFirst();
                ((StartActivity)getActivity()).redirectMainActivity();
            }
        });

    }

    private void setFBImageLang(ImageView img){

        String lang = Locale.getDefault().getLanguage();
        if(lang.equals("ko")){
            img.setImageResource(R.drawable.ic_login_facebook_ko);
        }else{
            img.setImageResource(R.drawable.ic_login_facebook_en);
        }

    }

    public void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.snackbar_color));
        snackbar.show();
    }



}
