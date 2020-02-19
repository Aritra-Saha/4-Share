package org.myoralvillage.cashcalculatormodule.models;

import androidx.annotation.Nullable;

import java.math.BigDecimal;

public class DenominationModel implements Comparable<DenominationModel> {
    private BigDecimal value;
    private int imageResource;

    public DenominationModel(BigDecimal value, int imageResource) {
        this.value = value;
        this.imageResource = imageResource;
    }

    public BigDecimal getValue() {
        return value;
    }

    public int getImageResource() {
        return imageResource;
    }

    @Override
    public int compareTo(DenominationModel o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof DenominationModel))
            return false;

        DenominationModel dm = (DenominationModel) obj;
        return value.equals(dm.value) && imageResource == dm.imageResource;
    }
}