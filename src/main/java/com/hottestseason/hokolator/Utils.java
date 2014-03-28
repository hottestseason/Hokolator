package com.hottestseason.hokolator;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Utils {
    public static Random random = new Random(0);

    public static <E> E getRandomlyFrom(Collection<E> collection) {
        int index = 0, matchedIndex = random.nextInt(collection.size());
        for (E e : collection) {
            if (index == matchedIndex) {
                return e;
            }
            index++;
        }
        return null;
    }

    public static <E> E getRandomlyFrom(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }
}
