package tk.twpooi.seoultrip;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tw on 2016-09-23.
 */
public class FileManager {

    private Context context;

    private static final String favoriteFileName = "favorite.st";
    private static final String myCourseFileName = "mycourse.st";
    private static final String sharedFileName = "Shared.st";
    private static final String courseGameFileName = "coursegame.st";
    private static final String interestCourseFileName = "interestCourse.st";

    public FileManager(Context context){
        this.context = context;
    }

    public ArrayList<HashMap<String, String>> readFavorite(){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        ObjectInputStream input;

        try{
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(), "") + File.separator + favoriteFileName)));
            list = (ArrayList<HashMap<String, String>>)input.readObject();
            input.close();
        }catch(FileNotFoundException e){
        }catch(IOException e){
        }catch(Exception e){
        }

        return list;

    }

    public boolean writeFavorite(ArrayList<HashMap<String, String>> list){

        ObjectOutput out = null;

        boolean check = false;

        try{
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(),"")+File.separator+favoriteFileName));
            out.writeObject(list);
            out.close();
            check = true;
        }catch(FileNotFoundException e){
        }catch (IOException e){
        }

        return check;
    }

    public ArrayList<HashMap<String, Object>> readMyCourse(){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        ObjectInputStream input;

        try{
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(), "") + File.separator + myCourseFileName)));
            list = (ArrayList<HashMap<String, Object>>)input.readObject();
            input.close();
        }catch(FileNotFoundException e){
        }catch(IOException e){
        }catch(Exception e){
        }

        return list;

    }

    public boolean writeMyCourse(ArrayList<HashMap<String, Object>> list){

        ObjectOutput out = null;

        boolean check = false;

        try{
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(),"")+File.separator+myCourseFileName));
            out.writeObject(list);
            out.close();
            check = true;
        }catch(FileNotFoundException e){
        }catch (IOException e){
        }

        return check;
    }

    public ArrayList<String> readSharedFile(){

        ArrayList<String> list = new ArrayList<>();

        ObjectInputStream input;

        try{
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(), "") + File.separator + sharedFileName)));
            list = (ArrayList<String>)input.readObject();
            input.close();
        }catch(FileNotFoundException e){
        }catch(IOException e){
        }catch(Exception e){
        }

        return list;

    }

    public void writeSharedFile(ArrayList<String> list){

        ObjectOutput out = null;

        try{
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(),"")+File.separator+sharedFileName));
            out.writeObject(list);
            out.close();
        }catch(FileNotFoundException e){
        }catch (IOException e){
        }

    }

    public ArrayList<HashMap<String, Object>> readCourseGameFile(){

        ArrayList<HashMap<String, Object>> list = new ArrayList<>();

        ObjectInputStream input;

        try{
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(), "") + File.separator + courseGameFileName)));
            list = (ArrayList<HashMap<String, Object>>)input.readObject();
            input.close();
        }catch(FileNotFoundException e){
        }catch(IOException e){
        }catch(Exception e){
        }

        return list;

    }

    public void writeCourseGameFile(ArrayList<HashMap<String, Object>> list){

        ObjectOutput out = null;

        try{
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(),"")+File.separator+courseGameFileName));
            out.writeObject(list);
            out.close();
        }catch(FileNotFoundException e){
        }catch (IOException e){
        }

    }

    public ArrayList<String> readInterestListFile(){

        ArrayList<String> list = new ArrayList<>();

        ObjectInputStream input;

        try{
            input = new ObjectInputStream(new FileInputStream(new File(new File(context.getFilesDir(), "") + File.separator + interestCourseFileName)));
            list = (ArrayList<String>)input.readObject();
            input.close();
        }catch(FileNotFoundException e){
        }catch(IOException e){
        }catch(Exception e){
        }

        return list;

    }

    public void writeInterestListFile(ArrayList<String> list){

        ObjectOutput out = null;

        try{
            out = new ObjectOutputStream(new FileOutputStream(new File(context.getFilesDir(),"")+File.separator+interestCourseFileName));
            out.writeObject(list);
            out.close();
        }catch(FileNotFoundException e){
        }catch (IOException e){
        }

    }
}
