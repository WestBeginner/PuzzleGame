package com.example.newland.pintu_game;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.List;

public class MyAdapter extends BaseAdapter {
    private Context context;
    private List<Bitmap> imageList;
    private int width,height;
    private float bate;
    private int gridSize;

    public MyAdapter(Context context, List<Bitmap> imageList, int width, int height, float bate, int gridSize) {
        this.context = context;
        this.imageList = imageList;
        this.width=width;
        this.height=height;
        this.bate=bate;
        this.gridSize=gridSize;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object getItem(int position) {
        return imageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if (convertView == null) {
            // 如果没有可复用的视图，则创建新的 ImageView
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(change(width/gridSize-1), change((int) (width/gridSize*1.0/bate))));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            // 否则，使用可复用的视图
            imageView = (ImageView) convertView;
        }

        // 设置 ImageView 的图片
        imageView.setImageBitmap(imageList.get(position));

        return imageView;
    }
    private int change(int i) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(i * density);
    }
}
