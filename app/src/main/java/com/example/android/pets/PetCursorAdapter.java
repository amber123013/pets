package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

/**
 * Created by ASUS on 2017-03-29.
 */

/**
 * CursorAdapter 使用cursor作为数据源
 * 为listview提供列表项视图
 */
public class PetCursorAdapter extends CursorAdapter {

    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    /**
     *新建一个空的视图，上面没有数据
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    /**
     * 将Cursor中当前行的数据绑定到列表项视图
     * 如 设置text的值
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // 查找要修改的视图
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

        // 查找 需要的宠物属性所在的列
        int nameColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);

        // 从cursor获取宠物属性
        String petName = cursor.getString(nameColumnIndex);
        String petBreed = cursor.getString(breedColumnIndex);
        // 如果品种为空 设置为 未知品种
        if (TextUtils.isEmpty(petBreed)) {
            petBreed = context.getString(R.string.unknown_breed);
        }

        // 更新textview上的值
        nameTextView.setText(petName);
        summaryTextView.setText(petBreed);
    }
}
