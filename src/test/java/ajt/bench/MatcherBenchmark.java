package ajt.bench;

import ajt.common.enums.Side;
import ajt.events.EventSink;
import ajt.matcher.impl.PriceTimeMatcher;
import ajt.order.Order;
import ajt.order.books.OrderBookImpl;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Microbenchmark of PriceTimeMatcher.match with a no-op EventSink, so printing and formatting are excluded.
 *
 * Workload (one invocation = one "cycle" of 4 match() calls, so ns/op is reported per match call):
 *   1. rest  BUY  A  qty 100 at price P            (rests)
 *   2. rest  BUY  B  qty 100 at price P            (rests, behind A in the queue)
 *   3. SELL aggressor qty 150 at price P           (fully fills A, partially fills B)
 *   4. SELL aggressor qty  50 at price P           (fills the rest of B)
 * and the mirrored cycle for the ask side. Every cycle nets to zero, so the book stays in a steady state
 * and the benchmark can run indefinitely. Prices are drawn from a seeded array, so every run (and every
 * code change you compare) sees the identical order stream.
 *
 * A static background book of resting orders sits far from the traded prices and never trades. It gives the
 * TreeMaps realistic depth, so lookups cost what they would on a populated book.
 *
 * Note: a new Order is allocated per match call, because Order.leavesQty is mutated. That is a constant
 * floor in the gc.alloc.rate.norm figure and is the same before and after each change you compare.
 */
@BenchmarkMode({Mode.AverageTime, Mode.SampleTime})
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 8, time = 2)
@Fork(2)
public class MatcherBenchmark {

    private static final int CYCLES_PRESET = 4096;   // length of the pre-generated price array
    private static final long BID_MIN = 9_000_00L;   // traded bids:  9000.00 .. 9999.00 (scaled by 100 here)
    private static final long ASK_MIN = 10_001_00L;  // traded asks: 10001.00 .. 11000.00
    private static final long STEP = 100L;
    private static final int TRADED_LEVELS = 1000;

    private static final EventSink NO_OP = new EventSink() {
        @Override public void accept(Order order) { }
        @Override public void rest(Order order) { }
        @Override public void fill(Order aggressor, Order resting, long price, long qty) { }
    };

    @State(Scope.Thread)
    public static class Book {

        /** Number of price levels of background (never traded) depth on each side. */
        @Param({"100", "1000"})
        int backgroundLevels;

        /** Resting orders per background level. */
        @Param({"10"})
        int ordersPerLevel;

        OrderBookImpl book;
        PriceTimeMatcher matcher;
        long[] bidPrices;
        long[] askPrices;
        long nextId;
        int cursor;

        @Setup(Level.Trial)
        public void setUp() {
            book = new OrderBookImpl();
            matcher = new PriceTimeMatcher(NO_OP);
            nextId = 1;

            // Background depth, far from traded prices: bids below 9000, asks above 11000.
            for (int level = 0; level < backgroundLevels; level++) {
                long bidPrice = 8_999_00L - level * STEP;
                long askPrice = 11_001_00L + level * STEP;
                for (int i = 0; i < ordersPerLevel; i++) {
                    book.add(new Order(nextId++, 1, Side.BUY, bidPrice, 100));
                    book.add(new Order(nextId++, 1, Side.SELL, askPrice, 100));
                }
            }

            // Seeded, pre-generated traded prices, so the order stream is identical on every run.
            Random random = new Random(42);
            bidPrices = new long[CYCLES_PRESET];
            askPrices = new long[CYCLES_PRESET];
            for (int i = 0; i < CYCLES_PRESET; i++) {
                bidPrices[i] = BID_MIN + random.nextInt(TRADED_LEVELS) * STEP;
                askPrices[i] = ASK_MIN + random.nextInt(TRADED_LEVELS) * STEP;
            }
        }
    }

    /** Bid-side cycle: two resting buys, then two selling aggressors that fill them. 4 match calls. */
    @Benchmark
    @OperationsPerInvocation(4)
    public void bidSideCycle(Book s) {
        long price = s.bidPrices[s.cursor++ & (CYCLES_PRESET - 1)];
        s.matcher.match(new Order(s.nextId++, 1, Side.BUY, price, 100), s.book);
        s.matcher.match(new Order(s.nextId++, 1, Side.BUY, price, 100), s.book);
        s.matcher.match(new Order(s.nextId++, 1, Side.SELL, price, 150), s.book);
        s.matcher.match(new Order(s.nextId++, 1, Side.SELL, price, 50), s.book);
    }

    /** Ask-side mirror: two resting sells, then two buying aggressors that fill them. 4 match calls. */
    @Benchmark
    @OperationsPerInvocation(4)
    public void askSideCycle(Book s) {
        long price = s.askPrices[s.cursor++ & (CYCLES_PRESET - 1)];
        s.matcher.match(new Order(s.nextId++, 1, Side.SELL, price, 100), s.book);
        s.matcher.match(new Order(s.nextId++, 1, Side.SELL, price, 100), s.book);
        s.matcher.match(new Order(s.nextId++, 1, Side.BUY, price, 150), s.book);
        s.matcher.match(new Order(s.nextId++, 1, Side.BUY, price, 50), s.book);
    }
}
