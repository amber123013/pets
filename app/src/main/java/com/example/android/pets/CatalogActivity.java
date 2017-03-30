
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * 显示 宠物列表
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{


    /** 用于loader的标示符 只要数字是唯一就行 */
    private static final int PET_LOADER = 0;

    /** listview的Adapter */
    PetCursorAdapter mCursorAdapter;

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

         ListView petListView = (ListView) findViewById(R.id.list);
        //无数据 显示的视图
         View emptyView = findViewById(R.id.empty_view);
         petListView.setEmptyView(emptyView);
        //设置数据
         mCursorAdapter = new PetCursorAdapter(this, null);
         petListView.setAdapter(mCursorAdapter);
         //设置ListView列表项监听
         petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // new intent
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // 将 id append 到 uri
                Uri currentPetUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);

                // 让intent携带 至 DditorActivity
                intent.setData(currentPetUri);

                // 启动编辑界面
                startActivity(intent);
            }
        });

         // 启动loader
         getLoaderManager().initLoader(PET_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate  res/menu/menu_catalog.xml file.
        // 添加应用程序菜单栏
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

/**
    private void displayDatabaseInfo() {

        // 创建或者打开数据库 读取
//        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        // 执行查询语句
        // 获取 所要数据 （以Cursor返回
        //查询的数据
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };
    /**
        Cursor cursor = db.query(
                PetEntry.TABLE_NAME,   // 查询的表名
                projection,            // 返回的列
                null,                  // where子句的列
                null,                  // where子句的值
                null,                  // Don't group the rows
                null,                  // 不按行分组
                null);                   // 排序

        Cursor cursor = getContentResolver().query(PetEntry.CONTENT_URI,projection,null,null,null);
        /**设置数据
        ListView petListView = (ListView) findViewById(R.id.list);
        PetCursorAdapter petCursorAdapter = new PetCursorAdapter(this, cursor);
        petListView.setAdapter(petCursorAdapter);
        View emptyView = findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);


    }
*/
    private void insertPet() {
        // 创建一个ContentValues 存放键对值
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, "Toto");
        values.put(PetEntry.COLUMN_PET_BREED, "Terrier");
        values.put(PetEntry.COLUMN_PET_GENDER, PetEntry.GENDER_MALE);
        values.put(PetEntry.COLUMN_PET_WEIGHT, 7);

        // 使用contentresolver用于添加数据 它调用的应是PetProvider中德insert方法（根据uri）
        //返回的是一个uri
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
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
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 删除所有数据
     */
    private void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from pet database");
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // 查询的字段
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        // 在后台线程执行Provider的query方法
        //后台线程只CursorLoader 它继承自AsyncTaskLoader（实现了Runnable）
        //这里实现的三个抽象方法 都是回调函数执行在主线程
        // （Activiity调用CursorLoader中的方法，CursorLoader再调用Activity中的方法）
        //如 数据发生更新时 调用 onLoadFinished 在ui线程完成界面数据更新
        return new CursorLoader(this,
                PetEntry.CONTENT_URI, //执行Provider.query 方法时使用的uri
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // 更新 {@link PetCursorAdapter} 包含新的数据
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // 当删除数据时调用
        //设为空 再设置新的数据
        mCursorAdapter.swapCursor(null);
    }
}
