package com.armaninvestment.parsparandreporterapplication.utils;

import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class DateConvertor {
    public String convertGregorianToJalali(LocalDateTime localDateTime) {

        // Create a DateTimeFormatter for formatting the time part.
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        // Use DateConverter to convert Gregorian date to Jalali.
        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(
                localDateTime.getYear(),
                localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth()
        );

        // Manually format the Jalali date to ensure two-digit month and day.
        String formattedJalaliDate = String.format("%d/%02d/%02d",
                jalaliDate.getYear(), jalaliDate.getMonthPersian().getValue(), jalaliDate.getDay());

        // Format the time part using the created formatter.
        String formattedTime = localDateTime.format(timeFormatter);

        // Combine formatted Jalali date and formatted time into one string.
        return formattedJalaliDate + " - " + formattedTime;
    }

    public static LocalDate convertJalaliToGregorian(String jalaliDate) {
        DateConverter dateConverter = new DateConverter();

        String[] parts = jalaliDate.split("/");
        int jalaliYear = Integer.parseInt(parts[0]);
        int jalaliMonth = Integer.parseInt(parts[1]);
        int jalaliDay = Integer.parseInt(parts[2]);

        return dateConverter.jalaliToGregorian(jalaliYear, jalaliMonth, jalaliDay);
    };
}
