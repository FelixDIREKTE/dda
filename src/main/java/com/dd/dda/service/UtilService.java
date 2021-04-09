package com.dd.dda.service;

import com.dd.dda.model.exception.DDAException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UtilService {

    private final SimpleDateFormat dateCsvFormat;
    private final SimpleDateFormat dateJsFormat;

    public UtilService() {
        this.dateCsvFormat = new SimpleDateFormat("dd.MM.yyyy");
        this.dateJsFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateCsvFormat.setTimeZone(TimeZone.getDefault());
    }

    public String mapToString(Map<Object, Integer> m){
        String result = "";
        boolean notFirst = false;
        for(Map.Entry e : m.entrySet()){
            if(notFirst){
                result = result + ";";
            }
            notFirst = true;
            result = result + e.getKey().toString() + ":" + e.getValue().toString();
            if(
                    e.getKey().toString().contains(":") || e.getValue().toString().contains(":") ||
                    e.getKey().toString().contains(";") || e.getValue().toString().contains(";")
            ) {
                throw new DDAException("verbotene Charactere beim Konvertieren: " + e.toString());
            }
        }
        return result;
    }

    public Date trimDate(Date date) {
        if (date == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public Date parseDate(String birthdate) {
        if (birthdate != null && !birthdate.isEmpty()) {
            Date bd = null;
            try {
                bd = dateCsvFormat.parse(birthdate);
            } catch (ParseException e) {
                return null;
            }
            return bd;
        }
        return null;
    }

    //2020-12-12T00:00:00.000+0000
    public Date parseJsDate(String jsdate){
        if (jsdate != null && !jsdate.isEmpty()) {
            Date bd = null;
            try {
                bd = dateJsFormat.parse(jsdate.substring(0,10));
            } catch (ParseException e) {
                return null;
            }
            return bd;
        }
        return null;
    }



    public Date parseDate(String dateAsString, SimpleDateFormat dateCsvFormat) throws ParseException {
        dateCsvFormat.setTimeZone(TimeZone.getDefault());
        return dateCsvFormat.parse(dateAsString);
    }

    public Date parseAndTrimDate(String dateAsString, SimpleDateFormat dateformat) throws ParseException {
        return trimDate(parseDate(dateAsString, dateformat));
    }

    public boolean isSameDate(Date date1, Date date2) {
        return date1 != null && date2 != null && trimDate(date1).equals(trimDate(date2));
    }

    public List<Long> stringToArray(String readNotificationsIds) {
        return  Arrays.stream(readNotificationsIds.split(";")).filter(s -> ! s.isEmpty()).map(s -> Long.parseLong(s)).collect(Collectors.toList());
    }
}
