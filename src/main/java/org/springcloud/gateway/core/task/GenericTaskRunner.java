/*
 * Copyright 2017 ~ 2025 the original author or authors. <springcloudgateway@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springcloud.gateway.core.task;

import static org.springcloud.gateway.core.lang.Assert2.notNull;
import static org.springcloud.gateway.core.lang.Assert2.state;
import static org.springcloud.gateway.core.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springcloud.gateway.core.log.SmartLogger;

/**
 * Generic local scheduler & task runner.
 * 
 * @author springcloudgateway <springcloudgateway@gmail.com>
 * @version v1.0.0
 * @since
 * @see {@link org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler}
 * @see <a href=
 *      "http://www.doc88.com/p-3922316178617.html">ScheduledThreadPoolExecutor
 *      Retry task OOM resolution</a>
 */
public abstract class GenericTaskRunner<C extends RunnerProperties> implements Closeable, Runnable {
    protected final SmartLogger log = getLogger(getClass());

    /** Running state. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /** Runner task properties configuration. */
    private final C config;

    /** Runner master thread. */
    private Thread masterThread;

    /** Runner worker thread group pool. */
    private SafeScheduledTaskPoolExecutor worker;

    @SuppressWarnings("unchecked")
    public GenericTaskRunner() {
        this((C) new RunnerProperties());
    }

    public GenericTaskRunner(C config) {
        notNull(config, "GenericTaskRunner properties can't null");
        this.config = config;
    }

    @Override
    public void run() {
        // Ignore
    }

    /**
     * Startup and initialization.
     */
    public void start() {
        if (running.compareAndSet(false, true)) {
            // Call PreStartup
            startingPropertiesSet();

            // Create worker(if necessary)
            if (config.getConcurrency() > 0) {
                // See:https://www.jianshu.com/p/e7ab1ac8eb4c
                ThreadFactory tf = new NamedThreadFactory(getThreadNamePrefix().concat("-worker"));
                worker = new SafeScheduledTaskPoolExecutor(config.getConcurrency(), config.getKeepAliveTime(), tf,
                        config.getAcceptQueue(), config.getReject());
            } else {
                log.warn("No start threads worker, because the number of workthreads is less than 0");
            }

            // Header asynchronously execution.(if necessary)
            switch (config.getStartupMode()) {
            case SYNC:
                run(); // Sync execution.
                break;
            case ASYNC:
                masterThread = new NamedThreadFactory(getThreadNamePrefix().concat("-header")).newThread(this);
                masterThread.start();
                break;
            default:
                break;
            }

            // Call post startup
            startedPropertiesSet();
        } else {
            log.warn("Could not startup runner! because already builders are read-only and do not allow task modification");
        }
    }

    /**
     * Do starting properties processing.
     */
    protected void startingPropertiesSet() {
        // Ignore
    }

    /**
     * Do started properties processing.
     */
    protected void startedPropertiesSet() {
        // Ignore
    }

    /**
     * Closing properties processing.
     */
    protected void closingPropertiesSet() throws IOException {
        // Ignore
    }

    /**
     * Closed properties processing.
     */
    protected void closedPropertiesSet() throws IOException {
        // Ignore
    }

    /**
     * Gets task runner thread-pools names prefix.
     * 
     * @return
     */
    protected String getThreadNamePrefix() {
        return getClass().getSimpleName();
    }

    /**
     * Gets configuration properties.
     * 
     * @return
     */
    protected C getConfig() {
        return config;
    }

    /**
     * Is the current runner active.
     * 
     * @return
     */
    public boolean isActive() {
        return nonNull(masterThread) && !masterThread.isInterrupted() && isStarted();
    }

    /**
     * Is the current runner started.
     * 
     * @return
     */
    public boolean isStarted() {
        return running.get();
    }

    /**
     * Thread pool executor worker.
     * 
     * @return
     */
    public SafeScheduledTaskPoolExecutor getWorker() {
        state(nonNull(worker),
                "The worker thread group is not enabled(must concurrency>0)? or  it has not been initialized yet, it must be called at least in the after #postStartupProperties().");
        return worker;
    }

    /**
     * Note: It is recommended to use the {@link AtomicBoolean} mechanism to
     * avoid using synchronized. </br>
     * Error example:
     * 
     * <pre>
     * public abstract class ParentClass implements Closeable, Runnable {
     *     public synchronized void close() {
     *         // Some close or release ...
     *     }
     * }
     * 
     * public class SubClass extends ParentClass {
     *     public synchronized void run() {
     *         // Long-time jobs ...
     * 
     *         // For example:
     *         // while(true) {
     *         // ...
     *         // }
     *     }
     * }
     * </pre>
     * 
     * At this time, it may lead to deadlock, because SubClass.run() has not
     * been executed and is not locked, resulting in the call to
     * ParentClass.close() always waiting. </br>
     * </br>
     */
    @Override
    public void close() throws IOException {
        if (running.compareAndSet(true, false)) {
            // Call pre close
            closingPropertiesSet();

            // Close for thread pool worker.
            if (!isNull(worker)) {
                try {
                    worker.shutdown();
                    worker = null;
                } catch (Exception e) {
                    log.error("Runner worker shutdown failed!", e);
                }
            }

            // Close for thread-boss.
            try {
                if (!isNull(masterThread)) {
                    masterThread.interrupt();
                    masterThread = null;
                }
            } catch (Exception e) {
                log.error("Runner boss interrupt failed!", e);
            }

            // Call post close
            closedPropertiesSet();
        }
    }

    /**
     * The named thread factory
     */
    private class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threads = new AtomicInteger(1);
        private final ThreadGroup group;
        private final String prefix;

        NamedThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            if (isBlank(prefix)) {
                prefix = GenericTaskRunner.class.getSimpleName() + "-default";
            }
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, prefix + "-" + threads.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}