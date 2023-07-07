package com.example.newland.pintu_game;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private Bitmap bitmap;
    private GridView gridView;
    private int gridSize = 3; // 拼图的行列数
    public ImageView[] imageViews;
    private int[] positions; // 拼图碎片位置数组
    private int blankPosition; // 空白碎片位置
    private int wid_sum=0,hei_sum=0;
    public int width,height;
    public float bate=0f;
    boolean isFinish=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        imageView=findViewById(R.id.imageView);
        gridView=findViewById(R.id.gridview);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        height = (int) (displayMetrics.heightPixels / displayMetrics.density);
        width = (int) ( displayMetrics.widthPixels / displayMetrics.density);
        System.out.println("height:"+height+"---width:"+width);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//初始化选项菜单

        menu.add(1, 1, 1, "拍照上传图片");
        menu.add(1, 2, 2, "开始3*3拼图");
        menu.add(1, 3, 3, "开始4*4拼图");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {//选项回掉事件
        switch (item.getItemId()) {
            case 1:
                takephoto();
                break;
            case 2:
                if(bitmap!=null)
                {
                    isFinish=false;
                    play(3);
                }
                else
                {
                    Toast.makeText(this, "请先拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            case 3:
                if(bitmap!=null)
                {
                    isFinish=false;
                    play(4);
                }
                else
                {
                    Toast.makeText(this, "请先拍照", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void play(int index) {
        gridSize=index;
        gridView.setNumColumns(gridSize);
        gridView.setColumnWidth(bitmap.getWidth()/gridSize);
        imageViews=new ImageView[gridSize*gridSize];
        wid_sum=bitmap.getWidth();
        hei_sum=bitmap.getHeight();
        positions=new int[gridSize*gridSize];
        bate=bitmap.getWidth()*1.0f/bitmap.getHeight();
        for(int i=0;i<positions.length;i++)
        {
            positions[i]=i;//初始化初始坐标顺序
        }

        shufflePositions(); // 打乱位置数组

        show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(isFinish)
                {
                    Toast.makeText(MainActivity.this, "换个玩法再开始吧！", Toast.LENGTH_SHORT).show();
                    return;
                }
                int position = i; // 获取在拼图布局中的位置
                System.out.println("move position="+position);
                if (canMove(position)) {
                    // 如果可以移动
                    swapPositions(position, blankPosition); // 交换位置数组中的两个位置
                    show();
                    if(isGameOver()) {
                        isFinish = true;
                        Toast.makeText(MainActivity.this, "恭喜你！成功啦！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void show() {
        List<Bitmap> imageList = new ArrayList<>();
        for(int i=0;i<positions.length;i++)
        {
            imageList.add(getBitmapByPosition(positions[i]));
        }
        MyAdapter myAdapter = new MyAdapter(this, imageList,width,height,bate,gridSize);
        gridView.setAdapter(myAdapter);
    }

    private void takephoto() {//调用相机
        // 检查摄像头权限是否已授权
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // 如果权限尚未授权，则向用户显示一个解释权限用途的对话框
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toast.makeText(this, "给予相机权限以完成任务", Toast.LENGTH_SHORT).show();
            }
            // 请求摄像头权限
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, 2023);
        } else {
            // 已经授权，可以开始使用摄像头
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {//权限请求回调函数
        if (requestCode == requestCode) {
            // 检查摄像头权限是否已授权
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 摄像头权限已授权，可以开始使用摄像头
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, "相机权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 将拍摄的照片保存到 Bitmap 中
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");

            // 在 ImageView 中显示拍摄的照片
            imageView.setImageBitmap(bitmap);

        }
    }


    // 根据位置获取拼图碎片位图
    private Bitmap getBitmapByPosition(int position) {
        for (int i = 0; i < positions.length; i++) {
            if(positions[i]==positions.length-1)
            {
                blankPosition=i;//Find blank
            }
        }

        int row = position / gridSize; // 行号
        int column = position % gridSize; // 列号

        int itemWidth = wid_sum / gridSize; // 拼图碎片宽度
        int itemHeight = hei_sum / gridSize; // 拼图碎片高度

        Bitmap itemBitmap = Bitmap.createBitmap(bitmap, column * itemWidth, row * itemHeight, itemWidth, itemHeight); // 根据位置裁剪图片位图

        if (position == positions.length - 1) {
            // 如果是最后一个位置，将图片位图设置为透明，作为空白碎片
            itemBitmap = Bitmap.createBitmap(itemWidth, itemHeight, Bitmap.Config.ARGB_8888);
            itemBitmap.eraseColor(Color.TRANSPARENT);
        }

        return itemBitmap; // 返回拼图碎片位图
    }

    private void shufflePositions() {
        Random random = new Random();

        // 打乱除了最后一个位置之外的所有位置
        for (int i = 0; i < positions.length - 1; i++) {
            int j = random.nextInt(i + 1); // 随机生成一个位置下标，范围是 [0, i]
            swapPositions(i, j); // 交换两个位置
        }

        if (!isSolvable()) {
            // 如果没有解，则交换最后两个位置
            swapPositions(positions.length - 3, positions.length - 2);
        }
    }

    // 判断拼图是否有解
    private boolean isSolvable() {
        int inversionCount = getInversionCount(); // 获取拼图的逆序数
        int blankRow = positions[positions.length - 1] / gridSize; // 获取空白格所在行号（从上往下数）

        if (gridSize % 2 == 1) {
            // 如果列数是奇数
            return inversionCount % 2 == 0; // 只有当逆序数是偶数时，才有解
        } else {
            // 如果列数是偶数
            return (inversionCount + gridSize - blankRow) % 2 == 1; // 只有当逆序数加上空白格所在行号（从下往上数）之和是奇数时，才有解
        }
    }

    // 获取拼图的逆序数
    private int getInversionCount() {
        int inversionCount = 0; // 初始化逆序数为0
        for (int i = 0; i < positions.length - 1; i++) {
            // 遍历除了最后一个位置之外的所有位置
            for (int j = i + 1; j < positions.length; j++) {
                // 遍历从i+1开始到最后一个位置之间的所有位置
                if (positions[i] != positions.length - 1 && positions[j] != positions.length - 1 && positions[i] > positions[j]) {
                    // 如果两个位置都不是空白格，并且前面的位置大于后面的位置
                    inversionCount++; // 逆序数加一
                }
            }
        }
        return inversionCount; // 返回逆序数
    }

    // 交换位置数组中的两个位置
    private void swapPositions ( int position1, int position2){
        int temp = positions[position1];
        positions[position1] = positions[position2];
        positions[position2] = temp;
    }


    // 判断位置是否可以移动
    private boolean canMove(int position){
        int row = position / gridSize; // 行号
        int column = position % gridSize; // 列号

        int blankRow = blankPosition / gridSize; // 空白位置行号
        int blankColumn = blankPosition % gridSize; // 空白位置列号

        if (row == blankRow && Math.abs(column - blankColumn) == 1) {
            // 如果在同一行并且相邻列，可以移动
            return true;
        }

        if (column == blankColumn && Math.abs(row - blankRow) == 1) {
            // 如果在同一列并且相邻行，可以移动
            return true;
        }

        return false; // 否则不可以移动
    }

    // 判断游戏是否结束
    private boolean isGameOver () {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i] != i) {
                // 如果有一个位置不正确，游戏没有结束
                return false;
            }
        }
        return true; // 否则游戏结束
    }
}
