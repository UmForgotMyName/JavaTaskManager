package com.example.javatasklistmanager;

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
            d2 = formatter.parse(o2.getDueDate());
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        assert d1 != null;
        return d1.compareTo(d2);
    }
}
