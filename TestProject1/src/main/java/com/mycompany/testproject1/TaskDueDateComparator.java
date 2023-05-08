/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.testproject1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 *
 * @author rehan
 */
public class TaskDueDateComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        Date d1 = null, d2 = null;
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            d1 = formatter.parse(o1.getDueDate());
            d2 = formatter.parse(o1.getDueDate());
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return d1.compareTo(d2);
    }
}