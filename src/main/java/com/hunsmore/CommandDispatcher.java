package com.hunsmore;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author htf
 */
public class CommandDispatcher {
    private static final String ILLEGAL_COMMANDS = "ERR ILLEGAL_COMMANDS";
    private static final String UNSUPPORTED_COMMAND = "ERR UNSUPPORTED_COMMAND_";

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ExecutorService executor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            (r) -> new Thread(Thread.currentThread().getThreadGroup(), String.format("hannah-kv-command-dispatcher-%d", threadNumber.getAndIncrement())),
            new ThreadPoolExecutor.CallerRunsPolicy());
    private final KeyValueService keyValueService;

    public CommandDispatcher(KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    public Future<Object> dispatchAndRun(String input) {
        String[] commandAndParameters = Iterables.toArray(Splitter.onPattern(" ").omitEmptyStrings().split(input), String.class);
        if (commandAndParameters.length == 0) {
            return Futures.immediateFuture(ILLEGAL_COMMANDS);
        }
        String command = commandAndParameters[0].toUpperCase(Locale.ENGLISH);
        switch (command) {
            case "GET":
                if (commandAndParameters.length != 2) {
                    return Futures.immediateFuture(ILLEGAL_COMMANDS);
                }
                return executor.submit(() -> keyValueService.getBytes(commandAndParameters[1]));
            case "SET":
                if (commandAndParameters.length != 3) {
                    return Futures.immediateFuture(ILLEGAL_COMMANDS);
                }
                return executor.submit(() -> keyValueService.setBytes(commandAndParameters[1], commandAndParameters[2].getBytes(StandardCharsets.UTF_8)));
            default:
                return Futures.immediateFuture(UNSUPPORTED_COMMAND + command);
        }
    }
}
