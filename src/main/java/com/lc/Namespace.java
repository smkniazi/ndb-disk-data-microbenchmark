package com.lc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Namespace {
  private List<Long> pks = new ArrayList<Long>(100000);
  private Random rand = new Random(System.currentTimeMillis());

  public synchronized  void put(long pk){
    pks.add(pk);
  }

  public synchronized long getRandom(){
    return pks.get(rand.nextInt(pks.size()));
  }
}
