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

    // The bread and butter of this program
    String convert(BufferedImage image, boolean inverse) {
        // Begin the timer
        dStart = System.nanoTime();
        // Scale the image
        image = scale(image, image.getType(), image.getWidth(), image.getHeight() / 2, 1, 0.5);
        StringBuilder sb = new StringBuilder((image.getWidth() + 1) * image.getHeight());
        for (int y = 0; y < image.getHeight(); y++) {
            if (sb.length() != 0) sb.append("\n");
            for (int x = 0; x < image.getWidth(); x++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                pixelColor = inverse ? new Color(255 - pixelColor.getRed(),
                        255 - pixelColor.getGreen(),
                        255 - pixelColor.getBlue()) : pixelColor;
                double gValue = (double) pixelColor.getRed() * 0.2989 + (double) pixelColor.getBlue() * 0.5870 + (double) pixelColor.getGreen() * 0.1140;
                final char s = isDark(gValue) ? darkGrayScaleMap(gValue) : lightGrayScaleMap(gValue);
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
        s += "Least used character: " + "'" + convRefArray[minloc] + "'" + " was used " + numberFormat.format(min) + "times \n";
        return s;
    }

    private static float getPercentage(int n, int total) {
        float proportion = ((float) n) / ((float) total);
        return proportion * 100;
    }


    private static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
        BufferedImage dbi = null;
        if (sbi != null) {
            dbi = new BufferedImage(dWidth, dHeight, imageType);
            Graphics2D g = dbi.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
            g.drawRenderedImage(sbi, at);
        }
        return dbi;
    }

    private boolean isDark(double g) {
        return g < 130;
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
        return str; // return the character
    }
}
