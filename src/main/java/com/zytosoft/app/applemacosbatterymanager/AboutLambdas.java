package com.zytosoft.app.applemacosbatterymanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntPredicate;

class AboutLambdas
{
    static List<Integer> meinFilter(final IntPredicate property, final List<Integer> list)
    {
        final List<Integer> sieve = new ArrayList<>();

        for (Integer element : list)
        {
            if (property.test(element))
            {
                sieve.add(element);
            }
        }

        return sieve;
    }

    public static void main(String[] args)
    {
        var list = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        final IntPredicate intPredicate = (value) -> value % 2 == 0;
        //final IntPredicate intPredicate2 = (value) -> doFunction(value);
        final IntPredicate intPredicate2 = AboutLambdas::doFunction;

        var sieve = meinFilter(intPredicate, list);

        for (var element : sieve)
        {
            System.out.print(element + " ");
        }

        var sieve2 = meinFilter(intPredicate2, list);

        for (var element : sieve2)
        {
            System.out.print(element + " ");
        }
    }

    private static boolean doFunction(int value)
    {
        return value % 2 == 0;
    }
}
