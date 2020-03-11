package org.myoralvillage.cashcalculator.fragments;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import org.myoralvillage.cashcalculator.MainActivity;
import org.myoralvillage.cashcalculator.R;
import org.myoralvillage.cashcalculator.SettingActivity;
import org.myoralvillage.cashcalculatormodule.models.AppStateModel;
import org.myoralvillage.cashcalculatormodule.models.CurrencyModel;
import org.myoralvillage.cashcalculatormodule.models.DenominationModel;
import org.myoralvillage.cashcalculatormodule.models.MathOperationModel;
import org.myoralvillage.cashcalculatormodule.services.AppService;
import org.myoralvillage.cashcalculatormodule.services.CountingService;
import org.myoralvillage.cashcalculatormodule.views.CountingTableView;
import org.myoralvillage.cashcalculatormodule.views.CurrencyScrollbarView;
import org.myoralvillage.cashcalculatormodule.views.NumberPadView;
import org.myoralvillage.cashcalculatormodule.views.listeners.CurrencyScrollbarListener;
import org.myoralvillage.cashcalculatormodule.views.listeners.NumberPadListener;
import org.myoralvillage.cashcalculatormodule.views.listeners.SwipeListener;

import androidx.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Locale;

public class CashCalculatorFragment extends Fragment {
    private final String APP_STATE_KEY = "appState";
    private AppService service;
    private CurrencyModel currCurrency;
    private CountingTableView countingTableView;
    private CurrencyScrollbarView currencyScrollbarView;
    private TextView sumView;
    private TextView numberInputView;
    private NumberPadView numberPadView;
    private ImageView calculateButton;
    private ImageView clearButton;
    private ImageView enterHistoryButton;
    private ImageView rightHistoryButton;
    private ImageView leftHistoryButton;


