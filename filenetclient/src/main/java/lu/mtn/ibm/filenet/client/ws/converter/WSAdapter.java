/**
 *
 */
package lu.mtn.ibm.filenet.client.ws.converter;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

/**
 * @author nguyent
 *
 */
public class WSAdapter {

    public static Date convertToDate(String dateTime) {
        return DatatypeConverter.parseDate(dateTime).getTime();
    }

    public static String convertFromDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return DatatypeConverter.printDate(calendar);
    }

    public static String convertFromDateTime(Date dateTime) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        return DatatypeConverter.printDateTime(calendar);
    }

}
