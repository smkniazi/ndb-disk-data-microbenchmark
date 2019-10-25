package com.lc;


import com.mysql.clusterj.ClusterJHelper;
import com.mysql.clusterj.SessionFactory;
import org.kohsuke.args4j.Option;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

public class MicroBenchMain {

  private AtomicInteger successfulOps = new AtomicInteger(0);
  private AtomicInteger failedOps = new AtomicInteger(0);
  private static long lastOutput = 0;
  private SynchronizedDescriptiveStatistics latency = new SynchronizedDescriptiveStatistics();
  private Configuration conf;
  private Namespace ns = new Namespace();

  Random rand = new Random(System.currentTimeMillis());
  ExecutorService executor = null;
  SessionFactory sf = null;
  @Option(name = "-help", usage = "Print usages")
  private boolean help = false;

  private List workers = new ArrayList<Worker>();


  public void startApplication(String[] args) throws Exception {
    conf = new Configuration();
    conf.parseArgs(args);

    setUpDBConnection();

    // write test
    createWorkers(MicroBenchType.WRITE_TEST);

    long startTime = System.currentTimeMillis();
    startMicroBench();
    long totExeTime = (System.currentTimeMillis() - startTime);

    long avgSpeed = (long) (((double) successfulOps.get() / (double) totExeTime) * 1000);
    double avgLatency = latency.getMean() / 1000000;

    String msg = "Results: Write Test  NumThreads: " + conf.getNumThreads() +
            " Total Ops: "+successfulOps+
            " Speed: " + avgSpeed + " ops/sec" + ".\t\tAvg Op Latency: " + avgLatency;
    blueColoredText(msg);
    executor.shutdown();

    latency.clear();
    successfulOps.set(0);
    failedOps.set(0);

    // read test
    createWorkers(MicroBenchType.READ_TEST);

    startTime = System.currentTimeMillis();
    startMicroBench();
    totExeTime = (System.currentTimeMillis() - startTime);

    avgSpeed = (long) (((double) successfulOps.get() / (double) totExeTime) * 1000);
    avgLatency = latency.getMean() / 1000000;

    msg = "Results: Read Test  NumThreads: " + conf.getNumThreads() +
            " Total Ops: "+successfulOps+
            " Speed: " + avgSpeed + " " + "ops" + "/sec" + ".\t\tAvg Op Latency: " + avgLatency;
    blueColoredText(msg);
    executor.shutdown();
  }

  public void setUpDBConnection() throws Exception {
    Properties props = new Properties();
    props.setProperty("com.mysql.clusterj.connectstring", conf.getDbHost());
    props.setProperty("com.mysql.clusterj.database", conf.getSchema());
    props.setProperty("com.mysql.clusterj.connect.retries", "4");
    props.setProperty("com.mysql.clusterj.connect.delay", "5");
    props.setProperty("com.mysql.clusterj.connect.verbose", "1");
    props.setProperty("com.mysql.clusterj.connect.timeout.before", "30");
    props.setProperty("com.mysql.clusterj.connect.timeout.after", "20");
    props.setProperty("com.mysql.clusterj.max.transactions", "1024");
    props.setProperty("com.mysql.clusterj.connection.pool.size", "1");
    sf = ClusterJHelper.getSessionFactory(props);
  }


  public void createWorkers(MicroBenchType type) throws InterruptedException, IOException {
    workers.clear();
    executor = Executors.newFixedThreadPool(conf.getNumThreads());

    for (int i = 0; i < conf.getNumThreads(); i++) {
      Worker worker = new Worker(successfulOps, failedOps,
              type, sf, latency, conf, ns);
      workers.add(worker);
    }
  }

  public void startMicroBench() throws InterruptedException, IOException {
    executor.invokeAll(workers); //blocking call
  }


  protected void redColoredText(String msg) {
    System.out.println((char) 27 + "[31m" + msg);
    System.out.print((char) 27 + "[0m");
  }

  protected static void blueColoredText(String msg) {
    System.out.println((char) 27 + "[36m" + msg);
    System.out.print((char) 27 + "[0m");
  }

}