    private CountingService countingService = new CountingService();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null && extras.containsKey(APP_STATE_KEY))
            service = new AppService((AppStateModel) extras.getSerializable(APP_STATE_KEY));
        else service = new AppService();

        View view = inflater.inflate(R.layout.fragment_activity, parent, false);
        sumView = view.findViewById(R.id.sum_view);
        numberInputView = view.findViewById(R.id.number_input_view);
        numberInputView.setVisibility(View.INVISIBLE);

        initializeCurrencyScrollbar(view);
        initializeCountingView(view);
        initializeCalculateButton(view);
        initializeClearButton(view);
        initializeHistoryButtons(view);
        initializeNumberPad(view);

        updateAll();
        return view;
    }

    private void initializeHistoryButtons(View view){
        enterHistoryButton = view.findViewById(R.id.enter_history_button);
        rightHistoryButton = view.findViewById(R.id.right_history_button);
        leftHistoryButton = view.findViewById(R.id.left_history_button);

        enterHistoryButton.setOnClickListener((e) -> {
            service.enterHistorySlideshow();
            updateAll();
        });

        rightHistoryButton.setOnClickListener((e) -> {
            service.gotoNextHistorySlide();
            updateAll();
        });

        leftHistoryButton.setOnClickListener((e) -> {
            service.gotoPreviousHistorySlide();
            updateAll();
        });}

    private void updateHistoryButtons() {
        if (service.isInHistorySlideshow()) {
            enterHistoryButton.setVisibility(View.INVISIBLE);
            leftHistoryButton.setVisibility(View.VISIBLE);
            rightHistoryButton.setVisibility(View.VISIBLE);
        } else {
            if (service.getAppState().getOperations().size() == 1)
                enterHistoryButton.setVisibility(View.INVISIBLE);
            else enterHistoryButton.setVisibility(View.VISIBLE);

            rightHistoryButton.setVisibility(View.INVISIBLE);
            leftHistoryButton.setVisibility(View.INVISIBLE);
        }
    }

    private void initializeCalculateButton(View view){
        calculateButton = view.findViewById(R.id.calculate_button);
        calculateButton.setOnClickListener((e) -> {
            if (!service.isInHistorySlideshow()) {
                service.calculate();
                updateAll();
            }
        });
    }

    private void updateCalculateButton() {
        calculateButton.setVisibility(View.VISIBLE);
        switch (service.getOperationMode()) {
            case STANDARD:
                calculateButton.setVisibility(View.INVISIBLE);
                break;
            case ADD:
                calculateButton.setImageResource(R.drawable.operator_plus);
                break;
            case SUBTRACT:
                calculateButton.setImageResource(R.drawable.operator_minus);
                break;
            case MULTIPLY:
                calculateButton.setImageResource(R.drawable.operator_times);
                break;
        }
    }

    private void initializeCountingView(View view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        countingTableView = view.findViewById(R.id.counting_table);
        countingTableView.initDenominationModels(currCurrency.getDenominations());

        countingTableView.setOnTouchListener(new SwipeListener(getActivity()) {
            @Override
            public void swipeLeft() {
                // Dragging towards the right
                if (!service.isInHistorySlideshow()) {
                    service.add();
                    switchState();
                    getActivity().overridePendingTransition(R.anim.activity_left_in,R.anim.activity_left_out);
                    getActivity().finish();

                }
            }

            @Override
            public void swipeRight() {
                // Dragging towards the left
                if (!service.isInHistorySlideshow()) {
                    service.subtract();
                    switchState();
                    getActivity().overridePendingTransition(R.anim.activity_right_in,R.anim.activity_right_out);
                    getActivity().finish();
                }
            }

            @Override
            public void longPress(float x, float y) {
                countingTableView.handleLongPress(x, y);
            }

            @Override
            public void swipeUp() {
                // Dragging towards the bottom
            }

            @Override
            public void swipeDown() {
                // Dragging towards the top
                if (!service.isInHistorySlideshow()) {
                    service.multiply();
                    switchState();
                    getActivity().overridePendingTransition(R.anim.activity_down_in,R.anim.activity_down_out);
                    getActivity().finish();
                }
            }
        });

        countingTableView.setCountingTableListener((model, oldCount, newCount) -> {
            BigDecimal amount = model.getValue().multiply(new BigDecimal(oldCount - newCount));
            if (service.getValue().compareTo(BigDecimal.ZERO) >= 0) {
                service.setValue(service.getValue().subtract(amount));
            } else {
                service.setValue(service.getValue().add(amount));
            }
            updateSumView();
            updateClearButton();
        });
    }

    private void updateCountingTable() {
        countingTableView.setDenominations(currCurrency.getDenominations().iterator(),
                countingService.allocate(service.getValue(), currCurrency), service.getValue());
    }

    private void updateSumView() {
        if (service.getValue().compareTo(BigDecimal.ZERO) < 0)
            sumView.setTextColor(getResources().getColor(R.color.negativeSum));
        else
            sumView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

        sumView.setText(String.format(Locale.CANADA, "%s %s",
                currCurrency.getCurrency().getSymbol(), service.getValue()));
    }

    private void initializeClearButton(View view){
        clearButton = view.findViewById(R.id.clear_button);
        clearButton.setOnClickListener((e) -> {
            service.reset();
            updateAll();
        });
    }

    private void updateClearButton() {
        if (service.getOperationMode() == MathOperationModel.MathOperationMode.STANDARD
                && service.getValue().equals(BigDecimal.ZERO))
            clearButton.setVisibility(View.INVISIBLE);
        else
            clearButton.setVisibility(View.VISIBLE);
    }

    private void initializeCurrencyScrollbar(View view){
        currencyScrollbarView = view.findViewById(R.id.currency_scrollbar);
        currencyScrollbarView.setCurrency(SettingActivity.getSettingService().getCurrencyName());
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
                TextView sum = view.findViewById(R.id.sum_view);
                TextView num_input = view.findViewById(R.id.number_input_view);

                switch(service.getAppState().getAppMode()) {
                    case IMAGE:
                        sum.setVisibility(View.VISIBLE);
                        num_input.setVisibility(View.INVISIBLE);
                        break;
                    case NUMERIC:
                        sum.setVisibility(View.INVISIBLE);

                        service.setValue(BigDecimal.ZERO);
                        break;
                }
            }
        });
    }

    private void switchAppMode() {
        service.switchAppMode();
        if (service.getAppState().getAppMode() == AppStateModel.AppMode.NUMERIC)
            service.setValue(BigDecimal.ZERO);
        updateAll();
    }

    private void initializeNumberPad(View view) {
        TextView sum = view.findViewById(R.id.sum_view);
        numberPadView = view.findViewById(R.id.number_pad_view);
        TextView num_input = view.findViewById(R.id.number_input_view);
        numberPadView.setListener(new NumberPadListener() {
            @Override
            public void onCheck(BigDecimal value) {
                num_input.setVisibility(View.INVISIBLE);
                sum.setVisibility(View.VISIBLE);
                service.setValue(value);
                numberInputView.setVisibility(View.INVISIBLE);
                updateAll();
            }

            @Override
            public void onBack(BigDecimal value) {
                numberInputView.setText(String.format(Locale.CANADA, "%s %s",
                        currCurrency.getCurrency().getSymbol(), value));
                updateSumView();
            }

            @Override
            public void onTapNumber(BigDecimal value) {

                sum.setVisibility(View.INVISIBLE);
                numberInputView.setVisibility(View.VISIBLE);
                numberInputView.setText(String.format(Locale.CANADA, "%s %s",
                        currCurrency.getCurrency().getSymbol(), value));
                updateSumView();
            }

            @Override
            public void onVerticalSwipe() {
                switchAppMode();
                TextView sum = view.findViewById(R.id.sum_view);
                TextView num_input = view.findViewById(R.id.number_input_view);

                switch(service.getAppState().getAppMode()) {
                    case IMAGE:
                        sum.setVisibility(View.VISIBLE);
                        num_input.setVisibility(View.INVISIBLE);
                        break;
                    case NUMERIC:
                        sum.setVisibility(View.INVISIBLE);

                        service.setValue(BigDecimal.ZERO);
                        break;
                }
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

    public void switchState() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra(APP_STATE_KEY, service.getAppState());
        startActivity(intent);
    }


    private void updateAll() {
        updateCountingTable();
        updateCalculateButton();
        updateHistoryButtons();
        updateClearButton();
        updateSumView();
        updateAppMode();
    }
}

