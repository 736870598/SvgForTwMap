package com.sxy.svgfortwmap;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * Created by Administrator on 2017/6/4/004.
 */
public class ParseDataItem {

    //路径
    private Path path;

    //绘制颜色
    private int drawColor;

    //Region
    private Region region;

    private  RectF rectF;

    public ParseDataItem(Path path) {
        this.path = path;
    }

    public ParseDataItem(Path path, int drawColor) {
        this.path = path;
        this.drawColor = drawColor;
        initRegion();
    }

    private void initRegion(){
        rectF = new RectF();
        path.computeBounds(rectF, true);
        region = new Region();
        region.setPath(path,
                new Region((int)rectF.left, (int)rectF.top, (int)rectF.right, (int)rectF.bottom));
    }

    public RectF getRectF() {
        return rectF;
    }

    /**
     * 绘制
     * @param canvas
     * @param paint
     * @param isSelect  是否被选择
     */
    public void draw(Canvas canvas, Paint paint, boolean isSelect){

        //绘制地图
        paint.setColor(drawColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);

        if(isSelect){
            //绘制背景
            paint.setStrokeWidth(2);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
        }else{
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.DKGRAY);
            canvas.drawPath(path, paint);
        }

    }

    /**
     * 是否坐落在path上
     */
    public boolean isTouch(int x, int y){
        return region.contains(x, y);
    }

}
