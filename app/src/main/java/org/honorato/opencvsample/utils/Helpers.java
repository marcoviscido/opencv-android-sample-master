package org.honorato.opencvsample.utils;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

/**
 * Created by m.viscido on 28/06/2016.
 */
public class Helpers {
    static Point p1 = new Point();
    static Point p2 = new Point();
    static Point p3 = new Point();
    static Point p4 = new Point();

    public static boolean isContourSquare(MatOfPoint thisContour) {

        Rect ret = null;

        MatOfPoint2f thisContour2f = new MatOfPoint2f();
        MatOfPoint approxContour = new MatOfPoint();
        MatOfPoint2f approxContour2f = new MatOfPoint2f();

        thisContour.convertTo(thisContour2f, CvType.CV_32FC2);

        Imgproc.approxPolyDP(thisContour2f, approxContour2f, 2, true);

        approxContour2f.convertTo(approxContour, CvType.CV_32S);

        if (approxContour.size().height == 4) {
            ret = Imgproc.boundingRect(approxContour);
        }

        return (ret != null);
    }

    public static List<MatOfPoint> getSquareContours(List<MatOfPoint> contours) {

        List<MatOfPoint> squares = null;

        for (MatOfPoint c : contours) {

            if (Helpers.isContourSquare(c))
            {
                if (squares == null)
                    squares = new ArrayList<MatOfPoint>();
                squares.add(c);
            }
        }

        return squares;
    }

    public static Mat findLargestRectangle(Mat original_image) {
        Mat imgProcess = new Mat();


        Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2HSV);
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(0,0), 7, 7, 0);

        java.util.List<Mat> hsvPlanes = new LinkedList<Mat>();
        Core.split(imgProcess, hsvPlanes);

        //Imgproc.adaptiveThreshold(hsvPlanes.get(0), hsvPlanes.get(0), 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 5, 0);
        //Imgproc.adaptiveThreshold(hsvPlanes.get(1), hsvPlanes.get(1), 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 5, 0);
        Imgproc.adaptiveThreshold(hsvPlanes.get(2), hsvPlanes.get(2), 235, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 9, 0);
        //Core.merge(hsvPlanes, imgProcess);

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(hsvPlanes.get(2), contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0) );

        List<MatOfPoint> squareContours = getSquareContours(contours);

        double maxArea = -1;
        MatOfPoint temp_contour = squareContours.get(0); //the largest is at the index 0 for starting point
        MatOfPoint2f approxCurve = new MatOfPoint2f();
        MatOfPoint2f maxCurve = new MatOfPoint2f();
        List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();

        for (int idx = 0; idx < contours.size(); idx++) {

            temp_contour = contours.get(idx);
            double contourarea = Imgproc.contourArea(temp_contour);
            //compare this contour to the previous largest contour found
            if (contourarea > maxArea) {
                //check if this contour is a square
                MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                int contourSize = (int)temp_contour.total();
                Imgproc.approxPolyDP(new_mat, approxCurve, contourSize*0.05, true);

                if (approxCurve.total() == 4) {
                    maxCurve = approxCurve;
                    maxArea = contourarea;
                    largest_contours.add(temp_contour);
                }

            }
        }



        double temp_double[] = maxCurve.get(0, 0);
        if(temp_double!=null) {
            p1 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p1.x, p1.y), 5, new Scalar(255, 0, 0), 5); //p1 is colored red
        }

        temp_double = maxCurve.get(1, 0);
        if(temp_double!=null) {
            p2 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p2.x, p2.y), 5, new Scalar(0, 255, 0), 5); //p2 is colored green
        }

        temp_double = maxCurve.get(2, 0);
        if(temp_double!=null) {
            p3 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p3.x, p3.y), 5, new Scalar(0, 0, 255), 5); //p3 is colored blue
        }

        temp_double = maxCurve.get(3, 0);
        if(temp_double!=null) {
            p4 = new Point(temp_double[0], temp_double[1]);
            Imgproc.circle(original_image, new Point(p4.x, p4.y), 5, new Scalar(0, 255, 255), 5); //p1 is colored violet
        }

        System.out.println("PUNTI" + p1+p2);
        return original_image;

    }




    public static Mat cropArea(Mat original_image) {


        //TAGLIARE IL RETTANGOLO
        Mat mask = new Mat(original_image.rows(), original_image.cols(), CvType.CV_8UC1);
        for(int i=0; i<mask.cols(); i++)
            for(int j=0; j<mask.rows(); j++)
                mask.put(i, j, 0);

        // Create Polygon from vertices
        MatOfPoint2f ROI_Poly = new MatOfPoint2f();
        MatOfPoint2f Vertices = new MatOfPoint2f(p1,p2,p3,p4);
        Imgproc.approxPolyDP(new MatOfPoint2f(p1,p2,p3,p4), ROI_Poly, 1.0, true);

        // Fill polygon white
        MatOfPoint RP = new MatOfPoint();
        ROI_Poly.convertTo(RP, CvType.CV_32S);
        Imgproc.fillConvexPoly(mask, RP, new Scalar(255, 255, 255), 8, 0);

        // Create new image for result storage
        Mat imageDest = new Mat(original_image.rows(), original_image.cols(), CvType.CV_8UC1);

        // Cut out ROI and store it in imageDest
        original_image.copyTo(imageDest, mask);

        return imageDest;
    }

    public static native int MySimpleBlobDetector(long matAddr);

    public static ArrayList findBlob(Mat original_image) {
        Mat imgProcess = new Mat();
        Imgproc.cvtColor(original_image, imgProcess, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgProcess, imgProcess, new Size(1, 1), 1);
        Imgproc.threshold(imgProcess, imgProcess, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        int r = MySimpleBlobDetector(imgProcess.getNativeObjAddr());

        ArrayList result = new ArrayList();
        result.add(imgProcess);
        result.add(r);
        return result;
    }



}
