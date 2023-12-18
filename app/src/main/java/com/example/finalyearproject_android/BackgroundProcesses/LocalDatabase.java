package com.example.finalyearproject_android.BackgroundProcesses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.finalyearproject_android.Models.ModelFaq;
import com.example.finalyearproject_android.Models.ModelGoal;
import com.example.finalyearproject_android.Models.ModelSteps;
import com.example.finalyearproject_android.Models.ModelUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocalDatabase extends SQLiteOpenHelper {

    Context ctx;
    SQLiteDatabase database;
    static final  private String DB_NAME="AiBasedActivityMonitoring";
    static final private String DB_TABLE1="Steps";
    static final private String DB_TABLE2="Goals";
    private static final String DB_USER = "Users";
    private static final String DB_FAQ = "Faq";
    private static final String DB_MODE = "DarkModeStatus";
    static final private int DB_VERSION=3;

    public LocalDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.ctx = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE1);
        db.execSQL("DROP TABLE IF EXISTS "+DB_TABLE2);
        db.execSQL("DROP TABLE IF EXISTS "+DB_USER);
        db.execSQL("DROP TABLE IF EXISTS "+DB_FAQ);
        db.execSQL("DROP TABLE IF EXISTS "+DB_MODE);
        db.execSQL("CREATE TABLE IF NOT EXISTS "+DB_TABLE1+" (_id integer primary key autoincrement, steps text, uId text, date text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+DB_TABLE2+" (_id integer primary key autoincrement, uId text, stepGoal text, calorieGoal text);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+DB_USER+" (_id integer primary key autoincrement, uId text, name text, email text, phone text, image text, role text, height text, gender text, weight text);");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+DB_MODE+" (status text)");
        db.execSQL("CREATE TABLE IF NOT EXISTS "+DB_FAQ+" (_id integer primary key autoincrement, qid text, question text, answer text)");
        db.execSQL("INSERT INTO "+DB_MODE+" (status) VALUES('disabled')");
    }

    public void createUser(ModelUser user) {
        SQLiteDatabase db = getWritableDatabase();
        onCreate(db);
        setUserData(user);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public boolean isDarkModeEnabled(){
        database = getReadableDatabase();
        Cursor cr = database.rawQuery("SELECT * FROM "+DB_MODE, null);
        String status = "false";
        while (cr.moveToNext()){
            status = cr.getString(0);
        }
        cr.close();
        return status.equalsIgnoreCase("true");
    }

    public void setDarkModeStatus(boolean status){
        if (status){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        database = getWritableDatabase();

        String s = "false";
        if (status){
            s = "true";
        }
        database.execSQL("UPDATE "+DB_MODE+" SET status='"+s+"'");
    }

    public void addOldSteps(List<ModelSteps> stepsList, String uId) {
        database = getReadableDatabase();
        for (ModelSteps stepsModel : stepsList){
            Cursor cr =database.rawQuery("SELECT COUNT(*) FROM "+DB_TABLE1+" WHERE uId = '"+uId+"' AND date = '"+stepsModel.getDate()+"';",null);
            cr.moveToNext();

            database=getWritableDatabase();

            if (cr.getString(0).equals("1")){
                database.execSQL("UPDATE "+DB_TABLE1+" SET steps = '"+ stepsModel.getSteps()+"' WHERE uId='"+uId+"' AND date ='"+stepsModel.getDate()+"';");
            }else {
                database.execSQL("INSERT INTO " + DB_TABLE1 + "(steps, uId, date) VALUES('" + stepsModel.getSteps() + "','" + uId + "','" + stepsModel.getDate() + "');");
            }
            cr.close();
        }
    }

    public List<ModelSteps> getOldSteps(String uId){
        List<ModelSteps> stepsList = new ArrayList<>();

        database=getReadableDatabase();
        Cursor cr =database.rawQuery("SELECT * FROM "+DB_TABLE1+" WHERE uId = '"+uId+"' ORDER BY date DESC;",null);
        int count = 0;
        while (cr.moveToNext()){
            try{
                String step =cr.getString(1);
                String date = cr.getString(3);
                stepsList.add(new ModelSteps(date,step));
            }catch (Exception e){
                Toast.makeText(ctx, "Error => "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            count++;
            if(count>=5){
                break;
            }
        }

        cr.close();
        return stepsList;
    }

    public void insertSteps(String steps, String uId){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String today = formatter.format(date);

        database=getReadableDatabase();

        Cursor cr =database.rawQuery("SELECT COUNT(*) FROM "+DB_TABLE1+" WHERE uId = '"+uId+"' AND date = '"+today+"';",null);
        cr.moveToNext();

        database=getWritableDatabase();

        if (cr.getString(0).equals("1")){
            database.execSQL("UPDATE "+DB_TABLE1+" SET steps = '"+steps+"' WHERE uId='"+uId+"' AND date ='"+today+"';");
        }else {
            database.execSQL("INSERT INTO " + DB_TABLE1 + "(steps, uId, date) VALUES('" + steps + "','" + uId + "','" + today + "');");
        }
        cr.close();
    }

    public String getSteps(String uId, String date){

        database=getReadableDatabase();
        Cursor cr =database.rawQuery("SELECT * FROM "+DB_TABLE1+" WHERE uId = '"+uId+"' AND date = '"+date+"';",null);

        String step = "0";
        while (cr.moveToNext()){
            try{
                step =cr.getString(1);
            }catch (Exception e){
                Toast.makeText(ctx, "Error => "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        cr.close();
        return step;
    }

    public void insertGoal(String uId, String steps, String calorie){
        database=getReadableDatabase();
        Cursor cr =database.rawQuery("SELECT COUNT(*) FROM "+DB_TABLE2+" WHERE uId = '"+uId+"';",null);
        cr.moveToNext();
        database=getWritableDatabase();
        if (cr.getString(0).equals("1")){
            database.execSQL("UPDATE "+DB_TABLE2+" SET stepGoal='"+steps+"', calorieGoal ='"+calorie+"';");
        }else {
            database.execSQL("INSERT INTO "+DB_TABLE2+"(uId, stepGoal, calorieGoal) VALUES('"+uId+"','"+steps+"','"+calorie+"');");
        }
        cr.close();
    }

    public ModelGoal getGoal(String uId){
        database=getReadableDatabase();
        Cursor cr =database.rawQuery("SELECT * FROM "+DB_TABLE2+" WHERE uId = '"+uId+"';",null);
        String step = "500",calorie = "100";
        boolean isDataAvailable = false;
        while (cr.moveToNext()) {
            step = cr.getString(2);
            calorie = cr.getString(3);
            isDataAvailable = true;
        }
        if (!isDataAvailable){
            insertGoal(uId, step, calorie);
        }
        cr.close();
        return new ModelGoal(step,calorie);
    }

    public ModelUser getUser(String uId){
        ModelUser user = new ModelUser();
        database = getReadableDatabase();
        Cursor cr = database.rawQuery("SELECT * FROM "+DB_USER+" WHERE uId = '"+uId+"'", null);
        while (cr.moveToNext()){
            try {
                user.setuId(uId);
                user.setName(cr.getString(2));
                user.setEmail(cr.getString(3));
                user.setPhone(cr.getString(4));
                user.setPhone(cr.getString(5));
                user.setImage(cr.getString(6));
                user.setRole(cr.getString(7));
                user.setHeight(cr.getString(8));
                user.setGender(cr.getString(9));
                user.setWeight(cr.getString(10));
            }catch (Exception e){
                Toast.makeText(ctx, "Error => "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }//(uId text, name text, email text, phone text, image text, role text, height text, gender text, weight text)
        }
        cr.close();
        return user;
    }

    public void setUserData(ModelUser user){
        database=getReadableDatabase();
        Cursor cr = database.rawQuery("SELECT COUNT(*) FROM "+DB_USER+" WHERE uId='"+user.getuId()+"'",null);
        cr.moveToNext();
        database=getWritableDatabase();
        if (cr.getString(0).equalsIgnoreCase("1")){
            database.execSQL("UPDATE "+DB_USER+" SET name = '"+user.getName()+"', email ='"+user.getEmail()+"', " +
                    "phone ='"+user.getPhone()+"', height ='"+user.getHeight()+"', weight= '"+user.getWeight()+"', gender ='"+user.getGender()+"', " +
                    "role ='"+user.getRole()+"', image ='"+user.getImage()+"' WHERE uId = '"+user.getuId()+"';");
        }else{
            database.execSQL("INSERT INTO USERS(uId, name, email, phone, height, weight, gender, role, image) " +
                    "VALUES('"+user.getuId()+"', '"+user.getName()+"', '"+user.getEmail()+"', '"+user.getPhone()+"', " +
                    "'"+user.getHeight()+"', '"+user.getWeight()+"', '"+user.getGender()+"', '"+user.getRole()+"', '"+user.getImage()+"');");
        }
        cr.close();
    }

    public List<ModelFaq> getFaqList(){
        List<ModelFaq> faqList = new ArrayList<>();
        database = getReadableDatabase();
        Cursor cr = database.rawQuery("SELECT * FROM "+DB_FAQ,null);
        while (cr.moveToNext()){
            try{
                String id = cr.getString(1);
                String question = cr.getString(2);
                String answer = cr.getColumnName(3);
                faqList.add(new ModelFaq(id, question, answer));
            }catch (Exception ignored){}
        }
        cr.close();
        return faqList;
    }

    public void addFaq(List<ModelFaq> faqList){
        database = getWritableDatabase();
        database.execSQL("DELETE FROM "+DB_FAQ);
        for (ModelFaq faq : faqList){
            database.execSQL("INSERT INTO "+DB_FAQ+"(qid, question, answer) VALUES('"+faq.getId()+"','"+faq.getQuestion()+"','"+faq.getAnswer()+"');");
        }
    }


}
