import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import pl.edu.uj.JImageStream.api.core.Filter;
import pl.edu.uj.JImageStream.collectors.BufferedImageCollector;
import pl.edu.uj.JImageStream.filters.color.*;
import pl.edu.uj.JImageStream.filters.convolve.BoxBlurFilter;
import pl.edu.uj.JImageStream.filters.convolve.EmbossFilter;
import pl.edu.uj.JImageStream.filters.convolve.SharpenFilter;
import pl.edu.uj.JImageStream.filters.edge.*;
import pl.edu.uj.JImageStream.filters.noise.GaussFilter;
import pl.edu.uj.JImageStream.filters.noise.SaltAndPepperFilter;
import pl.edu.uj.JImageStream.filters.statistical.MaxFilter;
import pl.edu.uj.JImageStream.filters.statistical.MeanFilter;
import pl.edu.uj.JImageStream.filters.statistical.MedianFilter;
import pl.edu.uj.JImageStream.filters.statistical.MinFilter;
import pl.edu.uj.JImageStream.model.ColorChannel;
import pl.edu.uj.JImageStream.model.StreamableImage;
import pl.edu.uj.JImageStream.predicates.CirclePredicate;
import pl.edu.uj.JImageStream.predicates.ColorPredicate;
import pl.edu.uj.JImageStream.predicates.ColorRangePredicate;
import pl.edu.uj.JImageStream.predicates.ThresholdPredicate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;

public class Main {
    static final ClassLoader loader = Main.class.getClassLoader();
    private static MarvinImage image,
            backupImage;
    private static MarvinImagePlugin imagePlugin;

