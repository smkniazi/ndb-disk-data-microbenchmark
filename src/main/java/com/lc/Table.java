package com.lc;

import com.mysql.clusterj.annotation.*;

@PersistenceCapable(table = "test")
@PartitionKey(column = "partition_id")
public interface Table {
  @PrimaryKey
  @Column(name = "id")
  long getId();
  void setId(long id);

  @Column(name = "data")
  byte[] getData();
  void setData(byte[] data);
}

