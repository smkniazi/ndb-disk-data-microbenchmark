package com.lc;


import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Configuration {

  private final int numThreadsDefault = 1;
  @Option(name = "-numThreads", usage = "Number of threads. Default: " + numThreadsDefault)
  private int numThreads = numThreadsDefault;

  private final int dataSizeDefault = 8000;
  @Option(name = "-dataSize", usage = "Disk column size. Default: " + dataSizeDefault)
  private int dataSize = dataSizeDefault;

  private final String schemaDefault = "test";
  @Option(name = "-schema", usage = "Schema name. Default: " + schemaDefault)
  private String schema = "test";

  private final String hostDefault = "localhost" ;
  @Option(name = "-dbHost", usage = "Host. Default: "+hostDefault)
  private String dbHost = "localhost";

  private final long benchmarkDurationDefault = 10000;
  @Option(name = "-bmDuration", usage = "For how long the bench mark should run. Time in ms. " +
          "Default: " + benchmarkDurationDefault)
  private long benchmarkDuration = benchmarkDurationDefault;

  private final int clientIdDefault = 0;
  @Option(name = "-clientId", usage = "Id of this application. Default: " + clientIdDefault)
  private int clientId = clientIdDefault;

  @Option(name = "-help", usage = "Print usages")
  private boolean help = false;

  public int getNumThreads() {
    return numThreads;
  }

  public String getSchema() {
    return schema;
  }

  public String getDbHost() {
    return dbHost;
  }

  public long getBenchmarkDuration() {
    return benchmarkDuration;
  }

  public int getClientId() {
    return clientId;
  }

  public int getDataSize() {
    return dataSize;
  }

  public void parseArgs(String[] args) {
    CmdLineParser parser = new CmdLineParser(this);
    parser.setUsageWidth(80);
    try {
      // parse the arguments.
      parser.parseArgument(args);

    } catch (Exception e) {
      showHelp(parser, true);
    }

    if (help) {
      showHelp(parser, true);
    }
  }

  private void showHelp(CmdLineParser parser, boolean kill) {
    parser.printUsage(System.err);
    if (kill) {
      System.exit(0);
    }
  }

}
