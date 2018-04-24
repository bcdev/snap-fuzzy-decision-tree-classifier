package org.esa.snap.fuzzydectree;

/**
 * Constants for generic fuzzy classification.
 *
 * @author olafd
 */
public class GenericClassifierConstants {

    public static final String FINAL_CLASS_BAND_NAME = "final_class";
    public static final String FUZZY_MAX_VAL_BAND_NAME = "fuzzy_max_value";

    public static final int[][] DEFAULT_CLASSIF_RGB = {
            {0, 0, 0},
            {0, 0, 255},
            {255, 113, 255},
            {255, 0, 0},
            {46, 139, 87},
            {0, 255, 0},
            {230, 230, 230},
            {255, 255, 75},
            {255, 215, 0},
            {238, 154, 0},
            {125, 38, 205},
            {167, 80, 162},
            {0, 60, 255}
    };

}
