package com.vmu.vectormeup.trace;
import com.vmu.vectormeup.spline.SplineManager;

import java.util.HashSet;
import java.util.Random;
import com.vmu.vectormeup.constants.*;

import com.vmu.vectormeup.constants.DIR;

/**
 * Created by daniel on 03/12/17.
 */

public class Tracer {
    private int front; //Starts facing north
    private byte back; //South is behind
    private int x;
    private int y;
    private int[] image;
    private int w;
    private int h;
    private int activeColor;
    private int currentIndex;
    private int searchedIndex;
    private int startPixel;
    private boolean backToStart = false;
    private SplineManager splineManager;

    public Tracer(int[] image,int w,int h, int color){
        this.image = image;
        this.w = w;
        this.h = h;
        this.activeColor = color;
    }

    public Tracer(int[] image,int w,int h){
        this.image = image;
        this.w = w;
        this.h = h;
        this.activeColor = -1;
    }

    private int changeDir(int direction,int turnVal){
        return (((direction+turnVal)%8)+8)%8;
    }

    public void setActiveColor(int color){
        activeColor = color;
    }

    public void assignSplineManager(SplineManager sm){
        splineManager = sm;
    }

    private int getPixelAtDirection(int dir){
        int xPos = getPixelX(currentIndex);
        int yPos = getPixelY(currentIndex);


        if(front == 0) //NORTH
            dir = dir; //Directions not shifted
        else if(front == 1) //EAST
            dir = changeDir(dir,2); //Directions shifted eastwards (ahead becomes right)
        else if(front == 2) //SOUTH
            dir = changeDir(dir,4); //Directions shifted southwards (ahead becomes behind)
        else if(front == 3) //WEST
            dir = changeDir(dir,-2);//Directions shifted westwards (ahead becomes left)
        else
            return -1;

        switch(dir) {
            case DIR.AHEAD: //get pixel ahead
                if (yPos > 0)
                    searchedIndex = getIndex(xPos, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.AHEAD_RIGHT: //get pixel ahead and right
                if (yPos > 0 && xPos < w-1)
                    searchedIndex = getIndex(xPos + 1, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.RIGHT: //get pixel to the right
                if (xPos < w-1)
                    searchedIndex = getIndex(xPos + 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case DIR.BEHIND_RIGHT: //get pixel behind and right
                if (xPos < w-1 && yPos < h-1)
                    searchedIndex = getIndex(xPos + 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.BEHIND: //get pixel behind
                if (yPos < h-1)
                    searchedIndex = getIndex(xPos, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.BEHIND_LEFT: //get pixel behind and left
                if (xPos > 0 && yPos < h-1)
                    searchedIndex = getIndex(xPos - 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.LEFT: //get pixel to the left
                if (xPos > 0)
                    searchedIndex = getIndex(xPos - 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case DIR.AHEAD_LEFT: //get pixel ahead and left
                if (xPos > 0 && yPos > 0)
                    searchedIndex = getIndex(xPos - 1, yPos - 1);
                else
                    searchedIndex = -1;
                break;
        }
        if(searchedIndex != -1){
//            //System.out.println("Scanned color: "+image[searchedIndex]+ ", active color: "+activeColor);
            if(searchedIndex < 0 || searchedIndex > image.length-1)
                return Integer.MAX_VALUE;
            else
                return image[searchedIndex];
        }

        else
            return Integer.MAX_VALUE;
    }

    private void changeDirection(int newDir){ //Sets the absolute direction of the tracer using a relative direction
        switch(newDir){
            case DIR.LEFT:
                front = (front-1 % 4 + 4) % 4;break;
            case DIR.RIGHT:
                front = (front+1 % 4 + 4) % 4;break;
            case DIR.BEHIND:
                front = (front+2 % 4 + 4) % 4;break;
            default:
                break;
        }
    }

    private void setStart(int index) {
        front = 0;
        currentIndex = index;
        startPixel = currentIndex;
        x = getPixelX(currentIndex);
        y = getPixelY(currentIndex);

    }

    private void findStart(){
        Random rand = new Random();
        boolean canStart;
        int maxSearch = w*h/4;
        int searchCount = 0;
        do{
            searchCount++;
            canStart = false;
            front = 0;
            currentIndex = rand.nextInt(image.length);
            startPixel = currentIndex;
            x = getPixelX(currentIndex);
            y = getPixelY(currentIndex);
//            System.out.println("Looking at ("+x+","+y+") for start position");
            if(getPixel(currentIndex) == activeColor){
                for(int i=0;i<4;i++){
                    if(getPixelAtDirection(DIR.BEHIND) != activeColor)
                        if(getPixelAtDirection(DIR.LEFT) != activeColor){
                            if(getPixelAtDirection(DIR.BEHIND_LEFT) != activeColor){
                                canStart = true;
                                //System.out.println("Found start position");
                                break;
                            }
                        }else{
                            //System.out.println("Found start position");
                            canStart = true;
                            break;
                        }
                    changeDirection(DIR.RIGHT);
                }
            }else{
                if(searchCount >= maxSearch){
                    for(int k=0;k<image.length;k++){
                        currentIndex = k;
                        startPixel = currentIndex;
                        x = getPixelX(currentIndex);
                        y = getPixelY(currentIndex);
                        if(getPixel(currentIndex) == activeColor) {
                            for (int i = 0; i < 4; i++) {
                                if (getPixelAtDirection(DIR.BEHIND) != activeColor)
                                    if (getPixelAtDirection(DIR.LEFT) != activeColor) {
                                        if (getPixelAtDirection(DIR.BEHIND_LEFT) != activeColor) {
                                            canStart = true;
                                            //System.out.println("Found start position");
                                            break;
                                        }
                                    } else {
                                        //System.out.println("Found start position");
                                        canStart = true;
                                        break;
                                    }
                                changeDirection(DIR.RIGHT);
                            }
                        }
                    }
                    break;
                }
            }
        }while(canStart == false);
    }

    private boolean startValid(int index) {
        boolean canStart = false;
        if(index < image.length){
//            System.out.println("Looking for "+activeColor+",found "+getPixel(currentIndex)+"for thread "+Thread.currentThread().getId());
            if (getPixel(currentIndex) == activeColor) {
//                System.out.println("Landed on active for thread "+Thread.currentThread().getId());
                for (int i = 0; i < 4; i++) {
                    if (getPixelAtDirection(DIR.BEHIND) != activeColor)
                        if (getPixelAtDirection(DIR.LEFT) != activeColor) {
                            if (getPixelAtDirection(DIR.BEHIND_LEFT) != activeColor) {
                                canStart = true;
                                break;
                            }
                        } else {
                            canStart = true;
                            break;
                        }
                    changeDirection(DIR.RIGHT);
                }
            }
        }
        return canStart;
    }

    public int getPixelX(int index){
        return index % w;
    }

    public int getPixelY(int index){
        return index / w;
    }

    public int getPixel(int index){
        return image[index];
    }

    public int getIndex(int x,int y){
        return x + w*y;
    }

    private void moveTracer(int dir){
        int xPos = getPixelX(currentIndex);
        int yPos = getPixelY(currentIndex);

        if(front == 1) //EAST
            dir = changeDir(dir,2); //Directions shifted eastwards (ahead becomes right)
        else if(front == 2) //SOUTH
            dir = changeDir(dir,4); //Directions shifted southwards (ahead becomes behind)
        else if(front == 3) //WEST
            dir = changeDir(dir,-2);//Directions shifted westwards (ahead becomes left)

        switch(dir) {
            case DIR.AHEAD: //get pixel ahead
                if (yPos > 0)
                    searchedIndex = getIndex(xPos, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.AHEAD_RIGHT: //get pixel ahead and right
                if (yPos > 0 && xPos < w)
                    searchedIndex = getIndex(xPos + 1, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.RIGHT: //get pixel to the right
                if (xPos < w)
                    searchedIndex = getIndex(xPos + 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case DIR.BEHIND_RIGHT: //get pixel behind and right
                if (xPos < w && yPos < h)
                    searchedIndex = getIndex(xPos + 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.BEHIND: //get pixel behind
                if (yPos < h)
                    searchedIndex = getIndex(xPos, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.BEHIND_LEFT: //get pixel behind and left
                if (xPos > 0 && yPos < h)
                    searchedIndex = getIndex(xPos - 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case DIR.LEFT: //get pixel to the left
                if (xPos > 0)
                    searchedIndex = getIndex(xPos - 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case DIR.AHEAD_LEFT: //get pixel ahead and left
                if (xPos > 0 && yPos > 0)
                    searchedIndex = getIndex(xPos - 1, yPos - 1);
                else
                    searchedIndex = -1;
                break;
        }
        if(searchedIndex != -1){
            //System.out.println("Moving");
            currentIndex = searchedIndex;
            x = getPixelX(currentIndex);
            y = getPixelY(currentIndex);
        }

    }

    Contour edge = new Contour(activeColor,(w*h)/4);
    private PixelHashSet foundPixels = new PixelHashSet((w*h)/4);
    private Pixel lastPixel = null;

    private void advanceTracer(Contour e, int p, int d,int code){
        moveTracer(p);
        changeDirection(d);
        addPixel(x,y,code);
//        Pixel newPixel = new Pixel(x,y,activeColor, code,getIndex(x,y));

//        e.addPixel(newPixel);
//        foundPixels.add(getIndex(x,y));
//        System.out.println("Added pixel with color "+activeColor+" at ("+x+","+y+")");
    }

    private void addStartPixel(int x, int y, int code){
        Pixel newPixel = new Pixel(x,y,activeColor, code,getIndex(x,y));
        newPixel.setAsStart();
        edge.addPixel(newPixel);
        foundPixels.add(newPixel);
    }

    private void addPixel(int x, int y, int code){
        Pixel newPixel = new Pixel(x,y,activeColor, code,getIndex(x,y));
        edge.addPixel(newPixel);
        foundPixels.add(newPixel);
    }

    public Contour trace(){
        int sampleRate = 1;
        int resolution = 8;
        boolean initialisedStart = false;
        for(int i=0;i<image.length;i+=sampleRate){ //Uses every pixel as the start pixel

            setStart(i);
            if(!startValid(i)) //If the start pixel is not valid go to next start pixel
                continue;

            boolean startFoundAlready = false;
//            for (int j = 0; j < edge.size(); j++) { //Checks if selected start pixel in edge list
//                if (startPixel == edge.getPixel(j).getIndex(w)){
//                    startFoundAlready = true;
//                    break;
//                }
//            }
            if(foundPixels.contains(startPixel))
                startFoundAlready = true;

            if(startFoundAlready == true) //If the start pixel is already in the edge list then go to next start pixel
                continue;

            if(splineManager.canSetStart()){
                splineManager.setStart(new Pixel(x,y,activeColor, CODE.INNER,getIndex(x,y)));
            }


            do{
//                System.out.println("Contouring begins");
                //Stage 1
                if(getPixelAtDirection(DIR.BEHIND_LEFT) == activeColor){ //Case 1
                    if(getPixelAtDirection(DIR.LEFT) == activeColor){
                        advanceTracer(edge,DIR.LEFT,DIR.LEFT,CODE.INNER);
                        moveTracer(DIR.LEFT);
                        changeDirection(DIR.LEFT);
                        //System.out.println("Found INNER Pixel (Case 1)");
                    }
                    else{ //Case 2
                        edge.addPixel(new Pixel(x,y,activeColor, CODE.INNER_OUTER,getIndex(x,y)));
                        advanceTracer(edge,DIR.BEHIND_LEFT,DIR.BEHIND,CODE.INNER_OUTER);
                        //System.out.println("Found INNER-OUTER Pixel (Case 2)");
                    }
                }else{
                    if(getPixelAtDirection(DIR.LEFT) == activeColor){ //Case 3
                        advanceTracer(edge,DIR.LEFT,DIR.LEFT,CODE.STRAIGHT);
                        //System.out.println("Found CODE.STRAIGHT Pixel (Case 3)");
                    }else{ //Case 4
                        addPixel(x,y,CODE.OUTER);
//                        edge.addPixel(new Pixel(x,y,activeColor, CODE.OUTER,getIndex(x,y)));
//                        foundPixels.add(getIndex(x,y));
                        //System.out.println("Found OUTER Pixel (Case 4)");
                    }
                }
                //Stage 2
                if(getPixelAtDirection(DIR.AHEAD_LEFT) == activeColor) { //Case 6
                    if (getPixelAtDirection(DIR.AHEAD) == activeColor) {
                        advanceTracer(edge, DIR.AHEAD, DIR.LEFT, CODE.INNER);
                        moveTracer(DIR.AHEAD);
                        changeDirection(DIR.RIGHT);
                        //System.out.println("Found INNER Pixel (Case 6)");
                    } else { //Case 5
                        addPixel(x, y, CODE.INNER_OUTER);
//                        edge.addPixel(new Pixel(x, y, activeColor, CODE.INNER_OUTER,getIndex(x,y)));
//                        foundPixels.add(getIndex(x,y));
                        advanceTracer(edge, DIR.AHEAD_LEFT, DIR.AHEAD, CODE.INNER_OUTER);
                        //System.out.println("Found INNER-OUTER Pixel (Case 5)");
                    }
                }else if(getPixelAtDirection(DIR.AHEAD) == activeColor){ //Case 7
                    moveTracer(DIR.AHEAD);
                    changeDirection(DIR.RIGHT);
                    //System.out.println("Case 7 (Move AHEAD and look RIGHT");
                }else { //Case 8
                    changeDirection(DIR.BEHIND);
                    edge.getLastPixel().setCode(CODE.OUTER);

                    //System.out.println("Found OUTER Pixel (Case 8)");
                }
//                if(edge.size() >= resolution){
//                    splineManager.draw(edge); //Joins all the pixels found in the contour list
//                    for(int k=0;k<edge.size()-1;k++) //Removes all but the most recently discovered pixel from the contour list
//                        edge.remove(k);
//                }
            }while(currentIndex != startPixel);
            if(currentIndex == startPixel){
                addStartPixel(x,y,CODE.INNER);
//                Pixel newStartPixel = new Pixel(x,y,activeColor, CODE.INNER,getIndex(x,y));
//                edge.add(newStartPixel);
//                foundPixels.add(getIndex(x,y));
//                edge.clear();
            }
        }
        splineManager.draw(edge);

        System.out.println("Trace completed by thread "+Thread.currentThread().getId());
        return edge;
    }






}
