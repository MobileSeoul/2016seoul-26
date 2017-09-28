package tk.twpooi.seoultrip;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tw on 2016-08-16.
 */
public class DBHelper extends SQLiteOpenHelper {

    private String TABLE_NAME = "AttractionsTbl";
    private String TABLE_NAME_EN = "AttractionsEnTbl";
    private String CONVERT_TABLE_NAME = "convertTbl";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    public String getTABLE_NAME(){
        return TABLE_NAME;
    }
    public String getTABLE_NAME_EN(){
        return TABLE_NAME_EN;
    }
    public String getCONVERT_TABLE_NAME(){
        return CONVERT_TABLE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        Log.v("DBHelper", "onCreate");

        try{
            String DROP_SQL = "drop table if exists " + TABLE_NAME;
            db.execSQL(DROP_SQL);
            DROP_SQL = "drop table if exists " + TABLE_NAME_EN;
            db.execSQL(DROP_SQL);
            DROP_SQL = "drop table if exists " + CONVERT_TABLE_NAME;
            db.execSQL(DROP_SQL);
        }catch (Exception e){e.printStackTrace();}

        String CREATE_SQL = "create table " + TABLE_NAME + "("
                + "_id integer PRIMARY KEY autoincrement, "
                + "title VARCHAR(120),"
                + "sContents MEDIUMTEXT,"
                + "contents LONGTEXT,"
                + "address VARCHAR(120),"
                + "district VARCHAR(20),"
                + "telephone VARCHAR(100),"
                + "web MEDIUMTEXT,"
                + "detail LONGTEXT,"
                + "url MEDIUMTEXT,"
                + "mainImage MEDIUMTEXT,"
                + "subImage MEDIUMTEXT,"
                + "lat VARCHAR(40),"
                + "lng VARCHAR(40),"
                + "categorize MEDIUMTEXT,"
                + "favorite CHAR(2)"
                + ");";

        String CREATE_SQL_EN = "create table " + TABLE_NAME_EN + "("
                + "_id integer PRIMARY KEY autoincrement, "
                + "title VARCHAR(120),"
                + "sContents MEDIUMTEXT,"
                + "contents LONGTEXT,"
                + "address VARCHAR(120),"
                + "district VARCHAR(20),"
                + "telephone VARCHAR(100),"
                + "web MEDIUMTEXT,"
                + "detail LONGTEXT,"
                + "url MEDIUMTEXT,"
                + "mainImage MEDIUMTEXT,"
                + "subImage MEDIUMTEXT,"
                + "lat VARCHAR(40),"
                + "lng VARCHAR(40),"
                + "categorize MEDIUMTEXT,"
                + "favorite CHAR(2)"
                + ");";

        String CREATE_SQL_CONVERT = "create table " + CONVERT_TABLE_NAME + "("
                + "title_ko VARCHAR(120),"
                + "title_en VARCHAR(120)"
                +" );";

        try{
            db.execSQL(CREATE_SQL);
            db.execSQL(CREATE_SQL_EN);
            db.execSQL(CREATE_SQL_CONVERT);
        }catch (Exception e){e.printStackTrace();}


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.v("DBHelper", "onUpgrade");
    }

    public boolean insert(String SQL){
        boolean state = false;
        SQLiteDatabase db = getWritableDatabase();
        try{
            db.execSQL(SQL);
            db.close();
            state = true;
        }catch (Exception e){
            e.printStackTrace();
        }

        return state;
    }

    public boolean delete(String SQL){
        boolean state = false;
        SQLiteDatabase db = getWritableDatabase();
        try{
            db.execSQL(SQL);
            db.close();
            state = true;
        }catch (Exception e ){
            e.printStackTrace();
        }

        return state;
    }

