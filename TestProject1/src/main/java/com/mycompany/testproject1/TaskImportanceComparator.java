/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.testproject1;

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
