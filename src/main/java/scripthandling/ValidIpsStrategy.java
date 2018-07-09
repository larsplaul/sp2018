package scripthandling;

import entity.exceptions.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import utils.ValidIps;

/**
 *
 * @author plaul1
 */
public class ValidIpsStrategy extends ScriptHandler {

  protected ValidIpsStrategy(String script, String separator) {
    super(script, separator);
  }

  @Override
  protected void handleScriptTemplate(Scanner scan) throws ScriptException {
    List<String> ipValues = new ArrayList();
    boolean clearAllLineRead = false;
    boolean clearAllFirst = false;
    while (scan.hasNext()) {

      String line = getNextLine(scan);
      if (isLineToSkip(line)) {
        continue;
      }

      if (!clearAllLineRead) {
        String[] lineParts = line.split(SEPARATOR);
        if (!lineParts[0].equals("clearallfirst")) {
          throw new ScriptException(makeError("Second Line must start with 'clearallfirst;' followed by yes or no"));
        }
        if (! (lineParts[1].equals("yes") || lineParts[1].equals("no"))) {
          throw new ScriptException(makeError("Second Line must start with 'clearallfirst;' followed by 'yes' or 'no'"));
        }
        clearAllFirst = lineParts[1].equals("yes") ? true : false;
        clearAllLineRead = true;
        continue;
      }
      String[] parts = line.split("\\.");
      System.out.println("Line: " + line + "Parts: " + parts.length);
      if (parts.length != 4) {
        throw new ScriptException(makeError("Entries must be valid ips given like: xxx.xxx.xxx.xxx (only one entry per line)"));
      }
      for (String ipPart : parts) {
        try {
          int val = Integer.parseInt(ipPart);
          if (val < 0 || val > 255) {
            throw new ScriptException(makeError("Ip parts must be >= 0 and <= 255"));
          }
        } catch (NumberFormatException nfe) {
          throw new ScriptException(makeError("Ip entries can only contain numbers separated by '.'s"));
        }
      }
      ipValues.add(line);
    }
   
    ValidIps.addValidIpsToDB(ipValues,clearAllFirst);
   
  }
}
