package com.sweetsoft.ui.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.NumberPicker;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sweetsoft.R;
import com.sweetsoft.databinding.DialogJalaliRangeBinding;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.Jalali;

/** Two Jalali date pickers (from / to) built from NumberPickers. */
public final class JalaliRangeDialog {
    public interface Listener {
        void onRange(Jalali from, Jalali to);

        /** Dialog closed without applying a range. */
        void onCancel();
    }

    private JalaliRangeDialog() {}

    public static void show(Context ctx, Jalali initialFrom, Jalali initialTo, Listener listener) {
        DialogJalaliRangeBinding b = DialogJalaliRangeBinding.inflate(LayoutInflater.from(ctx));
        Jalali today = Jalali.today();
        Jalali from = initialFrom != null ? initialFrom : today.minusDays(7);
        Jalali to = initialTo != null ? initialTo : today;
        setup(b.fromYear, b.fromMonth, b.fromDay, from, today.year);
        setup(b.toYear, b.toMonth, b.toDay, to, today.year);

        final boolean[] applied = {false};
        new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.filter_custom)
                .setView(b.getRoot())
                .setPositiveButton(R.string.filter_apply, (d, w) -> {
                    Jalali f = new Jalali(b.fromYear.getValue(), b.fromMonth.getValue(), b.fromDay.getValue());
                    Jalali t = new Jalali(b.toYear.getValue(), b.toMonth.getValue(), b.toDay.getValue());
                    if (f.isAfter(t)) {
                        Jalali tmp = f;
                        f = t;
                        t = tmp;
                    }
                    applied[0] = true;
                    listener.onRange(f, t);
                })
                .setNegativeButton(R.string.cancel, null)
                .setOnDismissListener(d -> {
                    if (!applied[0]) listener.onCancel();
                })
                .show();
    }

    private static void setup(NumberPicker year, NumberPicker month, NumberPicker day, Jalali value, int maxYear) {
        year.setMinValue(maxYear - 10);
        year.setMaxValue(maxYear);
        year.setFormatter(Fa::digits);
        year.setValue(value.year);
        year.setWrapSelectorWheel(false);

        month.setMinValue(1);
        month.setMaxValue(12);
        month.setDisplayedValues(Jalali.MONTHS);
        month.setValue(value.month);

        day.setFormatter(Fa::digits);
        day.setMinValue(1);
        day.setMaxValue(Jalali.monthLength(value.year, value.month));
        day.setValue(value.day);

        NumberPicker.OnValueChangeListener fixDays = (p, o, n) -> {
            int max = Jalali.monthLength(year.getValue(), month.getValue());
            day.setMaxValue(max);
        };
        year.setOnValueChangedListener(fixDays);
        month.setOnValueChangedListener(fixDays);
    }
}
