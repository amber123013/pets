package com.example.android.pets.data;

import android.provider.BaseColumns;

/**
 * Created by ASUS on 2017-03-27.
 */

/**宠物数据库（合约类*/
public final class PetContract {
    private PetContract() {

    }

    /**每一个内部类表示pets数据库中的一个表*/
    public static final class PetEntry implements BaseColumns {
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
         * P从屋的可能性别
         * P从屋的可能性别
         */
        public static final int GENDER_UNKNOWN = 0;
        public static final int GENDER_MALE = 1;
        public static final int GENDER_FEMALE = 2;

    }
}
