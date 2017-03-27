
package com.example.android.pets;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.android.pets.data.PetContract.PetEntry;
import com.example.android.pets.data.PetDBHelper;

/**
 * 显示 宠物列表
 */
public class CatalogActivity extends AppCompatActivity {
    //PetDBHelper 实例
    private PetDBHelper mDBHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // 点击fab打开 EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        mDBHelper = new PetDBHelper(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate  res/menu/menu_catalog.xml file.
        // 添加应用程序菜单栏
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    private void insertPet() {

        // 创建或者打开数据库 读取
        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Garfield");
        values.put(PetEntry.COLUMN_PET_BREED, "Tabby");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);
        db.insert(PetEntry.TABLE_NAME, null, values);
        displayDatabaseInfo();
    }
    private void displayDatabaseInfo() {

        // 创建或者打开数据库 读取
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // 执行查询语句
        // 获取 所要数据 （以Cursor返回
        //查询的数据
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };

        Cursor cursor = db.query(
                PetEntry.TABLE_NAME,   // 查询的表名
                projection,            // 返回的列
                null,                  // where子句的列
                null,                  // where子句的值
                null,                  // Don't group the rows
                null,                  // 不按行分组
                null);                   // 排序
        TextView displayView = (TextView) findViewById(R.id.text_view_pet);
        try {
            // 将cursor之中的数据放在 textview 上
            displayView.setText("The pets table contains " + cursor.getCount() + " pets.\n\n");
            displayView.append(PetEntry._ID + " - " +
                    PetEntry.COLUMN_PET_NAME + " - " +
                    PetEntry.COLUMN_PET_BREED + " - " +
                    PetEntry.COLUMN_PET_GENDER + " - " +
                    PetEntry.COLUMN_PET_WEIGHT + "\n");

            // 得到面对列对应的索引
            int idColumnIndex = cursor.getColumnIndex(PetEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // 遍历所有的行 默认位置 为 -1 取到无数据时返回 false
            while (cursor.moveToNext()) {
                // 在当前的position取出 int or String
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                String currentBreed = cursor.getString(breedColumnIndex);
                int currentGender = cursor.getInt(genderColumnIndex);
                int currentWeight = cursor.getInt(weightColumnIndex);
                // 添加到 textview显示
                displayView.append(("\n" + currentID + " - " +
                        currentName + " - " +
                        currentBreed + " - " +
                        currentGender + " - " +
                        currentWeight));
            }
        } finally {
            // 使用完后释放资源 ，防止发生资源泄露
            cursor.close();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 用户点击菜单栏上的溢出菜单
        switch (item.getItemId()) {
            // 点击了插入数据
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // 点击了删除数据
            case R.id.action_delete_all_entries:
                // Do nothing for now
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
