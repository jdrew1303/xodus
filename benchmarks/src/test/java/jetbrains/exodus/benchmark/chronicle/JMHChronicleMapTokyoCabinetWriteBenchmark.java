/**
 * Copyright 2010 - 2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.exodus.benchmark.chronicle;

import net.openhft.chronicle.map.ChronicleMap;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JMHChronicleMapTokyoCabinetWriteBenchmark extends JMHChronicleMapTokyoCabinetBenchmarkBase {

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 2)
    @Measurement(iterations = 4)
    @Fork(4)
    public void successiveWrite() {
        computeInTransaction(new TransactionalComputable<Void>() {
            @Override
            public Void compute(@NotNull ChronicleMap<String, String> map) {
                writeSuccessiveKeys(map);
                return null;
            }
        });
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 2)
    @Measurement(iterations = 4)
    @Fork(4)
    public void randomWrite() {
        computeInTransaction(new TransactionalComputable<Void>() {
            @Override
            public Void compute(@NotNull ChronicleMap<String, String> map) {
                for (final String key : randomKeys) {
                    map.put(key, key);
                }
                return null;
            }
        });
    }
}
