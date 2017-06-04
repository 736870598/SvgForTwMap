package com.sxy.svgfortwmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * Created by Administrator on 2017/6/4/004.
 */
public class SvgMapView extends View {

    //保存颜色信息的数组
    private String[] colorArray;
    //保存path的lis
    private List<ParseDataItem> itemList;
    //缩放倍数
    private float scale = 1f;
    //选中的item
    private ParseDataItem selectItem;
    //画笔
    private Paint paint;
    //触摸管理
    private GestureDetectorCompat gestureDetectorCompat;
    //设置的最小宽高
    private int minW,minH;
    //测量后的控件大小
    private int viewWidth, viewHeight;

    public SvgMapView(Context context) {
        this(context, null);
    }

    public SvgMapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SvgMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        colorArray = context.getApplicationContext().getResources().getStringArray(R.array.map_color1);
        itemList = new ArrayList();
        paint = new Paint();
        paint.setAntiAlias(true);

        minW = getResources().getDimensionPixelSize(R.dimen.min_w);
        minH = getResources().getDimensionPixelSize(R.dimen.min_h);

        gestureDetectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                handlerTouch(e.getX(), e.getY());
                return true;
            }
        });

        loadDataThread.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int heigthMode = MeasureSpec.getMode(heightMeasureSpec);
        int heigth = MeasureSpec.getSize(heightMeasureSpec);

        switch (widthMode){
            case MeasureSpec.EXACTLY : //布局中固定dp 或match
                viewWidth = width>minW?width:minW;
                break;
            case MeasureSpec.AT_MOST:          //wrap
            case MeasureSpec.UNSPECIFIED:
                viewWidth = minW;
                break;
        }

        switch (heigthMode){
            case MeasureSpec.EXACTLY : //布局中固定dp 或match
                viewHeight = heigth>minH?heigth:minH;
                break;
            case MeasureSpec.AT_MOST:          //wrap
            case MeasureSpec.UNSPECIFIED:
                viewHeight = minH;
                break;
        }

//        setMeasuredDimension((viewWidth),(viewHeight));
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(itemList != null){
            canvas.save();

            canvas.scale(scale, scale);

            for (ParseDataItem dataItem : itemList){
                //绘制item
                dataItem.draw(canvas, paint, dataItem == selectItem);
            }
        }
    }

    /**
     * 触摸事件，将触摸事件交给gestureDetectorCompat处理
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }

    /**
     * 处理触摸事件
     * @param x
     * @param y
     */
    private void handlerTouch(float x, float y) {
        if(itemList != null){
            for (ParseDataItem dataItem : itemList){
                if( dataItem.isTouch((int)(x/scale),(int)(y/scale)) ){
                    if(dataItem != selectItem){
                        selectItem = dataItem;
                        postInvalidate();
                    }
                    break;
                }
            }
        }
    }


    /**
     * 线程解析xml数据
     */
    Thread loadDataThread = new Thread(){
        @Override
        public void run() {
            super.run();

            try {

                float left=0, top=0, right=0, bottom=0;

                //解析xml
                InputStream inputStream = getResources().openRawResource(R.raw.tw);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(inputStream);
                Element rootElement = doc.getDocumentElement();
                //得到所有的path
                NodeList items = rootElement.getElementsByTagName("path");

                for (int i = 0; i < items.getLength(); i++){
                    Element element = (Element) items.item(i);
                    //获取path标签下的内容
                    String pathData = element.getAttribute("android:pathData");
                    Path path = PathParser.createPathFromPathData(pathData);

                    //封装为ParseDataItem
                    ParseDataItem dataItem = new ParseDataItem(path,
                            Color.parseColor(colorArray[i % colorArray.length]));
                    itemList.add(dataItem);

                    //获取上下左右四个临界值
                    RectF rectF = dataItem.getRectF();
                    left = (left == 0 ? rectF.left : Math.min(rectF.left, left));
                    top = (top == 0 ? rectF.top : Math.min(rectF.top, top));
                    right = (right == 0 ? rectF.right : Math.max(rectF.right, right));
                    bottom = (bottom == 0 ? rectF.bottom : Math.max(rectF.bottom, bottom));
                }

                //循环之后算出合适的缩放比例
                scale = Math.min(viewWidth/(right-left), viewHeight/(bottom-top));

                postInvalidate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}
