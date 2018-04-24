package org.esa.snap.fuzzydectree;

import com.bc.ceres.core.ProgressMonitor;
import com.bc.dectree.DecTreeDoc;
import com.bc.dectree.DecTreeFunction;
import com.bc.dectree.DecTreeParseException;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.Parameter;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Performs generic classification based on fuzzy decision tree as defined in input yml file.
 *
 * @author olafd
 */
@OperatorMetadata(alias = "GenericClassifier", version = "0.1",
        authors = "Olaf Danne, Norman Fomferra (Brockmann Consult)",
        category = "Classification",
        copyright = "Copyright (C) 2018 by Brockmann Consult",
        description = "Performs generic classification based on fuzzy decision tree as defined in input yml file.")
public class GenericClassifierOp extends Operator {


    @Parameter(description = "A yml file defining the decision tree for fuzzy classification.",
            label = "Decision tree yml file.")
    private File yamlFile;

    @SourceProduct(description = "Source product",
            label = "Classification input product")
    private Product sourceProduct;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    private DecTreeFunction genericClassifier;


    @Override
    public void initialize() throws OperatorException {
        try {
            final DecTreeDoc doc = DecTreeDoc.parse(yamlFile);
            genericClassifier = DecTreeFunction.load(doc);

            createTargetProduct();
        } catch (IOException | DecTreeParseException e) {
            System.err.printf("Error: %s\n", e.getMessage());
        }
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle targetRectangle, ProgressMonitor pm) throws OperatorException {
        final int numSrcBands = genericClassifier.getInputSize();
        final int numTargetBands = genericClassifier.getOutputSize();

        Tile[] srcTile = new Tile[numSrcBands];
        for (int i = 0; i < numSrcBands; i++) {
            final int bandIndex = Integer.parseInt(genericClassifier.getInputNames()[i].substring(1)) - 1;
            srcTile[i] = getSourceTile(sourceProduct.getBandAt(bandIndex), targetRectangle);
        }

        final Tile finalClassTargetTile =
                targetTiles.get(targetProduct.getBand(GenericClassifierConstants.FINAL_CLASS_BAND_NAME));
        final Tile fuzzyMaxValTargetTile =
                targetTiles.get(targetProduct.getBand(GenericClassifierConstants.FUZZY_MAX_VAL_BAND_NAME));

        double[] inputs = new double[numSrcBands];
        double[] outputs = new double[numTargetBands];
        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {
                for (int i = 0; i < numSrcBands; i++) {
                    inputs[i] = srcTile[i].getSampleFloat(x, y);
                }
                genericClassifier.apply(inputs, outputs);
                
                double outputMax = Double.MIN_VALUE;
                int maxOutputIndex = -1;
                for (int i = 0; i < outputs.length; i++) {
                    if (i < outputs.length-1 && outputs[i] > outputMax) {
                        outputMax = outputs[i];
                        maxOutputIndex = i;
                    }
                    final String targetBandName = genericClassifier.getOutputNames()[i];
                    targetTiles.get(targetProduct.getBand(targetBandName)).setSample(x, y, outputs[i]);
                }

                finalClassTargetTile.setSample(x, y, maxOutputIndex);
                fuzzyMaxValTargetTile.setSample(x, y, outputMax);
            }
        }
    }

    private void createTargetProduct() {
        final int w = sourceProduct.getSceneRasterWidth();
        final int h = sourceProduct.getSceneRasterHeight();
        targetProduct = new Product(sourceProduct.getName(), sourceProduct.getProductType(), w, h);

        ProductUtils.copyMetadata(sourceProduct, targetProduct);
        ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
        targetProduct.setStartTime(sourceProduct.getStartTime());
        targetProduct.setEndTime(sourceProduct.getEndTime());

        for (int i = 0; i < genericClassifier.getOutputSize(); i++) {
            targetProduct.addBand(genericClassifier.getOutputNames()[i], ProductData.TYPE_FLOAT32);
        }
        targetProduct.addBand(GenericClassifierConstants.FUZZY_MAX_VAL_BAND_NAME, ProductData.TYPE_FLOAT32);
        addFinalClassBand();
    }

    private void addFinalClassBand() {
        Band finalClassBand = targetProduct.addBand(GenericClassifierConstants.FINAL_CLASS_BAND_NAME,
                                                    ProductData.TYPE_INT8);

        final IndexCoding finalClassIndexCoding =
                new IndexCoding(GenericClassifierConstants.FINAL_CLASS_BAND_NAME);

        final int numClasses = genericClassifier.getOutputSize() - 1;
        ColorPaletteDef.Point[] points = new ColorPaletteDef.Point[numClasses];
        for (int i = 0; i < numClasses; i++) {
            // Some default colors. Continue with random colors if these are not enough.
            // todo: mechanism to provide user defined color tables via yml file?
            Color color;
            if (i < GenericClassifierConstants.DEFAULT_CLASSIF_RGB.length) {
                final int r = GenericClassifierConstants.DEFAULT_CLASSIF_RGB[i][0];
                final int g = GenericClassifierConstants.DEFAULT_CLASSIF_RGB[i][1];
                final int b = GenericClassifierConstants.DEFAULT_CLASSIF_RGB[i][2];
                color = new Color(r, g, b);
            } else {
                final Random random = new Random();
                color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            }
            final String descr = genericClassifier.getOutputNames()[i];
            points[i] = new ColorPaletteDef.Point(i, color, descr);
            finalClassIndexCoding.addIndex(descr, i, descr);
        }
        final ColorPaletteDef cpd = new ColorPaletteDef(points);
        final ImageInfo imageInfo = new ImageInfo(cpd);
        finalClassBand.setImageInfo(imageInfo);
        finalClassBand.setSampleCoding(finalClassIndexCoding);

        targetProduct.getIndexCodingGroup().add(finalClassIndexCoding);
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(GenericClassifierOp.class);
        }
    }
}
