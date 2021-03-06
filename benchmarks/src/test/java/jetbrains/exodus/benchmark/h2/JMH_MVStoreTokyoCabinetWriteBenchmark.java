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
package jetbrains.exodus.benchmark.h2;

import org.h2.mvstore.MVStore;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.SECONDS)
public class JMH_MVStoreTokyoCabinetWriteBenchmark extends JMH_MVStoreTokyoCabinetBenchmarkBase {

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 2)
    @Measurement(iterations = 4)
    @Fork(4)
    public void successiveWrite() {
        computeInTransaction(new TransactionalComputable() {
            @Override
            public Object compute(@NotNull final MVStore store) {
                writeSuccessiveKeys(createTestMap(store));
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
        computeInTransaction(new TransactionalComputable() {
            @Override
            public Object compute(@NotNull final MVStore store) {
                final Map<Object, Object> map = createTestMap(store);
                for (final String key : randomKeys) {
                    map.put(key, key);
                }
                return null;
            }
        });
    }
}

