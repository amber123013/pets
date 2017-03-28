package com.example.android.pets.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ASUS on 2017-03-27.
 */

/**宠物数据库（合约类*/
public final class PetContract {
    private PetContract() {

    }

    /**与Mainifest中定义的一致*/
    public static final String CONTENT_AUTHORITY = "com.example.android.pets";

    /**
     *content:// scheme + content_authority
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * 其中一个可能路径
     * 这里是pets表
     */
    public static final String PATH_PETS = "pets";

    /**每一个内部类表示pets数据库中的一个表*/
    public static final class PetEntry implements BaseColumns {
        /**
         * CURSOR_DIR_BASE_TYPE（映射到常数“vnd.android.cursor.dir”）
         * CURSOR_ITEM_BASE_TYPE（映射到常数“vnd.android.cursor.item”）
         */
        /**
         * mime类型 为宠物列标
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;

        /**
         * mime类型 为单个宠物
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PETS;


        /** 访问provider 中数据 使用的Uri */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PETS);

        /** pets数据库表的名称 */
        public final static String TABLE_NAME = "pets";

        /**
         * 宠物的id(仅用在表中).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * 宠物的名称
         * Type: TEXT
         */
        public final static String COLUMN_PET_NAME ="name";

        /**
         * 宠物的品种
         *
         * Type: TEXT
         */
        public final static String COLUMN_PET_BREED = "breed";

        /**
         * 宠物的性别 值是以下三个
         * The only possible values are {@link #GENDER_UNKNOWN}, {@link #GENDER_MALE},
         * or {@link #GENDER_FEMALE}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PET_GENDER = "gender";

        /**
         * 宠物的体重
         * Type: INTEGER
         */
        public final static String COLUMN_PET_WEIGHT = "weight";

        /**
         * 宠物的可能性别
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

        /**是否存在给定的性别*/
        public static boolean isValidGender(int gender) {
            if (gender == GENDER_UNKNOWN || gender == GENDER_MALE || gender == GENDER_FEMALE) {
                return true;
            }
            return false;
        }
    }
}
