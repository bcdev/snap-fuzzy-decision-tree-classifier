package org.esa.snap.fuzzydectree;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.core.datamodel.*;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.Tile;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;
import org.esa.snap.fuzzydectree.generated.IntertidalFlatClassifier;
import org.esa.snap.fuzzydectree.generated.IntertidalFlatClassifierFuz;

import java.awt.*;
import java.util.Map;

/**
 * Performs intertidal flat classification based on fuzzy decision tree.
 *
 * @author olafd
 */
@OperatorMetadata(alias = "IntertidalFlatClassifier", version = "0.1",
        authors = "Olaf Danne, Norman Fomferra (Brockmann Consult)",
        category = "Classification",
        copyright = "Copyright (C) 2018 by Brockmann Consult",
        description = "Performs intertidal flat classification based on fuzzy decision tree.")
public class IntertidalFlatClassifierOp extends Operator {


    @SourceProduct(description = "Source product",
            label = "Classification input product")
    private Product sourceProduct;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    IntertidalFlatClassifierFuz intertidalFlatClassifier;

    @Override
    public void initialize() throws OperatorException {
        intertidalFlatClassifier = new IntertidalFlatClassifierFuz();
        createTargetProduct();
    }

    @Override
    public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle targetRectangle, ProgressMonitor pm) throws OperatorException {
        final int numSrcBands = intertidalFlatClassifier.getInputSize();
        final int numTargetBands = intertidalFlatClassifier.getOutputSize();
        Tile[] srcTile = new Tile[numSrcBands];
        for (int i = 0; i < numSrcBands; i++) {
            final String srcBandName = IntertidalFlatClassifierConstants.INPUT_NAMES[i][1];
            srcTile[i] = getSourceTile(sourceProduct.getBand(srcBandName), targetRectangle);
        }

        double[] inputs = new double[numSrcBands];
        double[] outputs = new double[numTargetBands];
        for (int y = targetRectangle.y; y < targetRectangle.y + targetRectangle.height; y++) {
            checkForCancellation();
            for (int x = targetRectangle.x; x < targetRectangle.x + targetRectangle.width; x++) {
                for (int i = 0; i < numSrcBands; i++) {
                    inputs[i] = srcTile[i].getSampleFloat(x, y);
                }
                if (x == 1490 && y == 2850) {
                    System.out.println("x = " + x);
                }
                intertidalFlatClassifier.apply(inputs, outputs);
                double outputMax = Double.MIN_VALUE;
                int maxOutputIndex = -1;
                for (int i = 0; i < numSrcBands; i++) {
                    if (outputs[i] > outputMax) {
                        outputMax = outputs[i];
                        maxOutputIndex = IntertidalFlatClassifierConstants.CLASSIF_CLASS[i];
                    }
                    final String targetBandName = intertidalFlatClassifier.getOutputNames()[i];
                    targetTiles.get(targetProduct.getBand(targetBandName)).setSample(x, y, outputs[i]);
                }
                targetTiles.get(targetProduct.getBand(IntertidalFlatClassifierConstants.FINAL_CLASS_BAND_NAME)).
                        setSample(x, y, maxOutputIndex);
                targetTiles.get(targetProduct.getBand(IntertidalFlatClassifierConstants.FUZZY_MAX_VAL_BAND_NAME)).
                        setSample(x, y, outputMax);
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

        for (int i = 0; i < intertidalFlatClassifier.getOutputSize(); i++) {
            targetProduct.addBand(intertidalFlatClassifier.getOutputNames()[i], ProductData.TYPE_FLOAT32);
        }
        targetProduct.addBand(IntertidalFlatClassifierConstants.FUZZY_MAX_VAL_BAND_NAME, ProductData.TYPE_FLOAT32);
        addFinalClassBand();
    }

    private void addFinalClassBand() {
        Band finalClassBand = targetProduct.addBand(IntertidalFlatClassifierConstants.FINAL_CLASS_BAND_NAME,
                                                    ProductData.TYPE_INT8);

        final IndexCoding finalClassIndexCoding =
                new IndexCoding(IntertidalFlatClassifierConstants.FINAL_CLASS_BAND_NAME);

        ColorPaletteDef.Point[] points =
                new ColorPaletteDef.Point[IntertidalFlatClassifierConstants.CLASSIF_CLASS.length];
        for (int i = 0; i < IntertidalFlatClassifierConstants.CLASSIF_CLASS.length; i++) {
            final int classifClass = IntertidalFlatClassifierConstants.CLASSIF_CLASS[i];
            final int r = IntertidalFlatClassifierConstants.CLASSIF_RGB[classifClass - 1][0];
            final int g = IntertidalFlatClassifierConstants.CLASSIF_RGB[classifClass - 1][1];
            final int b = IntertidalFlatClassifierConstants.CLASSIF_RGB[classifClass - 1][2];
            final Color color = new Color(r, g, b);
            final String descr = intertidalFlatClassifier.getOutputNames()[i];
            points[i] = new ColorPaletteDef.Point(classifClass, color, descr);
            finalClassIndexCoding.addIndex(descr, classifClass, descr);
        }
        final ColorPaletteDef cpd = new ColorPaletteDef(points);
        final ImageInfo imageInfo = new ImageInfo(cpd);
        finalClassBand.setImageInfo(imageInfo);
        finalClassBand.setSampleCoding(finalClassIndexCoding);

        targetProduct.getIndexCodingGroup().add(finalClassIndexCoding);
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(IntertidalFlatClassifierOp.class);
        }
    }
}
