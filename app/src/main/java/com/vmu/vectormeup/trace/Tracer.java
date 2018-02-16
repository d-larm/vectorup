package com.vmu.vectormeup.trace;
import com.vmu.vectormeup.spline.SplineManager;

import java.util.HashSet;
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
    private final int AHEAD = 0;
    private final int AHEAD_RIGHT = 1;
    private final int RIGHT = 2;
    private final int BEHIND_RIGHT = 3;
    private final int BEHIND = 4;
    private final int BEHIND_LEFT = 5;
    private final int LEFT = 6;
    private final int AHEAD_LEFT = 7;


    private final int INNER = 0;
    private final int OUTER = 1;
    private final int INNER_OUTER = 2;
    private final int STRAIGHT = 3;


    private int changeDir(int direction,int turnVal){
        return (((direction+turnVal)%8)+8)%8;
    }

    public enum Dir{
        AHEAD{
            @Override
            public Dir prev() {
                return values()[values().length-1]; // see below for options for this line
            };
        },
        AHEAD_RIGHT,RIGHT,BEHIND_RIGHT,BEHIND,BEHIND_LEFT,LEFT,
        AHEAD_LEFT
        {
            @Override
            public Dir next() {
                return values()[0]; // see below for options for this line
            };
        };
        int size = values().length;
        private Dir[] vals = values();
        public Dir next(){
            return values()[ordinal()+1];
        }

        public Dir next2(){
            int len = values().length;
            return values()[(ordinal()+2)%len];
        }

        public Dir next4(){
            int len = values().length;
            return values()[(ordinal()+4)%len];
        }

        public Dir prev(){
            return values()[ordinal()-1];
        }
        public Dir prev2(){
            int len = values().length;
            return values()[(((ordinal()-2)%len)+len)%len];
        }

        public Dir prev4(){
            int len = values().length;
            return values()[(((ordinal()-4)%len)+len)%len];
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

    public void setActiveColor(int color){
        activeColor = color;
    }

    public void assignSplineManager(SplineManager sm){
        splineManager = sm;
    }


    private int getPixelAtDirection(int dir){
        int xPos = getPixelX(currentIndex);
        int yPos = getPixelY(currentIndex);
//        if(front == 0) //NORTH
//            relativePos = dir; //Directions not shifted
//        else if(front == 1) //EAST
//            relativePos = dir.next().next(); //Directions shifted eastwards (ahead becomes right)
//        else if(front == 2) //SOUTH
//             relativePos = dir.next().next().next().next(); //Directions shifted southwards (ahead becomes behind)
//        else if(front == 3) //WEST
//            relativePos = dir.prev().prev();//Directions shifted westwards (ahead becomes left)
//        else
//            return -1;

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

    private void changeDirection(int newDir){ //Sets the absolute direction of the tracer using a relative direction
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
                    if(getPixelAtDirection(BEHIND) != activeColor)
                        if(getPixelAtDirection(LEFT) != activeColor){
                            if(getPixelAtDirection(BEHIND_LEFT) != activeColor){
                                canStart = true;
                                //System.out.println("Found start position");
                                break;
                            }
                        }else{
                            //System.out.println("Found start position");
                            canStart = true;
                            break;
                        }
                    changeDirection(RIGHT);
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
                                if (getPixelAtDirection(BEHIND) != activeColor)
                                    if (getPixelAtDirection(LEFT) != activeColor) {
                                        if (getPixelAtDirection(BEHIND_LEFT) != activeColor) {
                                            canStart = true;
                                            //System.out.println("Found start position");
                                            break;
                                        }
                                    } else {
                                        //System.out.println("Found start position");
                                        canStart = true;
                                        break;
                                    }
                                changeDirection(RIGHT);
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
                    if (getPixelAtDirection(BEHIND) != activeColor)
                        if (getPixelAtDirection(LEFT) != activeColor) {
                            if (getPixelAtDirection(BEHIND_LEFT) != activeColor) {
                                canStart = true;
                                break;
                            }
                        } else {
                            canStart = true;
                            break;
                        }
                    changeDirection(RIGHT);
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
    HashSet<Integer> foundPixels = new HashSet<Integer>(w*h);
    private void advanceTracer(Contour e, int p, int d,int code){
        moveTracer(p);
        changeDirection(d);
        e.addPixel(new Pixel(x,y,activeColor, code,getIndex(x,y)));
        foundPixels.add(getIndex(x,y));

//        System.out.println("Added pixel with color "+activeColor+" at ("+x+","+y+")");
    }

    private void addStartPixel(int x, int y,Contour e){
        Pixel start = new Pixel(x,y,activeColor, INNER,getIndex(x,y));
        start.setAsStart();
        e.add(start);
        foundPixels.add(getIndex(x,y));


    }

    public Contour trace(){
        Contour edge = new Contour(activeColor,w*h/4);
        int sampleRate = 1;
        int resolution = 8;
        int count = 0;
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

//            if(splineManager.canSetStart()){
//                splineManager.setStart(new Pixel(x,y,activeColor, INNER,getIndex(x,y)));
//            }

            addStartPixel(x,y,edge);

            do{
//                System.out.println("Contouring begins");
                //Stage 1
                if(getPixelAtDirection(BEHIND_LEFT) == activeColor){ //Case 1
                    if(getPixelAtDirection(LEFT) == activeColor){
                        advanceTracer(edge,LEFT,LEFT,INNER);
                        advanceTracer(edge,LEFT,LEFT,-1);
//                        moveTracer(LEFT);
//                        changeDirection(LEFT);
//                        System.out.println("Found INNER Pixel (Case 1)" + printLocation() );
                    }
                    else{ //Case 2
                        edge.getLastPixel().setCode(INNER);
//                       edge.addPixel(new Pixel(x,y,activeColor, INNER_OUTER,getIndex(x,y)));
                        advanceTracer(edge,BEHIND_LEFT,BEHIND,INNER_OUTER);
//                        System.out.println("Found INNER-OUTER Pixel (Case 2)"+ printLocation());
                    }
                }else{
                    if(getPixelAtDirection(LEFT) == activeColor){ //Case 3
                        advanceTracer(edge,LEFT,LEFT,STRAIGHT);
//                        System.out.println("Found STRAIGHT Pixel (Case 3)"+ printLocation());
                    }else{ //Case 4
//                        edge.addPixel(new Pixel(x,y,activeColor, OUTER,getIndex(x,y)));
//                        foundPixels.add(getIndex(x,y));
                        edge.getLastPixel().setCode(OUTER);
//                        System.out.println("Found OUTER Pixel (Case 4)"+ printLocation());
                    }
                }
                //Stage 2
                if(getPixelAtDirection(AHEAD_LEFT) == activeColor) { //Case 6
                    if (getPixelAtDirection(AHEAD) == activeColor) {
                        advanceTracer(edge, AHEAD, LEFT, INNER);
                        advanceTracer(edge, AHEAD, RIGHT, -1);
//                        moveTracer(AHEAD);
//                        changeDirection(RIGHT);
//                        System.out.println("Found INNER Pixel (Case 6)"+ printLocation());
                    } else { //Case 5
//                        edge.addPixel(new Pixel(x, y, activeColor, INNER_OUTER,getIndex(x,y)));
//                        foundPixels.add(getIndex(x,y));
                        edge.getLastPixel().setCode(INNER_OUTER);
                        advanceTracer(edge, AHEAD_LEFT, AHEAD, INNER_OUTER);
//                        System.out.println("Found INNER-OUTER Pixel (Case 5)"+ printLocation());
                    }
                }else if(getPixelAtDirection(AHEAD) == activeColor){ //Case 7
                    advanceTracer(edge, AHEAD, RIGHT, -1);
//                    edge.addPixel(new Pixel(x,y,activeColor, -1,getIndex(x,y)));
//                    moveTracer(AHEAD);
//                    changeDirection(RIGHT);
//                    System.out.println("Case 7 (Move AHEAD and look RIGHT"+ printLocation());
                }else { //Case 8
                    changeDirection(BEHIND);
                    if(edge.getLastPixel() != null)
                        edge.getLastPixel().setCode(OUTER);

//                    System.out.println("Found OUTER Pixel (Case 8)"+ printLocation());
                }

            }while(currentIndex != startPixel);
            if(currentIndex == startPixel){
                Pixel start = new Pixel(x,y,activeColor, INNER,getIndex(x,y));
                start.setAsStart();
                edge.add(start);
                count++;
//                System.out.println("Start pixel found again"+ printLocation());

//                foundPixels.add(getIndex(x,y));

//                edge.clear();
            }
        }
        System.out.println("There were " + count + "contours found");
        splineManager.draw(edge);

        System.out.println("Trace completed by thread "+Thread.currentThread().getId());
        return edge;
    }

    private String printLocation(){
        return "("+x+","+y+")";
    }





}
