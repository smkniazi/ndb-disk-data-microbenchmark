package com.lc;

public enum MicroBenchType {
  READ_TEST("READ_TEST"),
  WRITE_TEST("WRITE_TEST");
  private final String name;
  private MicroBenchType(String name){
    this.name = name;
  }

  public boolean equalsName(String otherName) {
    // (otherName == null) check is not needed because name.equals(null) returns false
    return name.equals(otherName);
  }

  public String toString() {
    return this.name;
  }

}
