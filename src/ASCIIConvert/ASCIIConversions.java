package ASCIIConvert;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

class ASCIIConversions {

    private int[] statsArray = new int[12];
    char[] convRefArray = {' ', '~', '-', ':', '+', '%', '=', 'W', '@', '$', '#', '▒'};
    char[] imgArray;
    private long dStart;
    private long dEnd;

    /**
     * The main conversion method.
     * @param image A BufferedImage containing the image that needs to be converted
     * @param inverse When inverse is true, all pixel values will be inverted. Thus creating a negative image
     * @return A string containing the ASCII version of the original image.
     */
    String convert(BufferedImage image, boolean inverse) {
        // Reset statistics before anything
        statsArray = new int[12];
        // Begin the timer
        dStart = System.nanoTime();
        // Scale the image
        image = scale(image, image.getWidth(), image.getHeight() / 2, 1, 0.5);
        // The +1 is for the newline characters
        StringBuilder sb = new StringBuilder((image.getWidth() + 1) * image.getHeight());

        for (int y = 0; y < image.getHeight(); y++) {
            // At the end of each line, add a newline character
            if (sb.length() != 0) sb.append("\n");
            for (int x = 0; x < image.getWidth(); x++) {
                //
                Color pixelColor = new Color(image.getRGB(x, y));
                pixelColor = inverse ? new Color(255 - pixelColor.getRed(),
                        255 - pixelColor.getGreen(),
                        255 - pixelColor.getBlue()) : pixelColor;
                double gValue = (double) pixelColor.getRed() * 0.2989 + (double) pixelColor.getBlue() * 0.5870 + (double) pixelColor.getGreen() * 0.1140;
                final char s = gValue < 130 ? darkGrayScaleMap(gValue) : lightGrayScaleMap(gValue);
                sb.append(s);
            }
        }
        imgArray = sb.toString().toCharArray();
        dEnd = System.nanoTime();
        return sb.toString();
    }

    // Stats stuff, yawn.
    String getStats() {
        Locale locale = new Locale("en", "EN");
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        String s = "";
        s += "Render Time: " + (dEnd - dStart)/ 1000000000.0 + " seconds.\n";
        int max = 0, min = Integer.MAX_VALUE, maxloc = 0, minloc = 0;
        DecimalFormat df = new DecimalFormat("##.00");
        for (int i = 0; i < statsArray.length; i++) {
            s += "Character: '" + convRefArray[i] + "' was used " + numberFormat.format(statsArray[i]) + " times. (" +
                    df.format(getPercentage(statsArray[i], imgArray.length)) + "%)\n";
            if (statsArray[i] > max) {
                max = statsArray[i];
                maxloc = i;
            }
            if (statsArray[i] < min) {
                min = statsArray[i];
                minloc = i;
            }
        }
        s += "Most used character: " + "'" + convRefArray[maxloc] + "'" + " was used " + numberFormat.format(max) + " times\n";
        s += "Least used character: " + "'" + convRefArray[minloc] + "'" + " was used " + numberFormat.format(min) + " times \n";
        return s;
    }

    private static float getPercentage(int n, int total) {
        float proportion = ((float) n) / ((float) total);
        return proportion * 100;
    }


    /**
     * Image scale method
     * @param imageToScale The image to be scaled
     * @param dWidth Desired width, the new image object is created to this size
     * @param dHeight Desired height, the new image object is created to this size
     * @param fWidth What to multiply the width by. value < 1 scales down, and value > one scales up
     * @param fHeight What to multiply the height by. value < 1 scales down, and value > one scales up
     * @return A scaled image
     */
    private static BufferedImage scale(BufferedImage imageToScale, int dWidth, int dHeight, double fWidth, double fHeight) {
        BufferedImage dbi = null;
        // Needed to create a new BufferedImage object
        int imageType = imageToScale.getType();
        if (imageToScale != null) {
            dbi = new BufferedImage(dWidth, dHeight, imageType);
            Graphics2D g = dbi.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
            g.drawRenderedImage(imageToScale, at);
        }
        return dbi;
    }

    private char darkGrayScaleMap(double g) {
        char str;
        if (g >= 120.0) {
            str = '=';
            statsArray[6]++;
        } else if (g >= 100.0) {
            str = 'W';
            statsArray[7]++;
        } else if (g >= 80.0) {
            str = '@';
            statsArray[8]++;
        } else if (g >= 70.0) {
            str = '$';
            statsArray[9]++;
        } else if (g >= 30.0) {
            str = '#';
            statsArray[10]++;
        } else {
            str = '▒';
            statsArray[11]++;
        }
        return str;
    }

    private char lightGrayScaleMap(double g) {
        char str;
        // Light
        if (g >= 240.0) {
            str = ' ';
            statsArray[0]++;
        } else if (g >= 220) {
            str = '~';
            statsArray[1]++;
        } else if (g >= 200.0) {
            str = '-';
            statsArray[2]++;
        } else if (g >= 180.0) {
            str = ':';
            statsArray[3]++;
        } else if (g >= 160.0) {
            str = '+';
            statsArray[4]++;
        } else {
            str = '%';
            statsArray[5]++;
        }
        return str;
    }
}