    public static void applyPredicate(StreamableImage streamableImage, Predicate predicate, String fileName) throws IOException {
        Instant before = Instant.now();

        BufferedImage bufferedImage = streamableImage.stream()
                .bounds(predicate)
                .apply(new RedFilter())
                .collect(new BufferedImageCollector());
        Instant after = Instant.now();
        System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
        File outputfile = new File(fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public static void applyFilter(StreamableImage streamableImage, Filter filter, String fileName) throws IOException {
        Instant before = Instant.now();
        BufferedImage bufferedImage = streamableImage.stream()
                .apply(filter)
                .collect(new BufferedImageCollector());

        Instant after = Instant.now();
        System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
        File outputfile = new File(fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public static void marvinFilter(String pluginPath, String source, String result) throws IOException {
        image = MarvinImageIO.loadImage(source);
        imagePlugin = MarvinPluginLoader.loadImagePlugin(pluginPath);
        Instant before = Instant.now();
        imagePlugin.process(image, image);
        image.update();
        Instant after = Instant.now();
        System.out.printf("%-30s : %-4d ms\n", result, Duration.between(before, after).toMillis());
        ImageIO.write(image.getBufferedImage(), "png", new File(result));
    }

    public static void openCVGauss() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

            Mat source = Imgcodecs.imread("lena.png",
                    Imgcodecs.CV_LOAD_IMAGE_COLOR);

            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Instant before = Instant.now();
            Imgproc.GaussianBlur(source, destination, new Size(45, 45), 0);
            Instant after = Instant.now();
            String fileName = "opencv-gauss.png";

            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void openCVTreshold() {
        try {

            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("lena.png", Imgcodecs.CV_LOAD_IMAGE_COLOR);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());

            destination = source;
            Instant before = Instant.now();
            Imgproc.threshold(source, destination, 127, 255, Imgproc.THRESH_TOZERO);
            Instant after = Instant.now();
            String fileName = "opencv-tresholding.png";
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());

            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    public static void openCVSharpen() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("lena.png",
                    Imgcodecs.CV_LOAD_IMAGE_COLOR);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Instant before = Instant.now();
            Imgproc.GaussianBlur(source, destination, new Size(0, 0), 50);
            Core.addWeighted(source, 1.5, destination, -0.5, 1, destination);
            Instant after = Instant.now();
            String fileName = "opencv-sharp.png";
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
        }
    }

    public static void openCVErosionDiliation() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("lena.png", Imgcodecs.CV_LOAD_IMAGE_COLOR);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            destination = source;

            int erosion_size = 5;
            int dilation_size = 5;
            Instant before = Instant.now();
            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * erosion_size + 1, 2 * erosion_size + 1));
            Imgproc.erode(source, destination, element);
            String fileName = "opencv-erosion.jpg";
            Instant after = Instant.now();
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);

            source = Imgcodecs.imread("lena.png", Imgcodecs.CV_LOAD_IMAGE_COLOR);
            destination = source;
            before = Instant.now();
            Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * dilation_size + 1, 2 * dilation_size + 1));
            Imgproc.dilate(source, destination, element1);
            after = Instant.now();
            fileName = "opencv-dilation.png";
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    public static void openCVGrayScale() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            File input = new File("lena.png");
            BufferedImage image = ImageIO.read(input);
            Instant before = Instant.now();
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, data);

            Mat mat1 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
            Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

            byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int) (mat1.elemSize())];
            mat1.get(0, 0, data1);
            BufferedImage image1 = new BufferedImage(mat1.cols(), mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
            image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);
            String fileName = "opencv-grayscale.png";
            Instant after = Instant.now();
            File ouptut = new File(fileName);

            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            ImageIO.write(image1, "png", ouptut);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void openCVConvolution() {
        try {
            int kernelSize = 3;
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("opencv-grayscale.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Instant before = Instant.now();
            Mat kernel = new Mat(kernelSize, kernelSize, CvType.CV_32F) {
                {
                    put(0, 0, 0);
                    put(0, 1, 0);
                    put(0, 2, 0);

                    put(1, 0, 0);
                    put(1, 1, 1);
                    put(1, 2, 0);

                    put(2, 0, 0);
                    put(2, 1, 0);
                    put(2, 2, 0);
                }
            };
            Imgproc.filter2D(source, destination, -1, kernel);
            String fileName = "opencv-convolution.png";
            Instant after = Instant.now();
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void openCVSobel() {
        try {
            int kernelSize = 9;
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("opencv-grayscale.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Instant before = Instant.now();
            Mat kernel = new Mat(kernelSize, kernelSize, CvType.CV_32F) {
                {
                    put(0, 0, -1);
                    put(0, 1, 0);
                    put(0, 2, 1);

                    put(1, 0 - 2);
                    put(1, 1, 0);
                    put(1, 2, 2);

                    put(2, 0, -1);
                    put(2, 1, 0);
                    put(2, 2, 1);
                }
            };
            Imgproc.filter2D(source, destination, -1, kernel);
            Instant after = Instant.now();
            String fileName = "opencv-sobel.png";
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void openCVPrewitt() {
        try {
            int kernelSize = 9;
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("lena.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Instant before = Instant.now();
            Mat kernel = new Mat(kernelSize, kernelSize, CvType.CV_32F) {
                {
                    put(0, 0, -1);
                    put(0, 1, 0);
                    put(0, 2, 1);

                    put(1, 0 - 1);
                    put(1, 1, 0);
                    put(1, 2, 1);

                    put(2, 0, -1);
                    put(2, 1, 0);
                    put(2, 2, 1);
                }
            };
            Imgproc.filter2D(source, destination, -1, kernel);
            Instant after = Instant.now();
            String fileName = "opencv-prewitt.png";
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void openCVAverage() {
        try {
            int kernelSize = 9;
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            Mat source = Imgcodecs.imread("opencv-grayscale.png", Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            Instant before = Instant.now();
            Mat kernel = Mat.ones(kernelSize, kernelSize, CvType.CV_32F);
            {
                for (int i = 0; i < kernel.rows(); i++) {
                    for (int j = 0; j < kernel.cols(); j++) {
                        double[] m = kernel.get(i, j);
                        for (int k = 0; k < m.length; k++) {
                            if (i == 1 && j == 1) {
                                m[k] = 10 / 18;
                            } else {
                                m[k] = m[k] / (18);
                            }
                        }
                        kernel.put(i, j, m);
                    }
                }
            }
            ;
            Imgproc.filter2D(source, destination, -1, kernel);
            String fileName = "opencv-average.png";
            Instant after = Instant.now();
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            Imgcodecs.imwrite(fileName, destination);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void openCVHSV() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            File input = new File("lena.png");
            BufferedImage image = ImageIO.read(input);
            Instant before = Instant.now();
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, data);

            Mat mat1 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2HSV);

            byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int) (mat1.elemSize())];
            mat1.get(0, 0, data1);
            BufferedImage image1 = new BufferedImage(mat1.cols(), mat1.rows(), 5);
            image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

            Instant after = Instant.now();
            String fileName = "opencv-hsv.png";
            System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());
            File ouptut = new File(fileName);
            ImageIO.write(image1, "png", ouptut);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void robertsFilter(StreamableImage streamableImage, String fileName) {

        Instant before = Instant.now();

        BufferedImage bufferedImageX = streamableImage.parallelStream()
                .apply(new GrayScaleFilter())
                .apply(new RobertsCrossXFilter())
                .collect(new BufferedImageCollector());

        BufferedImage bufferedImageY = streamableImage.parallelStream()
                .apply(new GrayScaleFilter())
                .apply(new RobertsCrossYFilter())
                .collect(new BufferedImageCollector());

        BufferedImage bufferedImage = streamableImage.parallelStream()
                .apply(new EdgeDetectionFilter(bufferedImageX, bufferedImageY))
                .collect(new BufferedImageCollector());
        Instant after = Instant.now();

        System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());

        File outputfile = new File(fileName);
        try {
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sobelFilter(StreamableImage streamableImage, String fileName) {

        Instant before = Instant.now();

        BufferedImage bufferedImageX = streamableImage.parallelStream()
                .apply(new GrayScaleFilter())
                .apply(new SobelXFilter())
                .collect(new BufferedImageCollector());

        BufferedImage bufferedImageY = streamableImage.parallelStream()
                .apply(new GrayScaleFilter())
                .apply(new SobelYFilter())
                .collect(new BufferedImageCollector());

        BufferedImage bufferedImage = streamableImage.parallelStream()
                .apply(new EdgeDetectionFilter(bufferedImageX, bufferedImageY))
                .collect(new BufferedImageCollector());
        Instant after = Instant.now();

        System.out.printf("%-30s : %-4d ms\n", fileName, Duration.between(before, after).toMillis());

        File outputfile = new File(fileName);
        try {
            ImageIO.write(bufferedImage, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        StreamableImage streamableImage = new StreamableImage(new File(loader.getResource("").getPath() + "lena.png"));
        String sourcePath = loader.getResource("").getPath() + "lena.png";

        image = MarvinImageIO.loadImage(loader.getResource("").getPath() + "lena.png");
        backupImage = image.clone();

        System.out.println("JImageStream filters:");
        applyPredicate(streamableImage, new CirclePredicate(100, 100, 20), "predicate_circle.png");
        applyPredicate(streamableImage, new ColorPredicate(Color.red), "predicate_color.png");
        applyPredicate(streamableImage, new ColorRangePredicate(10, 200, ColorChannel.RED), "predicate_color_range.png");
        applyPredicate(streamableImage, new ThresholdPredicate(100), "predicate_threshold.png");

        applyFilter(streamableImage, new BoxBlurFilter(7), "filter_box_blur.png");
        applyFilter(streamableImage, new GaussFilter(7, 4.0), "filter_gauss.png");
        applyFilter(streamableImage, new EmbossFilter(), "filter_emboss.png");
        applyFilter(streamableImage, new SharpenFilter(), "filter_sharpen.png");
        applyFilter(streamableImage, new RedFilter(), "filter_red.png");
        applyFilter(streamableImage, new GreenFilter(), "filter_green.png");
        applyFilter(streamableImage, new BlueFilter(), "filter_blue.png");

        applyFilter(streamableImage, new GrayScaleFilter(), "filter_grayscale.png");
        applyFilter(streamableImage, new SepiaFilter(), "filter_sepia.png");

        applyFilter(streamableImage, new SaltAndPepperFilter(), "filter_salt&pepper.png");
        applyFilter(streamableImage, new MeanFilter(), "filter_mean.png");
        applyFilter(streamableImage, new MedianFilter(), "filter_median.png");

        applyFilter(streamableImage, new MaxFilter(), "filter_max.png");
        applyFilter(streamableImage, new MinFilter(), "filter_min.png");

//        applyFilter(streamableImage, new RobertsCrossXFilter(), "filter_roberts_cross_x.png");
//        applyFilter(streamableImage, new RobertsCrossYFilter(), "filter_roberts_cross_y.png");


        robertsFilter(streamableImage, "filter_roberts.png");

//        applyFilter(streamableImage, new SobelXFilter(), "filter_sobel_x.png");
//        applyFilter(streamableImage, new SobelYFilter(), "filter_sobel_y.png");

        sobelFilter(streamableImage,"filter_sobel.png");


        System.out.println("\n\nMarvin filters:");
        marvinFilter("org.marvinproject.image.color.grayScale.jar", sourcePath, "marvin-grayScale.png");

        marvinFilter("org.marvinproject.image.blur.gaussianBlur.jar", sourcePath, "marvin-gaussianBlur.png");

        marvinFilter("org.marvinproject.image.color.sepia.jar", sourcePath, "marvin-sepia.png");

        marvinFilter("org.marvinproject.image.statistical.median.jar", sourcePath, "marvin-median.png");

        marvinFilter("org.marvinproject.image.statistical.maximum.jar", sourcePath, "marvin-maximum.png");

        marvinFilter("org.marvinproject.image.edge.roberts.jar", sourcePath, "marvin-edge.roberts.png");

        marvinFilter("org.marvinproject.image.edge.sobel.jar", sourcePath, "marvin-edge.sobel.png");

        marvinFilter("org.marvinproject.image.color.thresholding.jar", sourcePath, "marvin-color.thresholding.png");

        marvinFilter("org.marvinproject.image.color.colorChannel.jar", sourcePath, "marvin-color.colorChannel.png");

        marvinFilter("org.marvinproject.image.color.emboss.jar", sourcePath, "marvin-color.emboss.png");

        System.out.println("\n\nOpen-CV filters:");
        openCVGauss();

        openCVGrayScale();

        openCVSharpen();

        openCVTreshold();

        openCVErosionDiliation();

        openCVConvolution();

        openCVPrewitt();

        openCVSobel();

        openCVAverage();

        openCVHSV();

    }


}


