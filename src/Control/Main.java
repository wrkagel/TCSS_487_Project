package Control;

import View.GUI;

public class Main {

    public static void main(String[] args) {
        GUI view = new GUI();
        Controller cont = new Controller(view);
        view.setCont(cont);
    }
}
