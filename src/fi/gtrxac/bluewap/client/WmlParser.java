//#ifdef BLUEWAP_CLIENT
package fi.gtrxac.bluewap.client;

import java.util.Vector;
import org.kxml2.io.KXmlParser;

public class WmlParser extends KXmlParser {
    public Vector warnings = new Vector(5);
    public Vector warningLocations = new Vector(5);

    public void addWarning(String text) {
        warnings.addElement(text);
        warningLocations.addElement(getPositionDescription());
    }
}
//#endif