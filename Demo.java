/*
Author: Hardik Sachdeva
Coursework for Image processing ECS605U
*/

import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.nio.file.Files;

public class Demo extends Component implements ActionListener {

    static JComboBox AllFiles, OptionsBar, formats,ChooseSecondImage, ChooseROI;
    static JButton ResetToDefault, undoButton,scaleShiftButton, scaleShiftResetButton,NegativeImg,
                  resetArithOperation, resetBoolOperation,CalcPowerLaw, calcBitPlane, resetBitPlaneNPower,histogramButton, 
                  treshbUtton, resetTresh;
    static JTextField rescaleinput, shiftinput,powerLawInput, bitPlaneInput,treshiNput;
    static ButtonGroup imageArithOpList, imageBooleanOpList;
    static JRadioButton[] imageArithmaticOperations, imageBooleanOperations;
    static JLabel histogramNoPixels;
    static JCheckBox[] FilterOptions,orderStatFilterOp,pointProcessing;
    Stack<BufferedImage> undoHistory = new Stack<>();

    private BufferedImage bi, biFiltered; //the first selected image will be stored as bi
    int w, h, w1, h1;

    public Demo() {
        try {
            bi = ImageIO.read(new File("images/mars.jpg")); //load the first image
            w = bi.getWidth(null);
            h = bi.getHeight(null);
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0, null);
                biFiltered = bi = bi2;}
              undoHistory.push(biFiltered);} 

        catch (IOException event) {  
            JOptionPane.showMessageDialog(null, "invalid Image");}}//pops up this message when an invalid image is selected


    public Dimension getPreferredSize() {return new Dimension(w+w1, h);}

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"-select-","bmp","gif","jpeg","jpg","png"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());}
        return formatSet.toArray(new String[0]);}

    public void paint(Graphics g){//  Repaint will call this function so the image will change.
      g.drawImage(biFiltered, 0, 0, null);
    }

    // *************
    // Convert the Buffered Image to Array
    // *************
    private static int[][][] convertToArray(BufferedImage image){
      int width = image.getWidth();
      int height = image.getHeight();

      int[][][] result = new int[width][height][4];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int p = image.getRGB(x,y);
            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;

            result[x][y][0]=a;
            result[x][y][1]=r;
            result[x][y][2]=g;
            result[x][y][3]=b;}}
   
      return result;}


    //************
    //  Convert the  Array to BufferedImage
    //************
    public BufferedImage convertToBimage(int[][][] TmpArray){

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];

                int p = (a<<24) | (r<<16) | (g<<8) | b;
                tmpimg.setRGB(x, y, p);}}

        return tmpimg;}

    // To shift an image by t and rescale by s without finding the min and the max(From lecture slides)
    public void ImageRescaleShift(){
      double [] inputs = convertInputScaleShift();
      double t = inputs[0];
      double s = inputs[1];

      int width = bi.getWidth();
      int height = bi.getWidth();

      int[][][] ImageArray1 = convertToArray(biFiltered);
      int[][][] ImageArray2 = convertToArray(biFiltered);

      for(int y=0; y<height; y++){
        for(int x=0; x<width; x++){
          ImageArray2[x][y][1] = (int)(s*(ImageArray1[x][y][1]+t)); //r
          ImageArray2[x][y][2] = (int)(s*(ImageArray1[x][y][2]+t)); //g
          ImageArray2[x][y][3] = (int)(s*(ImageArray1[x][y][3]+t)); //b

          if (ImageArray2[x][y][1]<0) { ImageArray2[x][y][1] = 0; }
          if (ImageArray2[x][y][2]<0) { ImageArray2[x][y][2] = 0; }
          if (ImageArray2[x][y][3]<0) { ImageArray2[x][y][3] = 0; }
          if (ImageArray2[x][y][1]>255) { ImageArray2[x][y][1] = 255; }
          if (ImageArray2[x][y][2]>255) { ImageArray2[x][y][2] = 255; }
          if (ImageArray2[x][y][3]>255) { ImageArray2[x][y][3] = 255; }}}

      biFiltered = convertToBimage(ImageArray2);
      undoHistory.push(biFiltered);
    }


    //getting the user input via UI
    public double [] convertInputScaleShift(){
      double [] values = new double[2];
      values[0] = -1;
      values[1] = -1;

      if(shiftinput.getText().equals("")){values[0] = 0;}
      else if(!shiftinput.getText().equals("")){values[0] = Double.parseDouble(shiftinput.getText());}
      if(rescaleinput.getText().equals("")){values[1] = 1;}
      else if(!rescaleinput.getText().equals("")){values[1] = Double.parseDouble(rescaleinput.getText());}
      return values;}

    //Fuction to create an Image negative
      public BufferedImage ImageNegative(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Converting image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = 255-ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255-ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255-ImageArray[x][y][3];  //b
            }
        }

        return convertToBimage(ImageArray);  // converting array to buffered Image
    }

    //Fuction to perform arithmatic operations on Images (Add, Subtract, Multiply, Divide)
    public void ImageArithmetic(int selectedArithmetic){
      
      try{
        BufferedImage imageBuffer = ImageIO.read(new File("images/"+ChooseSecondImage.getSelectedItem().toString()));
        int width = imageBuffer.getWidth(null);
        int height = imageBuffer.getHeight(null);

        int [][][] ImgArray1 = convertToArray(biFiltered);
        int [][][] ImgArray2 = convertToArray(imageBuffer);
        int [][][] newImage = convertToArray(bi);


        for(int y=0; y<height; y++){
          for(int x=0; x<width; x++){

            if (selectedArithmetic == 0){ //logic for add 
                newImage[x][y][1] = ImgArray1[x][y][1] + ImgArray2[x][y][1];  //r
                newImage[x][y][2] = ImgArray1[x][y][2] + ImgArray2[x][y][2];  //g
                newImage[x][y][3] = ImgArray1[x][y][3] + ImgArray2[x][y][3];} //b
            
            else if(selectedArithmetic == 1){ // logic for subtract
              newImage[x][y][1] = ImgArray1[x][y][1] - ImgArray2[x][y][1];  //r
              newImage[x][y][2] = ImgArray1[x][y][2] - ImgArray2[x][y][2];  //g
              newImage[x][y][3] = ImgArray1[x][y][3] - ImgArray2[x][y][3];} //b  
            
            else if(selectedArithmetic == 2){ // logic for multiply
              newImage[x][y][1] = ImgArray1[x][y][1] * ImgArray2[x][y][1];  //r
              newImage[x][y][2] = ImgArray1[x][y][2] * ImgArray2[x][y][2];  //g
              newImage[x][y][3] = ImgArray1[x][y][3] * ImgArray2[x][y][3];} //b
            

            else if(selectedArithmetic == 3){ //logic for divide
              if (ImgArray2[x][y][1]==0){newImage[x][y][1] = ImgArray1[x][y][1];}
              else{newImage[x][y][1] = ImgArray1[x][y][1] / ImgArray2[x][y][1];} //r
              if (ImgArray2[x][y][2]==0){newImage[x][y][2] = ImgArray1[x][y][2];}
              else{newImage[x][y][2] = ImgArray1[x][y][2] / ImgArray2[x][y][2];} //g
              if (ImgArray2[x][y][3]==0){newImage[x][y][3] = ImgArray1[x][y][3];}
              else{newImage[x][y][3] = ImgArray1[x][y][3] / ImgArray2[x][y][3];}} //b

            // creating a range for values from 0-255, if the values are out of this limit the code will round it off to the nearest range value
            if (newImage[x][y][1]<0) { newImage[x][y][1] = 0;}
            if (newImage[x][y][2]<0) { newImage[x][y][2] = 0;}
            if (newImage[x][y][3]<0) { newImage[x][y][3] = 0;}
            if (newImage[x][y][1]>255) { newImage[x][y][1] = 255;}
            if (newImage[x][y][2]>255) { newImage[x][y][2] = 255;}
            if (newImage[x][y][3]>255) { newImage[x][y][3] = 255;}}}

        biFiltered = convertToBimage(newImage);
        undoHistory.push(biFiltered);}
      
      catch (IOException event) { //exception handling
        JOptionPane.showMessageDialog(null, "No second image found!");
        imageArithOpList.clearSelection();}}


    //Function to perform boolean operations on the image(NOT, AND, OR, XOR)
    public void ImageBoolean(int selectedBoolean){
      try{
        BufferedImage image2buf = ImageIO.read(new File("images/ROI/"+ChooseROI.getSelectedItem().toString()));//gets the selected ROI

        int width = image2buf.getWidth(null);
        int height = image2buf.getHeight(null);

        int [][][] ImgArray1 = convertToArray(biFiltered);
        int [][][] ImgArray2 = convertToArray(image2buf);
        int [][][] newImage = convertToArray(bi);

        for(int y=0; y<height; y++){
          for(int x=0; x<width; x++){
            if (selectedBoolean == 0){ //if NOT is selected

              int r = ImgArray1[x][y][1]; //r
              int g = ImgArray1[x][y][2]; //g
              int b = ImgArray1[x][y][3]; //b

              newImage[x][y][1] = (~r)&0xFF; //r
              newImage[x][y][2] = (~g)&0xFF; //g
              newImage[x][y][3] = (~b)&0xFF;} //b

            
            else if(selectedBoolean == 1){ //if AND is selected
              if(ImgArray2[x][y][1] == 0){newImage[x][y][1] = 0;}
              else if (ImgArray2[x][y][1] == 255){newImage[x][y][1] = ImgArray1[x][y][1];}
              if(ImgArray2[x][y][2] == 0){newImage[x][y][2] = 0;}
              else if (ImgArray2[x][y][2] == 255){newImage[x][y][2] = ImgArray1[x][y][2];}
              if(ImgArray2[x][y][3] == 0){newImage[x][y][3] = 0;}
              else if (ImgArray2[x][y][3] == 255){newImage[x][y][3] = ImgArray1[x][y][3];}}
            

            else if(selectedBoolean == 2){ //if OR is selected
              if(ImgArray2[x][y][1] == 255){newImage[x][y][1] = 255;}
              else if (ImgArray2[x][y][1] == 0){newImage[x][y][1] = ImgArray1[x][y][1];}
              if(ImgArray2[x][y][2] == 255){newImage[x][y][2] = 255;}
              else if (ImgArray2[x][y][2] == 0){newImage[x][y][2] = ImgArray1[x][y][2];}
              if(ImgArray2[x][y][3] == 255){newImage[x][y][3] = 255;}
              else if (ImgArray2[x][y][3] == 0){newImage[x][y][3] = ImgArray1[x][y][3];}}
            

            else if(selectedBoolean == 3){ //if XOR is selected
              if(ImgArray1[x][y][1] == 0 && ImgArray2[x][y][1] == 0){newImage[x][y][1] = 0;}
              else if(ImgArray1[x][y][1] == 255 && ImgArray2[x][y][1] == 0){newImage[x][y][1] = 255;}
              else if(ImgArray1[x][y][1] == 0 && ImgArray2[x][y][1] == 255){newImage[x][y][1] = 255;}
              else if(ImgArray1[x][y][1] == 255 && ImgArray2[x][y][1] == 255){newImage[x][y][1] = 0;}

              if(ImgArray1[x][y][2] == 0 && ImgArray2[x][y][2] == 0){newImage[x][y][2] = 0;}
              else if(ImgArray1[x][y][2] == 255 && ImgArray2[x][y][2] == 0){newImage[x][y][2] = 255;}
              else if(ImgArray1[x][y][2] == 0 && ImgArray2[x][y][2] == 255){newImage[x][y][2] = 255;}
              else if(ImgArray1[x][y][2] == 255 && ImgArray2[x][y][2] == 255){newImage[x][y][2] = 0;}

              if(ImgArray1[x][y][3] == 0 && ImgArray2[x][y][3] == 0){newImage[x][y][3] = 0;}
              else if(ImgArray1[x][y][3] == 255 && ImgArray2[x][y][3] == 0){newImage[x][y][3] = 255;}
              else if(ImgArray1[x][y][3] == 0 && ImgArray2[x][y][3] == 255){newImage[x][y][3] = 255;}
              else if(ImgArray1[x][y][3] == 255 && ImgArray2[x][y][3] == 255){newImage[x][y][3] = 0;}}}}

        biFiltered = convertToBimage(newImage);
        undoHistory.push(biFiltered);}
      
      catch (IOException event) {//exception handling
          JOptionPane.showMessageDialog(null, "Choose ROI image!");
          imageBooleanOpList.clearSelection();}}
    
    //This function performs Point Processing and Bit Plane Slicing 
    public void ImagePointProcessing(int selectedOption){
        // selectedOption contains the checkbox selected by the user
        // index 0,1,2 are the checkboxed, 4 and 5 are the buttons (powerlaw and bitplate)

        int width = bi.getWidth();
        int height = bi.getHeight();

        int[][][] ImgArray = convertToArray(biFiltered); //image to array
        int[][][] newImage = convertToArray(bi);
        int[] LUT = new int[256];

        if(selectedOption == 0){ //checkbox for negative linear
          for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImgArray[x][y][1] = 255-ImgArray[x][y][1];  //r
                ImgArray[x][y][2] = 255-ImgArray[x][y][2];  //g
                ImgArray[x][y][3] = 255-ImgArray[x][y][3];} //b
          }biFiltered = convertToBimage(ImgArray);
          undoHistory.push(biFiltered);
          return;}
        

        
        else if(selectedOption == 2){//checkbox for random lookup table
          for(int k=0; k<=255; k++){
            LUT[k] = (int)(Math.random() * 255) + 1;}}

        else if(selectedOption == 4){//button for power-law
            if(!powerLawInput.getText().equals("")){
              double p = Double.parseDouble(powerLawInput.getText());
              for(int k=0; k<=255; k++){
                LUT[k] = (int)(Math.pow(255,1-p)*Math.pow(k,p));}}

        
        else if(selectedOption == 1){//for log function
          for(int k=0; k<=255; k++){LUT[k] = (int)(Math.log(1+k)*255/Math.log(256));}}
          else{return;}
        }
        

        else if(selectedOption == 5){//button for bit slice
          if(!bitPlaneInput.getText().equals("")){
            
            int k = Integer.parseInt(bitPlaneInput.getText());

            for(int y=0; y<height; y++){
              for(int x=0; x<width; x++){
                int r = ImgArray[x][y][1]; //r
                int g = ImgArray[x][y][2]; //g
                int b = ImgArray[x][y][3]; //b

                r = (r>>k)&1;
                g = (g>>k)&1;
                b = (b>>k)&1;

                // pixel is black if it ends in 0
                if(r==0 || g==0 || b==0){ 
                  newImage[x][y][1]=0;
                  newImage[x][y][2]=0;
                  newImage[x][y][3]=0;}

                // pixel is white if it ends in 1
                else if(r == 1 || g==1 || b==1){ 
                  newImage[x][y][1]=255;
                  newImage[x][y][2]=255;
                  newImage[x][y][3]=255;}}}

            biFiltered = convertToBimage(newImage);
            undoHistory.push(biFiltered);
            return;}

          else{return;}}

        for(int y=0; y<height; y++){  // log Power-law Random LUT table
            for(int x =0; x<width; x++){
              newImage[x][y][1] = LUT[ImgArray[x][y][1]];
              newImage[x][y][2] = LUT[ImgArray[x][y][2]];
              newImage[x][y][3] = LUT[ImgArray[x][y][3]];

              // defining the range from 0-255
              if (newImage[x][y][1]<0) { newImage[x][y][1] = 0; }
              if (newImage[x][y][2]<0) { newImage[x][y][2] = 0; }
              if (newImage[x][y][3]<0) { newImage[x][y][3] = 0; }
              if (newImage[x][y][1]>255) { newImage[x][y][1] = 255; }
              if (newImage[x][y][2]>255) { newImage[x][y][2] = 255; }
              if (newImage[x][y][3]>255) { newImage[x][y][3] = 255; }}}

        biFiltered = convertToBimage(newImage); 
        undoHistory.push(biFiltered);// pushing the changes into history stack for undo button
        return;}



    //This function provides the functions related to histogram (Finding histogram, Histogram Normalisation, Histogram Equalisation)
    public void ImageHistogram(){
      System.out.println("Histogram");
      int width = biFiltered.getWidth();
      int height = biFiltered.getHeight();
      int r, g, b;

      int nHistogramR[] = new int[256];
      int nHistogramG[] = new int[256];
      int nHistogramB[] = new int[256];

      int HistogramR[] = new int[256];
      int HistogramG[] = new int[256];
      int HistogramB[] = new int[256];
      int[][][] ImageArray = convertToArray(biFiltered); 
// To construct the histograms for RGB components of an image
      for(int k = 0; k < 256; k++) {
          HistogramR[k] = 0;
          HistogramG[k] = 0;
          HistogramB[k] = 0;
      }

      for(int y=0; y<height; y++){        // Bin Histograms
          for(int x=0; x<width; x++){
              r = ImageArray[x][y][1]; //r
              g = ImageArray[x][y][2]; //g
              b = ImageArray[x][y][3]; //b

              HistogramR[r]++;
              HistogramG[g]++;
              HistogramB[b]++;
          }
      }

      for(int k = 0; k < 256; k++) {      // Normalisation
          nHistogramR[k] = (HistogramR[k]/height)/ width;  // R
          nHistogramG[k] = (HistogramG[k]/height)/ width;  // G
          nHistogramB[k] = (HistogramB[k]/height)/ width;  // B
      }
      //printing out the output in terminal
      System.out.println("Bin Histogram:");
      System.out.println(Arrays.toString(HistogramR));
      System.out.println(Arrays.toString(HistogramG));
      System.out.println(Arrays.toString(HistogramB));
      /*outputHistogram(HistogramR);
      outputHistogram(HistogramG);
      outputHistogram(HistogramB);*/
      
      System.out.println("Normalisation Histogram:");
      /*outputHistogram(nHistogramR);
      outputHistogram(nHistogramG);
      outputHistogram(nHistogramB);*/
      
      System.out.println(Arrays.toString(nHistogramR));
      System.out.println(Arrays.toString(nHistogramG));
      System.out.println(Arrays.toString(nHistogramB));

      return;}


    // Image Filtering 
    public void ImageFiltering(int selectedFilter){
      
      int[][][] newImage = convertToArray(bi);
      int[][][] ImgArray = convertToArray(biFiltered);
      double[][] CurrentMask = new double[][]{{-1,-1,-1},
                                        {-1,-1,-1},
                                        {-1,-1,-1}};

      int width = biFiltered.getWidth();
      int height = biFiltered.getHeight();

      if (selectedFilter == 0){
        double avgValue = 1.0 / 9.0;
        CurrentMask = new double[][] {{avgValue,avgValue,avgValue},
                              {avgValue,avgValue,avgValue},
                              {avgValue,avgValue,avgValue}};}

      else if(selectedFilter == 1){
        double WeightedAvg = 1.0 / 16.0;
        CurrentMask = new double[][]{{WeightedAvg*1.0,WeightedAvg*2.0,WeightedAvg*1.0},
                              {WeightedAvg*2.0,WeightedAvg*4.0,WeightedAvg*2.0},
                              {WeightedAvg*1.0,WeightedAvg*2.0,WeightedAvg*1.0}};}

      else if(selectedFilter == 2){
        CurrentMask = new double[][]{{0.0, -1.0, 0.0},
                              {-1.0, 4.0, -1.0},
                              {0.0, -1.0, 0.0}};}

      else if(selectedFilter == 3){
        CurrentMask = new double[][]{{-1.0, -1.0, -1.0},
                              {-1.0, 8.0, -1.0},
                              {-1.0, -1.0, -1.0}};}

      else if(selectedFilter == 4){
        CurrentMask = new double[][]{{0.0, -1.0, 0.0},
                              {-1.0, 5.0, -1.0},
                              {0.0, -1.0, 0.0}};}

      else if(selectedFilter == 5){
        CurrentMask = new double[][]{{-1.0, -1.0, -1.0},
                              {-1.0, 9.0, -1.0},
                              {-1.0, -1.0, -1.0}};}

      else if(selectedFilter == 6){
        CurrentMask = new double[][]{{0.0, 0.0, 0.0},
                              {0.0, 0.0, -1.0},
                              {0.0, 1.0, 0.0}};}

      else if(selectedFilter == 7){
        CurrentMask = new double[][]{{0.0, 0.0, 0.0},
                              {0.0, -1.0, 0.0},
                              {0.0, 1.0, 1.0}};}

      else if(selectedFilter == 8){
        CurrentMask = new double[][]{{-1.0, 0.0, 1.0},
                              {-2.0, 0.0, 2.0},
                              {-1.0, 0.0, 1.0}};}

      else if(selectedFilter == 9){
        CurrentMask = new double[][]{{-1.0, -2.0, -1.0},
                              {0.0, 0.0, 0.0},
                              {1.0, 2.0, 1.0}};}


      for(int y=1; y<height-1; y++){
        for(int x=1; x<width-1; x++){
          double r=0;
          double g=0;
          double b=0;

          for(int s=-1; s<=1; s++){
            for(int t=-1; t<=1; t++){
              r = r + CurrentMask[1-s][1-t]* (double) ImgArray[x+s][y+t][1]; //r
              g = g + CurrentMask[1-s][1-t]* (double) ImgArray[x+s][y+t][2]; //g
              b = b + CurrentMask[1-s][1-t]* (double) ImgArray[x+s][y+t][3];}} //b
            
          newImage[x][y][1] = (int) Math.round(r); //r
          newImage[x][y][2] = (int) Math.round(g); //g
          newImage[x][y][3] = (int) Math.round(b);}} //b
      
      biFiltered = convertToBimage(newImage);
      undoHistory.push(biFiltered);}


    //This function provides image filtering (Averaging,neighbour Laplacian,neighbour Laplacian Enhancement,Roberts and SobelX and SobelY)
    public void ImageOrderFilter(int orderFilterOptions){
      int [] newValue = new int[3];

      int [][][] newImg = convertToArray(bi);
      int [][][] saltPepper = convertToArray(biFiltered);
      int [][][] ImageArray = convertToArray(biFiltered);
      Random randomNumber = new Random();
      int bounding = 256;

      int width = biFiltered.getWidth();
      int height = biFiltered.getHeight();

      if(orderFilterOptions == 0){ //if salt and pepper noise is selected
        for(int y=0; y<height; y++){
          for(int x=0; x<width; x++){
            saltPepper[x][y][1] = randomNumber.nextInt(bounding);
            saltPepper[x][y][2] = randomNumber.nextInt(bounding);
            saltPepper[x][y][3] = randomNumber.nextInt(bounding);

            if(saltPepper[x][y][1] == 0){
              newImg[x][y][1] = 0;
              newImg[x][y][2] = 0;
              newImg[x][y][3] = 0;}

            else if(saltPepper[x][y][2] == 0){
              newImg[x][y][1] = 0;
              newImg[x][y][2] = 0;
              newImg[x][y][3] = 0;}

            else if(saltPepper[x][y][3] == 0){
              newImg[x][y][1] = 0;
              newImg[x][y][2] = 0;
              newImg[x][y][3] = 0;}

            else if(saltPepper[x][y][1] == 255){
              newImg[x][y][1] = 255;
              newImg[x][y][2] = 255;
              newImg[x][y][3] = 255;}

            else if(saltPepper[x][y][2] == 255){
              newImg[x][y][1] = 255;
              newImg[x][y][2] = 255;
              newImg[x][y][3] = 255;}

            else if(saltPepper[x][y][3] == 255){
              newImg[x][y][1] = 255;
              newImg[x][y][2] = 255;
              newImg[x][y][3] = 255;}}}}

      else if(orderFilterOptions > 0){ // if min-filter is selected
        int [] windowR = new int [9];
        int [] windowG = new int [9];
        int [] windowB = new int [9];

        for(int y=1; y<height-1; y++){
          for(int x=1; x<width-1; x++){
            int k = 0;
            for(int s=-1; s<=1; s++){
              for(int t=-1; t<=1; t++){
                windowR[k] = ImageArray[x+s][y+t][1]; //r
                windowG[k] = ImageArray[x+s][y+t][2]; //g
                windowB[k] = ImageArray[x+s][y+t][3]; //b
                k++;}}
           
            Arrays.sort(windowR);
            Arrays.sort(windowG);
            Arrays.sort(windowB);

            if(orderFilterOptions == 1){  // if min-filter is selected
              newValue[0] = windowR[0];
              newValue[1] = windowG[0];
              newValue[2] = windowB[0];}


            else if(orderFilterOptions == 2){  // if max-filter is selected
              newValue[0] = windowR[8];
              newValue[1] = windowG[8];
              newValue[2] = windowB[8];}

            else if(orderFilterOptions == 3){ // if midpoint-filter is selected
              newValue[0] = Math.round((windowR[0]+windowR[8]) / 2);
              newValue[1] = Math.round((windowG[0]+windowG[8]) / 2);
              newValue[2] = Math.round((windowB[0]+windowB[8]) / 2);}

            else if(orderFilterOptions == 4){ // if median-filter is selected
              newValue[0] = windowR[4];
              newValue[1] = windowG[4];
              newValue[2] = windowB[4];}

            newImg[x][y][1] = newValue[0];    //r
            newImg[x][y][2] = newValue[1];    //g
            newImg[x][y][3] = newValue[2];}}} //b

      biFiltered = convertToBimage(newImg);
      undoHistory.push(biFiltered);}

    //this function provides the threshold function in our program
    public void ImageThresholding(int threshold){
      int [][][] imageThresh = convertToArray(bi);
        int [][][] newGreyImage = imageThresh;
        for(int y=0; y<imageThresh[1].length; y++){
          for(int x=0; x<imageThresh[0].length; x++){
            double avgValue = (0.3 * imageThresh[x][y][1]) + (0.59 * imageThresh[x][y][2]) + (0.11 * imageThresh[x][y][3]);
            int avgPixel = (int) Math.round(avgValue);
            newGreyImage[x][y][1] = avgPixel;
            newGreyImage[x][y][2] = avgPixel;
            newGreyImage[x][y][3] = avgPixel;}}
            imageThresh = newGreyImage;
      //}
      
      int width = bi.getWidth();
      int height = bi.getHeight();

      for(int y=0; y<height; y++){
        for(int x=0; x<width; x++){

          // everything over the treshhold will be set to black
          if(imageThresh[x][y][1] > threshold){
            imageThresh[x][y][1] = 255;
            imageThresh[x][y][2] = 255;
            imageThresh[x][y][3] = 255;}

          // everything under the treshhold will be set to white
          else{
            imageThresh[x][y][1] = 0;
            imageThresh[x][y][2] = 0;
            imageThresh[x][y][3] = 0;}}}

      biFiltered = convertToBimage(imageThresh);
      undoHistory.push(biFiltered);}
    
    //This function resets all the changes that user made in an image
    public void ResetToDefault(){
      formats.setSelectedIndex(0);
      rescaleinput.setText("");
      shiftinput.setText("");
      imageArithOpList.clearSelection();
      imageBooleanOpList.clearSelection();
      ChooseSecondImage.setSelectedIndex(0);
      ChooseROI.setSelectedIndex(0);
      for(int i = 0; i < pointProcessing.length; i++){pointProcessing[i].setSelected(false);}
      
      powerLawInput.setText("");
      bitPlaneInput.setText("");
      treshiNput.setText("");      
      for(int i = 0; i < FilterOptions.length; i++){
        FilterOptions[i].setSelected(false);}
      for(int i = 0; i < orderStatFilterOp.length; i++){
        orderStatFilterOp[i].setSelected(false);}

    }
     

    //This function performs the required action when the user interacts with the UI
    public void actionPerformed(ActionEvent event) { 

      biFiltered = bi;

      if (event.getSource() == scaleShiftButton){ImageRescaleShift();}
      if (event.getSource() == NegativeImg){biFiltered = ImageNegative(biFiltered);}
      if (event.getSource() == scaleShiftResetButton){
        rescaleinput.setText("");
        shiftinput.setText("");}

      if (event.getSource() == resetArithOperation){imageArithOpList.clearSelection();}
      if (event.getSource() == resetBoolOperation){imageBooleanOpList.clearSelection();}
      if (event.getSource() == CalcPowerLaw){ImagePointProcessing(4);}
      if (event.getSource() == calcBitPlane){ImagePointProcessing(5);}
      if (event.getSource() == resetBitPlaneNPower){
        powerLawInput.setText("");
        bitPlaneInput.setText("");}
      if (event.getSource() == histogramButton){ImageHistogram();}
      if (event.getSource() == resetTresh){
        treshiNput.setText("");}
      if (event.getSource() == treshbUtton){
        if(!treshiNput.getText().equals("")){
          ImageThresholding(Integer.parseInt(treshiNput.getText()));}}
      if (event.getSource() == ResetToDefault){ResetToDefault();}

      for (int i = 0; i < imageArithmaticOperations.length; i++){ //keep checking if any checkbox for arithmatic operation has been pressed or not
          if(imageArithmaticOperations[i].isSelected()){
            ImageArithmetic(i);}}
      for (int i = 0; i < imageBooleanOperations.length; i++){ //keep checking if any checkbox for boolean operation has been pressed or not
          if(imageBooleanOperations[i].isSelected()){
            ImageBoolean(i);}}
      for ( int i = 0; i < pointProcessing.length; i++){ //keep checking if checkbox for point processing has been pressed or not
        if(pointProcessing[i].isSelected()){
          ImagePointProcessing(i);}}
      for (int i = 0; i < FilterOptions.length; i++){ //keep checking if any checkbox for filtering options has been pressed or not
        if(FilterOptions[i].isSelected()){
          ImageFiltering(i);}}
      for (int i = 0; i < orderStatFilterOp.length; i++){ //keep checking if any checkbox for order statistics filtering has been pressed or not
        if(orderStatFilterOp[i].isSelected()){
          ImageOrderFilter(i);}}


       if (event.getSource() == formats) {
          String format = (String)formats.getSelectedItem();
          if(!format.equals("-select-")){
            File saveFile = new File("savedimage."+format);
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(saveFile);
            int rval = chooser.showSaveDialog(formats);
            if (rval == JFileChooser.APPROVE_OPTION) {
                saveFile = chooser.getSelectedFile();
                try {ImageIO.write(biFiltered, format, saveFile);} 
                catch (IOException ex) {}}}
          formats.setSelectedIndex(0);}

       else if (event.getSource() == AllFiles){
          String selectedFile = AllFiles.getSelectedItem().toString();
          try{
              if (selectedFile.contains(".raw")){loadRawImage(new File("images/" + selectedFile));}
              
              else{
                  bi = ImageIO.read(new File("images/" + selectedFile));
                  w = bi.getWidth(null);
                  h = bi.getHeight(null);
                  if (bi.getType() != BufferedImage.TYPE_INT_RGB) {biFiltered = bi;}}}
          catch(IOException err){}}

      if (event.getSource() == undoButton){
        if(!undoHistory.empty()){biFiltered = undoHistory.pop();}
        //if can't undo anymore
        else{System.out.println("No History");}
      }
      //re-paints the updated image according to the action performed
      repaint();
      }

    //To get all the files in images folder to display in dropdown, so that user can select the image they want to work with
    public static String[] getFiles(){
        ArrayList<String> files = new ArrayList<String>();
        files.add("-select-");
        File image = new File("images/");

        files.addAll(Arrays.asList(image.list()));
        files.sort(String::compareToIgnoreCase);
        
        for(int i = 0; i < files.size(); i++){
          if(files.get(i).matches("^\\..*$")){
            files.remove(i);}
          if(files.get(i).equals("ROI")){
            files.remove(i);}}
      
        String[] simplePaths = new String[files.size()];
        return files.toArray(simplePaths);}


    //function to get the selected ROI from images/ROI
    public static String[] getROIFiles(){

      ArrayList<String> files = new ArrayList<String>();
      files.add("-Select-");
      File image = new File("images/ROI/");

      files.addAll(Arrays.asList(image.list()));
      files.sort(String::compareToIgnoreCase);

      for(int i = 0; i < files.size(); i++){
        if(files.get(i).matches("^\\..*$")){
          files.remove(i);}
        if(files.get(i).equals("ROI")){
          files.remove(i);}}
 
      String[] simplePaths = new String[files.size()];
      return files.toArray(simplePaths);}


  //function to be able to read .raw file
  public void loadRawImage(File file){
        BufferedImage image;
        try{
            byte[] fc = Files.readAllBytes(file.toPath());
            
            // only for 128 square pixel images
            if(fc.length == (128*128)){
                image = new BufferedImage(128,128,BufferedImage.TYPE_BYTE_GRAY);
                image.getRaster().setDataElements(0, 0, 128, 128, fc);
                bi = image;
                w = bi.getWidth(null);
                h = bi.getHeight(null);
                if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                    biFiltered = bi;}}


            // only for 512 square pixel images
            else if(fc.length == 512*512){
                image = new BufferedImage(512,512,BufferedImage.TYPE_BYTE_GRAY);
                image.getRaster().setDataElements(0, 0, 512, 512, fc);
                bi = image;
                w = bi.getWidth(null);
                h = bi.getHeight(null);
                if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                    biFiltered = bi;}}}

        catch(IOException event){JOptionPane.showMessageDialog(null, "invalid Image");}}

        //Main function
    public static void main(String s[]) {
        String [] allFiles = getFiles(); //files from images directory

        JFrame frame = new JFrame("Image Processing Demo");
        frame.addWindowListener(new WindowAdapter(){ 
            public void windowClosing(WindowEvent event) {//checking if the user closes the GUI window
              System.exit(0);}});

        Demo defaultImage = new Demo(); //selected image
        Demo editedImage = new Demo(); //edited image

        //positions of both images
        frame.add("West", defaultImage);
        frame.add("Center", editedImage);

        //all images for the user to choose from
        AllFiles = new JComboBox(allFiles);
        AllFiles.setActionCommand("Files");
        AllFiles.addActionListener(editedImage);
        AllFiles.addActionListener(defaultImage);

        //all formats for the images
        formats = new JComboBox(editedImage.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(editedImage);

        // button to reset all the settings or changes made to the image
        ResetToDefault = new JButton("Reset All Settings");
        ResetToDefault.addActionListener(editedImage);

        // return the previous buffered image
        undoButton = new JButton("Undo");
        undoButton.addActionListener(editedImage);

        //panel on right side of the screen for all the buttons and filters
        JPanel RtMenuPanel = new JPanel();
        RtMenuPanel.setLayout(new BoxLayout(RtMenuPanel, BoxLayout.Y_AXIS));

        //dropdown menu for the images directory
        JPanel r0sub0 = new JPanel();
        r0sub0.setLayout(new FlowLayout());
        r0sub0.add(AllFiles);
        RtMenuPanel.add(r0sub0);

        //available formats dropdown
        JPanel r0sub1 = new JPanel();
        r0sub1.setLayout(new FlowLayout());
        r0sub1.add(new JLabel("Save As"));
        r0sub1.add(formats);
        RtMenuPanel.add(r0sub1);


        //undo and reset all settings button
        JPanel r0sub2 = new JPanel();
        r0sub2.setLayout(new FlowLayout());
        r0sub2.add(undoButton);
        r0sub2.add(ResetToDefault);
        RtMenuPanel.add(r0sub2);

        //buttons and input area for rescale and shift
        rescaleinput = new JTextField("");
        rescaleinput.setPreferredSize(new Dimension(50,30));
        shiftinput = new JTextField("");
        shiftinput.setPreferredSize(new Dimension(50,30));
        scaleShiftButton = new JButton("Rescale/Shift");
        scaleShiftButton.addActionListener(editedImage);
        scaleShiftResetButton = new JButton("Reset");
        scaleShiftResetButton.addActionListener(editedImage);
        NegativeImg = new JButton("Negative Image");
        NegativeImg.addActionListener(editedImage);
        //rescale & shifting label , and adding reset button
        JPanel r1sub0 = new JPanel();
        r1sub0.setBackground(Color.WHITE);
        r1sub0.setLayout(new FlowLayout());
        JLabel RescaleShiftLabel = new JLabel("Rescale and Shifting");
        RescaleShiftLabel.setForeground(Color.BLUE);
        r1sub0.add(RescaleShiftLabel);
        r1sub0.add(scaleShiftResetButton);
        RtMenuPanel.add(r1sub0);
        JPanel r1sub1 = new JPanel();
        r1sub1.setBackground(Color.WHITE);
        r1sub1.setLayout(new FlowLayout());
        r1sub1.add(new JLabel("Rescale(float,0-2):"));
        r1sub1.add(rescaleinput);
        r1sub1.add(new JLabel("Shift(int,0-255):"));
        r1sub1.add(shiftinput);
        RtMenuPanel.add(r1sub1);
        JPanel r1sub2 = new JPanel();
        r1sub2.setBackground(Color.WHITE);
        r1sub2.setLayout(new FlowLayout());
        r1sub2.add(scaleShiftButton);
        r1sub2.add(NegativeImg);
        RtMenuPanel.add(r1sub2);

        //dropdown for second image and ROI image
        ChooseSecondImage = new JComboBox(allFiles);
        ChooseROI = new JComboBox(getROIFiles());
        //options for Arithmatic operations
        imageArithmaticOperations = new JRadioButton[4];
 
        imageArithOpList = new ButtonGroup();

        for(int i = 0; i < imageArithmaticOperations.length; i++){
          imageArithmaticOperations[i] = new JRadioButton();
          imageArithmaticOperations[i].addActionListener(editedImage);
          imageArithOpList.add(imageArithmaticOperations[i]);}
        //reset button for arithmatic operations
        resetArithOperation = new JButton("Reset");
        resetArithOperation.addActionListener(editedImage);
        imageBooleanOperations = new JRadioButton[4];
        imageBooleanOpList = new ButtonGroup();

        for(int i = 0; i < imageBooleanOperations.length; i++){
          imageBooleanOperations[i] = new JRadioButton();
          imageBooleanOperations[i].addActionListener(editedImage);
          imageBooleanOpList.add(imageBooleanOperations[i]);}
        //reset button for boolean operation
        resetBoolOperation = new JButton("Reset");
        resetBoolOperation.addActionListener(editedImage);

        JPanel r2sub1 = new JPanel();
        r2sub1.setLayout(new FlowLayout());
        r2sub1.add(new JLabel("choose Second Image:"));
        RtMenuPanel.add(r2sub1);

        
        JPanel r2sub2 = new JPanel();
        r2sub2.setLayout(new FlowLayout());
        r2sub2.add(ChooseSecondImage);
        RtMenuPanel.add(r2sub2);

 
        JPanel r2sub3 = new JPanel();
        r2sub3.setLayout(new FlowLayout());
        r2sub3.add(new JLabel("choose ROI:"));
        RtMenuPanel.add(r2sub3);


 
        JPanel r2sub4 = new JPanel();
        r2sub4.setLayout(new FlowLayout());
        r2sub4.add(ChooseROI);
        RtMenuPanel.add(r2sub4);

        //the "Arithmatic operation label", and the options for arithmatic operations
        JPanel r3sub0 = new JPanel();
        r3sub0.setBackground(Color.WHITE);
        r3sub0.setLayout(new FlowLayout());
        JLabel imgari = new JLabel("Arithmetic Operations");
        imgari.setForeground(Color.BLUE);
        r3sub0.add(imgari);
        r3sub0.add(resetArithOperation);
        RtMenuPanel.add(r3sub0);
        //for add
        JPanel r3sub1 = new JPanel();
        r3sub1.setBackground(Color.WHITE);
        r3sub1.setLayout(new FlowLayout());
        r3sub1.add(imageArithmaticOperations[0]);
        r3sub1.add(new JLabel("Add"));
        RtMenuPanel.add(r3sub1);
        
        //for subtract
        JPanel r3sub2 = new JPanel();
        r3sub2.setBackground(Color.WHITE);
 
        r3sub2.add(imageArithmaticOperations[1]);
        r3sub2.add(new JLabel("Subtract"));
        RtMenuPanel.add(r3sub2);
        
        //for multiply
        JPanel r3sub3 = new JPanel();
        r3sub3.setBackground(Color.WHITE);
        r3sub3.setLayout(new FlowLayout());
        r3sub3.add(imageArithmaticOperations[2]);
        r3sub3.add(new JLabel("Multiply"));
        RtMenuPanel.add(r3sub3);
        //for divide
        JPanel r3sub4 = new JPanel();
        r3sub4.setBackground(Color.WHITE);
        r3sub4.add(imageArithmaticOperations[3]);
        r3sub4.add(new JLabel("Divide"));
        RtMenuPanel.add(r3sub4);


        //label for "Boolean Operations" and the options for operations
        JPanel boolOpLabel = new JPanel();
        boolOpLabel.setLayout(new FlowLayout());
        JLabel imgbool = new JLabel("Boolean Operations");
        imgbool.setForeground(Color.BLUE);
        boolOpLabel.add(imgbool);
        boolOpLabel.add(resetBoolOperation);

        //for NOT and AND
        JPanel r4sub1 = new JPanel();
        r4sub1.setLayout(new FlowLayout());
        r4sub1.add(imageBooleanOperations[0]);
        r4sub1.add(new JLabel("NOT"));
        r4sub1.add(imageBooleanOperations[1]);
        r4sub1.add(new JLabel("AND"));
        RtMenuPanel.add(r4sub1);


        //for OR and XOR
        JPanel r4sub2 = new JPanel();
        r4sub2.setLayout(new FlowLayout());
        r4sub2.add(imageBooleanOperations[2]);
        r4sub2.add(new JLabel("OR"));
        r4sub2.add(imageBooleanOperations[3]);
        r4sub2.add(new JLabel("XOR"));
        
        RtMenuPanel.add(boolOpLabel);
        RtMenuPanel.add(r4sub2);

        //power-law and bit plane slicing
        powerLawInput = new JTextField("");
        powerLawInput.setPreferredSize(new Dimension(70,25));
        bitPlaneInput = new JTextField("");
        bitPlaneInput.setPreferredSize(new Dimension(70,25));
        pointProcessing = new JCheckBox[3];

        for (int i = 0; i < pointProcessing.length; i++){
          pointProcessing[i] = new JCheckBox();
          pointProcessing[i].addActionListener(editedImage);}
        //button for calculating power-law
        CalcPowerLaw = new JButton("PowerLaw");
        CalcPowerLaw.addActionListener(editedImage);
        //button for calculating bit plane processing
        calcBitPlane = new JButton("BitPlane");
        calcBitPlane.addActionListener(editedImage);
        resetBitPlaneNPower = new JButton("Reset");
        resetBitPlaneNPower.addActionListener(editedImage);

        //label for point processing and bit plane slicing
        JPanel r5sub0 = new JPanel();
        r5sub0.setBackground(Color.WHITE);
        r5sub0.setLayout(new FlowLayout());
        JLabel PointProcessLabel = new JLabel("Point processing and bit plane slicing");
        PointProcessLabel.setForeground(Color.BLUE);
        r5sub0.add(PointProcessLabel);

        //label for Nagative linear transform
        JPanel r5sub1 = new JPanel();
        r5sub1.setBackground(Color.WHITE);
        r5sub1.setLayout(new FlowLayout());
        r5sub1.add(pointProcessing[0]);
        r5sub1.add(new JLabel("Negative Linear Transforms"));
        
        //label for Log Function
        JPanel r5sub2 = new JPanel();
        r5sub2.setBackground(Color.WHITE);
        r5sub2.setLayout(new FlowLayout());
        r5sub2.add(pointProcessing[1]);
        r5sub2.add(new JLabel("Logarithmic Function"));

        //label for random look up table
        JPanel r5sub3 = new JPanel();
        r5sub3.setBackground(Color.WHITE);
        r5sub3.setLayout(new FlowLayout());
        r5sub3.add(pointProcessing[2]);
        r5sub3.add(new JLabel("Random Look-Up Table"));
        RtMenuPanel.add(r5sub0);
        RtMenuPanel.add(r5sub1);
        RtMenuPanel.add(r5sub2);
        RtMenuPanel.add(r5sub3);

        //label for power law
        JPanel r6sub0 = new JPanel();
        r6sub0.setLayout(new FlowLayout());
        r6sub0.add(new JLabel("Power-Law (0.01-25):"));
        r6sub0.add(powerLawInput);
        
        //bit plane slicing label
        JPanel r6sub1 = new JPanel();
        r6sub1.setLayout(new FlowLayout());
        r6sub1.add(new JLabel("Bit-plane slicing (0-7):"));
        r6sub1.add(bitPlaneInput);
        
        //adding buttons for power law, bit plane, and reset
        JPanel r6sub2 = new JPanel();
        r6sub2.setLayout(new FlowLayout());
        r6sub2.add(CalcPowerLaw);
        r6sub2.add(calcBitPlane);
        r6sub2.add(resetBitPlaneNPower);
        
        RtMenuPanel.add(r6sub0);
        RtMenuPanel.add(r6sub1);
        RtMenuPanel.add(r6sub2);
        
        //buttons and labels for histogram
        histogramNoPixels = new JLabel("-");
        histogramNoPixels.setPreferredSize(new Dimension(80,30));
        histogramButton = new JButton("Press here");
        histogramButton.addActionListener(editedImage);
        JPanel r7sub0 = new JPanel();
        r7sub0.setBackground(Color.WHITE);
        r7sub0.setLayout(new FlowLayout());
        JLabel histoLabel = new JLabel("Histogram");
        histoLabel.setForeground(Color.BLUE);
        r7sub0.add(histoLabel);
        JPanel r7sub2 = new JPanel();
        r7sub2.setBackground(Color.WHITE);
        r7sub2.setLayout(new FlowLayout());
        r7sub2.add(new JLabel("Histogram (output in terminal):"));
        //button for histogram output
        r7sub2.add(histogramButton);
        RtMenuPanel.add(r7sub0);
        RtMenuPanel.add(r7sub2);
        //Treshold Apply button and reset button
        treshiNput = new JTextField("");
        treshiNput.setPreferredSize(new Dimension(50,30));
        treshbUtton = new JButton("Apply");
        treshbUtton.addActionListener(editedImage);
        resetTresh = new JButton("Reset");
        resetTresh.addActionListener(editedImage);

        //label "Thresholding"
        JPanel r10sub0 = new JPanel();
        r10sub0.setLayout(new FlowLayout());
        JLabel ThreshLabel = new JLabel("Thresholding");
        r10sub0.setBackground(Color.WHITE);
        ThreshLabel.setForeground(Color.BLUE);
        //adding the elements to the panel
        r10sub0.add(ThreshLabel);
        r10sub0.add(resetTresh);

        //Label to tell the range for user input
        JPanel r10sub1 = new JPanel();
        r10sub1.setLayout(new FlowLayout());
        r10sub1.setBackground(Color.WHITE);
        r10sub1.add(new JLabel("Thresholding value(0-255):"));

        //adding components to the panel
        JPanel r10sub2 = new JPanel();
        r10sub2.setLayout(new FlowLayout());
        r10sub2.add(treshiNput);
        r10sub2.setBackground(Color.WHITE);
        r10sub2.add(treshbUtton);

        RtMenuPanel.add(r10sub0);
        RtMenuPanel.add(r10sub1);
        RtMenuPanel.add(r10sub2);

        //checking for pressed filter options
        FilterOptions = new JCheckBox[10];
        for(int i = 0; i < FilterOptions.length; i++){
          FilterOptions[i] = new JCheckBox();
          FilterOptions[i].addActionListener(editedImage);}

        //label for image filtering
        JPanel r8sub0 = new JPanel();
        r8sub0.setLayout(new FlowLayout());
        JLabel ImgFilLabel = new JLabel("Image filtering");
        ImgFilLabel.setForeground(Color.BLUE);
        r8sub0.add(ImgFilLabel);
        RtMenuPanel.add(r8sub0);
        //labels and checkbox for all image filters from lab6
        JPanel r8sub1 = new JPanel();
        r8sub1.setLayout(new FlowLayout());
        r8sub1.add(FilterOptions[0]);
        r8sub1.add(new JLabel("Averaging"));
        r8sub1.add(FilterOptions[1]);
        r8sub1.add(new JLabel("Weighted Averaging"));
        RtMenuPanel.add(r8sub1);
        JPanel r8sub2 = new JPanel();
        r8sub2.setLayout(new FlowLayout());
        r8sub2.add(FilterOptions[2]);
        r8sub2.add(new JLabel("4-neighbour Laplacian"));
        RtMenuPanel.add(r8sub2);
        JPanel r8sub3 = new JPanel();
        r8sub3.setLayout(new FlowLayout());
        r8sub3.add(FilterOptions[3]);
        r8sub3.add(new JLabel("8-neighbour Laplacian"));
        RtMenuPanel.add(r8sub3);
        JPanel r8sub4 = new JPanel();
        r8sub4.setLayout(new FlowLayout());
        r8sub4.add(FilterOptions[4]);
        r8sub4.add(new JLabel("4-neighbour Laplacian Enhancement"));
        RtMenuPanel.add(r8sub4);
        JPanel r8sub5 = new JPanel();
        r8sub5.setLayout(new FlowLayout());
        r8sub5.add(FilterOptions[5]);
        r8sub5.add(new JLabel("8-neighbour Laplacian Enhancement"));
        RtMenuPanel.add(r8sub5);
        JPanel r8sub6 = new JPanel();
        r8sub6.setLayout(new FlowLayout());
        r8sub6.add(FilterOptions[6]);
        r8sub6.add(new JLabel("Roberts(i)"));
        r8sub6.add(FilterOptions[7]);
        r8sub6.add(new JLabel("Roberts(ii)"));
        RtMenuPanel.add(r8sub6);
        JPanel r8sub7 = new JPanel();
        r8sub7.setLayout(new FlowLayout());
        r8sub7.add(FilterOptions[8]);
        r8sub7.add(new JLabel("SobelX"));
        r8sub7.add(FilterOptions[9]);
        r8sub7.add(new JLabel("SobelY"));
        RtMenuPanel.add(r8sub7);

        //checking for pressed order-statistics filtering options
        orderStatFilterOp = new JCheckBox[5];
        for(int i = 0; i < orderStatFilterOp.length; i++){
          orderStatFilterOp[i] = new JCheckBox();
          orderStatFilterOp[i].addActionListener(editedImage);}
      
          
        //labels and checkbox for all order-stats filters from lab7
        JPanel r9sub0 = new JPanel();
        r9sub0.setBackground(Color.WHITE);
        r9sub0.setLayout(new FlowLayout());
        JLabel OrderStatLabel = new JLabel("Order-statistics filtering");
        OrderStatLabel.setForeground(Color.BLUE);
        r9sub0.add(OrderStatLabel);
        RtMenuPanel.add(r9sub0);
        JPanel r9sub1 = new JPanel();
        r9sub1.setBackground(Color.WHITE);
        r9sub1.setLayout(new FlowLayout());
        r9sub1.add(orderStatFilterOp[0]);
        r9sub1.add(new JLabel("Salt-and-Pepper Noise"));
        RtMenuPanel.add(r9sub1);
        JPanel r9sub2 = new JPanel();
        r9sub2.setBackground(Color.WHITE);
        r9sub2.setLayout(new FlowLayout());
        r9sub2.add(orderStatFilterOp[1]);
        r9sub2.add(new JLabel("Min-Filtering"));
        r9sub2.add(orderStatFilterOp[2]);
        r9sub2.add(new JLabel("Max-Filtering"));
        RtMenuPanel.add(r9sub2);
        JPanel r9sub3 = new JPanel();
        r9sub3.setBackground(Color.WHITE);
        r9sub3.setLayout(new FlowLayout());
        r9sub3.add(orderStatFilterOp[3]);
        r9sub3.add(new JLabel("Midpoint-Filtering"));
        r9sub3.add(orderStatFilterOp[4]);
        r9sub3.add(new JLabel("Median-Filtering"));
        RtMenuPanel.add(r9sub3);

        //scroll panel for the side bar 
        JScrollPane Scrollpanel = new JScrollPane(RtMenuPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //incrementing speed for scrolling both horizontally and vertically
        Scrollpanel.getVerticalScrollBar().setUnitIncrement(12);
        Scrollpanel.getHorizontalScrollBar().setUnitIncrement(12);
        //getting the size for scrollpanel by using the height and width of input image using function getPreferredSize()
        Scrollpanel.getPreferredSize();
        //location of the scrollpanel in the frame
        frame.add("East", Scrollpanel);
        frame.pack();
        //setting the frame visible
        frame.setVisible(true);
    }
}