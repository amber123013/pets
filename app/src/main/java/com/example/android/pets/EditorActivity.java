
package com.example.android.pets;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    //此变量 标志 是否对编辑框进行了修改
    private boolean mPetHasChanged = false;

    private static final int EXISTING_PET_LOADER = 0;

    /** uri指向一个现有的宠物，如果为空则为添加一个新的宠物 */
    private Uri mCurrentPetUri;

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

    //监听事件如果点击了编辑框则 置mPetHasChanged为true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // 找到输入的 view
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mBreedEditText = (EditText) findViewById(R.id.edit_pet_breed);
        mWeightEditText = (EditText) findViewById(R.id.edit_pet_weight);
        mGenderSpinner = (Spinner) findViewById(R.id.spinner_gender);

        //设置OnTouch监听
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        // 根据uri 判断是编辑还是 新增
        Intent intent = getIntent();
        mCurrentPetUri = intent.getData();

        // 如果为空 添加一个
        if (mCurrentPetUri == null) {
            // 设置标题
            setTitle(getString(R.string.editor_activity_title_new_pet));

            // 隐藏删除菜单选项
            /**
             * 执行invalidateOptionsMenu()方法
             * 系统将调用onPrepareOptionsMenu()
             */
            invalidateOptionsMenu();
        } else {
            // 设置标题
            setTitle(getString(R.string.editor_activity_title_edit_pet));

            // 初始化加载程序读取数据库中的数据
            //并在编辑器中显示当前值
            //此时onLoadFinished 获得Cursor中只包含单个宠物信息
            getLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //如果是新建则隐藏删除选项
        if (mCurrentPetUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    /**
     * 显示一个对话框，选择是否删除
     */
    private void showDeleteConfirmationDialog() {
        // 创建一个alertDialog 让用户选择是否确认删除
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 用户选择删除
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 用户选择取消，关闭对话框
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //从数据库中删除宠物
    private void deletePet() {
        if (mCurrentPetUri != null) {
            // 调用ContentResolver使用URI删除宠物
            int rowsDeleted = getContentResolver().delete(mCurrentPetUri, null, null);

            if (rowsDeleted == 0) {
                // 如果没有数据被删除
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // 提示成功
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();

        }
    }
    /**
     * 显示一个对话框，提醒有未保存的更改
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // 弹框让用户选择是否继续
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("放弃更改并退出编辑？");
        builder.setPositiveButton("放弃", discardButtonClickListener);
        builder.setNegativeButton("继续编辑", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //选择了继续编辑
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // 创建并显示
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //此方法覆盖了 手机的返回按钮
    @Override
    public void onBackPressed() {
        // 如果用户没有编辑 执行正常逻辑
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        // 否则，如果有未保存的更改，设置对话框警告用户。
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 用户选择了放弃
                        finish();
                    }
                };

        // 显示对话框
        showUnsavedChangesDialog(discardButtonClickListener);
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

    private void savePet() {
        // 读取输入的值插入到数据库
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        //体重设为0
        int weight = 0;
        //如果体重不为空，转化为int 否则默认为零
        if (!TextUtils.isEmpty(weightString)) {
            weight = Integer.parseInt(weightString);
        }

        if (mCurrentPetUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) &&
                TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
            // 没有一个数据被修改 直接返回不执行savePet操作
            return;
        }
        // 创建ContentValues 用于放置键对值
        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, nameString);
        values.put(PetEntry.COLUMN_PET_BREED, breedString);
        values.put(PetEntry.COLUMN_PET_GENDER, mGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        if (mCurrentPetUri == null) { //新添一个宠物
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
        } else { //更新一个宠物
            int rowsAffected = getContentResolver().update(mCurrentPetUri, values, null, null);

            // 根据返回的 int 值 显示提示消息
            if (rowsAffected == 0) {
                // 显示失败消息
                Toast.makeText(this, "宠物信息更新失败",
                        Toast.LENGTH_SHORT).show();
            } else {
                // 显示成功消息
                Toast.makeText(this, "宠物信息更新成功",
                        Toast.LENGTH_SHORT).show();
            }
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
                savePet();
                //退出EditorActivity
                finish();
                return true;
            // 选择删除
            case R.id.action_delete:
                //弹出对话框
                showDeleteConfirmationDialog();
                return true;
            // 放回上一个界面
            case android.R.id.home:
                // 返回父Activity (CatalogActivity)
                if (!mPetHasChanged) {//无修改
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // 有创建对话框
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //用户选择放弃.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // 开启对话框
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };

        //原理与CatalogActivity中相同
        return new CursorLoader(this,
                mCurrentPetUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // 没有数据的话
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // 获得第一行数据（只有一行
        if (cursor.moveToFirst()) {
            // 获取字段对应的column
            int nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);

            // 根据column索引获取内容
            String name = cursor.getString(nameColumnIndex);
            String breed = cursor.getString(breedColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            int weight = cursor.getInt(weightColumnIndex);

            // 设置数据
            mNameEditText.setText(name);
            mBreedEditText.setText(breed);
            mWeightEditText.setText(Integer.toString(weight));

            // 设置下拉框
            switch (gender) {
                case PetEntry.GENDER_MALE:
                    mGenderSpinner.setSelection(1);
                    break;
                case PetEntry.GENDER_FEMALE:
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
        }
    }
    //loader 被重置从而使其数据无效时调用
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //重置数据
        mNameEditText.setText("");
        mBreedEditText.setText("");
        mWeightEditText.setText("");
        mGenderSpinner.setSelection(0);
    }
}