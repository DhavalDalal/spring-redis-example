package com.tsys.digital.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.embedded.RedisServer;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class EmbeddedRedisServerRunner {

  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedRedisServerRunner.class);
  private final int waitToStartUntil;
  private final TimeUnit timeUnit;
  private RedisServer redisServer;
  private int waitCycles;
  final ExecutorService executorService;

  EmbeddedRedisServerRunner(File redisServerExecutable, int port, int waitToStartUntil, TimeUnit timeUnit) {
    this.waitToStartUntil = waitToStartUntil;
    this.timeUnit = timeUnit;
    executorService = Executors.newSingleThreadExecutor();
    redisServer = new RedisServer(redisServerExecutable, port) {
      @Override
      protected String redisReadyPattern() {
        return ".*Ready to accept connections";
      }
    };
  }
  public void start() throws InterruptedException {
    LOG.info("RedisServer Starting...");
    executorService.submit(redisServer::start);
    while(!redisServer.isActive()) {
      if (waitCycles == 1) {
        LOG.error("Giving up Waiting for RedisServer to start...");
        executorService.awaitTermination(30, TimeUnit.MILLISECONDS);
        executorService.shutdown();
        throw new RuntimeException("Could not start EmbeddedRedisServer! Increase wait time");
      }
      try {
        LOG.info("Waiting for RedisServer to be fully up...");
        Thread.sleep(timeUnit.toMillis(waitToStartUntil));
        waitCycles++;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    LOG.info("RedisServer Started!");
  }

  public void stop() throws InterruptedException {
    LOG.info("Stopping RedisServer...");
    redisServer.stop();
    executorService.awaitTermination(30, TimeUnit.MILLISECONDS);
    executorService.shutdown();
    LOG.info("RedisServer Stopped!");
  }
}
