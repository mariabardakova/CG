package com.cgvsu.triangulation;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class TriangleRasterization {

    /**
     * Отрисовка контура треугольника
     */
    public static void drawTriangle(
            GraphicsContext gc,
            int x1, int y1,
            int x2, int y2,
            int x3, int y3,
            Color color) {
        drawLine(gc, x1, y1, x2, y2, color);
        drawLine(gc, x2, y2, x3, y3, color);
        drawLine(gc, x3, y3, x1, y1, color);
    }

    /**
     * Отрисовка контура треугольника с интерполяцией по вершинам
     */
    public static void drawInterpolatedTriangle(
            GraphicsContext gc,
            int x1, int y1, Color color1,
            int x2, int y2, Color color2,
            int x3, int y3, Color color3
    ) {
        drawLineInterpolated(gc, x1, y1, color1, x2, y2, color2);
        drawLineInterpolated(gc, x2, y2, color2, x3, y3, color3);
        drawLineInterpolated(gc, x3, y3, color3, x1, y1, color1);
    }

    /**
     * Заливка треугольника сплошным цветом с использованием scanline-алгоритма
     */
    public static void fillTriangle(
            GraphicsContext gc,
            int x1, int y1,
            int x2, int y2,
            int x3, int y3,
            Color color) {

        drawTriangle(gc, x1, y1, x2, y2, x3, y3, color);

        int[] xs = {x1, x2, x3};
        int[] ys = {y1, y2, y3};

        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (ys[i] > ys[j]) {
                    int tempX = xs[i];
                    xs[i] = xs[j];
                    xs[j] = tempX;

                    int tempY = ys[i];
                    ys[i] = ys[j];
                    ys[j] = tempY;
                }
            }
        }

        int t0x = xs[0], t0y = ys[0];
        int t1x = xs[1], t1y = ys[1];
        int t2x = xs[2], t2y = ys[2];

        int total_height = t2y - t0y;

        for (int i = 0; i < total_height; i++) {
            boolean second_half = i > t1y - t0y || t1y == t0y;

            int segment_height = second_half ? t2y - t1y : t1y - t0y;

            if (total_height == 0 || segment_height == 0) continue;

            float alpha = (float) i / total_height;

            int base_i = second_half ? i - (t1y - t0y) : i;
            float beta = (float) base_i / segment_height;

            int Ax = t0x + (int)((t2x - t0x) * alpha);
            int Bx;

            if (second_half) {
                Bx = t1x + (int)((t2x - t1x) * beta);
            } else {
                Bx = t0x + (int)((t1x - t0x) * beta);
            }

            if (Ax > Bx) {
                int temp = Ax;
                Ax = Bx;
                Bx = temp;
            }

            int currentY = t0y + i;
            drawLine(gc, Ax, currentY, Bx, currentY, color);
        }
    }

    /**
     * Заливка треугольника с интерполяцией цветов вершин
     */
    public static void fillTriangleInterpolated(
            GraphicsContext gc,
            int x1, int y1, Color color1,
            int x2, int y2, Color color2,
            int x3, int y3, Color color3) {

        drawInterpolatedTriangle(gc,
                x1, y1, color1,
                x2, y2, color2,
                x3, y3, color3
        );
        int[] xs = {x1, x2, x3};
        int[] ys = {y1, y2, y3};
        Color[] colors = {color1, color2, color3};

        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 3; j++) {
                if (ys[i] > ys[j]) {
                    swap(xs, i, j);
                    swap(ys, i, j);
                    swap(colors, i, j);
                }
            }
        }

        int t0x = xs[0], t0y = ys[0];
        int t1x = xs[1], t1y = ys[1];
        int t2x = xs[2], t2y = ys[2];
        Color t0c = colors[0];
        Color t1c = colors[1];
        Color t2c = colors[2];

        int total_height = t2y - t0y;
        if (total_height == 0) return;

        for (int i = 0; i < total_height; i++) {
            boolean second_half = i > t1y - t0y || t1y == t0y;
            int currentY = t0y + i;

            if (second_half) {
                int segment_height = t2y - t1y;
                if (segment_height == 0) continue;

                float beta = (float)(currentY - t1y) / segment_height;

                int Ax = t1x + (int)((t2x - t1x) * beta);
                Color colorA = interpolateColor(t1c, t2c, beta);

                float alpha = (float)(currentY - t0y) / total_height;
                int Bx = t0x + (int)((t2x - t0x) * alpha);
                Color colorB = interpolateColor(t0c, t2c, alpha);

                if (Ax > Bx) {
                    int tempX = Ax; Ax = Bx; Bx = tempX;
                    Color tempC = colorA; colorA = colorB; colorB = tempC;
                }

                drawLineInterpolated(gc, Ax, currentY, colorA, Bx, currentY, colorB);
            } else {
                int segment_height = t1y - t0y;
                if (segment_height == 0) continue;

                float alpha = (float)(currentY - t0y) / segment_height;

                int Ax = t0x + (int)((t1x - t0x) * alpha);
                Color colorA = interpolateColor(t0c, t1c, alpha);

                float gamma = (float)(currentY - t0y) / total_height;
                int Bx = t0x + (int)((t2x - t0x) * gamma);
                Color colorB = interpolateColor(t0c, t2c, gamma);

                if (Ax > Bx) {
                    int tempX = Ax; Ax = Bx; Bx = tempX;
                    Color tempC = colorA; colorA = colorB; colorB = tempC;
                }

                drawLineInterpolated(gc, Ax, currentY, colorA, Bx, currentY, colorB);
            }
        }
    }

    /**
     * Отрисовка линии с использованием алгоритма Брезенхэма
     */
    public static void drawLine(
            GraphicsContext gc,
            int x1, int y1, int x2, int y2,
            Color color) {
        PixelWriter pw = gc.getPixelWriter();
        int x, y, dx, dy, incx, incy, pdx, pdy, es, el, err;

        dx = x2 - x1;
        dy = y2 - y1;
        incx = (int) Math.signum(dx);
        incy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);

        if (dx > dy) {
            pdx = incx;
            pdy = 0;
            es = dy;
            el = dx;
        } else {
            pdx = 0;
            pdy = incy;
            es = dx;
            el = dy;
        }

        x = x1;
        y = y1;
        err = el/2;
        pw.setColor(x, y, color);

        for (int t = 0; t < el; t++) {
            err -= es;
            if (err < 0) {
                err += el;
                x += incx;
                y += incy;
            } else {
                x += pdx;
                y += pdy;
            }

            pw.setColor(x, y, color);
        }
    }

    /**
     * Отрисовка интерполированной линии по цветам концов отрезка
     */
    public static void drawLineInterpolated(
            GraphicsContext gc,
            int x1, int y1, Color color1,
            int x2, int y2, Color color2
    ) {
        PixelWriter pw = gc.getPixelWriter();
        int x, y, dx, dy, incx, incy, pdx, pdy, es, el, err;

        dx = x2 - x1;
        dy = y2 - y1;
        incx = (int) Math.signum(dx);
        incy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);

        double totalLength = Math.sqrt(dx * dx + dy * dy);
        if (totalLength == 0) return;

        if (dx > dy) {
            pdx = incx;
            pdy = 0;
            es = dy;
            el = dx;
        } else {
            pdx = 0;
            pdy = incy;
            es = dx;
            el = dy;
        }

        x = x1;
        y = y1;
        err = el / 2;

        float t = 0;
        Color color = interpolateColor(color1, color2, t);
        pw.setColor(x, y, color);

        for (int i = 0; i < el; i++) {
            err -= es;
            if (err < 0) {
                err += el;
                x += incx;
                y += incy;
            } else {
                x += pdx;
                y += pdy;
            }

            double currentLength = Math.sqrt((x - x1) * (x - x1) + (y - y1) * (y - y1));
            t = (float)(currentLength / totalLength);

            color = interpolateColor(color1, color2, t);
            pw.setColor(x, y, color);
        }

        pw.setColor(x2, y2, color2);
    }

    /**
     * Метод для интерполяции цвета по коэффициенту t
     */
    private static Color interpolateColor(Color color1, Color color2, float t) {
        t = Math.max(0, Math.min(1, t));

        double r = Math.min(color1.getRed() * (1 - t) + color2.getRed() * t, 1.0);
        double g = Math.min(color1.getGreen() * (1 - t) + color2.getGreen() * t, 1.0);
        double b = Math.min(color1.getBlue() * (1 - t) + color2.getBlue() * t, 1.0);

        return new Color(r, g, b, 1.0);
    }

    /**
     * Вспомогательный метод для обмена элементов в массиве int
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /**
     * Вспомогательный метод для обмена элементов в массиве Color
     */
    private static void swap(Color[] arr, int i, int j) {
        Color temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
