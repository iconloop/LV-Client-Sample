package iconloop.client.menu;
import java.util.Scanner;
import iconloop.client.communication.CommunicateManager;

import java.util.Map;
import java.util.HashMap;

public class Menu {
    public void selectedMenu() {
        Scanner sc = new Scanner(System.in);

        int selected_number = -1;
        System.out.println("1. Connect Manager Server");
        System.out.println("2. Connect Storage Server");

        // MEMO: Sample Input code.
        selected_number = sc.nextInt();

        // MEMO : Sample Request code.
        CommunicateManager cmManager = new CommunicateManager();
        cmManager.CommunicateSample();
    }
}
