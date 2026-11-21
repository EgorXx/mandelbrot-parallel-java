package mandelbrot.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class MandelbrotTest {

    @Test
    void pixelColor_originOfSet_isBlack() {
        RenderParameters params = new RenderParameters(1, 1, -0.5, 0.5, -0.5, 0.5, 100);
        assertEquals(0, Mandelbrot.pixelColor(0, 0, params));
    }

    @Test
    void pixelColor_pointOutsideSet_isNotBlack() {
        RenderParameters params = new RenderParameters(1, 1, 1.5, 2.5, -0.5, 0.5, 100);
        assertNotEquals(0, Mandelbrot.pixelColor(0, 0, params));
    }

    @Test
    void pixelColor_farPointEscapesImmediately_isNotBlack() {
        RenderParameters params = new RenderParameters(1, 1, 9.5, 10.5, -0.5, 0.5, 1000);
        assertNotEquals(0, Mandelbrot.pixelColor(0, 0, params));
    }

    @Test
    void pixelColor_repeatedCall_returnsSameColor() {
        RenderParameters params = new RenderParameters(16, 16, -2.5, 1.0, -1.25, 1.25, 200);
        assertEquals(Mandelbrot.pixelColor(5, 7, params), Mandelbrot.pixelColor(5, 7, params));
    }
}
