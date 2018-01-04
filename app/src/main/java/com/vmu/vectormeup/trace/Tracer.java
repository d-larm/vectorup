package com.vmu.vectormeup.trace;
import java.util.Random;

/**
 * Created by daniel on 03/12/17.
 */

public class Tracer {
    /*
        NORTH = 0           SOUTH = 4
        NORTH EAST = 1      SOUTH WEST = 5
        EAST = 2            WEST = 6
        SOUTH EAST = 3      NORTH WEST = 7

     */

    public enum Dir{
        AHEAD{
            @Override
            public Dir prev() {
                return values()[values().length-1]; // see below for options for this line
            }
        },
        AHEAD_RIGHT,RIGHT,BEHIND_RIGHT,BEHIND,BEHIND_LEFT,LEFT,
        AHEAD_LEFT
        {
            @Override
            public Dir next() {
                return values()[0]; // see below for options for this line
            }
        };
        public Dir next(){
            return values()[ordinal()+1];
        }

        public Dir prev(){
            return values()[ordinal()-1];
        }
    }

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

    public void setActiveColor(int color){
        activeColor = color;
    }

    private int getPixelAtDirection(Dir dir){
        int xPos = getPixelX(currentIndex);
        int yPos = getPixelY(currentIndex);
        Dir relativePos;
        if(front == 0) //NORTH
            relativePos = dir; //Directions not shifted
        else if(front == 1) //EAST
            relativePos = dir.next().next(); //Directions shifted eastwards (ahead becomes right)
        else if(front == 2) //SOUTH
            relativePos = dir.next().next().next().next(); //Directions shifted southwards (ahead becomes behind)
        else if(front == 3) //WEST
            relativePos = dir.prev().prev();//Directions shifted westwards (ahead becomes left)
        else
            return -1;

        switch(relativePos) {
            case AHEAD: //get pixel ahead
                if (yPos > 0)
                    searchedIndex = getIndex(xPos, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case AHEAD_RIGHT: //get pixel ahead and right
                if (yPos > 0 && xPos < w-1)
                    searchedIndex = getIndex(xPos + 1, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case RIGHT: //get pixel to the right
                if (xPos < w-1)
                    searchedIndex = getIndex(xPos + 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case BEHIND_RIGHT: //get pixel behind and right
                if (xPos < w-1 && yPos < h-1)
                    searchedIndex = getIndex(xPos + 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case BEHIND: //get pixel behind
                if (yPos < h-1)
                    searchedIndex = getIndex(xPos, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case BEHIND_LEFT: //get pixel behind and left
                if (xPos > 0 && yPos < h-1)
                    searchedIndex = getIndex(xPos - 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case LEFT: //get pixel to the left
                if (xPos > 0)
                    searchedIndex = getIndex(xPos - 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case AHEAD_LEFT: //get pixel ahead and left
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

    private void changeDirection(Dir newDir){ //Sets the absolute direction of the tracer using a relative direction
        switch(newDir){
            case LEFT:
                front = (front-1 % 4 + 4) % 4;break;
            case RIGHT:
                front = (front+1 % 4 + 4) % 4;break;
            case BEHIND:
                front = (front+2 % 4 + 4) % 4;break;
            default:
                break;
        }
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
                    if(getPixelAtDirection(Dir.BEHIND) != activeColor)
                        if(getPixelAtDirection(Dir.LEFT) != activeColor){
                            if(getPixelAtDirection(Dir.BEHIND_LEFT) != activeColor){
                                canStart = true;
                                //System.out.println("Found start position");
                                break;
                            }
                        }else{
                            //System.out.println("Found start position");
                            canStart = true;
                            break;
                        }
                    changeDirection(Dir.RIGHT);
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
                                if (getPixelAtDirection(Dir.BEHIND) != activeColor)
                                    if (getPixelAtDirection(Dir.LEFT) != activeColor) {
                                        if (getPixelAtDirection(Dir.BEHIND_LEFT) != activeColor) {
                                            canStart = true;
                                            //System.out.println("Found start position");
                                            break;
                                        }
                                    } else {
                                        //System.out.println("Found start position");
                                        canStart = true;
                                        break;
                                    }
                                changeDirection(Dir.RIGHT);
                            }
                        }
                    }
                    break;
                }
            }
        }while(!canStart);
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

    private void moveTracer(Dir dir){
        int xPos = getPixelX(currentIndex);
        int yPos = getPixelY(currentIndex);
        Dir relativePos = dir;

        if(front == 1)
            relativePos = dir.next().next();
        else if(front == 2)
            relativePos = dir.next().next().next().next();
        else if(front == 3)
            relativePos = dir.prev().prev();

        if(dir == null)
            return;

        switch(relativePos) {
            case AHEAD: //get pixel ahead
                if (yPos > 0)
                    searchedIndex = getIndex(xPos, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case AHEAD_RIGHT: //get pixel ahead and right
                if (yPos > 0 && xPos < w)
                    searchedIndex = getIndex(xPos + 1, yPos - 1);
                else
                    searchedIndex = -1;
                break;
            case RIGHT: //get pixel to the right
                if (xPos < w)
                    searchedIndex = getIndex(xPos + 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case BEHIND_RIGHT: //get pixel behind and right
                if (xPos < w && yPos < h)
                    searchedIndex = getIndex(xPos + 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case BEHIND: //get pixel behind
                if (yPos < h)
                    searchedIndex = getIndex(xPos, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case BEHIND_LEFT: //get pixel behind and left
                if (xPos > 0 && yPos < h)
                    searchedIndex = getIndex(xPos - 1, yPos + 1);
                else
                    searchedIndex = -1;
                break;
            case LEFT: //get pixel to the left
                if (xPos > 0)
                    searchedIndex = getIndex(xPos - 1, yPos);
                else
                    searchedIndex = -1;
                break;
            case AHEAD_LEFT: //get pixel ahead and left
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

    private void advanceTracer(Contour e, Dir p, Dir d, Pixel.Code code){
        moveTracer(p);
        changeDirection(d);
        e.addPixel(new Pixel(x,y,activeColor, code));
//        System.out.println("Added pixel with color "+activeColor+" at ("+x+","+y+")");
    }

    private void eliminatePoints(Contour c){
        if(c.getLastPixel().getType() == Pixel.Code.STRAIGHT && c.getPixel(c.size()-2).getType() == Pixel.Code.STRAIGHT)
            c.remove(c.size()-2);
    }


    public Contour trace(){
        int repeatTimes = 100;
        Contour edge = new Contour(activeColor);
        for(int i=0;i<repeatTimes;i++){
            int searchCount = 0;
            boolean startFoundAlready = false;
            do {
//                System.out.println("Finding start");
                startFoundAlready = false;
                findStart();
                if(edge.size() == 0)
                    break;
                for (int j = 0; j < edge.size(); j++) {
                    if (startPixel == edge.getPixel(j).getIndex(w)){
                        startFoundAlready = true;
                        break;
                    }
                }
                searchCount++;
            }while(startFoundAlready == true && searchCount < edge.size());

            if(startFoundAlready == true)
                break;
            do{
                //Stage 1
                if(getPixelAtDirection(Dir.BEHIND_LEFT) == activeColor){ //Case 1
                    if(getPixelAtDirection(Dir.LEFT) == activeColor){
                        advanceTracer(edge,Dir.LEFT,Dir.LEFT,Pixel.Code.INNER);
                        //System.out.println("Found INNER Pixel (Case 1)");
                    }
                    else{ //Case 2
                        edge.addPixel(new Pixel(x,y,activeColor, Pixel.Code.INNER_OUTER));
                        advanceTracer(edge,Dir.BEHIND_LEFT,Dir.BEHIND,Pixel.Code.INNER_OUTER);
                        //System.out.println("Found INNER-OUTER Pixel (Case 2)");
                    }
                }else{
                    if(getPixelAtDirection(Dir.LEFT) == activeColor){ //Case 3
                        advanceTracer(edge,Dir.LEFT,Dir.LEFT,Pixel.Code.STRAIGHT);
                        //System.out.println("Found STRAIGHT Pixel (Case 3)");
                    }else{ //Case 4
                        edge.addPixel(new Pixel(x,y,activeColor, Pixel.Code.OUTER));
                        //System.out.println("Found OUTER Pixel (Case 4)");
                    }
                }
                //Stage 2
                if(getPixelAtDirection(Dir.AHEAD_LEFT) == activeColor) { //Case 6
                    if (getPixelAtDirection(Dir.AHEAD) == activeColor) {
                        advanceTracer(edge, Dir.AHEAD, Dir.LEFT, Pixel.Code.INNER);
                        moveTracer(Dir.AHEAD);
                        changeDirection(Dir.RIGHT);
                        //System.out.println("Found INNER Pixel (Case 6)");
                    } else { //Case 5
                        edge.addPixel(new Pixel(x, y, activeColor, Pixel.Code.INNER_OUTER));
                        advanceTracer(edge, Dir.AHEAD_LEFT, Dir.AHEAD, Pixel.Code.INNER_OUTER);
                        //System.out.println("Found INNER-OUTER Pixel (Case 5)");
                    }
                }else if(getPixelAtDirection(Dir.AHEAD) == activeColor){ //Case 7
                    moveTracer(Dir.AHEAD);
                    changeDirection(Dir.RIGHT);
                    //System.out.println("Case 7 (Move AHEAD and look RIGHT");
                }else { //Case 8
                    changeDirection(Dir.BEHIND);
                    edge.getLastPixel().setCode(Pixel.Code.OUTER);
                    //System.out.println("Found OUTER Pixel (Case 8)");
                }
                //System.out.println("Started at ("+getPixelX(startPixel)+","+getPixelY(startPixel)+")"+", currently at ("+x+","+y+")");
                if(edge.size() > w*h)
                    break;
                eliminatePoints(edge);
            }while(currentIndex != startPixel);
        }

        System.out.println("Trace complete");
        return edge;
    }





}
