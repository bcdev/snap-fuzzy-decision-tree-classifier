package org.esa.snap.fuzzydectree;

import org.esa.snap.core.datamodel.Product;
import org.esa.snap.core.gpf.Operator;
import org.esa.snap.core.gpf.OperatorException;
import org.esa.snap.core.gpf.OperatorSpi;
import org.esa.snap.core.gpf.annotations.OperatorMetadata;
import org.esa.snap.core.gpf.annotations.SourceProduct;
import org.esa.snap.core.gpf.annotations.TargetProduct;
import org.esa.snap.core.util.ProductUtils;

/**
 * Performs classifications based on fuzzy decision trees.
 *
 * @author olafd
 */
@OperatorMetadata(alias = "FuzzyDecisionTreeClassifier", version = "0.1",
        authors = "Olaf Danne, Norman Fomferra (Brockmann Consult)",
        category = "Classification",
        copyright = "Copyright (C) 2018 by Brockmann Consult",
        description = "Performs classifications based on fuzzy decision trees.")
public class FuzzyDecisionTreeClassifierOp extends Operator {


    @SourceProduct(description = "Source product",
            label = "Source product")
    private Product sourceProduct;

    @TargetProduct(description = "The target product.")
    private Product targetProduct;

    @Override
    public void initialize() throws OperatorException {
        // todo
        targetProduct = createTargetProduct();
    }

    private Product createTargetProduct() {
        final int w = sourceProduct.getSceneRasterWidth();
        final int h = sourceProduct.getSceneRasterHeight();
        Product targetProduct = new Product(sourceProduct.getName(),
                                            sourceProduct.getProductType(),
                                            w, h);

        ProductUtils.copyMetadata(sourceProduct, targetProduct);
        ProductUtils.copyGeoCoding(sourceProduct, targetProduct);
        ProductUtils.copyFlagCodings(sourceProduct, targetProduct);
        ProductUtils.copyFlagBands(sourceProduct, targetProduct, true);
        ProductUtils.copyMasks(sourceProduct, targetProduct);
        ProductUtils.copyTiePointGrids(sourceProduct, targetProduct);
        targetProduct.setStartTime(sourceProduct.getStartTime());
        targetProduct.setEndTime(sourceProduct.getEndTime());

        // todo: continue

        return targetProduct;
    }


    public static class Spi extends OperatorSpi {

        public Spi() {
            super(FuzzyDecisionTreeClassifierOp.class);
        }
    }
}
