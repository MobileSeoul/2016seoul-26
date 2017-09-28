package tk.twpooi.seoultrip.fragment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import tk.twpooi.seoultrip.Information;

/**
 * Created by tw on 2016-09-06.
 */
public class IncreaseShareCourseItem extends Thread{

    private boolean result;
    private HashMap<String, String> map;

    public IncreaseShareCourseItem(HashMap<String, String> map){
        this.map = map;
        result = false;
    }

    public void run(){

        String addr = Information.MAIN_SERVER_ADDRESS + "updateShareCourse.php";
        String response = new String();

        try {
            URL url = new URL(addr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // 해당 URL에 연결

            conn.setConnectTimeout(10000); // 타임아웃: 10초
            conn.setUseCaches(false); // 캐시 사용 안 함
            conn.setRequestMethod("POST"); // POST로 연결
            conn.setDoInput(true);
            conn.setDoOutput(true);

            if (map != null) { // 웹 서버로 보낼 매개변수가 있는 경우우
                OutputStream os = conn.getOutputStream(); // 서버로 보내기 위한 출력 스트림
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8")); // UTF-8로 전송
                bw.write(getPostString(map)); // 매개변수 전송
                bw.flush();
                bw.close();
                os.close();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 연결에 성공한 경우
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); // 서버의 응답을 읽기 위한 입력 스트림

                while ((line = br.readLine()) != null) // 서버의 응답을 읽어옴
                    response += line;
            }

            conn.disconnect();
        } catch (MalformedURLException me) {
            me.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getPostString(HashMap<String, String> map) {
        StringBuilder result = new StringBuilder();
        boolean first = true; // 첫 번째 매개변수 여부

        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (first)
                first = false;
            else // 첫 번째 매개변수가 아닌 경우엔 앞에 &를 붙임
                result.append("&");

            try { // UTF-8로 주소에 키와 값을 붙임
                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ue) {
                ue.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    public boolean getResult(){
        return result;
    }

}