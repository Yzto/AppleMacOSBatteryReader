package com.zytosoft.app.applemacosbatterymanager;

import java.io.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/*
Copy right Dr. Torsten Zytowski, 2020, Germany

A Single Class Application that contains a classes internally.
 */

public class BatteryManager
{
    private static final float SECONDS_PER_HOUR = 3600F;
    private static final int WAIT_TIME_IN_SECONDS = 60;
    int repetitionCounter = 0;

    public static void main(String[] args)
    {
        BatteryManager readBatteryManager = new BatteryManager();

        Timer timer = new Timer();

        try
        {
            TimerTask timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    readBatteryManager.doIt();
                }
            };

            timer.schedule(timerTask, 1, WAIT_TIME_IN_SECONDS * 1000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static String getFirstLine(String fullAnswer, String startsWithToken)
    {
        final StringReader stringReader = new StringReader(fullAnswer);

        final BufferedReader bufferedReader = new BufferedReader(stringReader);

        //int lineCounter = 0;

        try
        {
            String line;

            // Initialize
            line = bufferedReader.readLine();

            while (line != null)
            {
                //lineCounter++;

                if (line.contains("|"))
                {
                    // Pipe must be escaped!
                    line = line.replaceAll("\\|", " ").trim();
                }

                if (line.contains("+-o"))
                {
                    // Pipe must be escaped!
                    line = line.replaceAll("\\+-o", " ").trim();
                }

                if (line.startsWith(startsWithToken))
                {
                    return line;
                }

                line = bufferedReader.readLine();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";

    }

    private void doIt()
    {
        // Precondition
        repetitionCounter++;

        if (repetitionCounter > 60)
        {
            System.exit(0);
        }

        if (currentRuntime == null)
        {
            currentRuntime = Runtime.getRuntime();
        }

        try
        {
            initializeGenerals();

            final String fullAnswer = getStatusInformationBlock(currentRuntime);

            setNextOutputHeader();

            setVoltageCurrentState(fullAnswer);

            setVoltageChanges();

            setCurrentChargeState(fullAnswer);

            setChargeChanges();

            setCurrentCurrentState();

            setEnergyCurrentState();

            setEnergyChanges();

            setPowerCurrentState();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private String getStatusInformationBlock(final Runtime runtime)
    {
        String result;
        try
        {
            final Process process = runtime.exec("ioreg -l");

            final InputStream inputStream = process.getInputStream();

            final BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            final byte[] allBytes = bufferedInputStream.readAllBytes();

            result = new String(allBytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();

            result = "ERROR";
        }

        return result;
    }

    private void setNextOutputHeader()
    {
        Instant currentInstant = new Date().toInstant();

        final long milliseconds;

        if (lastInstant != null)
        {
            milliseconds = currentInstant.toEpochMilli() - lastInstant.toEpochMilli();
        }
        else
        {
            milliseconds = 0;
        }

        int waitingDelay = (int) (milliseconds / 1000L);

        String number = numberFormatInt.format(waitingDelay);

        System.out.println("\n------- " + currentInstant + " - " + number + " seconds ----------\n");

        lastInstant = currentInstant;
    }


    // 1. Voltage - CurrentState:
    // Millivolt Volt / Volt
    private void setVoltageCurrentState(final String fullAnswer)
    {
        System.out.println("\t 1.1. Voltage - Current State");

        String currentVoltageInMilliVoltAsString = getFirstLine(fullAnswer, "\"Voltage\" = 1");

        currentVoltageInMilliVoltAsString = currentVoltageInMilliVoltAsString.substring(12);

        currentVoltageInMilliVolt = Integer.parseInt(currentVoltageInMilliVoltAsString);

        String number = numberFormatInt.format(currentVoltageInMilliVolt);
        String unit = "mV";
        String explanation = "[U](mV)";

        String out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        //

        currentVoltageInVolt = ((float) currentVoltageInMilliVolt) / 1000F;

        number = numberFormatFloat.format(currentVoltageInVolt);
        unit = "V";
        explanation = "1000 * [U](mV)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);
    }

    private void setVoltageChanges()
    {
        System.out.println("\t 1.2. Voltage - Change during the last <" + WAIT_TIME_IN_SECONDS + "> seconds");

        final int voltageChangeInMilliVolt;

        if (lastVoltageInMilliVolt < 0.000001F)
        {
            voltageChangeInMilliVolt = 0;
        }
        else
        {
            voltageChangeInMilliVolt = currentVoltageInMilliVolt - lastVoltageInMilliVolt;
        }

        lastVoltageInMilliVolt = currentVoltageInMilliVolt;

        String number = numberFormatInt.format(voltageChangeInMilliVolt);
        String unit = "mV";
        String explanation = "Δ[U](mV)";

        String out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        final float voltageChangeRateInMilliVoltPerSecond = ((float) voltageChangeInMilliVolt) / ((float) WAIT_TIME_IN_SECONDS);

        number = numberFormatFloat.format(voltageChangeRateInMilliVoltPerSecond);
        unit = "mV/s";
        explanation = "Δ[U](mV) / Δ[t](s)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);
    }

    private void setCurrentChargeState(final String fullAnswer)
    {
        System.out.println("\t 2.1. Charge - Current State");

        // 1. Charge Amount:
        // Coulomb is The amount of Charge (particles) in a certain duration.
        // C = A / second
        // Milliampere Hours / Ampere Hours / Ampere Seconds

        String currentCurrentInMilliAmpereHoursAsString = getFirstLine(fullAnswer, "\"AppleRawCurrentCapacity\"");

        currentCurrentInMilliAmpereHoursAsString = currentCurrentInMilliAmpereHoursAsString.substring(28);

        currentCurrentInMilliAmpereHours = Integer.parseInt(currentCurrentInMilliAmpereHoursAsString);

        String number = numberFormatInt.format(currentCurrentInMilliAmpereHours);
        String unit = "mAh";
        String explanation = "[I t](mA h)";

        String out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        //System.out.println("\t\t <" + number + "> mAh");

        //
        currentCurrentInAmpereHours = currentCurrentInMilliAmpereHours / 1000F;

        number = numberFormatFloat.format(currentCurrentInAmpereHours);
        unit = "Ah";
        explanation = "1000 * [I t](mA h)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        // 1 Ah = 3600 As
        currentCurrentInAmpereSeconds_or_Coulomb = currentCurrentInAmpereHours * SECONDS_PER_HOUR;

        number = numberFormatInt.format(currentCurrentInAmpereSeconds_or_Coulomb);
        unit = "As | C";
        explanation = "[I t](mA h) * 3600 [t](s) / [t](h)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);


        currentCurrentInMilliMolOfElectrons = (float) ((double) currentCurrentInAmpereSeconds_or_Coulomb / 96.48533212331D);

        number = numberFormatFloat.format(currentCurrentInMilliMolOfElectrons);
        unit = "mmol electrons";
        explanation = "[Q](C) / 96,485 [Q](C) / [N(a)](mmol electrons)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);
        //System.out.println("\t\t <" + number + "> mmol of electrons");
    }

    private void setChargeChanges()
    {
        System.out.println("\t 2.2. Charge - Consumed / Loaded during the last <" + WAIT_TIME_IN_SECONDS + "> seconds");
        // 3. Usage / Consumption / Loading / Charging

        if (lastCurrentInMilliAmpereHours < 1)
        {
            currentConsumedMilliAmpereHours = 0;
            currentChargeChangeInMilliMolOfElectrons = 0;
        }
        else
        {
            currentConsumedMilliAmpereHours = currentCurrentInMilliAmpereHours - lastCurrentInMilliAmpereHours;
            currentChargeChangeInMilliMolOfElectrons = currentCurrentInMilliMolOfElectrons - lastMilliMolOfElectrons;
        }

        String number = numberFormatInt.format(currentConsumedMilliAmpereHours);
        System.out.println("\t\t <" + number + "> mAh");

        final float consumedMilliAmpereSeconds = currentConsumedMilliAmpereHours * SECONDS_PER_HOUR;

        number = numberFormatInt.format(consumedMilliAmpereSeconds);
        System.out.println("\t\t <" + number + "> mAs");

        currentChargeChangeInAmpereSeconds_or_Coulomb = consumedMilliAmpereSeconds / 1000F;

        number = numberFormatFloat.format(currentChargeChangeInAmpereSeconds_or_Coulomb);
        System.out.println("\t\t <" + number + "> As or C");

        number = numberFormatFloat.format(currentChargeChangeInMilliMolOfElectrons);
        System.out.println("\t\t <" + number + "> mmol electrons");

        lastMilliMolOfElectrons = currentCurrentInMilliMolOfElectrons;
    }

    private void initializeGenerals()
    {
        // General environment
        Locale locale = new Locale("de", "GER");

        numberFormatInt = NumberFormat.getInstance(locale);

        numberFormatInt.setMaximumFractionDigits(0);

        numberFormatFloat = NumberFormat.getInstance(locale);

        numberFormatFloat.setMaximumFractionDigits(3);
        numberFormatFloat.setMinimumFractionDigits(3);
    }

    private void setEnergyCurrentState()
    {
        System.out.println("\t 4.1. Energy - Current State");

        // Power is: Voltage * Current
        // [L] = [U] * [I]
        // Watt = Volt * Ampere

        // 1. Energy is: Voltage * Current * Duration
        // Joule = Volt * Ampere * Time [in seconds]
        // [E] = [U] * [I] * [T]
        // J = V * A * s
        // 2.1. Energy is: Power * Duration (in Seconds)
        // [E] = [L] * [T]
        // J = W * s
        currentEnergyInJoule_or_WattSeconds = currentVoltageInVolt * currentCurrentInAmpereSeconds_or_Coulomb;

        String number = numberFormatFloat.format(currentEnergyInJoule_or_WattSeconds);
        System.out.println("\t\t <" + number + "> J or Ws");

        float currentEnergyInKiloJoule = currentEnergyInJoule_or_WattSeconds / 1000F;

        number = numberFormatFloat.format(currentEnergyInKiloJoule);
        System.out.println("\t\t <" + number + "> kJ");

        // Or another unit is Watt Hours
        // 2.2. Energy is: Power * Duration (in Hours)
        // J = W * 3600 * h
        currentEnergyInWattHours = currentVoltageInVolt * currentCurrentInAmpereHours;

        number = numberFormatFloat.format(currentEnergyInWattHours);
        System.out.println("\t\t <" + number + "> Wh");
    }

    private void setEnergyChanges()
    {
        System.out.println("\t 4.2. Energy - Consumed / Loaded during the last <" + WAIT_TIME_IN_SECONDS + "> seconds");

        if (lastEnergyInJoule_or_WattSeconds < 0.000001F)
        {
            currentEnergyChangeInJoule_or_WattSeconds = 0F;
        }
        else
        {
            currentEnergyChangeInJoule_or_WattSeconds = currentEnergyInJoule_or_WattSeconds - lastEnergyInJoule_or_WattSeconds;
        }

        lastEnergyInJoule_or_WattSeconds = currentEnergyInJoule_or_WattSeconds;

        String number = numberFormatFloat.format(currentEnergyChangeInJoule_or_WattSeconds);
        String unit = "J";
        String explanation = "Δ[E](J) or Δ[Pt](Ws)";

        String out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        number = numberFormatFloat.format(currentEnergyChangeInJoule_or_WattSeconds / 1000F);
        unit = "kJ";
        explanation = "Δ[E](J) /1000";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);
        //System.out.println("\t\t <" + number + "> kJ ");

        if (lastEnergyInWattHours < 0.000001)
        {
            currentEnergyChangeInWattHours = 0F;
        }
        else
        {
            currentEnergyChangeInWattHours = lastEnergyInWattHours - currentEnergyInWattHours;
        }

        lastEnergyInWattHours = currentEnergyInWattHours;

        number = numberFormatFloat.format(currentEnergyChangeInWattHours);

        System.out.println("\t\t <" + number + "> Wh");
    }

    private void setCurrentCurrentState()
    {
        System.out.println("\t 3.1. Current or Streams - Current State");

        // The duration between two measurements is 60 seconds (by default)

        //
        final float currentCurrentInAmpere_Or_CoulombPerSecond = currentChargeChangeInAmpereSeconds_or_Coulomb / ((float) WAIT_TIME_IN_SECONDS);

        MeasuredValue currentCurrentInAmpere_Or_CoulombPerSecondMeasureValue = new MeasuredValue(currentCurrentInAmpere_Or_CoulombPerSecond,
                new MeasuredValueUnit[]{
                        new MeasuredValueUnit("A", "Δ[I]")
                        , new MeasuredValueUnit("C/s", "Δ[Q] / Δ[t]")});

        currentCurrentInAmpere_Or_CoulombPerSecondMeasureValue.print();

        //
        final float currentCurrentInMilliAmpereHoursPerSecond = ((float) currentConsumedMilliAmpereHours) / ((float) WAIT_TIME_IN_SECONDS);

        MeasuredValue currentCurrentInMilliAmpereHoursPerSecondMeasureValue = new MeasuredValue(currentCurrentInMilliAmpereHoursPerSecond,
                new MeasuredValueUnit[]{
                        new MeasuredValueUnit("mAh/s", "Δ[It] / Δ[t] ")
                });

        currentCurrentInMilliAmpereHoursPerSecondMeasureValue.print();

        lastCurrentInMilliAmpereHours = currentCurrentInMilliAmpereHours;

        //
        final float consumedMilliMolOfElectronsPerSecond = 1000F * currentChargeChangeInMilliMolOfElectrons / ((float) WAIT_TIME_IN_SECONDS);

        currentCurrentInMilliAmpereHoursPerSecondMeasureValue =
                new MeasuredValue(consumedMilliMolOfElectronsPerSecond,
                        new MeasuredValueUnit[]{
                                new MeasuredValueUnit("µmol/s (electrons)", "Δ[N(a)] / Δ[t] ")
                        });

        currentCurrentInMilliAmpereHoursPerSecondMeasureValue.print();
    }

    private void setPowerCurrentState()
    {
        System.out.println("\t 5.1. Power - Current State");

        float currentPowerInJoulePerSecond = currentEnergyChangeInJoule_or_WattSeconds / WAIT_TIME_IN_SECONDS;

        String number = numberFormatFloat.format(currentPowerInJoulePerSecond);
        String unit = "J/s";
        String explanation = "Δ[E](J) / [T](s)";

        String out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        final float powerInWattHoursPerSecond = currentEnergyChangeInWattHours / WAIT_TIME_IN_SECONDS;

        number = numberFormatFloat.format(powerInWattHoursPerSecond);
        unit = "Wh/s";
        explanation = "Δ[E](Wh) / [T](s)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        //System.out.println("\t\t <" + number + "> Wh/s <= ");

        final float watt = powerInWattHoursPerSecond * 3600F;

        number = numberFormatFloat.format(watt);
        unit = "W";
        explanation = "Δ[E](Wh/s) * 3600 [t](s)/[t](h)";

        out = formatLine(2, number, unit, explanation);
        System.out.println(out);

        final float powerVoltTimesCoulomb = currentVoltageInVolt * currentChargeChangeInAmpereSeconds_or_Coulomb / ((float) WAIT_TIME_IN_SECONDS);

        number = numberFormatFloat.format(powerVoltTimesCoulomb);
        unit = "VC/s | J/s | W";
        explanation = "[U](V) * Δ[Q](C) / Δ[t](s) or [U](V) * Δ[It](As) / Δ[t](s)";

        out = formatLine(3, number, unit, explanation);
        System.out.println(out);
    }

    private String formatLine(int tabCount, String number, String unit, String explanation)
    {
        // ("\t\t <" + number + "> J/s <= Δ[E](J) / [T](s)");
        final StringBuilder line = new StringBuilder();

        // 1. Left Tabs
        line.append("\t".repeat(tabCount));

        // 2. Number
        final StringBuilder numberPart = new StringBuilder("<" + number + ">");

        while (numberPart.length() < 12)
        {
            numberPart.append(" ");
        }

        line.append(numberPart);

        // 3. Unit
        final StringBuilder unitPart = new StringBuilder(unit);

        while (unitPart.length() < 12)
        {
            unitPart.append(" ");
        }

        line.append(unitPart);

        // 4. Explanation
        line.append(" <= ");

        line.append(explanation);

        return line.toString();
    }

    static class MeasuredValueUnit
    {
        final String unit;

        //-----properties-----properties-----properties-----properties-----properties
        final String explanation;

        MeasuredValueUnit(final String unit, final String explanation)
        {
            this.unit = unit;
            this.explanation = explanation;
        }
    }

    class MeasuredValue
    {
        MeasuredValue(final float value, final MeasuredValueUnit[] measuredValueUnits)
        {
            this.value = value;
            this.measuredValueUnits = measuredValueUnits;
        }

        public void print()
        {
            final String number = numberFormatFloat.format(value);

            StringBuilder unit = new StringBuilder();

            boolean firstHandled = false;

            for(MeasuredValueUnit measuredValueUnit : measuredValueUnits)
            {
                if(!firstHandled)
                {
                    firstHandled = true;
                    unit.append(measuredValueUnit.unit);
                }
                else
                {
                    unit.append(" | ").append(measuredValueUnit.unit);
                }
            }

            StringBuilder explanation = new StringBuilder("<= ");

            firstHandled = false;

            for(MeasuredValueUnit measuredValueUnit : measuredValueUnits)
            {
                if(!firstHandled)
                {
                    firstHandled = true;
                    explanation.append(measuredValueUnit.explanation);
                }
                else
                {
                    explanation.append(" | ").append(measuredValueUnit.explanation);
                }
            }

            final String out = formatLine(2, number, unit.toString(), explanation.toString());

            System.out.println(out);
        }

        //-----properties-----properties-----properties-----properties-----properties

        final float value;
        final MeasuredValueUnit[] measuredValueUnits;
    }


    //-----properties-----properties-----properties-----properties-----properties


    private Runtime currentRuntime;

    private NumberFormat numberFormatInt;
    private NumberFormat numberFormatFloat;

    private Instant lastInstant;

    // Voltage
    private int currentVoltageInMilliVolt = 0;
    private int lastVoltageInMilliVolt = 0;
    private float currentVoltageInVolt;

    // Charge
    // Charge State
    private int currentCurrentInMilliAmpereHours;
    private int lastCurrentInMilliAmpereHours;
    private float currentCurrentInAmpereHours;
    private float currentCurrentInAmpereSeconds_or_Coulomb;
    private float currentCurrentInMilliMolOfElectrons;
    private float lastMilliMolOfElectrons;

    // Charge Change
    private int currentConsumedMilliAmpereHours;
    private float currentChargeChangeInMilliMolOfElectrons;
    private float currentChargeChangeInAmpereSeconds_or_Coulomb;

    // Energy
    // Energy State
    private float currentEnergyInJoule_or_WattSeconds;
    private float currentEnergyInWattHours;
    private float lastEnergyInWattHours;

    // Energy Change
    private float lastEnergyInJoule_or_WattSeconds;
    private float currentEnergyChangeInJoule_or_WattSeconds;
    private float currentEnergyChangeInWattHours;

    // Power State
}
