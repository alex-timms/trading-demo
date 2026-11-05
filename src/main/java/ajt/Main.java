package ajt;

import ajt.app.AppFactory;
import ajt.app.TradingRuntime;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        IO.println(String.format("Hello and welcome!"));

        try (TradingRuntime runtime = AppFactory.createTradingRuntime()) {
            runtime.start();
            runtime.publishOrderFix("8=FIX.4.4|35=D|11=1|55=EUR/USD|54=1|38=150000|40=2|44=1.0853|");
        }

    }
}
