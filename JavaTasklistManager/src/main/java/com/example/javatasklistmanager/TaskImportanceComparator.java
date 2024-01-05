package com.example.javatasklistmanager;

import java.util.Comparator;

/**
 *
 * @author rehan
 */
public class TaskImportanceComparator implements Comparator<Task>{

    @Override
    public int compare(Task o1, Task o2) {
        return o1.getImportance() - o2.getImportance();
    }
}
