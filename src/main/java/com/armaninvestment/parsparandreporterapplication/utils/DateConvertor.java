package com.armaninvestment.parsparandreporterapplication.utils;

import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

@Component
public class DateConvertor {
    private static YearRepository yearRepository;

    public DateConvertor(YearRepository yearRepository) {
        DateConvertor.yearRepository = yearRepository;
    }

    public static String convertGregorianToJalali(LocalDateTime localDateTime) {

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
    public static String convertGregorianToJalali(LocalDate localDate) {

        DateConverter dateConverter = new DateConverter();
        JalaliDate jalaliDate = dateConverter.gregorianToJalali(
                localDate.getYear(),
                localDate.getMonthValue(),
                localDate.getDayOfMonth()
        );

        return String.format("%d/%02d/%02d",
                jalaliDate.getYear(), jalaliDate.getMonthPersian().getValue(), jalaliDate.getDay());
    }

    public static Year findYearFromLocalDate(LocalDate date) {
        if (date != null) {
            String jalali = convertGregorianToJalali(date);
            String jalaliYear = jalali.substring(0, 4);
            Optional<Year> optionalYear = yearRepository.findByName(Long.valueOf(jalaliYear));
           if (optionalYear.isPresent()){
               return optionalYear.get();
           }
        } else {
            return yearRepository.findAll()
                    .stream().max(Comparator.comparing(Year::getName))
                    .orElse(null);
        }
        return null;
    }
}
