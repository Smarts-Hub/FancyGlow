package dev.smartshub.fancyglow.plugin.task;

import dev.smartshub.fancyglow.api.glow.GlowMode;
import dev.smartshub.fancyglow.plugin.registry.GlowModeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TaskIntervalResolver {

    public static int gcd(List<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return 0;
        }

        int result = Math.abs(numbers.getFirst());
        for (int i = 1; i < numbers.size(); i++) {
            result = gcd(result, Math.abs(numbers.get(i)));
        }
        return result;
    }

    public static int gcd(int a, int b) {
        if (b == 0) return Math.abs(a);
        return gcd(b, a % b);
    }

    public static int resolvePeriodTicks(Set<GlowMode> modes) {
        if (modes == null || modes.isEmpty()) {
            return 1;
        }
        List<Integer> intervals = new ArrayList<>();
        for (GlowMode mode : modes) {
            long tpc = mode.getTicksPerColor();
            if (tpc > 0) {
                intervals.add((int) tpc);
            }
        }
        int gcd = gcd(intervals);
        return gcd <= 0 ? 1 : gcd;
    }

    public static int resolvePeriodTicks(GlowModeRegistry registry) {
        return resolvePeriodTicks(registry.getAllGlowModes());
    }
}
