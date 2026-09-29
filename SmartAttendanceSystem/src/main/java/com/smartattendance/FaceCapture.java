package com.smartattendance;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;

public class FaceCapture {
    private final CascadeClassifier detector;

    public FaceCapture() {
        detector = new CascadeClassifier(
            "src/main/resources/haarcascade_frontalface_default.xml"
        );
    }

    public int capture(int studentId, int targetImages) {
        new File("dataset").mkdirs();

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            throw new IllegalStateException("Could not open webcam.");
        }

        int saved = 0;
        Mat frame = new Mat();

        while (saved < targetImages) {
            if (!camera.read(frame)) break;

            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

            MatOfRect faces = new MatOfRect();
            detector.detectMultiScale(gray, faces);

            Rect[] detected = faces.toArray();

            if (detected.length > 0) {
                Rect face = largest(detected);
                Mat crop = new Mat(gray, face);

                String path = String.format(
                    "dataset/user.%d.%d.jpg", studentId, saved + 1
                );
                Imgcodecs.imwrite(path, crop);
                saved++;

                Imgproc.rectangle(
                    frame,
                    face,
                    new org.opencv.core.Scalar(0, 255, 0),
                    2
                );
                Imgproc.putText(
                    frame,
                    "Captured: " + saved + "/" + targetImages,
                    new org.opencv.core.Point(20, 40),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.8,
                    new org.opencv.core.Scalar(0, 255, 0),
                    2
                );
            } else {
                Imgproc.putText(
                    frame,
                    "Place your face in front of camera",
                    new org.opencv.core.Point(20, 40),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.7,
                    new org.opencv.core.Scalar(0, 0, 255),
                    2
                );
            }

            HighGui.imshow("Face Registration - Press Q to stop", frame);
            int key = HighGui.waitKey(60);
            if (key == 'q' || key == 27) break;
        }

        camera.release();
        HighGui.destroyAllWindows();
        return saved;
    }

    private Rect largest(Rect[] faces) {
        Rect best = faces[0];
        for (Rect r : faces) {
            if (r.area() > best.area()) best = r;
        }
        return best;
    }
}
