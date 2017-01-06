import marvin.image.MarvinImage;
import marvin.io.MarvinImageIO;
import marvin.plugin.MarvinImagePlugin;
import marvin.util.MarvinPluginLoader;
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
        System.out.println(fileName + " : " + Duration.between(after, before).toMillis() + " ms");
        File outputfile = new File(fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public static void applyFilter(StreamableImage streamableImage, Filter filter, String fileName) throws IOException {
        Instant before = Instant.now();
        BufferedImage bufferedImage = streamableImage.stream()
                .apply(filter)
                .collect(new BufferedImageCollector());

        Instant after = Instant.now();
        System.out.println(fileName + " : " + Duration.between(after, before).toMillis() + " ms");
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
        System.out.println(result + " : " + Duration.between(after, before).toMillis() + " ms");
        ImageIO.write(image.getBufferedImage(), "png", new File(result));
    }

    public static void main(String[] args) throws IOException {

        Main main = new Main();
        StreamableImage streamableImage = new StreamableImage(new File(loader.getResource("").getPath() + "lena.png"));
        String sourcePath = loader.getResource("").getPath() + "lena.png";

        image = MarvinImageIO.loadImage(loader.getResource("").getPath() + "lena.png");
        backupImage = image.clone();

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

        applyFilter(streamableImage, new RobertsCrossXFilter(), "filter_roberts_cross_x.png");
        applyFilter(streamableImage, new RobertsCrossYFilter(), "filter_roberts_cross_y.png");
//        applyFilter(streamableImage, new EdgeDetectionFilter(), "filter_roberts_cross_x.png");
        applyFilter(streamableImage, new SobelXFilter(), "filter_sobel_x.png");
        applyFilter(streamableImage, new SobelYFilter(), "filter_sobel_y.png");
//               applyFilter(streamableImage, new EdgeDetectionFilter(), "filter_roberts_cross_x.png");



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

    }



}
