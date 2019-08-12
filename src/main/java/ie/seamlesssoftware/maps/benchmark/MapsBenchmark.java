package ie.seamlesssoftware.maps.benchmark;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.commons.collections.IterableMap;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.HashedMap;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.map.MutableMap;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)					// Prints benchmark results using milliseconds as time unit
@Fork(value = 2, jvmArgs = {"-Xms1G", "-Xmx1G"})
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MICROSECONDS)
public class MapsBenchmark {
    static private int initialCapacity = 100000;
    static private Map<Integer, Integer> map = new HashMap<Integer, Integer>(initialCapacity);
    static private IterableMap iterableMap = new HashedMap(initialCapacity);
    static private MutableMap mutableMap = Maps.mutable.withInitialCapacity(initialCapacity);

    @State(Scope.Benchmark)
    public static class MyState {

    	@TearDown(Level.Trial)
        public void tearDown() {
        	System.gc();
        }
        
        @Setup
        public void setUp() {
            map.clear();
            for (int i = 0; i < initialCapacity; i++) map.put(new Integer(i), new Integer(i));

            iterableMap.clear();
            for (int i = 0; i < initialCapacity; i++) iterableMap.put(new Integer(i), new Integer(i));

            mutableMap.clear();
            for (int i = 0; i < initialCapacity; i++) mutableMap.put(new Integer(i), new Integer(i));
    	}
    }

    @Benchmark
    public void usingWhileIteratorMapEntry(MyState state) {
        long i = 0;

        Iterator<Map.Entry<Integer, Integer>> it = map.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Integer, Integer> pair = it.next();
            i += pair.getKey() + pair.getValue();
        }
    }

    @Benchmark
    public void usingForMapEntry(MyState state) {
        long i = 0;

        for (Map.Entry<Integer, Integer> pair : map.entrySet())
            i += pair.getKey() + pair.getValue();
    }

    @Benchmark
    public void usingForEachJava8(MyState state) {
        final long[] i = {0};

        map.forEach((k, v) -> i[0] += k + v);
    }

    @Benchmark
    public void usingForKeySet(MyState state) {
        long i = 0;

        for (Integer key : map.keySet())
            i += key + map.get(key);
    }

    @Benchmark
    public void usingWhileIteratorKeySet(MyState state) {
        long i = 0;
        Iterator<Integer> itr2 = map.keySet().iterator();

        while (itr2.hasNext()) {
            Integer key = itr2.next();
            i += key + map.get(key);
        }
    }

    @Benchmark
    public void usingForIteratorMapEntry(MyState state) {
        long i = 0;

        for (Iterator<Map.Entry<Integer, Integer>> entries = map.entrySet().iterator(); entries.hasNext(); ) {
            Map.Entry<Integer, Integer> entry = entries.next();
            i += entry.getKey() + entry.getValue();
        }
    }

    @Benchmark
    public void usingJava8Streams(MyState state) {
        final long[] i = {0};

        map.entrySet().stream().forEach(e -> i[0] += e.getKey() + e.getValue());
    }

    @Benchmark
    public void usingJava8ParallelStreams(MyState state) {
        final AtomicInteger[] i = { new AtomicInteger(0)};

        map.entrySet().stream().parallel().forEach(e -> i[0].getAndAdd(e.getKey() + e.getValue()));
    }

    @Benchmark
    public void usingApacheCollections(MyState state) {
        long i = 0;

        MapIterator it = iterableMap.mapIterator();
        while (it.hasNext()) {
            i += (Integer) it.next() + (Integer) it.getValue();
        }
    }

    @Benchmark
    public void usingEclipseCollections(MyState state) {
        final long[] i = {0};

        mutableMap.forEachKeyValue((key, value) -> {
            i[0] += (Integer) key + (Integer) value;
        });
    }

}
