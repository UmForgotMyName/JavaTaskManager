package com.example.javatasklistmanager;

import java.util.Comparator;

/**
 *
 * @author rehan
 */
public class TaskCreationDateComparator implements Comparator<Task>{

    @Override
    public int compare(Task o1, Task o2) {
        return o1.getCreationDate().compareTo(o2.getCreationDate());
    }

}
