package org.kapott.hbci4java;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kapott.hbci.GV.AbstractHBCIJob;
import org.kapott.hbci.GV_Result.HBCIJobResult;
import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.callback.HBCICallbackConsole;
import org.kapott.hbci.manager.HBCIDialog;
import org.kapott.hbci.manager.HBCIJobFactory;
import org.kapott.hbci.manager.MessageFactory;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.PinTanPassport;
import org.kapott.hbci.status.HBCIExecStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


public class AbstractTestGV extends AbstractTest {
    private final static int LOGLEVEL = 5;
    protected final static Map<Integer, String> settings = new HashMap<Integer, String>() {{
        put(HBCICallback.NEED_COUNTRY, "DE");
        put(HBCICallback.NEED_FILTER, "Base64");
        put(HBCICallback.NEED_PASSPHRASE_LOAD, "test");
        put(HBCICallback.NEED_PASSPHRASE_SAVE, "test");
        put(HBCICallback.NEED_PORT, "443");
        put(HBCICallback.NEED_CONNECTION, ""); // ignorieren
        put(HBCICallback.CLOSE_CONNECTION, ""); // ignorieren
    }};

    protected static File dir = null;

    protected PinTanPassport passport = null;
    protected HBCIDialog dialog = null;
    protected Properties params = new Properties();


    @Test
    public void test() {
        System.out.println("---------Erstelle Job");

        AbstractHBCIJob job = HBCIJobFactory.newJob(getJobname(), dialog.getPassport());

        int source_acc_idx = Integer.parseInt(params.getProperty("source_account_idx"));
        job.setParam("src", passport.getAccounts()[source_acc_idx]);

        System.out.println("---------Für Job zur Queue");
        dialog.addTask(job);


        HBCIExecStatus ret = dialog.execute(true);
        HBCIJobResult res = job.getJobResult();
        System.out.println("----------Result: " + res.toString());

        Assert.assertEquals("Job Result ist nicht OK!", true, res.isOK());
    }

    /**
     * Erzeugt das Passport-Objekt.
     *
     * @throws Exception
     */
    @Before
    public void beforeTest() throws Exception {
        // Testdatei im Arbeitsverzeichnis - sollte in der Run-Konfiguration auf ein eigenes Verzeichnis zeigen
        String workDir = System.getProperty("user.dir");
        InputStream in = new FileInputStream(workDir + "/" + getTestfilename());
        params.load(in);

        settings.put(HBCICallback.NEED_BLZ, params.getProperty("blz"));
        settings.put(HBCICallback.NEED_CUSTOMERID, params.getProperty("customerid"));
        settings.put(HBCICallback.NEED_HOST, params.getProperty("host"));
        settings.put(HBCICallback.NEED_PT_PIN, params.getProperty("pin"));
        settings.put(HBCICallback.NEED_USERID, params.getProperty("userid"));
        settings.put(HBCICallback.NEED_PT_SECMECH, params.getProperty("secmech"));

        Properties props = new Properties();
        props.put("log.loglevel.default", Integer.toString(LOGLEVEL));
        props.put("infoPoint.enabled", Boolean.FALSE.toString());

        props.put("client.passport.PinTan.filename", dir.getAbsolutePath() + File.separator + System.currentTimeMillis() + ".pt");
        props.put("client.passport.PinTan.init", "1");
        props.put("client.passport.PinTan.checkcert", "0"); // Check der SSL-Zertifikate abschalten - brauchen wir nicht fuer den Test

        // falls noetig
        props.put("client.passport.PinTan.proxy", ""); // host:port
        props.put("client.passport.PinTan.proxyuser", "");
        props.put("client.passport.PinTan.proxypass", "");

        HBCICallback callback = new HBCICallbackConsole() {
            public void callback(HBCIPassport passport, int reason, String msg, int datatype, StringBuffer retData) {
                // haben wir einen vordefinierten Wert?
                String value = settings.get(reason);
                if (value != null) {
                    retData.replace(0, retData.length(), value);
                    return;
                }

                // Ne, dann an Super-Klasse delegieren
                super.callback(reason, msg, datatype, retData);
            }
        };

//      HBCIUtils.init(props,callback);
//      this.passport = (PinTanPassport) AbstractHBCIPassport.getInstance("PinTan");

        // init handler
        this.dialog = new HBCIDialog(passport);
    }

    /**
     * Erzeugt das Passport-Verzeichnis.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
        dir = new File(tmpDir, "hbci4java-junit-" + System.currentTimeMillis());
        dir.mkdirs();
    }

    /**
     * Loescht das Passport-Verzeichnis.
     *
     * @throws Exception
     */
    @AfterClass
    public static void afterClass() throws Exception {
        if (!dir.delete())
            throw new Exception("unable to delete " + dir);
    }

    protected String getJobname() {
        Assert.assertEquals("Jobname not defined", true, false);
        return "";
    }

    protected String getTestfilename() {
        return getJobname() + ".properties";
    }

    private void dump(String name, Properties props) {
        System.out.println("--- BEGIN: " + name + " -----");
        Iterator keys = props.keySet().iterator();
        while (keys.hasNext()) {
            Object key = keys.next();
            System.out.println(key + ": " + props.get(key));
        }
        System.out.println("--- END: " + name + " -----");
    }

}
