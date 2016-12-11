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
import java.util.function.Predicate;

public class Main {
    static final ClassLoader loader = Main.class.getClassLoader();

    public void applyPredicate(StreamableImage streamableImage, Predicate predicate, String fileName) throws IOException {
        long startTime = System.currentTimeMillis();

        BufferedImage bufferedImage = streamableImage.stream()
                .bounds(predicate)
                .apply(new RedFilter())
                .collect(new BufferedImageCollector());
        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(fileName + " : " + estimatedTime + " ms");
        File outputfile = new File(fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public void applyFilter(StreamableImage streamableImage, Filter filter, String fileName) throws IOException {
        long startTime = System.currentTimeMillis();
        BufferedImage bufferedImage = streamableImage.stream()
                .apply(filter)
                .collect(new BufferedImageCollector());

        long estimatedTime = System.currentTimeMillis() - startTime;
        System.out.println(fileName + " : " + estimatedTime + " ms");
        File outputfile = new File(fileName);
        ImageIO.write(bufferedImage, "png", outputfile);
    }

    public static void main(String[] args) throws IOException {

        Main main = new Main();
        StreamableImage streamableImage = new StreamableImage(new File(loader.getResource("").getPath() + "lena.png"));

        main.applyPredicate(streamableImage, new CirclePredicate(100, 100, 20), "predicate_circle.png");
        main.applyPredicate(streamableImage, new ColorPredicate(Color.red), "predicate_color.png");
        main.applyPredicate(streamableImage, new ColorRangePredicate(10, 200, ColorChannel.RED), "predicate_color_range.png");
        main.applyPredicate(streamableImage, new ThresholdPredicate(100), "predicate_threshold.png");

        main.applyFilter(streamableImage, new BoxBlurFilter(7), "filter_box_blur.png");
        main.applyFilter(streamableImage, new GaussFilter(7, 4.0), "filter_gauss.png");
        main.applyFilter(streamableImage, new EmbossFilter(), "filter_emboss.png");
        main.applyFilter(streamableImage, new SharpenFilter(), "filter_sharpen.png");
        main.applyFilter(streamableImage, new RedFilter(), "filter_red.png");
        main.applyFilter(streamableImage, new GreenFilter(), "filter_green.png");
        main.applyFilter(streamableImage, new BlueFilter(), "filter_blue.png");

        main.applyFilter(streamableImage, new GrayScaleFilter(), "filter_grayscale.png");
        main.applyFilter(streamableImage, new SepiaFilter(), "filter_sepia.png");

        main.applyFilter(streamableImage, new SaltAndPepperFilter(), "filter_salt&pepper.png");
        main.applyFilter(streamableImage, new MeanFilter(), "filter_mean.png");
        main.applyFilter(streamableImage, new MedianFilter(), "filter_median.png");

        main.applyFilter(streamableImage, new MaxFilter(), "filter_max.png");
        main.applyFilter(streamableImage, new MinFilter(), "filter_min.png");

        main.applyFilter(streamableImage, new RobertsCrossXFilter(), "filter_roberts_cross_x.png");
        main.applyFilter(streamableImage, new RobertsCrossYFilter(), "filter_roberts_cross_y.png");
//        main.applyFilter(streamableImage, new EdgeDetectionFilter(), "filter_roberts_cross_x.png");
        main.applyFilter(streamableImage, new SobelXFilter(), "filter_sobel_x.png");
        main.applyFilter(streamableImage, new SobelYFilter(), "filter_sobel_y.png");
        //        main.applyFilter(streamableImage, new EdgeDetectionFilter(), "filter_roberts_cross_x.png");




    }
}
