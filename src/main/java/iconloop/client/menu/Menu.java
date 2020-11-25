package iconloop.client.menu;
import java.util.Scanner;

public class Menu {
    public void showMenu() {
        Scanner sc = new Scanner(System.in);

        int selected_number = -1;
        while(0 != selected_number) {
            System.out.println("1. Connect Manager Server");
            System.out.println("2. Connect Storage Server");

            selected_number = sc.nextInt();
        }
    }
}