    public String getConvertTitle(String title, String lang){

        String convertTitle = null;

        SQLiteDatabase db = getReadableDatabase();

        String tableName = CONVERT_TABLE_NAME;

        String rowTitle = "";
        String getrowTitle = "";
        if(lang.equals("ko")){
            rowTitle = "title_ko";
            getrowTitle = "title_en";
        }else{
            rowTitle = "title_en";
            getrowTitle = "title_ko";
        }

        try{
            String SQL = "SELECT " + getrowTitle + " FROM " + tableName + " WHERE " + rowTitle + " = '" + title + "';";
            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){

                convertTitle = cursor.getString(0);

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return convertTitle;
    }


    public boolean setFavoriteAttration(String title, boolean isAdd, String lang){
        boolean result = false;
        SQLiteDatabase db = getWritableDatabase();
        String favoriteText="";
        if(isAdd){
            favoriteText = "'1'";
        }else{
            favoriteText = "null";
        }

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        String SQL = "UPDATE " + tableName
                + " SET favorite = " + favoriteText + " "
                + "WHERE title = '" + title + "';";

        try{
            db.execSQL(SQL);
            db.close();
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }


        return result;
    }

    // initial database download
    public ArrayList<HashMap<String, String>> getResultAll(String lang){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        try{
            String SQL = "SELECT * FROM " + tableName;
            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){
                HashMap<String, String> temp = new HashMap<>();

                temp.put("title", cursor.getString(1));
                temp.put("sContents", cursor.getString(2));
                temp.put("contents", cursor.getString(3));
                temp.put("address", cursor.getString(4));
                temp.put("district", cursor.getString(5));
                temp.put("telephone", cursor.getString(6));
                temp.put("web", cursor.getString(7));
                temp.put("detail", cursor.getString(8));
                temp.put("url", cursor.getString(9));
                temp.put("mainImage", cursor.getString(10));
                temp.put("subImage", cursor.getString(11));
                temp.put("lat", cursor.getString(12));
                temp.put("lng", cursor.getString(13));
                temp.put("categorize", cursor.getString(14));
                temp.put("favorite", cursor.getString(15));
                temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                result.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<HashMap<String, String>> getResultAllByCategorize(ArrayList<String> list, String title, String lang){

        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        if(list.size() > 0 && list != null) {

            SQLiteDatabase db = getReadableDatabase();

            String categorize = " WHERE (";

            for (int i = 0; i < list.size(); i++) {

                categorize = categorize + "categorize LIKE '%" + list.get(i) + "%'";

                if (i < list.size() - 1) {
                    categorize += " OR ";
                }
            }

            categorize += ")";

            try {
                String SQL = "SELECT * FROM " + tableName + categorize + ";";
                Cursor cursor = db.rawQuery(SQL, null);
                while (cursor.moveToNext()) {
                    HashMap<String, String> temp = new HashMap<>();

                    String titleTemp = cursor.getString(1);

                    if(!titleTemp.equals(title)) {

                        temp.put("title", titleTemp);
                        temp.put("sContents", cursor.getString(2));
                        temp.put("contents", cursor.getString(3));
                        temp.put("address", cursor.getString(4));
                        temp.put("district", cursor.getString(5));
                        temp.put("telephone", cursor.getString(6));
                        temp.put("web", cursor.getString(7));
                        temp.put("detail", cursor.getString(8));
                        temp.put("url", cursor.getString(9));
                        temp.put("mainImage", cursor.getString(10));
                        temp.put("subImage", cursor.getString(11));
                        temp.put("lat", cursor.getString(12));
                        temp.put("lng", cursor.getString(13));
                        temp.put("categorize", cursor.getString(14));
                        temp.put("favorite", cursor.getString(15));
                        temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                        result.add(temp);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    public ArrayList<HashMap<String, String>> getResultAllBySearchQuery(String query, String lang){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        try{
            String SQL = "SELECT * FROM " + tableName
                    + " WHERE ("
                    + "title LIKE '%" + query + "%'" + " OR "
                    + "sContents LIKE '%" + query + "%'" + " OR "
                    + "contents LIKE '%" + query + "%'" + " OR "
                    + "address LIKE '%" + query + "%'" + " OR "
                    + "district LIKE '%" + query + "%'" + " OR "
                    + "telephone LIKE '%" + query + "%'" + " OR "
                    + "web LIKE '%" + query + "%'" + " OR "
                    + "detail LIKE '%" + query + "%'" + " OR "
                    + "url LIKE '%" + query + "%'" + " OR "
                    + "categorize LIKE '%" + query + "%');";
            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){
                HashMap<String, String> temp = new HashMap<>();

                temp.put("title", cursor.getString(1));
                temp.put("sContents", cursor.getString(2));
                temp.put("contents", cursor.getString(3));
                temp.put("address", cursor.getString(4));
                temp.put("district", cursor.getString(5));
                temp.put("telephone", cursor.getString(6));
                temp.put("web", cursor.getString(7));
                temp.put("detail", cursor.getString(8));
                temp.put("url", cursor.getString(9));
                temp.put("mainImage", cursor.getString(10));
                temp.put("subImage", cursor.getString(11));
                temp.put("lat", cursor.getString(12));
                temp.put("lng", cursor.getString(13));
                temp.put("categorize", cursor.getString(14));
                temp.put("favorite", cursor.getString(15));
                temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                result.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<HashMap<String, String>> getResultFavorite(String lang){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        try{
            String SQL = "SELECT * FROM " + tableName + " WHERE favorite <> 'null';";

            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){
                HashMap<String, String> temp = new HashMap<>();

                temp.put("title", cursor.getString(1));
                temp.put("sContents", cursor.getString(2));
                temp.put("contents", cursor.getString(3));
                temp.put("address", cursor.getString(4));
                temp.put("district", cursor.getString(5));
                temp.put("telephone", cursor.getString(6));
                temp.put("web", cursor.getString(7));
                temp.put("detail", cursor.getString(8));
                temp.put("url", cursor.getString(9));
                temp.put("mainImage", cursor.getString(10));
                temp.put("subImage", cursor.getString(11));
                temp.put("lat", cursor.getString(12));
                temp.put("lng", cursor.getString(13));
                temp.put("categorize", cursor.getString(14));
                temp.put("favorite", cursor.getString(15));
                temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                result.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<HashMap<String, String>> getResultDistrict(String district, String lang){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        try{
            String SQL = "SELECT * FROM " + tableName;
            if((!district.equals("전체"))&&(!district.equals("All"))){
                SQL = SQL + " WHERE district='" + district + "';";
            }
            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){
                HashMap<String, String> temp = new HashMap<>();

                temp.put("title", cursor.getString(1));
                temp.put("sContents", cursor.getString(2));
                temp.put("contents", cursor.getString(3));
                temp.put("address", cursor.getString(4));
                temp.put("district", cursor.getString(5));
                temp.put("telephone", cursor.getString(6));
                temp.put("web", cursor.getString(7));
                temp.put("detail", cursor.getString(8));
                temp.put("url", cursor.getString(9));
                temp.put("mainImage", cursor.getString(10));
                temp.put("subImage", cursor.getString(11));
                temp.put("lat", cursor.getString(12));
                temp.put("lng", cursor.getString(13));
                temp.put("categorize", cursor.getString(14));
                temp.put("favorite", cursor.getString(15));
                temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                result.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<HashMap<String, String>> getResultOrderBy(String orderBy, String lang){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        try{
            String SQL = "SELECT * FROM " + tableName;
            if(orderBy.equals("오름차순") || orderBy.equals("Ascending")){
                SQL = SQL + " ORDER BY title ASC;";
            }else{
                SQL = SQL + " ORDER BY title DESC;";
            }
            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){
                HashMap<String, String> temp = new HashMap<>();

                temp.put("title", cursor.getString(1));
                temp.put("sContents", cursor.getString(2));
                temp.put("contents", cursor.getString(3));
                temp.put("address", cursor.getString(4));
                temp.put("district", cursor.getString(5));
                temp.put("telephone", cursor.getString(6));
                temp.put("web", cursor.getString(7));
                temp.put("detail", cursor.getString(8));
                temp.put("url", cursor.getString(9));
                temp.put("mainImage", cursor.getString(10));
                temp.put("subImage", cursor.getString(11));
                temp.put("lat", cursor.getString(12));
                temp.put("lng", cursor.getString(13));
                temp.put("categorize", cursor.getString(14));
                temp.put("favorite", cursor.getString(15));
                temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                result.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

    public ArrayList<HashMap<String, String>> getResultSearchTitle(String title, String lang){
        ArrayList<HashMap<String, String>> result = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        String tableName;
        if(lang.equals("ko")){
            tableName = TABLE_NAME;
        }else{
            tableName = TABLE_NAME_EN;
        }

        try{
            String SQL = "SELECT * FROM " + tableName + " WHERE title LIKE '%" + title + "%';";
            Cursor cursor = db.rawQuery(SQL, null);
            while(cursor.moveToNext()){
                HashMap<String, String> temp = new HashMap<>();

                temp.put("title", cursor.getString(1));
                temp.put("sContents", cursor.getString(2));
                temp.put("contents", cursor.getString(3));
                temp.put("address", cursor.getString(4));
                temp.put("district", cursor.getString(5));
                temp.put("telephone", cursor.getString(6));
                temp.put("web", cursor.getString(7));
                temp.put("detail", cursor.getString(8));
                temp.put("url", cursor.getString(9));
                temp.put("mainImage", cursor.getString(10));
                temp.put("subImage", cursor.getString(11));
                temp.put("lat", cursor.getString(12));
                temp.put("lng", cursor.getString(13));
                temp.put("categorize", cursor.getString(14));
                temp.put("favorite", cursor.getString(15));
                temp.put("clicked", "0"); // AttractionListCustomAdapter에서 상세보기에 사용

                result.add(temp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

}
