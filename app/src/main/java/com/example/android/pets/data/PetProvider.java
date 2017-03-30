package com.example.android.pets.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.pets.data.PetContract.PetEntry;

/**
 * Created by ASUS on 2017-03-28.
 * 宠物应用的Provider
 */

public class PetProvider extends ContentProvider {

    //uriMatcher
    /**查询宠物表格的URI matcher code*/
    private static final int PETS = 100;
    /**查询宠物表格中单个宠物的URI matcher code*/
    private static final int PET_ID = 101;

    /**urimatcher用于匹配contentURI
     *到相应的 URI matcher code
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    //静态代码块 只初始化一次
    static {
        /**uri 及其对应的matcher code*/
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS + "/#",PET_ID);
    }

    /** tag */
    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    private PetDBHelper mDBHelper;

    /**
     * 初始化Provider 和数据库辅助对象PetDBHelper
     * Android 系统会在创建提供程序后立即调用此方法。
     * ContentResolver 对象尝试访问提供程序时，系统才会创建它
     */
    @Override
    public boolean onCreate() {
        // 初始化一个 PetDBHelper 对象访问数据库
        mDBHelper = new PetDBHelper(getContext());
        return true;
    }

    /**
     * 执行给定的URI查询使用给定的 projection, selection, selection arguments和 sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // 获得readable database
        SQLiteDatabase database = mDBHelper.getReadableDatabase();

        // 保存查询的返回值
        Cursor cursor = null;

        // 找出匹配的uri  code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                /**查询宠物列表
                 * projection 返回的列
                 */
                cursor  = database.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case PET_ID:
                // 从uri获取要查询的id
                // 下面两句解析相当于 where _ID = 5
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // 执行查询
                cursor = database.query(PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        /**在cursor上设置通知uri
         *当此uri指示的数据发生变化时 就能得知需要更新cursor
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * 使用contentValues插入新的内容
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    //插入数据
    private Uri insertPet(Uri uri,ContentValues values) {
        /**
         * 插入之前 对数据进行完整性检查
         */
        //名字
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }
        //性别
        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        //体重
        //体重可以为空，如不提供 数据库默认设为0kg
        //如不为空 则 必须确保重量大于等于 0kg
        Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        /**
         * 数据验证通过，执行插入操作
         */

        // 创建或者打开数据库 读取
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        //插入成功 返回id号
        long id = db.insert(PetContract.PetEntry.TABLE_NAME, null, values);
        //返回添加id的uri
        if (id == -1) { //添加失败
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            //返回空的uri
            return null;
        }
        //插入成功 表格已被修改
        // 通知监听器 数据已经被修改
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * 使用给定的内容更新数据
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePet(uri, contentValues, selection, selectionArgs);
            case PET_ID:
                //根据uri获取要修改的行id
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    //更新数据
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //如果不存在参数 return 0
        if (values.size() == 0) {
            return 0;
        }

        //是否包含宠物名 -- 名字是否为空
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        // 是否包含性别 性别是否有效
        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetEntry.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        // 是否包含重量 值是否有意义
        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
        }

        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        //受影响的行数
        int rowsUpdated = database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
        //表格被update 通知监听器 数据已经被修改
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // 返回受影响的行数
        return rowsUpdated;

    }

    /**
     * 删除内容 使用给定的 selection and selectionArgs
     *
     * URI: content://com.example.android.pets/pets
     *Selection: “breed=?”
     *SelectionArgs: { “Calico” }
     *==DELETE pets WHERE breed= ‘Calico’
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case PETS:
                // 删除所有宠物
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PET_ID:
                // 根据id 删除单个宠物
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                //删除一个宠物
                rowsDeleted = database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        //有数据被删除
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // 返回被删除的行数
        return rowsDeleted;
    }

    /**
     * 返回内容 URI 对应的 MIME 类型
     * content://com.example.android.pets/pets → Returns directory MIME type 数据目录
     * content://com.example.android.pets/pets/# → Returns item MIME type 单个数据行即单个数据项
     * MIME 类型字符串按约定以“vnd.android.cursor…”开头，后面跟宠物内容主机名，以及数据路径
     * 目录 MIME 类型： vnd.android.cursor.dir/com.example.android.pet/pet
     * 项 MIME 类型： vnd.android.cursor.item/com.example.android.pet/pets
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PET_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
