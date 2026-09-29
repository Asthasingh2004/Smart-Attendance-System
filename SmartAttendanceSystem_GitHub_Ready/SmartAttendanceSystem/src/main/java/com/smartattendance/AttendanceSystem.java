package com.smartattendance;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.objdetect.CascadeClassifier;

public class AttendanceSystem {
    private final FaceRecognizer recognizer;
    private final CascadeClassifier detector = new CascadeClassifier(
        "src/main/resources/haarcascade_frontalface_default.xml"
    );

    public AttendanceSystem(FaceRecognizer recognizer) {
        this.recognizer = recognizer;
    }

    public void start() {
        recognizer.load();

        VideoCapture camera = new VideoCapture(0);
        if (!camera.isOpened()) {
            throw new IllegalStateException("Could not open webcam.");
        }

        Mat frame = new Mat();

        while (true) {
            if (!camera.read(frame)) break;

            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

            MatOfRect faces = new MatOfRect();
            detector.detectMultiScale(gray, faces);

            for (Rect rect : faces.toArray()) {
                Mat face = new Mat(gray, rect);
                int studentId = recognizer.predict(face);

                String message = "Unknown";
                org.opencv.core.Scalar color = new org.opencv.core.Scalar(0, 0, 255);

                if (studentId != -1) {
                    try {
                        Student student = Database.getStudent(studentId);
                        if (student != null) {
                            boolean marked = Database.markAttendance(studentId);
                            message = marked
                                ? "Present: " + student.getName()
                                : "Already Present: " + student.getName();
                            color = new org.opencv.core.Scalar(0, 255, 0);
                        }
                    } catch (Exception e) {
                        message = "Database error";
                    }
                }

                Imgproc.rectangle(frame, rect, color, 2);
                Imgproc.putText(
                    frame, message,
                    new org.opencv.core.Point(rect.x, Math.max(25, rect.y - 10)),
                    Imgproc.FONT_HERSHEY_SIMPLEX, 0.65, color, 2
                );
            }

            Imgproc.putText(
                frame, "Press Q or ESC to close",
                new org.opencv.core.Point(20, 30),
                Imgproc.FONT_HERSHEY_SIMPLEX, 0.7,
                new org.opencv.core.Scalar(255, 255, 255), 2
            );

            HighGui.imshow("Smart Attendance", frame);
            int key = HighGui.waitKey(30);
            if (key == 'q' || key == 27) break;
        }

        camera.release();
        HighGui.destroyAllWindows();
    }
}
