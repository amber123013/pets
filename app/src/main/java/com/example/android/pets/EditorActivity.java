
package com.example.android.pets;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.pets.data.PetContract.PetEntry;

/**
 * 允许创建一个或者编辑现有的宠物
 */
public class EditorActivity extends AppCompatActivity {

    /** 宠物名输入框 */
    private EditText mNameEditText;

    /** 宠物品种输入框 */
    private EditText mBreedEditText;

    /** 宠物重量输入框 */
    private EditText mWeightEditText;

    /** 宠物性别选择 Spinner */
    private Spinner mGenderSpinner;

    /**
     * 存储了宠物性别的值
     */
    private int mGender = PetEntry.GENDER_UNKNOWN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // 找到输入的 view
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        setupSpinner();
    }

    /**
     * 设置性别下拉框
     */
    private void setupSpinner() {
        // 创建一个适配器 使用字符串数组 并使用默认布局
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // 添加适配器
        mGenderSpinner.setAdapter(genderSpinnerAdapter);


        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = PetEntry.GENDER_MALE; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = PetEntry.GENDER_FEMALE; // Female
                    } else {
                        mGender = PetEntry.GENDER_UNKNOWN; // Unknown
                    }
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    private void insertPet() {
        // 读取输入的值插入到数据库
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        int weight = Integer.parseInt(weightString);

        // 创建ContentValues 用于放置键对值
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        //向数据库中插入一行 (放回一个uri
        Uri newUri = getContentResolver().insert(PetEntry.CONTENT_URI, values);
        if (newUri == null) {
            // 如果uri为空 说明插入失败
            Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // 否则插入成功
            Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate   menu   res/menu/menu_editor.xml file.
        //这将菜单项添加到应用程序栏
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // 选择保存
            case R.id.action_save:
                // Do nothing for now
                insertPet();
                //退出EditorActivity
                finish();
                return true;
            // 选择删除
            case R.id.action_delete:
                // Do nothing for now
                return true;
            // 放回上一个界面
            case android.R.id.home:
                // 返回父Activity (CatalogActivity)
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}