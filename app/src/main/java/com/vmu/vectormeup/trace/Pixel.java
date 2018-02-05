package com.vmu.vectormeup.trace;

/**
 * Created by daniel on 03/12/17.
 */

public class Pixel {
//    public enum Code{
//        INNER,OUTER,INNER_OUTER,STRAIGHT
//    }
    private int x;
    private int y;
    private int type = -1;
    private int color;
    private float dx=0;
    private float dy=0;
    private int index;

    public Pixel(int x, int y){
        this.x = x;
        this.y = y;
        this.color = -1;
    }

    public Pixel(int x, int y, int color){
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Pixel(int x, int y, int color, int type){
        this.x = x;
        this.y = y;
        this.type = type;
        this.color = color;
    }

    public Pixel(int x, int y, int color, int type,int index){
        this.x = x;
        this.y = y;
        this.type = type;
        this.color = color;
        this.index = index;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getType(){
        return type;
    }

    public int getColor(){
        return color;
    }

    public int getIndex(){ return index; }

    public int getIndex(int imgWidth) { return x + imgWidth*y; }

    public void setCode(int type){
        this.type = type;
    }

    public void setColor(int color){
        this.color = color;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public void setDifferentials(float dx,float dy){
        this.dx = dx;
        this.dy = dy;
    }

    public float getDx(){
        return dx;
    }

    public float getDy(){
        return dy;
    }


    public void setPosition(int x, int y){
        setX(x);
        setY(y);
    }
}
