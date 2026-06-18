package main.java.mapreduce.hbase_table_utils;

import java.io.IOException;

import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.conf.Configuration;

public class HBaseTableUtils {

    public static void createTableIfNotExists(
            Configuration conf,
            String tableNameStr,
            String... columnFamilies
    ) throws IOException {

        Connection connection = ConnectionFactory.createConnection(conf);
        Admin admin = connection.getAdmin();

        TableName tableName = TableName.valueOf(tableNameStr);

        if (!admin.tableExists(tableName)) {

            TableDescriptorBuilder builder = TableDescriptorBuilder.newBuilder(tableName);

            for (String cf : columnFamilies) {
                builder.setColumnFamily(ColumnFamilyDescriptorBuilder.of(cf));
            }

            admin.createTable(builder.build());
        }

        admin.close();
        connection.close();
    }
}