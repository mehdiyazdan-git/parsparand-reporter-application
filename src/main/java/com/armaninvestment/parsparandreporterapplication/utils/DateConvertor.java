package com.armaninvestment.parsparandreporterapplication.utils;

import com.armaninvestment.parsparandreporterapplication.entities.Year;
import com.armaninvestment.parsparandreporterapplication.repositories.YearRepository;
import com.github.eloyzone.jalalicalendar.DateConverter;
import com.github.eloyzone.jalalicalendar.JalaliDate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

@Component
public class DateConvertor {
    private static YearRepository yearRepository;

    public DateConvertor(YearRepository yearRepository) {
        DateConvertor.yearRepository = yearRepository;
    }

    public static LocalDate convertJalaliToGregorian(String jalaliDate) {
        DateConverter dateConverter = getDateConverter();

        String[] parts = jalaliDate.split("/");
        int jalaliYear = Integer.parseInt(parts[0]);
        int jalaliMonth = Integer.parseInt(parts[1]);
        int jalaliDay = Integer.parseInt(parts[2]);

        return dateConverter.jalaliToGregorian(jalaliYear, jalaliMonth, jalaliDay);
    }

    public static String convertGregorianToJalali(LocalDate localDate) {

        DateConverter dateConverter = getDateConverter();
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
    private static DateConverter getDateConverter() {
        return new DateConverter();
    }
}
