package com.vmu.vectormeup.trace;

/**
 * Created by daniel on 03/12/17.
 */

public class Pixel {
    public enum Code{
        INNER,OUTER,INNER_OUTER,STRAIGHT
    }
    private int x;
    private int y;
    private Code type;
    private int color;

    public Pixel(int x, int y){
        this.x = x;
        this.y = y;
        this.type = null;
        this.color = -1;
    }

    public Pixel(int x, int y, int color){
        this.x = x;
        this.y = y;
        this.type = null;
        this.color = color;
    }

    public Pixel(int x, int y, int color, Code type){
        this.x = x;
        this.y = y;
        this.type = type;
        this.color = color;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public Code getType(){
        return type;
    }

    public int getColor(){
        return color;
    }

    public int getIndex(int imgWidth) { return x + imgWidth*y; }

    public void setCode(Code type){
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



    public void setPosition(int x, int y){
        setX(x);
        setY(y);
    }
}
