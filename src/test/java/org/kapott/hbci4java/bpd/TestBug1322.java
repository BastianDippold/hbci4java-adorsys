/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package org.kapott.hbci4java.bpd;

import org.kapott.hbci4java.AbstractTest;

import java.util.Hashtable;
import java.util.Iterator;

import org.junit.Test;
import org.kapott.hbci.manager.HBCIKernel;
import org.kapott.hbci.manager.MsgGen;
import org.kapott.hbci.protocol.MSG;

/**
 * Test fuer die neuen grossen BPD bei der GAD.
 * BUGZILLA 1322
 */
public class TestBug1322 extends AbstractTest {
    /**
     * Versucht, die BPD mit dem ueberlangen (mehr als 999 Zeichen) HIVISS Segment in der HBCI-Version
     * "FinTS3" zu parsen.
     *
     * @throws Exception
     */
    @Test
    public void test001() throws Exception {
        try {
            String data = getFile("bpd/bugzilla-1322.txt");
            HBCIKernel kernel = new HBCIKernel(null);
            kernel.rawNewMsg("DialogInitAnon");

            MsgGen gen = kernel.getMsgGen();
            MSG msg = new MSG("DialogInitAnonRes", data, data.length(), gen, MSG.CHECK_SEQ, true);
            Hashtable<String, String> ht = new Hashtable<String, String>();
            msg.extractValues(ht);

            // Wir checken noch, ob in der Testdatei tatsaechlich ein Segment mit
            // mehr als 999 Zeichen drin war. Wenn nicht, deckt die Testdatei
            // den Testfall gar nicht ab.

            Iterator<String> it = ht.values().iterator();
            while (it.hasNext()) {
                String value = it.next();
                if (value.length() > 999)
                    return;
            }

            throw new Exception("no BPD segment > 999 chars found");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}

