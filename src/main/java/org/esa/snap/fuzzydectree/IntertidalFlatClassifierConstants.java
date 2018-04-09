package org.esa.snap.fuzzydectree;

/**
 * todo: add comment
 * To change this template use File | Settings | File Templates.
 * Date: 06.04.2018
 * Time: 13:19
 *
 * @author olafd
 */
public class IntertidalFlatClassifierConstants {

    public static final String[][] INPUT_NAMES = {
            {"b1", "sand-tr_abundance"},
            {"b2", "sand-wc_abundance"},
            {"b3", "schatten_abundance"},
            {"b4", "summary_error"},
            {"b5", "steigung_red_nIR"},
//            {"b6", "steigung_nIR_SWIR1"},     // not used
            {"b7", "flh"},
            {"b8", "ndvi"},
            {"b12", "reflec_483"},
            {"b13", "reflec_561"},
            {"b14", "reflec_655"},
//            {"b15", "reflec_865"},           // not used
            {"b16", "reflec_1609"},
            {"b19", "muschelindex"}
    };

    public static final int[] CLASSIF_CLASS = {
            11, 10, 13, 8, 7, 6, 9, 1, 2, 3, 4, 5, 12
    };

    public static final int[][] CLASSIF_RGB = {
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

    public static final String FINAL_CLASS_BAND_NAME = "final_class";
    public static final String FUZZY_MAX_VAL_BAND_NAME = "fuzzy_max_value";
}
