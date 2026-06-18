package main.java.mapreduce.visits_users_join;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;

import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;

public class JoinMapper extends TableMapper<ImmutableBytesWritable, Put> {

    private static final byte[] VISIT_CF = Bytes.toBytes("info");
    private static final byte[] USER_CF = Bytes.toBytes("info");

    private static final byte[] VISIT_ENR_CF = Bytes.toBytes("visit");
    private static final byte[] USER_ENR_CF = Bytes.toBytes("user");
    
    private Map<String, UserProfile> users = new HashMap<>();

    private Connection connection;
    private Table userTable;

    @Override
    protected void setup(Context context) throws IOException {

        Configuration conf = context.getConfiguration();

        connection = ConnectionFactory.createConnection(conf);

        userTable = connection.getTable(TableName.valueOf("web_site.users"));

        Scan scan = new Scan();

        try (ResultScanner scanner = userTable.getScanner(scan)) {

            for (Result r : scanner) {

                String userId = Bytes.toString(r.getRow());

                users.put(
                    userId,
                    new UserProfile(
                        r.getValue(USER_CF, Bytes.toBytes("first_name")),
                        r.getValue(USER_CF, Bytes.toBytes("last_name")),
                        r.getValue(USER_CF, Bytes.toBytes("age")),
                        r.getValue(USER_CF, Bytes.toBytes("gender"))
                    )
                );
            }
        }
    }

    @Override
    public void map(ImmutableBytesWritable row, Result value, Context context) 
            throws IOException, InterruptedException {

        byte[] rowKey = row.get();

        String userId = Bytes.toString(value.getValue(VISIT_CF, Bytes.toBytes("user_id")));

        UserProfile user = users.get(userId);

        if (user == null) {
            return;
        }

        Put put = new Put(rowKey);

        //RESULT JOIN IN visits_enriched
        // VISIT
        put.addColumn(VISIT_ENR_CF, Bytes.toBytes("user_id"),Bytes.toBytes(userId));
        put.addColumn(VISIT_ENR_CF, Bytes.toBytes("page"),value.getValue(VISIT_CF, Bytes.toBytes("page")));
        put.addColumn(VISIT_ENR_CF, Bytes.toBytes("country"),value.getValue(VISIT_CF, Bytes.toBytes("country")));
        put.addColumn(VISIT_ENR_CF, Bytes.toBytes("timestamp"),value.getValue(VISIT_CF, Bytes.toBytes("timestamp")));

        // USER (ENRICHMENT)
        put.addColumn(USER_ENR_CF, Bytes.toBytes("first_name"), user.firstName);
        put.addColumn(USER_ENR_CF, Bytes.toBytes("last_name"), user.lastName);
        put.addColumn(USER_ENR_CF, Bytes.toBytes("age"), user.age);
        put.addColumn(USER_ENR_CF, Bytes.toBytes("gender"), user.gender);

        context.write(row, put);
    }

    @Override
    protected void cleanup(Context context) throws IOException {

        if (userTable != null) {
            userTable.close();
        }

        if (connection != null) {
            connection.close();
        }
    }
}