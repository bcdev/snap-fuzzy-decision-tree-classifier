package org.esa.snap.fuzzydectree;

/**
 * Constants for intertidal flat fuzzy classification.
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


    // CLASSIF_CLASS_* mapping follows original 'intertidal_flat_classif_envi_dec_tree.txt' by KS, Aug. 2017

    public static final int[] CLASSIF_CLASS = {
            11, 10, 13, 8, 7, 6, 9, 1, 2, 3, 4, 5, 12
    };

    public static final String[] CLASSIF_CLASS_DESCR = {
            "Sand",
            "Misch",
            "Misch2",
            "Schlick",
            "Schlick_t",
            "dense1",
            "dense2",
            "Muschel",
            "Strand",
            "Wasser",
            "nodata",
            "Wasser2",
            "Schill"
    };

    public static final int[][] CLASSIF_CLASS_RGB = {
            {255, 255, 75},
            {255, 215, 0},
            {238, 154, 0},
            {125, 38, 205},
            {167, 80, 162},
            {0, 255, 0},
            {46, 139, 87},
            {255, 0, 0},
            {230, 230, 230},
            {0, 0, 255},
            {0, 0, 0},
            {0, 60, 255},
            {255, 113, 255}
    };

}
