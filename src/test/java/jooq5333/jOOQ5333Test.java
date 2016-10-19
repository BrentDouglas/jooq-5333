package jooq5333;

import jooq5333.udt.records.BazRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import static jooq5333.Tables.T_BAR;
import static jooq5333.Tables.T_BAZ;
import static jooq5333.Tables.T_FOO;
import static jooq5333.Tables.V_BAZ;

/**
 * @author <a href="mailto:brent.douglas@ysura.com">Brent Douglas</a>
 */
public class jOOQ5333Test extends Assert {

    DSLContext db;

    @Before
    public void before() throws Exception {
        db = DSL.using(new DefaultConfiguration()
                .set(DriverManager.getConnection("jdbc:postgresql://localhost/jooq5333"))
                .set(SQLDialect.POSTGRES_9_4));
        db.truncate(T_BAZ).execute();
        db.truncate(T_BAR).execute();
        db.truncate(T_FOO).execute();
    }

    @Test
    public void testJOOQ5333() throws Exception {
        try {
            db.batch(
                    db.insertInto(T_FOO)
                            .set(T_FOO.ID, 1)
                            .set(T_FOO.NAME, "\"First"),
                    db.insertInto(T_FOO)
                            .set(T_FOO.ID, 2)
                            .set(T_FOO.NAME, "\"Quoted & , asdf")
                            .set(T_FOO.JSON, "{\"foo\":9,\"bar\":[\"a\",\"b\"],\"c\":{\"a\":[{\"b\":5},{\"b\":5}]}}"),
                    db.insertInto(T_FOO)
                            .set(T_FOO.ID, 3)
                            .set(T_FOO.NAME, "adf")
                            .set(T_FOO.JSON,
                                    "[{\"foo\":9,\"bar\":[\"a\",\"b\"],\"c\":{\"a\":[{\"b\":5},{\"b\":5}]}},{\"foo\":9,\"bar\":[\"a\",\"b\"],\"c\":{\"a\":[{\"b\":5},{\"b\":5}]}}]"),
                    db.insertInto(T_FOO)
                            .set(T_FOO.ID, 4)
                            .set(T_FOO.NAME, "asdgf"),
                    db.insertInto(T_BAR)
                            .set(T_BAR.ID, 1)
                            .set(T_BAR.FOO_IDS, new Integer[]{1, 2}),
                    db.insertInto(T_BAR)
                            .set(T_BAR.ID, 2)
                            .set(T_BAR.FOO_IDS, new Integer[]{3, 4}),
                    db.insertInto(T_BAZ)
                            .set(T_BAZ.ID, 1)
                            .set(T_BAZ.FOO_IDS, new Integer[]{1, 2, 3, 4})
                            .set(T_BAZ.BAR_IDS, new Integer[]{1, 2})
            ).execute();

            final List<BazRecord> ret = db
                    .select(V_BAZ.DATA)
                    .from(T_BAZ)
                    .leftJoin(V_BAZ)
                    .on(V_BAZ.ID.eq(T_BAZ.ID))
                    .fetchInto(BazRecord.class);

            assertEquals(1, ret.size());
        } catch (final DataAccessException e) {
            if (e.getCause() instanceof SQLException) {
                e.addSuppressed(((SQLException) e.getCause()).getNextException());
            }
            throw e;
        }
    }

}
