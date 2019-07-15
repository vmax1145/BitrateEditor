package org.vmax.amba.tables.math;

import java.util.ArrayList;
import java.util.List;


    public class DouglassPeucker {
        public static List<ChartPoint> reduce(List<ChartPoint> points, double err) {
            if (err < 0) {
                return new ArrayList<>(points);
            }
            double furthestPointDistance = 0.0;
            int furthestPointIndex = 0;
            LineInt line = new LineInt(points.get(0), points.get(points.size() - 1));
            for (int i = 1; i < points.size() - 1; i++) {
                double distance = line.distance(points.get(i));
                if (distance > furthestPointDistance ) {
                    furthestPointDistance = distance;
                    furthestPointIndex = i;
                }
            }
            if (furthestPointDistance > err) {
                List<ChartPoint> result1 = reduce(points.subList(0, furthestPointIndex+1), err);
                List<ChartPoint> result2 = reduce(points.subList(furthestPointIndex, points.size()), err);
                List<ChartPoint> result = new ArrayList<>(result1);
                result.addAll(result2.subList(1, result2.size()));
                return result;
            } else {
                return new ArrayList<>(line.asList());
            }
        }
    }

