package jooq5333;

import jooq5333.udt.records.BarRecord;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.DriverManager;
import java.util.List;

import static jooq5333.Tables.T_BAR;
import static jooq5333.Tables.T_FOO;
import static jooq5333.Tables.V_BAR;

/**
 * @author <a href="mailto:brent.douglas@ysura.com">Brent Douglas</a>
 */
public class jOOQ5333Test extends Assert {

    DSLContext db;

    @Before
    public void before() throws Exception{
        db = DSL.using(new DefaultConfiguration()
                .set(DriverManager.getConnection("jdbc:postgresql://localhost/jooq5333"))
                .set(SQLDialect.POSTGRES_9_4));
        db.truncate(T_BAR).execute();
        db.truncate(T_FOO).execute();
    }

    @Test
    public void testJOOQ5333() throws Exception {
        db.batch(
                db.insertInto(T_FOO)
                        .set(T_FOO.ID, 1)
                        .set(T_FOO.NAME, "First"),
                db.insertInto(T_FOO)
                        .set(T_FOO.ID, 2)
                        .set(T_FOO.NAME, "Second"),
                db.insertInto(T_BAR)
                        .set(T_BAR.ID, 1)
                        .set(T_BAR.FOO_IDS, new Integer[]{ 1, 2 })
        ).execute();

        final List<BarRecord> ret = db
                .select(V_BAR.DATA)
                .from(T_BAR)
                .leftJoin(V_BAR)
                .on(V_BAR.ID.eq(T_BAR.ID))
                .fetchInto(BarRecord.class);

        assertEquals(0, ret.size());
    }

}
