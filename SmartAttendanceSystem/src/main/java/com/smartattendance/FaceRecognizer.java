package com.smartattendance;

import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.face.LBPHFaceRecognizer;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FaceRecognizer {
    private final LBPHFaceRecognizer model = LBPHFaceRecognizer.create();
    private final String modelPath = "trainer/trainer.yml";

    public void train() {
        File folder = new File("dataset");
        File[] files = folder.listFiles((d, n) -> n.toLowerCase().endsWith(".jpg"));

        if (files == null || files.length == 0) {
            throw new IllegalStateException("No dataset images found.");
        }

        List<Mat> images = new ArrayList<>();
        List<Integer> labels = new ArrayList<>();

        for (File file : files) {
            String[] parts = file.getName().split("\\.");
            if (parts.length < 3) continue;

            try {
                int id = Integer.parseInt(parts[1]);
                Mat image = Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_GRAYSCALE);
                if (!image.empty()) {
                    images.add(image);
                    labels.add(id);
                }
            } catch (NumberFormatException ignored) {}
        }

        if (images.isEmpty()) {
            throw new IllegalStateException("No valid training images found.");
        }

        MatOfInt labelMat = new MatOfInt();
        labelMat.fromArray(labels.stream().mapToInt(Integer::intValue).toArray());

        model.train(images, labelMat);
        new File("trainer").mkdirs();
        model.save(modelPath);
    }

    public void load() {
        File file = new File(modelPath);
        if (!file.exists()) {
            throw new IllegalStateException("Train the face model first.");
        }
        model.read(file.getAbsolutePath());
    }

    public int predict(Mat grayFace) {
        int[] label = new int[1];
        double[] confidence = new double[1];
        model.predict(grayFace, label, confidence);

        // LBPH returns a distance; lower is a closer match.
        return confidence[0] <= 70.0 ? label[0] : -1;
    }
}
