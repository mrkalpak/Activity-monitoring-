package com.example.finalyearproject_android.DietPlan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.finalyearproject_android.DietSug.Food;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DietDatabase extends SQLiteOpenHelper {
    Context ctx;
    SQLiteDatabase database;
    static final  private String DB_NAME="AiBasedActivityMonitoring1";
    static final private int DB_VERSION=3;
    private static final String TABLE_1 = "UserDietPlan";
    private static final String TABLE_2 = "FoodItemList";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FOOD_NAME = "foodName";
    private static final String COLUMN_CALORIES = "calories";
    private static final String COLUMN_SERVING_SIZE = "servingSize";
    private static final String COLUMN_QTY = "qty";
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_1);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_2);
        String createTableQuery = "CREATE TABLE " + TABLE_1 + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FOOD_NAME + " TEXT, " +
                COLUMN_CALORIES + " INTEGER, " +
                COLUMN_SERVING_SIZE + " TEXT, " +
                COLUMN_QTY + " INTEGER, " +
                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createTableQuery);

        db.execSQL("CREATE TABLE " + TABLE_2 + "(_id integer primary key autoincrement, name text, calories text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public DietDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.ctx = context;
    }

//    User Diet Methods
// Modify the addDiet method to include the timestamp
public void addDiet(Food food) {
    database = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COLUMN_FOOD_NAME, food.getFoodName());
    values.put(COLUMN_CALORIES, food.getCalories());
    values.put(COLUMN_SERVING_SIZE, food.getServingSize());
    values.put(COLUMN_QTY, food.getQty());

    // Add the timestamp to the ContentValues
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    String currentDate = sdf.format(new Date());
    values.put("timestamp", currentDate);


    database.insert(TABLE_1, null, values);
    database.close();
    System.out.println("Added diet");
}


    public void removeDiet(Food diet){
        database = getWritableDatabase();
        try {
            database.delete(TABLE_1, COLUMN_FOOD_NAME + " = ?", new String[]{String.valueOf(diet.getFoodName())});
        }catch (Exception ignored){}
    }

    public List<Food> getDiet() {
        List<Food> foodList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_1 + " WHERE date(timestamp) = date('now')";
        SQLiteDatabase db = this.getReadableDatabase();
        onCreate(db);
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Food food = new Food(
                        cursor.getString(cursor.getColumnIndex(COLUMN_FOOD_NAME)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_CALORIES)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_SERVING_SIZE)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_QTY))
                );
                foodList.add(food);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return foodList;
    }


//    Saved Food Methods

    public void addFood(List<ModelFood> foodList){
        database = getWritableDatabase();
        onCreate(database);

        for (ModelFood food:foodList){
            database.execSQL("INSERT INTO "+TABLE_2+"(name, calories) VALUES('"+food.getName()+"','"+food.getCalories()+"')");
        }
    }

    public List<ModelFood> getFoodList(){
        List<ModelFood> foodList = new ArrayList<>();
        database = getReadableDatabase();

        Cursor cr = database.rawQuery("SELECT * FROM "+TABLE_2,null);
        while (cr.moveToNext()){
            String name = cr.getString(1);
            String calories = cr.getString(2);
            foodList.add(new ModelFood(name, calories));
        }
        cr.close();

        return foodList;
    }
}
