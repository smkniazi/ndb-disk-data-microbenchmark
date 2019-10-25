package com.lc;

import com.mysql.clusterj.LockMode;
import com.mysql.clusterj.Query;
import com.mysql.clusterj.Session;
import com.mysql.clusterj.SessionFactory;
import com.mysql.clusterj.query.Predicate;
import com.mysql.clusterj.query.QueryBuilder;
import com.mysql.clusterj.query.QueryDomainType;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class Worker implements Callable {
  final AtomicInteger successfulOps;
  final AtomicInteger failedOps;
  final MicroBenchType microBenchType;
  final SessionFactory sf;
  final SynchronizedDescriptiveStatistics latency;
  final Configuration conf;
  final Namespace ns;
  final byte[] data;
  Random rand = new Random(System.nanoTime());
  int counter = 0;
  long bmStartTime = 0;

  final List<Set<Row>> dataSet = new ArrayList<Set<Row>>();

  public Worker(AtomicInteger successfulOps, AtomicInteger failedOps,
                MicroBenchType bmType, SessionFactory sf,
                SynchronizedDescriptiveStatistics latency, Configuration conf,
                Namespace ns) {
    this.successfulOps = successfulOps;
    this.failedOps = failedOps;
    this.sf = sf;
    this.latency = latency;
    this.microBenchType = bmType;
    this.conf = conf;
    this.ns = ns;
    this.data = new byte[conf.getDataSize()];
  }

  @Override
  public Object call() {
    Session dbSession = sf.getSession();
    bmStartTime = System.currentTimeMillis();
    lastPrintTime = bmStartTime;
    while (true) {
      try {
        long startTime = System.nanoTime();
        dbSession.currentTransaction().begin();
        performOperation(dbSession);
        dbSession.currentTransaction().commit();
        long opExeTime = (System.nanoTime() - startTime);
        latency.addValue(opExeTime);
        successfulOps.incrementAndGet();
        printSpeed(bmStartTime, successfulOps);
      } catch (Throwable e) {
        failedOps.incrementAndGet();
        e.printStackTrace();
        dbSession.currentTransaction().rollback();
      } finally {
        if ((System.currentTimeMillis() - bmStartTime) > conf.getBenchmarkDuration()) {
          break;
        }
      }
    }
    dbSession.close();
    return null;
  }

  @Override
  protected void finalize() throws Throwable {
  }

  public void performOperation(Session session) throws Exception {
    switch (microBenchType) {
      case WRITE_TEST:
        writeTest(session);
        return;
      case READ_TEST:
        readTest(session);
        return;
      default:
        throw new IllegalStateException("Micro bench mark not supported");
    }
  }

  void writeTest(Session session) throws InterruptedException {
    Table row = session.newInstance(Table.class);
    long pk = rand.nextLong();
    row.setId(pk);
    row.setData(data);
    session.savePersistent(row);
    release(session, row);
    ns.put(pk);
  }

  void readTest(Session session) {
    long pk = ns.getRandom();
    Table dbRow = session.find(Table.class, pk);
    assert dbRow.getData().length == conf.getDataSize();
    release(session, dbRow);
  }

  private void release(Session session, Table row) {
    session.release(row);
  }

  static long lastPrintTime = System.currentTimeMillis();

  private synchronized static void printSpeed(long startTime, AtomicInteger successfulOps) {
    long curTime = System.currentTimeMillis();
    if ((curTime - lastPrintTime) > 5000) {
      long timeElapsed = (System.currentTimeMillis() - startTime);
      double speed = (successfulOps.get() / (double) timeElapsed) * 1000;
      System.out.println("Successful Ops: " + successfulOps + "\tSpeed: " + speed + " ops/sec.");
      lastPrintTime = System.currentTimeMillis();
    }
  }

}

