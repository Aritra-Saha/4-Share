package org.myoralvillage.cashcalculatormodule.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import org.myoralvillage.cashcalculatormodule.R;
import org.myoralvillage.cashcalculatormodule.models.AppStateModel;
import org.myoralvillage.cashcalculatormodule.models.CurrencyModel;
import org.myoralvillage.cashcalculatormodule.models.DenominationModel;
import org.myoralvillage.cashcalculatormodule.services.AppService;
import org.myoralvillage.cashcalculatormodule.views.CountingTableView;
import org.myoralvillage.cashcalculatormodule.views.CurrencyScrollbarView;
import org.myoralvillage.cashcalculatormodule.views.NumberPadView;
import org.myoralvillage.cashcalculatormodule.views.listeners.CountingTableListener;
import org.myoralvillage.cashcalculatormodule.views.listeners.CurrencyScrollbarListener;
import org.myoralvillage.cashcalculatormodule.views.listeners.NumberPadListener;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Locale;

public class CashCalculatorFragment extends Fragment {
    private final String APP_STATE_KEY = "appState";
    private View view;
    private AppService service;
    private CurrencyModel currCurrency;
    private CountingTableView countingTableView;
    private CurrencyScrollbarView currencyScrollbarView;
    private NumberPadView numberPadView;
    private TextView numberInputView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.containsKey(APP_STATE_KEY))
            service = new AppService((AppStateModel) extras.getSerializable(APP_STATE_KEY));
        else service = new AppService();

        view = inflater.inflate(R.layout.fragment_activity, parent, false);
        numberInputView = view.findViewById(R.id.number_input_view);
        numberInputView.setVisibility(View.INVISIBLE);

        return view;
    }

    //get the value of current total amount of money
    public BigDecimal getValue(){
        return service.getValue();
    }

    public void initialize(String currencyCode) {
        initializeCurrencyScrollbar(currencyCode);
        initializeCountingView();
        initializeNumberPad();

        updateAll();
    }

    private void initializeCountingView() {
        countingTableView = view.findViewById(R.id.counting_table);
        countingTableView.initialize(currCurrency, service.getAppState());
        countingTableView.setListener(new CountingTableListener() {
            @Override
            public void onSwipeAddition() {
                if (!service.isInHistorySlideshow()) {
                    service.add();
                    switchState();
                    getActivity().overridePendingTransition(R.anim.activity_left_in,R.anim.activity_left_out);
                    getActivity().finish();
                }
            }

            @Override
            public void onSwipeSubtraction() {
                if (!service.isInHistorySlideshow()) {
                    service.subtract();
                    switchState();
                    getActivity().overridePendingTransition(R.anim.activity_right_in,R.anim.activity_right_out);
                    getActivity().finish();
                }
            }

            @Override
            public void onSwipeMultiplication() {
                // Dragging towards the top
                if (!service.isInHistorySlideshow()) {
                    service.multiply();
                    switchState();
                    getActivity().overridePendingTransition(R.anim.activity_down_in,R.anim.activity_down_out);
                    getActivity().finish();
                }
            }

            @Override
            public void onDenominationChange(DenominationModel denomination, int oldCount, int newCount) {
                BigDecimal amount = denomination.getValue().multiply(new BigDecimal(oldCount - newCount));
                if (service.getValue().compareTo(BigDecimal.ZERO) >= 0) {
                    service.setValue(service.getValue().subtract(amount));
                } else {
                    service.setValue(service.getValue().add(amount));
                }
                updateAll();
            }

            @Override
            public void onTapCalculateButton() {
                service.calculate();
                switch(service.getAppState().getAppMode()) {
                    case NUMERIC:
                        numberInputView.setText(String.format(Locale.CANADA, "%s %s",
                                currCurrency.getCurrency().getSymbol(), service.getValue()));
                        numberPadView.setValue(service.getValue());
                        break;
                }
                updateAll();
            }

            @Override
            public void onTapClearButton() {
                switch(service.getAppState().getAppMode()) {
                    case NUMERIC:
                        numberInputView.setText(String.format(Locale.CANADA, "%s %s",
                                currCurrency.getCurrency().getSymbol(), 0));
                        numberPadView.setValue(BigDecimal.ZERO);
                        break;
                }
                service.reset();
                updateAll();
            }

            @Override
            public void onTapEnterHistory() {
                service.enterHistorySlideshow();
                updateAll();
            }

            @Override
            public void onTapNextHistory() {
                service.gotoNextHistorySlide();
                updateAll();
            }

            @Override
            public void onTapPreviousHistory() {
                service.gotoPreviousHistorySlide();
                updateAll();
            }
        });
    }

    private void updateCountingTable() {
        countingTableView.setAppState(service.getAppState());
    }

    private void initializeCurrencyScrollbar(String currencyCode){
        currencyScrollbarView = view.findViewById(R.id.currency_scrollbar);
        currencyScrollbarView.setCurrency(currencyCode);
        this.currCurrency = currencyScrollbarView.getCurrency();

        currencyScrollbarView.setCurrencyScrollbarListener(new CurrencyScrollbarListener() {
            @Override
            public void onTapDenomination(DenominationModel denomination) {
                service.setValue(service.getValue().add(denomination.getValue()));
                updateAll();
            }

            @Override
            public void onVerticalSwipe() {
                switchAppMode();
            }
        });
    }

    private void switchAppMode() {
        service.switchAppMode();
        if (service.getAppState().getAppMode() == AppStateModel.AppMode.NUMERIC)
            service.setValue(BigDecimal.ZERO);

        TextView sum = getView().findViewById(R.id.sum_view);

        switch(service.getAppState().getAppMode()) {
            case IMAGE:
                sum.setVisibility(View.VISIBLE);
                numberInputView.setVisibility(View.INVISIBLE);
                break;
            case NUMERIC:
                sum.setVisibility(View.INVISIBLE);

                service.setValue(BigDecimal.ZERO);
                break;
        }

        updateAll();
    }

    private void initializeNumberPad() {
        TextView sum = view.findViewById(R.id.sum_view);
        numberPadView = view.findViewById(R.id.number_pad_view);

        numberPadView.setListener(new NumberPadListener() {
            @Override
            public void onCheck(BigDecimal value) {
                sum.setVisibility(View.VISIBLE);
                service.setValue(value);
                numberInputView.setVisibility(View.INVISIBLE);
                updateAll();
            }

            @Override
            public void onBack(BigDecimal value) {
                numberInputView.setText(String.format(Locale.CANADA, "%s %s",
                        currCurrency.getCurrency().getSymbol(), value));
                service.setValue(value);
            }

            @Override
            public void onTapNumber(BigDecimal value) {
                service.setValue(value);
                sum.setVisibility(View.INVISIBLE);
                numberInputView.setVisibility(View.VISIBLE);
                numberInputView.setText(String.format(Locale.CANADA, "%s %s",
                        currCurrency.getCurrency().getSymbol(), value));
            }

            @Override
            public void onVerticalSwipe() {
                switchAppMode();
            }
        });
    }

    private void updateAppMode() {
        switch(service.getAppState().getAppMode()) {
            case IMAGE:
                numberPadView.setVisibility(View.INVISIBLE);
                currencyScrollbarView.setVisibility(View.VISIBLE);
                break;
            case NUMERIC:
                numberPadView.setVisibility(View.VISIBLE);
                currencyScrollbarView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void switchState() {
        Intent intent = new Intent(getActivity(), getActivity().getClass());
        intent.putExtra(APP_STATE_KEY, service.getAppState());
        startActivity(intent);
    }

    private void updateAll() {
        updateCountingTable();
        updateAppMode();
    }
}